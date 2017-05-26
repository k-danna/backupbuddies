package backupbuddies.cli;

import java.util.HashSet;

import backupbuddies.Debug;
import backupbuddies.network.Network;
import backupbuddies.shared.IInterface;

public class CliMain {

	private static int seek(String pattern, String[] args){
		for(int i=0; i<args.length; i++){
			if(args[i].equals(pattern))
				return i;
		}
		return -1;
	}
	
	public static void handle(String[] args) {
		//TODO command-line tool
		//This is a sloppy argument parser
		HashSet<String> flags=new HashSet<String>();
		
		for(String s:args)
			if(s.charAt(0) == '-')
				flags.add(s);
		
		if(flags.contains("-daemon")) {
			if(flags.contains("-load")){
				IInterface.INSTANCE.loadNetwork();
			}
			//Create a network
			int passwordPos=seek("-password", args);
			if(passwordPos == -1) {
				
			}
			//Command ends with -password. That's not valid, ever
			if(passwordPos == args.length-1) {
				return;
			}
			String password=args[passwordPos+1];
			
			IInterface.INSTANCE.login("", password);
			
			int peerPos=seek("-peer", args);
			if(peerPos == -1 || peerPos == args.length-1){
				
			} else {
				Debug.mark();
				IInterface.INSTANCE.login(args[peerPos+1], password);
			}
			IInterface.INSTANCE.saveNetwork();
		}
			
	}
	
	

}
