package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import backupbuddies.network.IPacketHandler;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.network.Protocol;

public class NotifyTransferFailed implements IPacketHandler {

	@Override
	public String getIdentifier() {
		return Protocol.NOTIFY_TRANSFER_FAILED;
	}
	
	public static void send(DataOutputStream outbound, String fileName, long bytesLimit) throws IOException {
		outbound.writeUTF(Protocol.NOTIFY_TRANSFER_FAILED);
		outbound.writeUTF(fileName);
		outbound.writeLong(bytesLimit);
	}

	@Override
	public void handlePacket(Peer peer, Network network, DataInputStream inbound) throws IOException {
		String fileName = inbound.readUTF();
		long theirLimit = inbound.readLong();
		Network.log("peer has no more space");
	}

}
