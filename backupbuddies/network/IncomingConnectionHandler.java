package backupbuddies.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import backupbuddies.Debug;

class IncomingConnectionHandler implements Runnable {

	/**
	 * 
	 */
	private final Network network;

	/**
	 * @param network
	 */
	IncomingConnectionHandler(Network network) {
		this.network = network;
	}

	//Runs accepting incoming connections and adds them as peers.
	@SuppressWarnings("resource")
	@Override
	public void run() {
		ServerSocket serverSocket;
		try{
			serverSocket = new ServerSocket(Protocol.DEFAULT_PORT);
		}catch(IOException e){
			throw(new RuntimeException(e));
		}
		
		while(true)
		{
			try {
				Socket incomingSocket = serverSocket.accept();
				Peer peer=new Peer(incomingSocket, this.network);
				Debug.dbg(peer.url);
				network.killPeerIfDuplicate(peer);
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}
	
}
