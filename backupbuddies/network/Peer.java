package backupbuddies.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

import static backupbuddies.Debug.*;

public class Peer {

	private boolean isDead=false;

	private Socket socket;

	DataOutputStream outbound;
	private Thread peerServicer;

	public final String url;

	boolean requireHandshake;
	String password;

	Network network;

	/**
	 * This always opens a new Socket, so it always has to send the handshake
	 */
	Peer(String url, String password, Network net) throws IOException{
		this(new Socket(url, Protocol.DEFAULT_PORT), password, true, net);
	}

	Peer(Socket socket, String password, boolean sendHandshake, Network net) {
		this.password=password;
		this.network=net;
		this.url=socket.getInetAddress().toString();

		try{
			outbound = new DataOutputStream(socket.getOutputStream());
			DataInputStream inbound = new DataInputStream(socket.getInputStream());

			requireHandshake = !sendHandshake;
			if(sendHandshake)
				sendHandshake(password);

			peerServicer = new Thread(new PeerServicer(this, inbound));
			peerServicer.start();
		} catch(Exception e) {
			this.kill();
		}
	}

	//Sends a handshake message
	private synchronized void sendHandshake(String password) throws IOException {
		outbound.writeUTF(Protocol.HANDSHAKE);
		outbound.writeUTF(password);
	}

	public boolean isDead(){
		return isDead;
	}

	// Call this if the connection is broken/shouldn't be used further
	public synchronized void kill(){
		//new Exception().printStackTrace();
		network.onConnectionDie(this);
		peerServicer=null;
		try{
			outbound.close();
		}catch(IOException e){
			//e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException|NullPointerException e) {
			//e.printStackTrace();
		}

		isDead=true;

	}

	public synchronized boolean uploadFile(Path filePath) {
		File file;
		long length;
		long i=0;
		try{
			file=filePath.toFile();
			length = file.length();
			FileInputStream fileStream = new FileInputStream(file);

			synchronized(this){
				outbound.writeUTF(Protocol.REQUEST_BACKUP);
				outbound.writeUTF(filePath.getFileName().toString());
				outbound.writeLong(length);
				for(i=0; i<length; i++){
					outbound.writeByte((byte) fileStream.read());
				}
				fileStream.close();
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public String getStoragePath() {
		return network.getBackupStoragePath();
	}

	public void recordStoredFile(String fileName) {
		synchronized(network.seenFiles) {
			network.seenFiles.add(fileName);
		}
		
	}

	public void requestUpdatedFileList() {
		try{
			outbound.writeUTF(Protocol.REQUEST_LIST_FILES);
		}catch(IOException e){
			e.printStackTrace();
			kill();
		}
	}

	public void sendStoredFileList() throws IOException {
		File storageRoot=new File(getStoragePath());
		String[] files = storageRoot.list();
		if(files==null)
			return;
		synchronized(this){
			outbound.writeUTF(Protocol.REPLY_LIST_FILES);
			outbound.writeInt(files.length);
			for(String fileName:files)
				outbound.writeUTF(fileName);
		}
	}

}
