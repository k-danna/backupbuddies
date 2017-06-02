package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import backupbuddies.Debug;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.network.Protocol;

/*
 * Initial handshake.
 */
public class Handshake {
	
	//Receives a handshake
	public static boolean checkHandshake(Peer peer, DataInputStream inbound) throws IOException, IllegalArgumentException {
		String magicNumber=inbound.readUTF();
		if(magicNumber==null)
			throw new IllegalArgumentException();

		if(!(magicNumber.equals(Protocol.MAGIC_NUMBER)))
			throw new IllegalArgumentException();

		//Reject anyone who tries to use the same display name as us
		//Chances are they __are__ us.
		peer.displayName=inbound.readUTF();
		if(peer.displayName.equals(peer.getNetwork().getDisplayName()))
			throw new IllegalArgumentException();
		
		//Check password
		String theirToken=inbound.readUTF();

		if(theirToken==null)
			throw new IllegalArgumentException();
		
		//Check that they sent us a valid UUID
		//This throws an IAE if the UUID is not valid
		UUID.fromString(theirToken);
		
		//Prevents an attack where
		//		A = attacker
		//		B = honest peer
		//B sends their token
		//A waits for this, then sends back the same token
		//B sends hash(their token + their token + password)
		//A sends that same hash back
		//B accepts it because swapping the tokens doesn't change it
		if(peer.token.equals(theirToken))
			throw new IllegalArgumentException();
		
		sendLoginToken(peer.token, theirToken, peer.getNetwork(), peer.getOutputStream());
		
		byte[] targetHash = computeHash(theirToken + peer.token + peer.getNetwork().password);
		
		byte[] theirHash = new byte[targetHash.length];
		
		int theirBytesRead = inbound.read(theirHash);
		
		if(theirBytesRead != theirHash.length)
			throw new IllegalArgumentException("Read "+theirBytesRead+", required "+theirHash.length);
		
		for(int i=0; i<theirHash.length; i++) {
			if(theirHash[i] != targetHash[i]) {
				Debug.dbg(Arrays.toString(targetHash));
				Debug.dbg(Arrays.toString(theirHash));				
				throw new IllegalArgumentException("Byte "+i+" mismatched!");
			}
		}
		
		peer.getNetwork().onValidHandshake(peer);
		
		return true;
	}
	
	//Sends a handshake message
	public static void sendHandshake(DataOutputStream outbound, String token, Network net) throws IOException {
		outbound.writeUTF(Protocol.MAGIC_NUMBER);
		outbound.writeUTF(net.getDisplayName());
		outbound.writeUTF(token);
	}
	
	private static void sendLoginToken(String token, String theirToken, Network network, DataOutputStream outbound) throws IOException {
		String loginKey = token + theirToken + network.password;
		
		byte[] ourHash = computeHash(loginKey);
		
		outbound.write(ourHash);
	}
	
	public static byte[] computeHash(String input){
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = md.digest(input.getBytes());
			return hashBytes;
		}catch(NoSuchAlgorithmException e){
			//We're hosed
			throw new RuntimeException(e);
		}
	}

}
