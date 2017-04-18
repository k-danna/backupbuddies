package backupbuddies.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import backupbuddies.Properties;

public class Network {
	
	private final String password;
	
	private HashMap<String, Peer> connections;
	
	public Network(String password){
		this.password=password;
		new Thread(new IncomingConnectionHandler()).start();
	}
	
	public void connect(String url){
		Peer peer=new Peer(url, password);
		//We have to lock connections so that no new peer overwrites it
		synchronized(connections){
			connections.put(url,peer);
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
					Peer conn=new Peer(incomingSocket, password);
					synchronized(connections){
						connections.put(conn.url, conn);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
