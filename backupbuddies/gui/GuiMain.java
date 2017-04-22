package backupbuddies.gui;

/*TODO:
    high priority:
        upload button
        download button
        ipname input
        password input
    
    low priority
        list of peers
        file list
        file status
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import backupbuddies.network.Network;

public class GuiMain extends JFrame {
  
	private static Network network;
	
    //////////////////////////////////////////////////////////////////////
    //FIXME: these methods need to be in the interface class
        //so we can call them on user input events
    public static void login(String ip, String pass) {
        System.out.printf("[+] connecting to '%s' with '%s'\n", ip, pass);
        network = new Network(pass);
        network.connect(ip);
    }
    public static void uploadFile(String fileName, String fileDir) {
        System.out.printf("[+] uploading '%s' from '%s'\n", 
                fileName, fileDir);
    }
    public static void downloadFile(String fileName, String fileDir) {
        System.out.printf("[+] downloading '%s' to '%s'\n", 
                fileName, fileDir);
    }

    //////////////////////////////////////////////////////////////////////

    static JFrame frame;
    static JTextField saveDir = new JTextField();

    public static void setSaveDir() {
        JFileChooser browser = new JFileChooser();
        browser.setDialogTitle("choose save location");
        browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        browser.setAcceptAllFileFilterUsed(false);
        if (browser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            saveDir.setText(browser.getSelectedFile().toString());
            //DEBUG:
            System.out.printf("[*] save directory set to '%s'\n",
                    saveDir.getText());
        }
    }

    public static void chooseAndUpload() {
        JFileChooser browser = new JFileChooser();
        browser.setDialogTitle("choose file to upload");
        if (browser.showOpenDialog(frame) == 
                JFileChooser.APPROVE_OPTION) {
            //browser.getSelectedFile().toString() for full path w/filename
                //since download will be separate name and directory
                //might be easier to keep separate
            uploadFile(browser.getSelectedFile().getName(),
                    browser.getCurrentDirectory().toString());
        }
    }

    public static void setDirAndDownload() {
        //FIXME: need to have a list of uploaded files to choose from
        String fileToGet = "filename";
        if (saveDir.getText().equals("")) {
            setSaveDir();
        }
        downloadFile(fileToGet, saveDir.getText());
    }

    public static JPanel filePanel() {
        //create panel
        JPanel filePanel = new JPanel();
        
        //create components
        JLabel fileLabel = new JLabel("backup your files");
        JButton uploadButton = new JButton("upload");
        JButton downloadButton = new JButton("download");
        JButton pathButton = new JButton("save to...");

        //bind methods to buttons
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseAndUpload();
            }
        });
        pathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSaveDir();
            }
        });
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDirAndDownload();
            }
        });

        //add components to panel and specify orientation
        filePanel.add(fileLabel);
        filePanel.add(pathButton);
        filePanel.add(uploadButton);
        filePanel.add(downloadButton);
        filePanel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);

        return filePanel;
    }

    public static JPanel loginPanel() {
        //create panel
        JPanel loginPanel = new JPanel();

        //create components
        JLabel loginLabel = new JLabel("join a network:");
        JButton loginButton = new JButton("join");
        JTextField ipField = new JTextField("network ip");
        JTextField passField = new JTextField("network password");
        
        //bind methods to buttons
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login(ipField.getText(), passField.getText());
            }
        });
        ipField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ipField.setText("");
            }
        });
        passField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                passField.setText("");
            }
        });

        //add components to panel and specify orientation
        loginPanel.add(loginLabel);
        loginPanel.add(ipField);
        loginPanel.add(passField);
        loginPanel.add(loginButton);
        loginPanel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);

        return loginPanel;    
    }

    public static void startGui() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //create the window and center it on screen
                frame = new JFrame("BackupBuddies");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                
                //these values are used to center despite pack() overriding
                frame.setSize(500, 500);
                frame.setLocationRelativeTo(null);

                //FIXME: migrate to SpringLayout
                    //this uses the easy yet terrible BorderLayout to
                        //prototype each panel

                //populate the window
                frame.add(loginPanel(), BorderLayout.NORTH);
                frame.add(filePanel(), BorderLayout.SOUTH);
    
                //display the window
                    //pack - layout manager auto sizes and auto locates
                        //fixes size issue with insets/border of frame
                        //aka use minimum frame size to display the content
                frame.pack();
                frame.setVisible(true);

            }
        });
    }
  
}
