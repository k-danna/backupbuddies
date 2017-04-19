package backupbuddies.gui;

/*TODO:
    high priority:
        upload button
        download button
        username input
        password input
    
    low priority
        list of peers
        file list
        file status
*/

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JTextArea;

import javax.swing.*;

public class GuiMain {
  // hi
  public static void startGui() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("BackupBuds");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1000, 500);
                frame.setVisible(true);


            }
        });
  }
  
}
