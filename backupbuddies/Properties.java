package backupbuddies;

import java.io.File;

public class Properties {
	
	//30 days
	public static final long PEER_REMEMBER_MILLIS = 2592000000L;
	public static final int LOG_MESSAGE_COUNT = 10;
	
	
	public static final File BUB_HOME = new File(System.getProperty("user.home"), "backupbuddies");
	
	public static final String NETWORK_FILE = "network.ser";
	
    public static final String ALGORITHM = "AES";
    public static final String TRANSFORMATION = "AES";	
	
	static{
		try{
			BUB_HOME.mkdirs();
		}catch(Exception e){
			System.err.println("Failed to mkdirs BUB_HOME!!! We're probably going to break!");
			e.printStackTrace();
		}
	}

}
