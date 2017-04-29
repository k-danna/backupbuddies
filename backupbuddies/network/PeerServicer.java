package backupbuddies.network;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

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
			if(peer.requireHandshake) {
				if(!checkHandshake()){
					peer.kill("Bad handshake");
					return;
				}
			}

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

	//Receives a handshake
	private boolean checkHandshake() throws IOException {
		String line=inbound.readUTF();
		if(line==null)
			return false;

		if(!(line.equals(Protocol.HANDSHAKE)))
			return false;

		//Check password
		line=inbound.readUTF();

		if(line==null)
			return false;
		
		if(!line.equals(peer.network.password))
			return false;

		//All checks passed = we're good
		return true;
	}
	
	//Backs up a file
	private void handleBackupRequest() throws IOException{
		synchronized(peer.network.fileStorageLock){
			String fileName=inbound.readUTF();
			long length=inbound.readLong();
			File file=new File(peer.getStoragePath(), fileName);
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
	
	private void handleRetrieveResponse() throws IOException {
		String fileName=inbound.readUTF();
		long length=inbound.readLong();
		File file=new File(peer.network.downloadingFileLocs.get(fileName), fileName);
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