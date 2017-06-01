// Network.java
package backupbuddies.network;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import backupbuddies.Debug;
import backupbuddies.Properties;

public class Network implements Serializable {


	private static final long serialVersionUID = 3;

	public final String password;

	//You can look up peers by their hostname
	//TODO is this what the GUI team needs?
	public transient HashMap<String, Peer> connections = new HashMap<>();
	
	//All connections we have ever seen, with timeouts
	//Format is IP -> time
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
	
	/*
	 * Key: IP address
	 * Value: An OfflinePeer
	 */
	public HashMap<String, OfflinePeer> offlinePeers = new HashMap<>();
	
	//A lock for the file storage
	public transient Object fileStorageLock = new Object();
	
	//A hash map from file names to the paths to store them at
	transient HashMap<String, String> downloadingFileLocs = new HashMap<>();
	
	//The currently active encryption key
	public String encryptionKey = "";
	
	public final String storagePath;
	
	//Number of bytes already stored
	private long bytesStored;
	
	private long bytesLimit;
	
	transient ArrayDeque<String> log=new ArrayDeque<>();
	
	String displayName;
	
	public Network(String password){
		Debug.mark();
		this.password=password;
		File f = new File(Properties.BUB_HOME, "files");
		f.mkdirs();
		storagePath=f.getAbsolutePath();
		
		//Set the initial limit to 5% of your initial hard drive space
		long freeBytes = getFileSystemFreeBytes();
		setBytesLimit(getFileSystemFreeBytes() / 20);
		Debug.dbg(bytesLimit);
		
		new Thread(new IncomingConnectionHandler(this)).start();
		
		displayName = guessComputerName();
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
			
			//Check for duplicates properly, before connecting to anything
			if(offlinePeers.containsKey(url)) {
				String hname = offlinePeers.get(url).displayName;
				if(hname != null && connections.get(hname) != null)
					return;
			}
			
			try{
				Peer peer=new Peer(url, this);
				killPeerIfDuplicate(peer);
			} catch(ConnectException e){
				this.log("Could not connnect to "+url);
			} catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	public void resetConnection(Peer peer, String message){
		peer.kill(message);
		connect(peer.url);
	}

	public void killPeerIfDuplicate(Peer peer) throws IOException {
		Debug.dbg(peer.url);
		synchronized(connections){
			if(connections.containsKey(peer.displayName)) {
				Debug.dbg("Peer "+peer.displayName+" is already connected!");
				//Can't use kill() - that removes it from connections
				peer.cleanup("Duplicate peers with display name "+peer.displayName+": "+peer.url+", "+connections.get(peer.displayName).url);
			}
		}
	}
	
	//Notifies the network that the given Peer has completed a valid handshake
	public void onValidHandshake(Peer peer) throws IOException{
		// Send new peer a list of peers we are already connected to
		for(Peer i: connections.values() ){
			peer.notifyNewPeer(i);
			i.notifyNewPeer(peer);
			Debug.dbg("Introducing "+i.displayName + " to "+peer.displayName);
		}
		//Connect with peer
		connections.put(peer.displayName, peer);
		seenConnections.put(peer.url, System.currentTimeMillis());
		OfflinePeer offline = peer.getPersistentData();
		offlinePeers.put(peer.url, offline);
		// Inform list of peers connected to about new peer
	}

	//If a Peer/connection fails, we shouldn't keep it around in connections
	//Remove it
	public void onConnectionDie(Peer peer) {
		synchronized(connections){
			connections.remove(peer.displayName);
		}
	}

	//Returns a collection of peers
	public Collection<Peer> getPeers(){
		synchronized(connections){
			return connections.values();
		}
	}

	public Collection<String> getPeerDisplayNames(){
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

	public String guessComputerName(){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Failed to guess host name! Making something up...");
			return UUID.randomUUID().toString();
		}

	}
	
	//Leaves a message for the file downloading system saying that we requested
	//a download, as well as where to put it once we get it.
	public void setFileLoc(String fileName, String fileDir) {
		this.downloadingFileLocs.put(fileName, fileDir);
	}
	
	public Peer getPeerByDisplayName(String peerName){
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

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String newName) {
		this.displayName=newName;
	}

	public long getFileSystemFreeBytes() {
		try {
			FileStore store = Files.getFileStore(new File(storagePath).toPath());
			return store.getUsableSpace();
		} catch (IOException e) {
			e.printStackTrace();
			//1 GB
			return 1024*1024*1024;
		}
	}

}
