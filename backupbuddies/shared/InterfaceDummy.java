package backupbuddies.shared;

import java.util.ArrayList;
import java.util.List;

import java.io.File;

import javax.swing.DefaultListModel;

import backupbuddies.gui.ListModel;

/**
 * A dummy version of Interface.
 * @author planetguy
 *
 */
public class InterfaceDummy implements IInterface {

    private DefaultListModel<ListModel> files = new DefaultListModel<>();
	private DefaultListModel<ListModel> users = new DefaultListModel<>();
	
	@Override
	public boolean loadNetwork() {
		System.out.printf("[+] Loading network from disk\n");
		return false;
	}

	@Override
	public boolean saveNetwork() {
		System.out.printf("[+] Saving network to disk\n");
		return false;
	}

	@Override
	public void login(String ip, String pass) {
	    System.out.printf("[+] connecting to '%s' with '%s'\n", ip, pass);
	}

	@Override
	public void uploadFile(File[] files, String peerName) {
	    for (File f : files) {
            System.out.printf("[+] uploading '%s'\n", 
                    f.toString());
	    }
	}

	@Override
	public void downloadFile(String fileName, String fileDir) {
	    System.out.printf("[+] downloading '%s' to '%s'\n", 
	            fileName, fileDir);
	}
	
	private void maybeAdd(DefaultListModel<ListModel> users, ListModel a){
		if(Math.random() < 0.75)
			users.addElement(a);
	}

	@Override
	public DefaultListModel<ListModel> fetchUserList() {
		users.clear();
		
        ListModel a = new ListModel("offlineUser1","0");
        ListModel b = new ListModel("offlineUser2","0");
        ListModel c = new ListModel("offlineUser3","0");
        ListModel d = new ListModel("onlineUser1","1");
        ListModel e = new ListModel("onlineUser2","1");
        ListModel f = new ListModel("onlineUser3","1");
        
        maybeAdd(users,a);
        maybeAdd(users,b);
        maybeAdd(users,c);
        maybeAdd(users,d);
        maybeAdd(users,e);
        maybeAdd(users,f);
        return users;
	}

	@SuppressWarnings("unused")
	@Override
	public DefaultListModel<ListModel> fetchFileList() {
		files.clear();
		
        ListModel a = new ListModel("unavailableFile1","0");
        ListModel b = new ListModel("availableFile1","1");
        ListModel c = new ListModel("fileInTransit1","2");
        ListModel d = new ListModel("unavailableFile2","0");
        ListModel e = new ListModel("availableFile2","1");
        ListModel f = new ListModel("fileInTransit2","2");
        ListModel g = new ListModel("unavailableFile3","0");
        ListModel h = new ListModel("availableFile3","1");
        ListModel i = new ListModel("fileInTransit3","2");
        
        maybeAdd(files,a);
        maybeAdd(files,b);
        maybeAdd(files,c);
        maybeAdd(files,d);
        maybeAdd(files,e);
        maybeAdd(files,f);
        maybeAdd(files,g);
        maybeAdd(files,h);
        maybeAdd(files,i);
        return files;    
	}

	@Override
	public void setStoragePath(String fileDir) {
	    System.out.printf("[+] Setting storage path to '%s'\n", fileDir);
	}

	@Override
	public void setEncryptKey(String key) {
		System.out.printf("[+] Setting encryption key to '%s'\n", key);
	}

	@Override
	public void setStorageSpace(int amount) {
		System.out.printf("[+] Setting storage limit to '%d'\n", amount);
	}

	@Override
	public List<String> getEventLog() {
		ArrayList<String> ls=new ArrayList<>();
		ls.add("Event 1");
		ls.add("Event 2");
		return ls;
	}

	public void setDisplayName(String newName) {
		System.out.printf("[+] Setting display name to '%s'\n", newName);
	}
	
}
