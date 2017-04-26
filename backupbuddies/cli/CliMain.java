package backupbuddies.cli;

import java.util.HashSet;

import backupbuddies.Debug;
import backupbuddies.network.Network;

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
			//Create a network
			int passwordPos=seek("-password", args);
			if(passwordPos == -1 || passwordPos == args.length-1) {
				Debug.dbg("Error: password argument not valid");
				return;
			}
			String password=args[passwordPos+1];
			
			Network net=new Network(password);
			
			int peerPos=seek("-peer", args);
			if(peerPos == -1 || peerPos == args.length-1){
				
			} else {
				net.connect(args[peerPos+1]);
			}
		}
			
	}
	
	

}
