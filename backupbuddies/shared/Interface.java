package backupbuddies.shared;

import backupbuddies.network.Network;
import backupbuddies.network.Peer;

import static backupbuddies.Debug.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public abstract class Interface {
	
	private static Network network;

	/*trigger: GUI-attempting to join a network-logging in with ip of net device & pw of net, also used for creating a net */
	public static void login(String ip, String pass) {
	    System.out.printf("[+] connecting to '%s' with '%s'\n", ip, pass);
	    if(network == null || !pass.equals(network.password))               //if loc net not defined OR pw differ from current loc net pw:
	    	network = new Network(pass);									//create a new loc net with given password. 
	    network.connect(ip);									//try and connect to ip given.
	}

	/*trigger:  */
	public static void uploadFile(String fileName, String fileDir) {
	    System.out.printf("[+] uploading '%s' from '%s'\n", 
	            fileName, fileDir);
	   
	    Path filePath = new File(fileDir,fileName).toPath();				//
	    
	    for(Peer peer:network.getPeers()){
	    	peer.uploadFile(filePath);
	    	peer.requestUpdatedFileList();
	    }
	}

	public static void downloadFile(String fileName, String fileDir) {
	    System.out.printf("[+] downloading '%s' to '%s'\n", 
	            fileName, fileDir);
	    
	    Path filePath = new File(fileDir,fileName).toPath();
	    
	    for(Peer peer:network.getPeers()){
	    	peer.downloadFile(filePath);
	    }
	}

	public static Map<String, Integer> fetchUserList(){
	    //FIXME: return a hashmap<string, int>
            //return type Map<String, Integer> map = new HashMap<String, Integer>();
	        //filename and current status
	        //status:   0 - unavailable
	        //          1 - available
		Map<String, Integer> result=new HashMap<>();
		if(network==null)
			return result;
        for(String s:network.getPeerIPAddresses()){
        	 result.put(s, 1);
		}
        return result;
	}

	public static Map<String, Integer> fetchFileList(){
		Map<String, Integer> fileMap=new HashMap<>();
		if(network==null)
			return fileMap;
	    for(String file :network.getKnownFiles()){
	    	fileMap.put(file, 0);
	    }
	    return fileMap;
	}
	
	public static void testFile(String fileDir){
	    System.out.printf("testFile()\n");
	}

}

//TEST