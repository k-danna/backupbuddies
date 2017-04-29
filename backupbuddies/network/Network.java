// Network.java
package backupbuddies.network;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import backupbuddies.Debug;

import static backupbuddies.Debug.*;

public class Network {

	public final String password;

	//You can look up peers by their IP
	//TODO is this what the GUI team needs?
	public HashMap<String, Peer> connections = new HashMap<>();
	
	//All connections we have ever seen
	public HashSet<String> seenConnections = new HashSet<>();

	/*
	 * All files we have ever seen
	 * 
	 * TODO this will need changing for various reasons
	 */
	HashSet<String> seenFiles = new HashSet<>();
	
	//A lock for the file storage
	public final Object fileStorageLock = new Object();
	
	//A hash map from file names to the paths to store them at
	HashMap<String, String> downloadingFileLocs = new HashMap<>();
	
	public String storagePath;
	
	public Network(String password){
		this.password=password;
		storagePath = new File(System.getProperty("user.home"), "backupbuddies")
				.getAbsolutePath();
		
		new Thread(new IncomingConnectionHandler(this)).start();
	}
	/*
	 * Creates a connection to a URL
	 */
	public void connect(String url){
		if(url.equals(""))
			return;
		try{
			Peer peer=new Peer(url, this);
			setupPeer(peer);
		}catch(IOException e){
			e.printStackTrace();
		}
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
				seenConnections.add(peer.url);
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

}
