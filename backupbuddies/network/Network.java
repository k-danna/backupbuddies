package backupbuddies.network;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static backupbuddies.Debug.*;

public class Network {
	
	public final String password;
	
	//You can look up peers by their IP
	//TODO is this what the GUI team needs?
	HashMap<String, Peer> connections = new HashMap<>();
	
	public Network(String password){
		this.password=password;
		new Thread(new IncomingConnectionHandler(this)).start();
	}
	/*
	 * Returns null if all is well, or the IOException if we failed to
	 * open the connection
	 */
	public IOException connect(String url){
		if(url.equals(""))
			return null;
		try{
			synchronized(connections){
				//If they're already connected, skip it
				if(connections.containsKey(url))
					return null;
				
				Peer peer=new Peer(url, password, this);
				//We have to lock connections so that no new peer overwrites it
				
				connections.put(url,peer);
			}
			return null;
		}catch(IOException e){
			return e;
		}
	}
	
	public void onConnectionDie(Peer peer) {
		synchronized(connections){
			connections.remove(peer.url);
		}
	}
	
	public Collection<Peer> getPeers(){
		return connections.values();
	}
	
	public String getPath() {
		//TODO can change this
		return "/home/planetguy/backupbuddies";
	}

}
