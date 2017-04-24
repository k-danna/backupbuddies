package backupbuddies.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import backupbuddies.Properties;

public class Peer {
	
	private boolean isDead=false;
	
	private Socket socket;
	
	private DataOutputStream outbound;
	BufferedReader inbound;
	
	private Thread peerServicer;
	
	public final String url;

	boolean requireHandshake;
	String password;
	
	/**
	 * This always opens a new Socket, so it always has to send the handshake
	 */
	Peer(String url, String password) throws IOException{
		this(new Socket(url, Properties.DEFAULT_PORT), password, true);
	}

	Peer(Socket socket, String password, boolean sendHandshake) {
		this.password=password;
		this.url=socket.getInetAddress().toString();
		
		try{
			outbound = new DataOutputStream(socket.getOutputStream());
			inbound = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			requireHandshake = !sendHandshake;
			if(sendHandshake)
				sendHandshake(password);
			
			peerServicer = new Thread(new PeerServicer(this));
			peerServicer.start();
		} catch(Exception e) {
			this.kill();
		}
	}
	
	//Sends a handshake message
	private void sendHandshake(String password) throws IOException {
		outbound.writeUTF(Properties.HANDSHAKE + "\n");
		outbound.writeUTF(password+"\n");
	}
	
	public boolean isDead(){
		return isDead;
	}
	
	// Call this if the connection is broken/shouldn't be used further
	public synchronized void kill(){
		new Exception().printStackTrace();
		peerServicer=null;
		try{
			outbound.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException|NullPointerException e) {
			e.printStackTrace();
		}

		isDead=true;
	}
	
}
