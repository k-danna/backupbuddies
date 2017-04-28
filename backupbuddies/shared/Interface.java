package backupbuddies.shared;

import backupbuddies.network.Network;
import backupbuddies.network.Peer;

import static backupbuddies.Debug.*;

import java.io.File;
import java.nio.file.Path;


public abstract class Interface {
	
	private static Network network;

	public static void login(String ip, String pass) {
	    System.out.printf("[+] connecting to '%s' with '%s'\n", ip, pass);
	    if(network == null || !pass.equals(network.password))
	    	network = new Network(pass);
	    Peer newPeer = network.connect(ip);

	}

	public static void uploadFile(String fileName, String fileDir) {
	    System.out.printf("[+] uploading '%s' from '%s'\n", 
	            fileName, fileDir);
	   
	    Path filePath = new File(fileDir,fileName).toPath();
	    
	    for(Peer peer:network.getPeers()){
	    	peer.uploadFile(filePath);
	    	peer.requestUpdatedFileList();
	    }
	}

	public static void downloadFile(String fileName, String fileDir) {
	    System.out.printf("[+] downloading '%s' to '%s'\n", 
	            fileName, fileDir);
	}

	public static String[] fetchUserList(){
	    //FIXME: return a hashmap<string, int>
            //return type Map<String, Integer> map = new HashMap<String, Integer>();
	        //filename and current status
	        //status:   0 - unavailable
	        //          1 - available
	    try {
            return network.getPeerIPAddresses()
                    .toArray(new String[0]);
		}
		catch (Exception e) {
            return new String[0];
		}
	}

	public static String[] fetchFileList(){
	    //FIXME: return a hashmap<string, int>
            //return type Map<String, Integer> map = new HashMap<String, Integer>();
	        //filename and current status
	        //status:   0 - unavailable
	        //          1 - available
	        //          2 - in transit
	    try {
            return network.getKnownFiles()
                    .toArray(new String[0]);
		}
		catch (Exception e) {
            return new String[0];
		}
	}
	
	public static void testFile(String fileDir){
	    System.out.printf("testFile()\n");
	}

}

//TEST