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
	    network.connect(ip);
	}

	public static void uploadFile(String fileName, String fileDir) {
	    System.out.printf("[+] uploading '%s' from '%s'\n", 
	            fileName, fileDir);
	   
	    Path filePath = new File(fileDir+"/"+fileName).toPath();
	    
	    for(Peer peer:network.getPeers()){
	    	peer.uploadFile(filePath);
	    }
	}

	public static void downloadFile(String fileName, String fileDir) {
	    System.out.printf("[+] downloading '%s' to '%s'\n", 
	            fileName, fileDir);
	}

	public static String[] fetchUserList(){
		String[] list = {"user1","user2","user3","user4"};
		System.out.printf("fetchingUsers->complete\n  users are:\n");
		int i=0;
		while(i<list.length){
			System.out.printf("     %s\n", list[i]);
			i++;
		}
		return list;
	}

	public static String[] fetchFileList(){
		String[] list = {"file1","file2","file3","file4"};
		System.out.printf("fetchingFiles->complete\n  files are:\n");
		int i=0;
		while(i<list.length){
			System.out.printf("     %s\n", list[i]);
			i++;
		}
		return list;
	}

}
