package backupbuddies.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import backupbuddies.Debug;
import backupbuddies.network.packet.BackupFile;
import backupbuddies.network.packet.ListFiles;
import backupbuddies.network.packet.NotifyNewPeer;
import backupbuddies.network.packet.NotifyTransferFailed;
import backupbuddies.network.packet.ReplyRestoreFile;
import backupbuddies.network.packet.RequestListOfFiles;
import backupbuddies.network.packet.RequestRestoreFile;

final class PeerServicer implements Runnable {

	private final Peer peer;
	
	private final DataInputStream inbound;
	
	private HashMap<String, IPacketHandler> packets = new HashMap<>();

	PeerServicer(Peer peer, DataInputStream inbound) {
		this.peer = peer;
		this.inbound = inbound;

		registerPacket(new RequestListOfFiles());
		registerPacket(new ListFiles());
		
		registerPacket(new NotifyNewPeer());
		
		registerPacket(new BackupFile());
		
		registerPacket(new RequestRestoreFile());
		registerPacket(new ReplyRestoreFile());
		
		registerPacket(new NotifyTransferFailed());
	}

	@Override
	public void run() {
		try{
			// If they fail to handshake properly, they're either not Backup
			// Buddies or don't have the password. Don't take their commands.
			try{
				checkHandshake();
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				peer.kill("Bad handshake");
				return;
			}
			
			//Now that they're auth'd, send them our file list
			peer.sendStoredFileList();


			while(!peer.isDead()){
				String command=inbound.readUTF();
				if(command==null){
					peer.kill("Null command");
					return;
				}
				
				IPacketHandler packetHandler = packets.get(command);
				if(packetHandler==null){
					peer.kill("Illegal command: "+command);
					return;
				} else {
					//Acquire a lock on the peer
					//This is often needed - packets may require replies.
					synchronized(peer){
						packetHandler.handlePacket(peer, peer.network, inbound);
					}
				}
				
			}
		}catch(IOException e){
			System.out.println("Connection lost to "+peer.url);
			peer.kill(e);
			return;
		}
	}
	
	private void registerPacket(IPacketHandler packet){
		packets.put(packet.getIdentifier(), packet);
	}

	//Receives a handshake
	private boolean checkHandshake() throws IOException, IllegalArgumentException {
		String handshake=inbound.readUTF();
		if(handshake==null)
			throw new IllegalArgumentException();

		if(!(handshake.equals(Protocol.HANDSHAKE)))
			throw new IllegalArgumentException();

		//Check password
		String theirToken=inbound.readUTF();

		if(theirToken==null)
			throw new IllegalArgumentException();
		
		//Check that they sent us a valid UUID
		//This throws an IAE if the UUID is not valid
		UUID.fromString(theirToken);
		
		//Prevents an attack where
		//		A = attacker
		//		B = honest peer
		//B sends their token
		//A waits for this, then sends back the same token
		//B sends hash(their token + their token + password)
		//A sends that same hash back
		//B accepts it because swapping the tokens doesn't change it
		if(peer.token.equals(theirToken))
			throw new IllegalArgumentException();
		
		peer.sendLoginToken(theirToken);
		
		byte[] targetHash = Peer.computeHash(theirToken + peer.token + peer.network.password);
		
		byte[] theirHash = new byte[targetHash.length];
		
		if(inbound.read(theirHash) != theirHash.length)
			throw new IllegalArgumentException();
		
		for(int i=0; i<theirHash.length; i++) {
			if(theirHash[i] != targetHash[i]) {
				Debug.dbg(Arrays.toString(targetHash));
				Debug.dbg(Arrays.toString(theirHash));				
				throw new IllegalArgumentException("Byte "+i+" mismatched!");
			}
		}
		
		return true;
	}
}