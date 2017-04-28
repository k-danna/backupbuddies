package backupbuddies;

import backupbuddies.cli.CliMain;
import backupbuddies.gui.GuiMain;

public class Main {
  
  public static void main(String[] args) {
    if(args.length == 0)
    	GuiMain.startGui();					//Start Gui version of backupbuddies
    else
    	CliMain.handle(args);				//Start Command line version of backupbuddies
  }
}
