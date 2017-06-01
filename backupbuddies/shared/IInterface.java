package backupbuddies.shared;

import java.util.List;

import java.io.File;

import javax.swing.DefaultListModel;

import backupbuddies.gui.ListModel;

public interface IInterface {

	public static final IInterface INSTANCE = Interface.make();
	

	//Loads the network from disk, from a standard place
	boolean loadNetwork();

	//Saves the network to disk, in a standard place
	boolean saveNetwork();

	/*trigger: GUI-attempting to join a network-logging in with ip of net device & pw of net, also used for creating a net */
	void login(String ip, String pass);

	/*trigger:  */
	void uploadFile(File[] files, String peerName);

	void downloadFile(String fileName, String fileDir);

	DefaultListModel<ListModel> fetchUserList();

	DefaultListModel<ListModel> fetchFileList();

	void setStoragePath(String fileDir);

	void setEncryptKey(String key);
	
	void setDisplayName(String newName);
	
	String getDisplayName();


	//FIXME: pass storage amount
	void setStorageSpace(int amount);

	int getStorageSpace();
	
	/**
	 * @return the absolute maximum we can physically store, by filling
	 * every free byte of the disk
	 */
	int getStorageSpaceLimit();

	
	//Gets the event list
	//Returns an array of up to Properties.LOG_MESSAGE_COUNT error messages
	List<String> getEventLog();

	
}
