package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
			String fileName=input.readUTF();
			String fileDir = network.getBackupStoragePath();
			Path filePath = new File(fileDir,fileName).toPath();
			try{
				ReplyRestoreFile.send(network, filePath, peer.getOutputStream());
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	}

}
