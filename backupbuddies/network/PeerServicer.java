package backupbuddies.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import backupbuddies.network.packet.BackupFile;
import backupbuddies.network.packet.Handshake;
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
			try{
				// If they fail to handshake properly, they're either not Backup
				// Buddies or don't have the password. Don't take their commands.
				try{
					Handshake.checkHandshake(peer, inbound);
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
				Network.log("Connection lost to "+peer.url);
				peer.kill(e);
				return;
			}
		}catch(ThreadDeath ignored){

		}
	}
	
	private void registerPacket(IPacketHandler packet){
		packets.put(packet.getIdentifier(), packet);
	}

}