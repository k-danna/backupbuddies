package backupbuddies.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import backupbuddies.Properties;
import backupbuddies.network.IPacketHandler;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.network.Protocol;

/**
 * We are sending a file to someone who asked us to restore it
 *
 */
public class ReplyRestoreFile implements IPacketHandler {

	@Override
	public String getIdentifier() {
		return Protocol.REPLY_RETRIEVE;
	}

	public static void send(Network network, String dirName, String fileName, DataOutputStream outbound) throws IOException {
		File file;
		if(dirName == null){
			file=new File(network.storagePath, fileName);
		} else {
			file=new File(new File(network.storagePath, dirName),fileName);
		}
		FileInputStream fileStream = new FileInputStream(file);
		long length=file.length();

		outbound.writeUTF(Protocol.REPLY_RETRIEVE);
		outbound.writeUTF(dirName+"/"+fileName);
		outbound.writeLong(length);
		for(long i=0; i<length; i++){
			outbound.writeByte((byte) fileStream.read());
		}
		fileStream.close();
	}
	@Override
	public void handlePacket(Peer peer, Network network, DataInputStream inbound) throws IOException {
		synchronized(network.fileStorageLock){
			// Get file name and length
			String fileNameWhole=inbound.readUTF();
			String fileName;
			if(fileNameWhole.contains("/")) {
				fileName=fileNameWhole.split("/")[1];
			} else {
				fileName=fileNameWhole;
			}
			long length=inbound.readLong();

			//If we don't have it, we didn't request the file
			String location=network.getAndRemoveDownloadingFileLocation(fileNameWhole);
			if(location==null)
				peer.kill("Tried to restore file that we didn't request!");

			File file=new File(location, fileName);

			//You can overwrite existing backups

			file.createNewFile();

			// Create a temporary file directory and file to read in the compressed file
			File temporaryFileDir = new File(Properties.BUB_HOME, "temp");
			temporaryFileDir.mkdirs();
			File compressedFile = new File(temporaryFileDir, "decompressing.tmp");

			FileOutputStream fout=new FileOutputStream(compressedFile);

			// read inbounding compressed file into temporary file
			for(long i=0; i<length; i++){
				fout.write(inbound.readByte());
			}
			fout.close();

			try {
				// decompress compressed fle into file
				decompress(compressedFile,file);
			} catch (Exception e) {
				System.out.print(e);
			}
			compressedFile.delete();
		}
	}
	
	public static void decompress(File source, File decompressed)throws Exception{
		byte[] buffer = new byte[1024];
		
		// File handler for source file
		FileInputStream fis = new FileInputStream(source);
		
		// File handler for decompress file
		FileOutputStream fos = new FileOutputStream(decompressed);
		
		// Zipped file handler
		GZIPInputStream gzis = new GZIPInputStream(fis);
		
		int read;
		// read() returns bytes read or -1 if none is read
		while((read = gzis.read(buffer)) != -1 ){
			// write read amount of bytes from buffer to output file
			fos.write(buffer,0, read);
		}		
		gzis.close(); 
		fos.close();
		fis.close();
	}

}
