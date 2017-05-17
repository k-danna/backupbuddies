package backupbuddies.shared;

import backupbuddies.Properties;
import backupbuddies.gui.ListModel;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;


public abstract class Interface {

    //true to enable debug fetchlist methods
        //aka you dont have to connect to a network to test
    static Boolean DEBUG = false;
    static DefaultListModel<ListModel> files = new DefaultListModel<>();
	static DefaultListModel<ListModel> users = new DefaultListModel<>();
	
	private static Network network;
	
	//Loads the network from disk, from a standard place
	public static boolean loadNetwork(){
		try{
			File networkFile=new File(System.getProperty("user.home"), "backupbuddies/network.ser");
			ObjectInputStream stream=new ObjectInputStream(new FileInputStream(networkFile));
			network=(Network) stream.readObject();
			if(network != null)
				network.init();
			stream.close();
			return network != null;
		}catch(IOException | ClassCastException | ClassNotFoundException e){
			//Mostly for debugging
			e.printStackTrace();
			return false;
		}
	}
	
	//Saves the network to disk, in a standard place
	public static boolean saveNetwork(){
		try{
			if(network==null)
				return false;
			File networkFile=new File(System.getProperty("user.home"), "backupbuddies/network.ser");
			ObjectOutputStream stream=new ObjectOutputStream(new FileOutputStream(networkFile));
			stream.writeObject(network);
			stream.close();
			return true;
		}catch(IOException | ClassCastException e){
			//Mostly for debugging
			e.printStackTrace();
			return false;
		}
	}

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
	    users.clear();
	    if (DEBUG) {            
            ListModel a = new ListModel("offlineUser1","0");
            ListModel b = new ListModel("offlineUser2","0");
            ListModel c = new ListModel("offlineUser3","0");
            ListModel d = new ListModel("onlineUser1","1");
            ListModel e = new ListModel("onlineUser2","1");
            ListModel f = new ListModel("onlineUser3","1");
            
            users.addElement(a);
            users.addElement(b);
            users.addElement(c);
            users.addElement(d);
            users.addElement(e);
            users.addElement(f);
            return users;
	    }
		//DefaultListModel<ListModel> result=new DefaultListModel<>();
		if(network==null)
			return users;
		for(String s:network.seenConnections.keySet()){
			ListModel a = new ListModel(s,"0");
			users.addElement(a);
			//System.out.println("hi");
		}
        for(String s:network.getPeerIPAddresses()){
        	ListModel a = new ListModel(s,"1");
        	users.addElement(a);
        	//System.out.println("ho");
		}
        return users;
	}

	public static DefaultListModel<ListModel> fetchFileList(){
	    //DEBUG set at top of class
		files.clear();
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
	            
	            files.addElement(a);
	            files.addElement(b);
	            files.addElement(c);
	            files.addElement(d);
	            files.addElement(e);
	            files.addElement(f);
	            return files;           
	    }
		if(network==null)
			return files;

	    for(String file :network.getKnownFiles()){
	    	ListModel a = new ListModel(file, "0");
	    	files.addElement(a);
	    	//System.out.println("hi");
	    }
	    for(Peer peer:network.connections.values()){
	    	for(String file:peer.getKnownFiles()){
	    		ListModel a = new ListModel(file, "1");
	    		files.addElement(a);
	    		//System.out.println("ho");
	    	}
	    }
	    return files;
	}
	
	public static void testFile(String fileDir){
		network.storagePath=fileDir;
	}

	public static void setEncryptKey(String key) {
		System.out.printf("[+] set encrypt key to: %s\n", key);
	}

	//FIXME: pass storage amount
	public static void setStorageSpace(int amount) {
		System.out.printf("[+] set storage space to: %d\n", amount);
	}

	//Gets the event list
	//Returns an array of up to Properties.LOG_MESSAGE_COUNT error messages
	public static List<String> getEventLog() {
		if(network != null)
			return Arrays.asList(
					network.getErrorLog()
					.toArray(new String[0]));
		else
			return new ArrayList<>();
	}

}

