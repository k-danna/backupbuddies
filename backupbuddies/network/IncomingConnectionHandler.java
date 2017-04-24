package backupbuddies.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import backupbuddies.Properties;

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

	//Server socket stays open for the life of the program
	//We don't care if it leaks, until (unless) we support multiple nets
	@SuppressWarnings("resource")
	@Override
	public void run() {
		ServerSocket serverSocket;
		try{
			serverSocket = new ServerSocket(Properties.DEFAULT_PORT);
		}catch(IOException e){
			throw(new RuntimeException(e));
		}
		
		while(true)
		{
			try {
				Socket incomingSocket = serverSocket.accept();
				Peer peer=new Peer(incomingSocket, this.network.password, false, this.network);
				synchronized(this.network.connections){
					this.network.connections.put(peer.url, peer);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}