package backupbuddies.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import backupbuddies.Properties;

public class Peer {
	
	boolean hasFailed=false;
	
	private Socket socket;
	
	private DataOutputStream outbound;
	private BufferedReader inbound;
	
	private Thread peerServicer;
	
	public final String url;
	
	/**
	 * Call to create a socket from a client
	 */
	Peer(String url, String password){
		this.url=url;
		
		int port = Properties.DEFAULT_PORT;
		if(url.contains(":")) {
			port = Integer.parseInt(url.substring(0, url.lastIndexOf(':')));
			url=url.substring(0, url.lastIndexOf(':'));
		}
		try {
			init(new Socket(url, port), password);
			sendHandshake(password);
		} catch (IOException e) {
			hasFailed=true;
		}
	}

	/**
	 * Call to create a socket from a server connection only
	 */
	Peer(Socket socket, String password) {
		this.url=socket.getInetAddress().toString();
		
		try{
			init(socket, password);
		} catch(Exception e) {
			hasFailed=true;
		}
	}
	
	//Sets up the fields
	private void init(Socket socket, String password) throws IOException{
		outbound = new DataOutputStream(socket.getOutputStream());
		inbound = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		peerServicer = new Thread(new PeerServicer(this, password));
	}
	
	//Sends a handshake
	private void sendHandshake(String password) throws IOException {
		outbound.writeUTF(Properties.HANDSHAKE + "\n");
		outbound.writeUTF(password+"\n");
	}
	
}
