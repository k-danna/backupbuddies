package backupbuddies.network;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
					peer.kill();
					return;
				}
			}

			while(!peer.isDead()){
				String command=inbound.readUTF();
				if(command==null){
					dbg(command);
					peer.kill();
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
					
					
				//If an invalid command is sent, kill the connection
				//It's incompatible with us
				default:
					
					peer.kill();
					break;
				}
			}
		}catch(IOException e){
			//TODO make this informative
			e.printStackTrace();
			peer.kill();
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
	
	//Receives list of files stored on some peer
	private void handleListResponse() throws IOException {
		int files=inbound.readInt();
		for(int i=0; i<files; i++){
			peer.recordStoredFile(inbound.readUTF());
		}
	}
}