package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

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
	
	public static boolean send(Peer peer, Path filePath){
		File temporaryFileDir = new File(System.getProperty("user.home"), "backupbuddies/temp");
		temporaryFileDir.mkdirs();
		
		File compressedFile = new File(temporaryFileDir, "compressing.tmp");
		
		try {
			compress(filePath.toFile(), compressedFile);
		} catch (Exception e) {
			System.out.print(e);
		}
		
		long length;
		long i=0;
		try{
			length = compressedFile.length();
			FileInputStream fileStream = new FileInputStream(compressedFile);
			
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
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void handlePacket(Peer peer, Network network, DataInputStream inbound) throws IOException {
		synchronized(network.fileStorageLock){
			String fileName=inbound.readUTF();
			long length=inbound.readLong();
			File file=new File(peer.getStoragePath(), fileName);
			
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

}
