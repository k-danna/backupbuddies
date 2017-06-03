package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Key;
import java.security.MessageDigest;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import backupbuddies.Properties;
import backupbuddies.network.IPacketHandler;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.network.Protocol;

/**
 * We are being sent a file and asked to store a backup of it.
 *
 */
public class BackupFile implements IPacketHandler {

	@Override
	public String getIdentifier() {
		return Protocol.REQUEST_BACKUP;
	}
	
	public static synchronized boolean send(Peer peer, Network network, Path filePath){
		File temporaryFileDir = new File(System.getProperty("user.home"), "backupbuddies/temp");
		temporaryFileDir.mkdirs();
		
		File compressedFile = new File(temporaryFileDir, "compressing.tmp");
		try {
			compress(filePath.toFile(), compressedFile);
		} catch (Exception e) {
			System.out.print(e);
		}
		
		String key = network.encryptionKey;
		File encryptedFile = new File(temporaryFileDir, "encrypting.tmp");
		// Encrpt compressed file
		try {
			encrypt(key, compressedFile, encryptedFile);
		} catch (Exception e) {
			Network.log("Encryption failed!");
			e.printStackTrace();
			return false;
		}
		
		long length;
		long i=0;
		try{
		    Network.log("Sending file");
			length = encryptedFile.length();
			FileInputStream fileStream = new FileInputStream(encryptedFile);
			
			DataOutputStream outbound=peer.getOutputStream();
			
			synchronized(peer){
				outbound.writeUTF(Protocol.REQUEST_BACKUP);
				outbound.writeUTF(filePath.getFileName().toString());
				outbound.writeLong(length);
				for(i=0; i<length; i++){
					outbound.writeByte((byte) fileStream.read());
				}
				fileStream.close();
				compressedFile.delete();
				encryptedFile.delete();
		        Network.log("Send success");
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
		    Network.log("Send failed");
			return false;
		}
	}

	@Override
	public void handlePacket(Peer peer, Network network, DataInputStream inbound) throws IOException {
		synchronized(network.fileStorageLock){
			String fileName=inbound.readUTF();
			long length=inbound.readLong();
			File file=new File(new File(peer.getStoragePath(), peer.displayName), fileName);
			
			//File exceeds our allocated space
			//Don't let them send the whole file - close the connection to indicate
			//failure. we still want to be connected, so reconnect
			if(!network.requestSpaceForFile(length)) {
				NotifyTransferFailed.send(peer.getOutputStream(), fileName, network.getBytesLimit() - network.getBytesStored());
				network.resetConnection(peer, "Cannot store oversize file: "+fileName);
			}
			
			//You can overwrite existing backups
			if(file.exists())
				file.delete();

			file.getParentFile().mkdirs();

			file.createNewFile();
			FileOutputStream out=new FileOutputStream(file);

			for(long i=0; i<length; i++){
				out.write(inbound.readByte());
			}
			out.close();
		}
	}
	
	public static void compress(File source, File destination) throws Exception{
		byte[] buffer = new byte[1024];
		
		// File handler for source file
		FileInputStream fis = new FileInputStream(source);
		
		// File handler for destination file
		FileOutputStream fos = new FileOutputStream(destination);
		
		// Zipped file handler
		GZIPOutputStream gzos = new GZIPOutputStream(fos);
		
		int read;
		// read() returns bytes read or -1 if none is read
		while((read = fis.read(buffer)) != -1 ){
			// write read amount of bytes from buffer to output file
			gzos.write(buffer,0, read);
		}
		gzos.finish();
		gzos.close(); 
		fos.close();
		fis.close();
	}
	
	private static void encrypt( String key, File inputFile, File outputFile) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = md.digest(key.getBytes());
		
		byte[] hashBytesCut = new byte[16];
		for(int i=0; i<hashBytesCut.length; i++)
			hashBytesCut[i]=hashBytes[i];
		
		int cipherMode = Cipher.ENCRYPT_MODE;
		Key secretKey = new SecretKeySpec(hashBytesCut, Properties.ALGORITHM);
		Cipher cipher = Cipher.getInstance(Properties.TRANSFORMATION);
		cipher.init(cipherMode, secretKey);
             
		FileInputStream inputStream = new FileInputStream(inputFile);
		byte[] inputBytes = new byte[(int) inputFile.length()];
		inputStream.read(inputBytes);
             
		byte[] outputBytes = cipher.doFinal(inputBytes);
             
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		outputStream.write(outputBytes);
		inputStream.close();
		outputStream.close();
    }
	
	

}
