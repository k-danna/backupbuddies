package backupbuddies;

import java.io.File;

public class Properties {
	
	//30 days
	public static final long PEER_REMEMBER_MILLIS = 2592000000L;
	public static final int LOG_MESSAGE_COUNT = 10;
	
	
	public static final File BUB_HOME = new File(System.getProperty("user.home"), "backupbuddies");

}
