package backupbuddies.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import backupbuddies.Debug;
import backupbuddies.network.packet.BackupFile;
import backupbuddies.network.packet.ListFiles;
import backupbuddies.network.packet.NotifyNewPeer;
import backupbuddies.network.packet.RequestListOfFiles;
import backupbuddies.network.packet.RequestRestoreFile;

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
	
	@SuppressWarnings("deprecation")
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
		return BackupFile.send(this, filePath);
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
				RequestListOfFiles.send(outbound);
			}
		}catch(IOException e){
			e.printStackTrace();
			kill(e);
		}
	}

	public void sendStoredFileList() throws IOException {
		synchronized(this){
			ListFiles.send(this, network, outbound);
		}
	}
	
	// Method to notify a peer about another peer
	public void notifyNewPeer( Peer peer ) throws IOException{
		synchronized(this){
			NotifyNewPeer.send(outbound, peer, this);
		}
	}
	
	public void downloadFile(String fileName) {
		try{
			synchronized(this){
				RequestRestoreFile.send(outbound, fileName);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Collection<String> getKnownFiles() {
		return filesStored;
	}

	public DataOutputStream getOutputStream() {
		return outbound;
	}
	
	
	
	
	

}
