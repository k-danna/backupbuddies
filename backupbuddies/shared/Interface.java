package backupbuddies.shared;

import backupbuddies.network.Network;
import backupbuddies.network.Peer;

import static backupbuddies.Debug.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public abstract class Interface {

    //true to enable debug fetchlist methods
        //aka you dont have to connect to a network to test
    static Boolean DEBUG = true;
	
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
	    
	    network.setFileLoc(fileName, fileDir);
	    
	    for(Peer peer:network.getPeers()){
	    	peer.downloadFile(fileName);
	    }
	}

	public static Map<String, Integer> fetchUserList(){
	    //DEBUG set at top of class
	    if (DEBUG) {
            Map<String, Integer> result = new HashMap<>();
            result.put("offlineUser1", 0);
            result.put("onlineUser1", 1);
            result.put("offlineUser2", 0);
            result.put("onlineUser2", 1);
            result.put("offlineUser3", 0);
            result.put("onlineUser3", 1);
            return result;
	    }
		Map<String, Integer> result=new HashMap<>();
		if(network==null)
			return result;
		for(String s:network.seenConnections){
			result.put(s, 0);
		}
        for(String s:network.getPeerIPAddresses()){
        	 result.put(s, 1);
		}
        return result;
	}

	public static Map<String, Integer> fetchFileList(){
	    //DEBUG set at top of class
	    if (DEBUG) {
            Map<String, Integer> result = new HashMap<>();
            result.put("unavailableFile1", 0);
            result.put("availableFile1", 1);
            result.put("fileInTransit1", 2);
            result.put("unavailableFile2", 0);
            result.put("availableFile2", 1);
            result.put("fileInTransit2", 2);
            result.put("unavailableFile3", 0);
            result.put("availableFile3", 1);
            result.put("fileInTransit3", 2);
            return result;
	    }
		Map<String, Integer> fileMap=new HashMap<>();
		if(network==null)
			return fileMap;
	    for(String file :network.getKnownFiles()){
	    	fileMap.put(file, 0);
	    }
	    for(Peer peer:network.connections.values()){
	    	for(String file:peer.getKnownFiles()){
	    		fileMap.put(file, 1);
	    	}
	    }
	    return fileMap;
	}
	
	public static void testFile(String fileDir){
	    network.storagePath=fileDir;
	}

}

