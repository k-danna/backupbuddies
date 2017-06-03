package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import backupbuddies.Debug;
import backupbuddies.network.IPacketHandler;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.network.Protocol;

/**
 * Sender notifies receiver of a peer that they are connected to
 *
 */
public class NotifyNewPeer implements IPacketHandler {

	@Override
	public String getIdentifier() {
		return Protocol.NOTIFY_NEW_PEER;
	}
	
	public static void send(DataOutputStream outbound, Peer peerToAnnounce, Peer sendingPeer) throws IOException {
		try{
			outbound.writeUTF(Protocol.NOTIFY_NEW_PEER);
			outbound.writeUTF(peerToAnnounce.url);
			Debug.dbg(peerToAnnounce.url);
		}catch(IOException e){
			e.printStackTrace();
			sendingPeer.kill("Failed to send packet NotifyNewPeer");
		}
	}

	@Override
	public void handlePacket(Peer peer, Network network, DataInputStream input) throws IOException {
		String newPeer = input.readUTF();
		try{
			Debug.dbg("Trying to connect to "+newPeer);
			network.connect(newPeer);
		}catch(Exception e){
			Network.log("Failed to connect to peer");
			e.printStackTrace();
		}
	}

}
