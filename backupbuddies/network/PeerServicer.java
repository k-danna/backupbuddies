package backupbuddies.network;

import java.io.BufferedReader;
import java.io.IOException;

import backupbuddies.Properties;

final class PeerServicer implements Runnable {
	
	private final Peer peer;
	
	private String password;
	
	private BufferedReader inbound;

	PeerServicer(Peer peer, String password) {
		this.peer = peer;
		this.password=password;
	}

	@Override
	public void run() {
		try{
			// If they fail to handshake properly, don't take their commands
			if(!checkHandshake()) {
				this.peer.hasFailed=true;
				return;
			}
			
			while(true){
				String command=inbound.readLine();
				switch(command){
				//TODO this is where commands are handled
				}
			}
		}catch(IOException e){
			this.peer.hasFailed=true;
			return;
		}
	}
	
	//Receives a handshake
	private boolean checkHandshake() throws IOException {
		//Check handshake first part
		String line=inbound.readLine();
		System.out.println(line);
		if(!line.equals(Properties.HANDSHAKE))
			return false;
		
		//Check password
		line=inbound.readLine();
		System.out.println(line);
		if(!line.equals(password))
			return false;
		
		//All checks passed = we're good
		return true;
	}
}