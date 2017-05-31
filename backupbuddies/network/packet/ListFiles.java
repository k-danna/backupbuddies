package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import backupbuddies.network.IPacketHandler;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.network.Protocol;

/**
 * Sender tells receiver which files they are storing
 */
public class ListFiles implements IPacketHandler {

	@Override
	public String getIdentifier() {
		return Protocol.REPLY_LIST_FILES;
	}

	@Override
	public void handlePacket(Peer peer, Network network, DataInputStream inbound) throws IOException {
		int files=inbound.readInt();
		for(int i=0; i<files; i++){
			peer.recordStoredFile(inbound.readUTF());
		}
	}

	public static void send(Peer peer, Network network, DataOutputStream outbound) throws IOException {
		synchronized(network.fileStorageLock) {
			File storageRoot=new File(peer.getStoragePath());
			String[] files = storageRoot.list();
			if(files==null)
				return;
			List<String> result=new ArrayList<>();
			
			for(String thing:files){
				File f=new File(storageRoot, thing);
				if(f.isDirectory()){
					for(String subFile:f.list()) {
						result.add(thing+"/"+subFile);
					}
				} else {
					result.add(thing);
				}
			}
			
			outbound.writeUTF(Protocol.REPLY_LIST_FILES);
			outbound.writeInt(result.size());
			for(String fileName:result)
				outbound.writeUTF(fileName);
		}
	}

}
