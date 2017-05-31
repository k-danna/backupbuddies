package backupbuddies.network;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/*
 * Data for a peer that may or may not be offline.
 */
public class OfflinePeer implements Serializable {

	private static final long serialVersionUID = 3742360799210590426L;

	public final String url;
	public final String displayName;
	
	public final Set<String> filesStored;
	
	public OfflinePeer(String url, String displayName, Set<String> filesStored) {
		this.url=url;
		this.displayName=displayName;
		this.filesStored=filesStored;
	}
	
	public int hashCode(){
		return url.hashCode() * displayName.hashCode();
	}
	
	public boolean equals(Object peerObj){
		if(peerObj instanceof OfflinePeer){
			OfflinePeer peer=(OfflinePeer) peerObj;
			return peer.url.equals(url) && peer.displayName.equals(displayName);
		} else {
			return false;
		}
	}
	
}
