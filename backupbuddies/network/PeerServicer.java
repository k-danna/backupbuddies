package backupbuddies.network;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static backupbuddies.Debug.*;

final class PeerServicer implements Runnable {

	private final Peer connection;
	
	private final DataInputStream inbound;

	PeerServicer(Peer peer, DataInputStream inbound) {
		this.connection = peer;
		this.inbound = inbound;
	}

	@Override
	public void run() {
		try{
			// If they fail to handshake properly, they're either not Backup
			// Buddies or don't have the password. Don't take their commands.
			if(connection.requireHandshake) {
				if(!checkHandshake()){
					connection.kill();
					return;
				}
			}

			while(!connection.isDead()){
				String command=inbound.readUTF();
				if(command==null){
					dbg(command);
					connection.kill();
					return;
				}
				switch(command){
				case Protocol.REQUEST_BACKUP:
					handleBackupRequest();
					break;
				
				//Ask for and receive file list
				case Protocol.LIST_FILES:
					connection.sendStoredFileList();
					break;
				case Protocol.REPLY_WITH_FILES:
					handleListResponse();
					break;
					
					
				//If an invalid command is sent, kill the connection
				//It's incompatible with us
				default:
					
					connection.kill();
					break;
				}
			}
		}catch(IOException e){
			//TODO make this informative
			e.printStackTrace();
			connection.kill();
			return;
		}
	}

	//Receives a handshake
	private boolean checkHandshake() throws IOException {
		String line=inbound.readUTF();
		if(line==null)
			return false;

		//Eat all the non-printable characters
		//I don't know why they get in, but they do.
		//line=line.replaceAll("\\p{C}", "");
		
		dbg(Arrays.toString(line.getBytes()));
		dbg(line);
		dbg(Arrays.toString(Protocol.HANDSHAKE.getBytes()));

		if(!(line.equals(Protocol.HANDSHAKE)))
			return false;

		//Check password
		line=inbound.readUTF();

		if(line==null)
			return false;
		
		dbg(Arrays.toString(line.getBytes()));
		dbg(Arrays.toString(connection.password.getBytes()));
		
		if(!line.equals(connection.password))
			return false;

		//All checks passed = we're good
		return true;
	}
	
	//Backs up a file
	private void handleBackupRequest() throws IOException{
		String fileName=inbound.readUTF();
		long length=inbound.readLong();
		File file=new File(connection.getStoragePath(), fileName);
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
	
	//Receives list of files stored on some peer
	private void handleListResponse() throws IOException {
		int files=inbound.readInt();
		for(int i=0; i<files; i++){
			connection.recordStoredFile(inbound.readUTF());
		}
	}
}