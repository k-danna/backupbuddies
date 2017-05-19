package backupbuddies.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;

import backupbuddies.Properties;
import backupbuddies.gui.ListModel;
import backupbuddies.network.Network;
import backupbuddies.network.Peer;


public class Interface implements IInterface {

	private DefaultListModel<ListModel> files = new DefaultListModel<>();
	private DefaultListModel<ListModel> users = new DefaultListModel<>();

	private Network network;

	//Loads the network from disk, from a standard place
	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#loadNetwork()
	 */
	@Override
	public boolean loadNetwork(){
		try{
			File networkFile=new File(Properties.BUB_HOME, Properties.NETWORK_FILE);
			if(!networkFile.exists())
				return false;
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
	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#saveNetwork()
	 */
	@Override
	public boolean saveNetwork(){
		try{
			if(network==null)
				return false;
			File networkFile=new File(Properties.BUB_HOME, Properties.NETWORK_FILE);
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
	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#login(java.lang.String, java.lang.String)
	 */
	@Override
	public void login(String ip, String pass) {
		if(network == null || !pass.equals(network.password))               //if loc net not defined OR pw differ from current loc net pw:
			network = new Network(pass);									//create a new loc net with given password. 
		network.connect(ip);									//try and connect to ip given.
	}

	/*trigger:  */
	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#uploadFile(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void uploadFile(String fileName, String fileDir, String peerName) {
		Path filePath = new File(fileDir,fileName).toPath();				//

		Peer peer = network.getPeer(peerName);
		peer.uploadFile(filePath);
		peer.requestUpdatedFileList();

	}

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#downloadFile(java.lang.String, java.lang.String)
	 */
	@Override
	public void downloadFile(String fileName, String fileDir) {
		network.setFileLoc(fileName, fileDir);

		for(Peer peer:network.getPeers()){
			peer.downloadFile(fileName);
		}
	}

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#fetchUserList()
	 */
	@Override
	public DefaultListModel<ListModel> fetchUserList(){
		//DEBUG set at top of class
		users.clear();

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

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#fetchFileList()
	 */
	@Override
	public DefaultListModel<ListModel> fetchFileList(){
		files.clear();
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

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#testFile(java.lang.String)
	 */
	@Override
	public void setStoragePath(String fileDir){
		network.storagePath=fileDir;
	}

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#setEncryptKey(java.lang.String)
	 */
	@Override
	public void setEncryptKey(String key) {
		System.out.printf("[+] set encrypt key to: %s\n", key);
	}

	//FIXME: pass storage amount
	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#setStorageSpace(int)
	 */
	@Override
	public void setStorageSpace(int amount) {
		System.out.printf("[+] set storage space to: %d\n", amount);
	}

	//Gets the event list
	//Returns an array of up to Properties.LOG_MESSAGE_COUNT error messages
	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#getEventLog()
	 */
	@Override
	public List<String> getEventLog() {
		if(network != null)
			return Arrays.asList(
					network.getErrorLog()
					.toArray(new String[0]));
		else
			return new ArrayList<>();
	}

	public static IInterface make() {
		boolean DEBUG = false;
		if(DEBUG) {
			return new InterfaceDummy();
		} else {
			return new Interface();
		}
	}

}

