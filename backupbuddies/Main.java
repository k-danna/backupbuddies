package backupbuddies;

import backupbuddies.cli.CliMain;
import backupbuddies.gui.GuiMain;
import backupbuddies.UnitTest;

public class Main {
  
  public static void main(String[] args) {
    if(args.length == 0)
    	//Start Gui version of backupbuddies
    	GuiMain.startGui();					
    else {
        if (args.length == 1) {
            if (args[0].equals("testUnits")) {
                UnitTest.testUnits();
                return;
            }
        }
    	//Start Command line version of backupbuddies
    	CliMain.handle(args);				
    	}
  }
}
