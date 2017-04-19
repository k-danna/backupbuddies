package backupbuddies.gui;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JTextArea;

import javax.swing.*;

public class GuiMain {
  
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
