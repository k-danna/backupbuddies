package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import backupbuddies.network.IPacketHandler;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.network.Protocol;

/**
 * We are being asked to restore a file that we have backed up
 *
 */
public class RequestRestoreFile implements IPacketHandler {

	@Override
	public String getIdentifier() {
		return Protocol.REQUEST_RETRIEVE;
	}
	
	public static void send(DataOutputStream outbound, String fileName) throws IOException {
		outbound.writeUTF(Protocol.REQUEST_RETRIEVE);
		outbound.writeUTF(fileName);
	}

	@Override
	public void handlePacket(Peer peer, Network network, DataInputStream input) throws IOException {
		synchronized(network.fileStorageLock){
			String fileNameWhole=input.readUTF();
			if(fileNameWhole.contains("/")) {
				String[] parts=fileNameWhole.split("/");
				ReplyRestoreFile.send(network, parts[0], parts[1], peer.getOutputStream());
			} else {
				ReplyRestoreFile.send(network, null, fileNameWhole, peer.getOutputStream());
			}
		}

	}

}
