package backupbuddies.network;

public class Protocol {
	
	// It seems to be free when I checked here
	// https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
	// It comes from 32 16 8
	public static final int DEFAULT_PORT = 32168;
	
	// Handshake constant - if things don't send this after opening,
	// they're probably not BackupBuddies
	public static final String MAGIC_NUMBER = "BackupBuddies v0.3";
	
	//These function as message IDs. They need to be unique.
	//They're supposed to be a bit meaningful as well - not that
	//it matters, since you should be using the constants anyway
	public static final String REQUEST_BACKUP = "B";
	
	public static final String REQUEST_RETRIEVE = "R";
	
	public static final String REPLY_RETRIEVE = "r";
	
	public static final String REQUEST_LIST_FILES = "L";
	
	public static final String REPLY_LIST_FILES = "l";
	
	public static final String LIST_PEERS = "P";
	
	public static final String NOTIFY_NEW_PEER = "p";
	
	public static final String NOTIFY_TRANSFER_FAILED = "E";
	
}
