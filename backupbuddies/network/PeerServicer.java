package backupbuddies.network;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import backupbuddies.Debug;

import static backupbuddies.Debug.*;

final class PeerServicer implements Runnable {

	private final Peer peer;
	
	private final DataInputStream inbound;

	PeerServicer(Peer peer, DataInputStream inbound) {
		this.peer = peer;
		this.inbound = inbound;
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
					dbg(command);
					peer.kill("Null command");
					return;
				}
				//This is where messages are handled
				//To add a new message type, add a case for it
				switch(command){
				case Protocol.REQUEST_BACKUP:
					handleBackupRequest();
					break;
				
				//Ask for and receive file list
				case Protocol.REQUEST_LIST_FILES:
					peer.sendStoredFileList();
					break;
				case Protocol.REPLY_LIST_FILES:
					handleListResponse();
					break;
				case Protocol.NOTIFY_NEW_PEER:
					handleNewPeer();
					break;
					
				case Protocol.REQUEST_RETRIEVE:
					handleRetrieveRequest();
					break;
				case Protocol.REPLY_RETRIEVE:
					handleRetrieveResponse();
					break;
				case Protocol.NOTIFY_TRANSFER_FAILED:
					handleTransferFailed();
				//If an invalid command is sent, kill the connection
				//It's incompatible with us
				default:
					
					peer.kill("Bad command: "+command);
					break;
				}
			}
		}catch(IOException e){
			//TODO make this informative
			e.printStackTrace();
			peer.kill(e);
			return;
		}
	}

	private void handleTransferFailed() throws IOException {
		String fileName = inbound.readUTF();
		long theirLimit = inbound.readLong();
		peer.network.log(fileName+"failed to upload: " + peer.url + " can accept " + theirLimit/1000 + " more KB of data");
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
	
	//Backs up a file
	private void handleBackupRequest() throws IOException{
		synchronized(peer.network.fileStorageLock){
			String fileName=inbound.readUTF();
			long length=inbound.readLong();
			File file=new File(peer.getStoragePath(), fileName);
			
			//File exceeds our allocated space
			//Don't let them send the whole file - close the connection to indicate
			//failure. we still want to be connected, so reconnect
			if(!peer.network.requestSpaceForFile(length)) {
				peer.notifyFileRejection(fileName, peer.network.bytesLimit - peer.network.bytesStored);
				peer.kill("Cannot store oversize file: "+fileName);
				peer.network.connect(peer.url);
			}
			
			//You can overwrite existing backups
			if(file.exists())
				file.delete();

			file.getParentFile().mkdirs();

			file.createNewFile();
			FileOutputStream out=new FileOutputStream(file);

			for(long i=0; i<length; i++){
				out.write(inbound.readByte());
			}
			out.close();
		}
	}
	
	//Handles Retrieve request for a file
	private void handleRetrieveRequest() throws IOException{
		synchronized(peer.network.fileStorageLock){
			String fileName=inbound.readUTF();
			String fileDir = peer.network.getBackupStoragePath();
			Path filePath = new File(fileDir,fileName).toPath();
			try{
				peer.restoreFile(fileName, filePath);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	//We asked to retrieve a file, and someone is sending it
	private void handleRetrieveResponse() throws IOException {
		String fileName=inbound.readUTF();
		long length=inbound.readLong();
		
		//If we don't have it, we didn't request the file
		if(!peer.network.downloadingFileLocs.containsKey(fileName))
			peer.kill("Tried to restore file that we didn't request!");
		
		File file=new File(peer.network.downloadingFileLocs.get(fileName), fileName);
		
		peer.network.downloadingFileLocs.remove(fileName);

		//You can overwrite existing backups
		
		file.createNewFile();
		FileOutputStream out=new FileOutputStream(file);

		for(long i=0; i<length; i++){
			out.write(inbound.readByte());
		}
		out.close();
	}
	
	//Receives list of files stored on some peer
	private void handleListResponse() throws IOException {
		int files=inbound.readInt();
		for(int i=0; i<files; i++){
			peer.recordStoredFile(inbound.readUTF());
		}
	}
	
	//Receives new peer and attempts to connect with them
	public void handleNewPeer() throws IOException{
		String newPeer = inbound.readUTF();
		try{
			peer.network.connect(newPeer);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}