package backupbuddies.network;

import java.io.IOException;
import java.util.Arrays;

import backupbuddies.Properties;
import static backupbuddies.Debug.*;

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
				if(command==null){
					connection.kill();
					return;
				}
				switch(command){
				//TODO this is where messages are handled
				
				//If an invalid command is sent, kill the connection
				//It's incompatible with us
				default:
					connection.kill();
					break;
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
		//TODO this seems broken
		String line=connection.inbound.readLine();
		if(line==null)
			return false;

		//Eat all the non-printable characters
		//I don't know why they get in, but they do.
		line=line.replaceAll("\\p{C}", "");
		
		dbg(Arrays.toString(line.getBytes()));
		dbg(line);
		dbg(Arrays.toString(Properties.HANDSHAKE.getBytes()));

		if(!(line.equals(Properties.HANDSHAKE)))
			return false;

		//Check password
		line=connection.inbound.readLine();

		if(line==null)
			return false;
		
		dbg(Arrays.toString(line.getBytes()));
		dbg(Arrays.toString(connection.password.getBytes()));
		
		line=line.replaceAll("\r", "").replaceAll("\n", "");
		if(!line.equals(connection.password))
			return false;

		//All checks passed = we're good
		return true;
	}
}