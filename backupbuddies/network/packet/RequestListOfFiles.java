package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import backupbuddies.network.IPacketHandler;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.network.Protocol;

/**
 * Sender requests that receiver reply with the list of files they currently
 * store
 */
public class RequestListOfFiles implements IPacketHandler {

	@Override
	public String getIdentifier() {
		return Protocol.REQUEST_LIST_FILES;
	}
	
	public static void send(DataOutputStream out) throws IOException {
		out.writeUTF(Protocol.REQUEST_LIST_FILES);
	}

	@Override
	public void handlePacket(Peer peer, Network network, DataInputStream input) throws IOException {
		peer.sendStoredFileList();
	}

}
