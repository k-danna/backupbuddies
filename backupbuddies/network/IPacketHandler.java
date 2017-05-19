package backupbuddies.network;

import java.io.DataInputStream;
import java.io.IOException;

public interface IPacketHandler {
	
	/**
	 * 
	 * @return The packet's identifier string. When a message comes in with
	 * that string, we should call handle() afterwards.
	 */
	public String getIdentifier();
	
	/**
	 * Handles an incoming packet.
	 * 
	 * @param peer The Peer who sent us the packet
	 * @param network The network object
	 * @param input The input stream for reading the rest of the packet
	 * @throws IOException
	 */
	public void handlePacket(Peer peer, Network network, DataInputStream input) throws IOException;
	
}
