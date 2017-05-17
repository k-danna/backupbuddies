package backupbuddies.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import backupbuddies.Debug;

import static backupbuddies.Debug.*;

public class Peer {

	private boolean isDead=false;

	private Socket socket;

	DataOutputStream outbound;
	private Thread peerServicer;

	public final String url;

	boolean requireHandshake;
	//String password;

	Network network;
	
	Set<String> filesStored=new HashSet<>();
	
	
	//This is the UUID we generate
	final String token;

	/**
	 * This always opens a new Socket, so it always has to send the handshake
	 */
	Peer(String url, Network net) throws IOException{
		this(new Socket(url, Protocol.DEFAULT_PORT), true, net);
	}

	Peer(Socket socket, boolean sendHandshake, Network net) {
		this.network=net;
		
		//Generate our token at random
		token=UUID.randomUUID().toString();
		
		//It gives addrs in the form "example.com/127.0.0.1" - take only the IP
		String[] a=socket.getInetAddress().toString().split("/");
		this.url=a[a.length-1];

		try{
			outbound = new DataOutputStream(socket.getOutputStream());
			DataInputStream inbound = new DataInputStream(socket.getInputStream());

			requireHandshake = !sendHandshake;
			sendHandshake();

			peerServicer = new Thread(new PeerServicer(this, inbound));
			peerServicer.start();
		} catch(Exception e) {
			this.kill(e);
		}
	}

	//Sends a handshake message
	private synchronized void sendHandshake() throws IOException {
		outbound.writeUTF(Protocol.HANDSHAKE);
		outbound.writeUTF(token);
	}
	
	public synchronized void sendLoginToken(String theirToken) throws IOException {
		String loginKey = token + theirToken + network.password;
		
		byte[] ourHash = computeHash(loginKey);
		
		outbound.write(ourHash);
	}
	
	static byte[] computeHash(String input){
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = md.digest(input.getBytes());
			return hashBytes;
		}catch(NoSuchAlgorithmException e){
			//We're hosed
			throw new RuntimeException(e);
		}

	}

	public boolean isDead(){
		return isDead;
	}

	// Call this if the connection is broken/shouldn't be used further
	public synchronized void kill(Object error){
		Debug.dbg(error);
		network.onConnectionDie(this);
		cleanup();
	}
	
	public synchronized void cleanup(){
		peerServicer.stop();
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
	
	public boolean uploadFile(Path filePath) {
		File temporaryFileDir = new File(System.getProperty("user.home"), "backupbuddies/temp");
		temporaryFileDir.mkdirs();
		
		File compressedFile = new File(temporaryFileDir, "compressing.tmp");
		
		try {
			compress(filePath.toFile(), compressedFile);
		} catch (Exception e) {
			System.out.print(e);
		}
		
		long length;
		long i=0;
		try{
			length = compressedFile.length();
			FileInputStream fileStream = new FileInputStream(compressedFile);
			
			synchronized(this){
				outbound.writeUTF(Protocol.REQUEST_BACKUP);
				outbound.writeUTF(filePath.getFileName().toString());
				outbound.writeLong(length);
				for(i=0; i<length; i++){
					outbound.writeByte((byte) fileStream.read());
				}
				fileStream.close();
				compressedFile.delete();
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static void compress(File source, File destination) throws Exception{
		byte[] buffer = new byte[1024];
		
		// File handler for source file
		FileInputStream fis = new FileInputStream(source);
		
		// File handler for destination file
		FileOutputStream fos = new FileOutputStream(destination);
		
		// Zipped file handler
		GZIPOutputStream gzos = new GZIPOutputStream(fos);
		
		int read;
		// read() returns bytes read or -1 if none is read
		while((read = fis.read(buffer)) != -1 ){
			// write read amount of bytes from buffer to output file
			gzos.write(buffer,0, read);
		}
		gzos.finish();
		gzos.close(); 
		fos.close();
		fis.close();
	}


	public String getStoragePath() {
		return network.getBackupStoragePath();
	}

	public void recordStoredFile(String fileName) {
		synchronized(network.seenFiles) {
			network.seenFiles.add(fileName);
		}
		this.filesStored.add(fileName);
		
	}

	public void requestUpdatedFileList() {
		try{
			synchronized(this){
				outbound.writeUTF(Protocol.REQUEST_LIST_FILES);
			}
		}catch(IOException e){
			e.printStackTrace();
			kill(e);
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
	
	// Method to notify a peer about another peer
	public synchronized void notifyNewPeer( Peer peer ) throws IOException{
		outbound.writeUTF(Protocol.NOTIFY_NEW_PEER);
		outbound.writeUTF(peer.url);
		Debug.dbg(peer.url);
	}
	
	public void downloadFile(String fileName) {
		try{
			synchronized(this){
				outbound.writeUTF(Protocol.REQUEST_RETRIEVE);
				outbound.writeUTF(fileName);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public synchronized void restoreFile(String fileName, Path filePath) throws IOException {
		synchronized(network.fileStorageLock){
			synchronized(this){
				File file=filePath.toFile();
				FileInputStream fileStream = new FileInputStream(file);
				long length=file.length();
				
				outbound.writeUTF(Protocol.REPLY_RETRIEVE);
				outbound.writeUTF(filePath.getFileName().toString());
				outbound.writeLong(length);
				for(long i=0; i<length; i++){
					outbound.writeByte((byte) fileStream.read());
				}
				fileStream.close();
			}
		}
		
	}

	public Collection<String> getKnownFiles() {
		return filesStored;
	}

	public void notifyFileRejection(String fileName, long bytesLimit) throws IOException {
		synchronized(this){
			outbound.writeUTF(Protocol.NOTIFY_TRANSFER_FAILED);
			outbound.writeUTF(fileName);
			outbound.writeLong(bytesLimit);
		}
		
	}
	
	
	
	
	

}
