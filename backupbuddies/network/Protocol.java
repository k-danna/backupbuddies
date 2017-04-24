package backupbuddies.network;

public class Protocol {
	
	// It seems to be free when I checked here
	// https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
	// It comes from 32 16 8
	public static final int DEFAULT_PORT = 32168;
	
	// Handshake constant - if things don't send this after opening,
	// they're probably not BackupBuddies
	public static final String HANDSHAKE = "BackupBuddies v0.1";
	
	public static final String REQUEST_BACKUP = "B";
	
	public static final String REQUEST_RETRIEVE = "R";
	
}
