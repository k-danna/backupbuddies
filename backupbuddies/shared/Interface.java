package backupbuddies.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;

import backupbuddies.Debug;
import backupbuddies.Properties;
import backupbuddies.gui.ListModel;
import backupbuddies.network.Network;
import backupbuddies.network.OfflinePeer;
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
		File networkFile=new File(Properties.BUB_HOME, Properties.NETWORK_FILE);
		try{
			if(!networkFile.exists())
				return false;
			ObjectInputStream stream=new ObjectInputStream(new FileInputStream(networkFile));
			network=(Network) stream.readObject();
			if(network != null)
				network.init();
			stream.close();
			return network != null;
		}catch(InvalidClassException e){
			System.out.println("Save format changed!");
			networkFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
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
		if(network == null || !pass.equals(network.password)) {            //if loc net not defined OR pw differ from current loc net pw:
			network = new Network();									//create a new loc net with given password. 
			network.setPassword(pass);;
		}
		network.connect(ip);									//try and connect to ip given.
	}

	/*trigger:  */
	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#uploadFile(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean uploadFile(File[] files, String peerDisplayName) {
	    for (File f : files) {
            Path filePath = f.toPath();

            System.out.println("debug");
            System.out.println(filePath);

            Peer peer = network.getPeerByDisplayName(peerDisplayName);
            peer.uploadFile(filePath);
            peer.requestUpdatedFileList();
        }
        return true;
	}

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#downloadFile(java.lang.String, java.lang.String)
	 */
	@Override
	public void downloadFile(String fileName, String fileDir) {
		String fileNameNetFormat=fileName.replaceAll(" : ","/");
		network.setFileLoc(fileNameNetFormat, fileDir);

		for(Peer peer:network.getPeers()){
			//Only download once
			if(peer.getKnownFiles().contains(fileNameNetFormat)){
				peer.downloadFile(fileNameNetFormat);
				return;
			}
		}
		Network.log("Cannot find file");
	}

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#fetchUserList()
	 */
	@Override
	public DefaultListModel<ListModel> fetchUserList(){
		if(network==null) {
			users.clear();
			return users;
		}
		
		ArrayList<ListModel> things=new ArrayList<>();
		HashSet<String> usersAlreadyAccountedFor = new HashSet<String>();
		
		for(Peer peer:network.getPeers()){
			if(usersAlreadyAccountedFor.contains(peer.displayName)){
				System.err.println("Duplicate peer!");
			} else {
				ListModel a = new ListModel(peer.displayName,"1");
				things.add(a);
				usersAlreadyAccountedFor.add(peer.displayName);
				//System.out.println("ho");
			}
		}
		for(String someIP:network.seenConnections.keySet()){
			OfflinePeer offlineData = network.offlinePeers.get(someIP);
			if(offlineData == null){
				Debug.dbg("This should not happen!");
				ListModel a = new ListModel(someIP,"0");
				things.add(a);
			}
			if(usersAlreadyAccountedFor.contains(offlineData.displayName))
				continue;
			ListModel a = new ListModel(offlineData.displayName,"0");
			things.add(a);
			usersAlreadyAccountedFor.add(offlineData.displayName);
			//System.out.println("hi");
		}
		
		users.clear();
		Collections.sort(things);
		for(ListModel lm:things)
			users.addElement(lm);
		return users;
	}

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#fetchFileList()
	 */
	@Override
	public DefaultListModel<ListModel> fetchFileList(){
		if(network==null) {
			files.clear();
			return files;
		}
		
		ArrayList<ListModel> things=new ArrayList<>();
		HashSet<String> filesAlreadyAccountedFor = new HashSet<String>();
		
		for(Peer peer:network.getPeers()){
			for(String file:peer.getKnownFiles()){
				if(!filesAlreadyAccountedFor.contains(file)){
					ListModel a = new ListModel(file.replaceAll("/", " : "), "1");
					things.add(a);
					filesAlreadyAccountedFor.add(file);
				}
			}
		}
		
		for(String file :network.getKnownFiles()){
			if(filesAlreadyAccountedFor.contains(file))
				continue;
			ListModel a = new ListModel(file.replaceAll("/", " : "), "0");
			things.add(a);
			filesAlreadyAccountedFor.add(file);
		}
		
		files.clear();
		Collections.sort(things);
		for(ListModel a:things)
			files.addElement(a);
		return files;
	}

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#testFile(java.lang.String)
	 */
	@Override
	public void setStoragePath(String fileDir){
		//network.storagePath=fileDir;
	}

	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#setEncryptKey(java.lang.String)
	 */
	@Override
	public void setEncryptKey(String key) {
		if(network==null)return;
		network.encryptionKey=key;
		System.out.printf("[+] set encrypt key to: %s\n", key);
		Network.log("Set new encrypt key");
	}

	//FIXME: pass storage amount
	/* (non-Javadoc)
	 * @see backupbuddies.shared.IInterface#setStorageSpace(int)
	 */
	@Override
	public void setStorageSpace(int amount) {
		if(network==null)return;
		network.setBytesLimit(gibibytesToBytes(amount));
		Network.log("Set new storage amount");
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
		boolean DEBUG = true;
		if(DEBUG) {
			return new InterfaceDummy();
		} else {
			return new Interface();
		}
	}

	@Override
	public void setDisplayName(String newName) {
		network.setDisplayName(newName);
	}

	@Override
	public String getDisplayName() {
		if(network==null)
			return Network.guessComputerName();
		return network.getDisplayName();
	}

	@Override
	public int getStorageSpaceLimit(){
		return bytesToGibibytes(Network.getFileSystemFreeBytes());
	}
	
	@Override
	public int getStorageSpace() {
		if(network==null)
			return (int)bytesToGibibytes(Network.getFileSystemFreeBytes() / 20);
		return bytesToGibibytes(network.getBytesLimit());
	}
	
	private int bytesToGibibytes(long a){
		//In gibibytes (
		// 2^30 ~ 10^6
		long gibibytes = a >> (30);
		//If storageGigs isn't an integer number, round it up 
		if(gibibytes << 30 < a) {
			gibibytes ++;
		}
		return (int) gibibytes;
	}
	
	private long gibibytesToBytes(int a){
		return ((long)a) << 30;
	}
	
	@Override
	public boolean networkExists(){
		return network != null;
	}
	
}

