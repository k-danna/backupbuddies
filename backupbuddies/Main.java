package backupbuddies;

import backupbuddies.cli.CliMain;
import backupbuddies.gui.GuiMain;

public class Main {
  
  public static void main(String[] args) {
    System.out.println("Hello, world!");
    if(args.length == 0)
    	GuiMain.startGui();
    else
    	CliMain.handle(args);
  }
}
