package backupbuddies.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import backupbuddies.Properties;

public class Network {
	
	private final String password;
	
	//You can look up peers by their IP
	//TODO is this what the GUI team needs?
	private HashMap<String, Peer> connections = new HashMap<>();
	
	public Network(String password){
		this.password=password;
		new Thread(new IncomingConnectionHandler()).start();
	}
	/*
	 * Returns null if all is well, or the IOException if we failed to
	 * open the connection
	 */
	public IOException connect(String url){
		try{
			Peer peer=new Peer(url, password);
			//We have to lock connections so that no new peer overwrites it
			synchronized(connections){
				connections.put(url,peer);
			}
			return null;
		}catch(IOException e){
			return e;
		}
	}
	
	private class IncomingConnectionHandler implements Runnable {

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
					Peer peer=new Peer(incomingSocket, password, false);
					synchronized(connections){
						connections.put(peer.url, peer);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
