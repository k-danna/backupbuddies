package backupbuddies.network;

import java.io.IOException;

import backupbuddies.Properties;

final class PeerServicer implements Runnable {

	private final Peer connection;

	PeerServicer(Peer peer) {
		this.connection = peer;
	}

	@Override
	public void run() {
		try{
			// If they fail to handshake properly, they're either not Backup
			// Buddies or don't have the password. Don't take their commands.
			if(connection.requireHandshake) {
				if(!checkHandshake()){
					connection.kill();
					return;
				}
			}

			while(!connection.isDead()){
				String command=connection.inbound.readLine();
				switch(command){
				//TODO this is where messages are handled
				
				//Invalid command messages = kill the connection
				default:
					connection.kill();
				}
			}
		}catch(IOException e){
			connection.kill();
			return;
		}
	}

	//Receives a handshake
	private boolean checkHandshake() throws IOException {
		//Check handshake first part
		String line=connection.inbound.readLine();
		System.out.println(line);
		if(!line.equals(Properties.HANDSHAKE))
			return false;

		//Check password
		line=connection.inbound.readLine();
		System.out.println(line);
		if(!line.equals(connection.password))
			return false;

		//All checks passed = we're good
		return true;
	}
}