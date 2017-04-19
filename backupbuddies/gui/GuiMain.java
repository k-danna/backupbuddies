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

import java.awt.*;
import javax.swing.*;

public class GuiMain {
  // hi
  public static void startGui() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("BackupBuds");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1000, 700);
                frame.setVisible(true);

                
                JPanel panel = new JPanel();
                panel.setLayout(null);
                frame.add(panel);
                
                
                // upload button
                JButton up = new JButton("Upload");
                up.setBounds(270, 550, 175, 50);
                panel.add(up);
                
                // download button
                JButton down = new JButton("Download");
                down.setBounds(500, 550, 175, 50);
                panel.add(down);
            }
        });
  }
  
}
