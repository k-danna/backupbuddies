package backupbuddies.shared;


import backupbuddies.network.Network;
import backupbuddies.network.Peer;
import backupbuddies.gui.ListModel;

import static backupbuddies.Debug.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JList;


public abstract class Interface {

    //true to enable debug fetchlist methods
        //aka you dont have to connect to a network to test
    static Boolean DEBUG = false;
	
	private static Network network;

	/*trigger: GUI-attempting to join a network-logging in with ip of net device & pw of net, also used for creating a net */
	public static void login(String ip, String pass) {
	    System.out.printf("[+] connecting to '%s' with '%s'\n", ip, pass);
	    if(network == null || !pass.equals(network.password))               //if loc net not defined OR pw differ from current loc net pw:
	    	network = new Network(pass);									//create a new loc net with given password. 
	    network.connect(ip);									//try and connect to ip given.
	}

	/*trigger:  */
	public static void uploadFile(String fileName, String fileDir, String peerName) {
	    System.out.printf("[+] uploading '%s' from '%s'\n", 
	            fileName, fileDir);
	   
	    Path filePath = new File(fileDir,fileName).toPath();				//
	    
	    Peer peer = network.getPeer(peerName);
	    peer.uploadFile(filePath);
	    peer.requestUpdatedFileList();
	    
	}

    //FIXME: pass encryption key
    public static void setEncryptKey(String key) {
        System.out.printf("[+] set encrypt key to: %s\n", key);
    }

    //FIXME: pass storage amount
    public static void setStorageSpace(int amount) {
        System.out.printf("[+] set storage space to: %d\n", amount);
    }

    //FIXME: get event list
    public static List<String> getEventLog() {
        //note that this array can be cleared at any time and functionality still works
            //ie you add events until size is 100, then clear the array
            //handled by gui
        List<String> eventLog = new ArrayList<>();
        eventLog.add("event 0");
        eventLog.add("event 1");
        eventLog.add("event 2");
        eventLog.add("event 3");
        return eventLog;
    }

	public static void downloadFile(String fileName, String fileDir) {
	    System.out.printf("[+] downloading '%s' to '%s'\n", 
	            fileName, fileDir);
	    
	    network.setFileLoc(fileName, fileDir);
	    
	    for(Peer peer:network.getPeers()){
	    	peer.downloadFile(fileName);
	    }
	}

	public static DefaultListModel<ListModel> fetchUserList(){
	    //DEBUG set at top of class
		DefaultListModel<ListModel> result = new DefaultListModel<>();
	    if (DEBUG) {            
            ListModel a = new ListModel("offlineUser1","0");
            ListModel b = new ListModel("offlineUser2","0");
            ListModel c = new ListModel("offlineUser3","0");
            ListModel d = new ListModel("onlineUser1","1");
            ListModel e = new ListModel("onlineUser2","1");
            ListModel f = new ListModel("onlineUser3","1");
            
            result.addElement(a);
            result.addElement(b);
            result.addElement(c);
            result.addElement(d);
            result.addElement(e);
            result.addElement(f);
            return result;
	    }
		//DefaultListModel<ListModel> result=new DefaultListModel<>();
		if(network==null)
			return result;
		for(String s:network.seenConnections){
			ListModel a = new ListModel(s,"0");
			result.addElement(a);
		}
        for(String s:network.getPeerIPAddresses()){
        	ListModel a = new ListModel(s,"1");
        	result.addElement(a);
		}
        return result;
	}
   
	public static DefaultListModel<ListModel> fetchFileList(){
	    //DEBUG set at top of class
		DefaultListModel<ListModel> result = new DefaultListModel<>();
	    if (DEBUG) {
	    	 
	            ListModel a = new ListModel("unavailableFile1","0");
	            ListModel b = new ListModel("availableFile1","1");
	            ListModel c = new ListModel("fileInTransit1","2");
	            ListModel d = new ListModel("unavailableFile2","0");
	            ListModel e = new ListModel("availableFile2","1");
	            ListModel f = new ListModel("fileInTransit2","2");
	            ListModel g = new ListModel("unavailableFile3","0");
	            ListModel h = new ListModel("availableFile3","1");
	            ListModel i = new ListModel("fileInTransit3","2");
	            
	            result.addElement(a);
	            result.addElement(b);
	            result.addElement(c);
	            result.addElement(d);
	            result.addElement(e);
	            result.addElement(f);
	            return result;           
	    }
		if(network==null)
			return result;

	    for(String file :network.getKnownFiles()){
	    	ListModel a = new ListModel(file, "0");
	    	result.addElement(a);
	    }
	    for(Peer peer:network.connections.values()){
	    	for(String file:peer.getKnownFiles()){
	    		ListModel a = new ListModel(file, "1");
	    		result.addElement(a);
	    	}
	    }
	    return result;
	}
	
	
	
	
	
	public static void testFile(String fileDir){
	    network.storagePath=fileDir;
	}
	
	public static boolean isOnline(String ip){
		System.out.println("isOnline()\n");
		return true;
	}

}

