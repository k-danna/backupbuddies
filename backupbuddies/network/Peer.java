package backupbuddies.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import backupbuddies.network.packet.BackupFile;
import backupbuddies.network.packet.Handshake;
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
	public String displayName;

	//String password;

	Network network;
	
	Set<String> filesStored=new HashSet<>();
	
	
	//This is the UUID we generate
	public final String token;
	
	private static Socket openSocket(String ipAddress, int port) throws IOException {
		Socket sock=new Socket();
		sock.connect(new InetSocketAddress(ipAddress, port), 2000);
		return sock;
	}

	/**
	 * This always opens a new Socket, so it always has to send the handshake
	 */
	Peer(String url, Network net) throws IOException{
		this(openSocket(url, Protocol.DEFAULT_PORT), net);
	}

	Peer(Socket socket, Network net) {
		this.network=net;
		
		//Generate our token at random
		token=UUID.randomUUID().toString();
		
		//It gives addrs in the form "example.com/127.0.0.1" - take only the IP
		String[] a=socket.getInetAddress().toString().split("/");
		this.url=a[a.length-1];

		try{
			outbound = new DataOutputStream(socket.getOutputStream());
			DataInputStream inbound = new DataInputStream(socket.getInputStream());

			Handshake.sendHandshake(outbound, token, network);

			peerServicer = new Thread(new PeerServicer(this, inbound));
			peerServicer.start();
		} catch(Exception e) {
			this.kill(e);
		}
	}
	
	Peer(OfflinePeer offline, Network net) throws IOException{
		this(offline.url, net);
	}

	public boolean isDead(){
		return isDead;
	}

	// Call this if the connection is broken/shouldn't be used further
	public synchronized void kill(Object error){
		/*
		Debug.dbg(error);
		Debug.caller();
		if(error instanceof Exception)
			((Exception) error).printStackTrace();
		 */
		network.onConnectionDie(this);
		cleanup(error);
	}
	
	@SuppressWarnings("deprecation")
	public synchronized void cleanup(Object error){
		if(peerServicer != null){
			peerServicer.stop();
			peerServicer=null;
		}
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
		return BackupFile.send(this, network, filePath);
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
			//e.printStackTrace();
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
			//e.printStackTrace();
		}
	}

	public Collection<String> getKnownFiles() {
		return filesStored;
	}

	public DataOutputStream getOutputStream() {
		return outbound;
	}
	
	public OfflinePeer getPersistentData(){
		return new OfflinePeer(this.url, this.displayName, this.filesStored);
	}

	public Network getNetwork() {
		return network;
	}

}
