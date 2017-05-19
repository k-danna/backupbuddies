// Network.java
package backupbuddies.network;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import backupbuddies.Debug;
import backupbuddies.Properties;

public class Network implements Serializable {


	private static final long serialVersionUID = -6752762844617097329L;

	public final String password;

	//You can look up peers by their IP
	//TODO is this what the GUI team needs?
	public transient HashMap<String, Peer> connections = new HashMap<>();
	
	//All connections we have ever seen
	public HashMap<String, Long> seenConnections = new HashMap<>();

	/*
	 * All files we have ever seen
	 * 
	 * TODO this will need changing for various reasons
	 */
	HashSet<String> seenFiles = new HashSet<>();

	/*
     * Log events, append all events to this,
     * eventLog.add("x connected to y");
     * this should be returned in the interface method
	 */
	List<String> eventLog = new ArrayList<>();
	
	//A lock for the file storage
	public transient Object fileStorageLock = new Object();
	
	//A hash map from file names to the paths to store them at
	transient HashMap<String, String> downloadingFileLocs = new HashMap<>();
	
	public String storagePath;
	
	//Number of bytes already stored
	private long bytesStored;
	
	private long bytesLimit;
	
	transient ArrayDeque<String> log=new ArrayDeque<>();
	
	public Network(){
		password=null;
	}
	
	public Network(String password){
		this.password=password;
		storagePath = new File(Properties.BUB_HOME, "files")
				.getAbsolutePath();
		
		//Set the initial limit to 10% of your initial hard drive space
		setBytesLimit(new File(storagePath).getFreeSpace() / 10);
		
		new Thread(new IncomingConnectionHandler(this)).start();
	}
	
	//Apparently transient things don't get auto-created
	//We have to do these things ourselves
	public void init(){
		connections = new HashMap<>();
		fileStorageLock = new Object();
		downloadingFileLocs = new HashMap<>();
		log=new ArrayDeque<>();
		for(String s:seenConnections.keySet()){
			//Maybe you were the one who was offline for 30 days
			//In that case, you don't want to delete all your peers
			//Try all your peers first - this will update timeouts
			//for each.
			connect(s);
			if(System.currentTimeMillis() - seenConnections.get(s) 
					> Properties.PEER_REMEMBER_MILLIS) {
				seenConnections.remove(s);
			}
		}
	}
	/*
	 * Creates a connection to a URL
	 */
	public void connect(String url){
		synchronized(connections){
			if(url.equals(""))
				return;
			//Don't open a duplicate connection if one dies
			if(connections.containsKey(url))
				return;
			try{
				Peer peer=new Peer(url, this);
				setupPeer(peer);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	public void resetConnection(Peer peer, String message){
		peer.kill(message);
		connect(peer.url);
	}

	public void setupPeer(Peer peer) throws IOException {
		Debug.dbg(peer.url);
		synchronized(connections){
			if(connections.containsKey(peer.url))
				//Can't use kill() - that removes it from connections
				peer.cleanup();
			// Check if the new peer is connected 
			if(!peer.isDead()) {
				// Send new peer a list of peers we are already connected to
				for(Peer i: connections.values() ){
					peer.notifyNewPeer(i);
					i.notifyNewPeer(peer);
				}
				//Connect with peer
				connections.put(peer.url,peer);
				seenConnections.put(peer.url, System.currentTimeMillis());
				// Inform list of peers connected to about new peer
			}
		}
	}

	//If a Peer/connection fails, we shouldn't keep it around in connections
	//Remove it
	public void onConnectionDie(Peer peer) {
		synchronized(connections){
			connections.remove(peer.url);
		}
	}

	//Returns a collection of peers
	public Collection<Peer> getPeers(){
		synchronized(connections){
			return connections.values();
		}
	}

	public Collection<String> getPeerIPAddresses(){
		synchronized(connections){
			return connections.keySet();
		}
	}

	public String getBackupStoragePath() {
		//TODO can change this
		return storagePath;
	}

	@SuppressWarnings("unchecked")
	public Collection<String> getKnownFiles() {
		synchronized(seenFiles){
			return (Collection<String>) seenFiles.clone();
		}
	}
	
	public void setFileLoc(String fileName, String fileDir) {
		this.downloadingFileLocs.put(fileName, fileDir);
	}
	
	public Peer getPeer(String peerName){
		return connections.get(peerName);
	}
	
	//Requests and reserves space for a new file
	public boolean requestSpaceForFile(long length){
		if(getBytesStored() + length < getBytesLimit()) {
			setBytesStored(getBytesStored() + length);
			return true;
		} else {
			return false;
		}
	}
	
	public ArrayDeque<String> getErrorLog(){
		return log.clone();
	}
	
	public void log(String message){
		log.addFirst(message);
		if(log.size() > Properties.LOG_MESSAGE_COUNT)
			log.removeLast();
	}

	public String getAndRemoveDownloadingFileLocation(String fileName) {
		String s = this.downloadingFileLocs.get(fileName);
		if(s != null)
			downloadingFileLocs.remove(s);
		return s;
	}

	public long getBytesLimit() {
		return bytesLimit;
	}

	public void setBytesLimit(long bytesLimit) {
		this.bytesLimit = bytesLimit;
	}

	public long getBytesStored() {
		return bytesStored;
	}

	public void setBytesStored(long bytesStored) {
		this.bytesStored = bytesStored;
	}

}
