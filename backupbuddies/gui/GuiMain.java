package backupbuddies.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Map;
import java.util.HashMap;
import java.lang.*;

//do not import util.*
//there is a Timer class in util and swing that conflict
//currently using swing timer

import backupbuddies.shared.Interface;
import static backupbuddies.Debug.*;

@SuppressWarnings("serial")
public class GuiMain extends JFrame {
  
    //load assets, etc at runtime
    static JFrame frame;
    static JTextField saveDir = new JTextField();
    static final DefaultListModel<String> userModel = new DefaultListModel<String>();
    static final DefaultListModel<String> fileModel = new DefaultListModel<String>();
    static ImageIcon statusRed = new ImageIcon("/assets/RedCircle.png");
    static ImageIcon statusYellow = new ImageIcon("/assets/YellowCircle.png");
    static ImageIcon statusGreen = new ImageIcon("/assets/GreenCircle.png");

    //process lists returned from networking
    //public static String[] fetchFiles(){}

    //updates ui on interval
    public static void startIntervals(int interval) {
        ActionListener updateUI = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                //refresh user list
                Map<String, Integer> uList = Interface.fetchUserList();
                userModel.removeAllElements();
                for (Map.Entry<String, Integer> entry : uList.entrySet()){
                    String key = entry.getKey();
                    String status = "";
                    switch(entry.getValue()) {
                        case 0:     status = "status:OFFLINE"; break;
                        case 1:     status = "status:ONLINE"; break;
                    }
                    userModel.addElement(String.format("%-20s  %s", key, status));
                }

                //refresh file list
                Map<String, Integer> fList = Interface.fetchFileList();
                fileModel.removeAllElements();
                for (Map.Entry<String, Integer> entry : fList.entrySet()){
                    String key = entry.getKey();
                    String status = "";
                    switch(entry.getValue()) {
                        case 0:     status = "status:UNAVAILABLE"; break;
                        case 1:     status = "status:AVAILABLE"; break;
                        case 2:     status = "status:TRANSIT"; break;
                    }
                    fileModel.addElement(String.format("%-20s  %s", key, status));
                }    
            }
        };
        Timer timer = new Timer(interval, updateUI);
        timer.setRepeats(true);
        timer.start();
    }
    
    //user chooses directory to save to
    public static void setSaveDir() {
    	
        JFileChooser browser = new JFileChooser();
        browser.setDialogTitle("choose save location");
        browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        browser.setAcceptAllFileFilterUsed(false);
        
        if (browser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            saveDir.setText(browser.getSelectedFile().toString());
            Interface.testFile(saveDir.getText());
        }
    }
    
    //user selects a file and it uploads to network
    public static void chooseAndUpload() {
        JFileChooser browser = new JFileChooser();
        browser.setDialogTitle("choose file to upload");
        if (browser.showOpenDialog(frame) == 
                JFileChooser.APPROVE_OPTION) {
            //browser.getSelectedFile().toString() for full path w/filename
                //since download will be separate name and directory
                //might be easier to keep separate
            Interface.uploadFile(browser.getSelectedFile().getName(),
                    browser.getCurrentDirectory().toString());
        }
    }

    //user downloads a file to save directory (and chooses if not set)
    public static void setDirAndDownload() {
        //FIXME: need to have a list of uploaded files to choose from
        String fileToGet = "test.txt";
        if (saveDir.getText().equals("")) {
            setSaveDir();
        }
        Interface.downloadFile(fileToGet, saveDir.getText());
    }

    //upload, download, save control buttons
    public static JPanel controlPanel() {
        //create panel
        JPanel controlPanel = new JPanel();
        
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
        controlPanel.add(fileLabel);
        controlPanel.add(pathButton);
        controlPanel.add(uploadButton);
        controlPanel.add(downloadButton);
        controlPanel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);

        return controlPanel;
    }

    //allows user to input ip and pass and connect to network
    public static JPanel loginPanel() {
        //create panel
        final JPanel loginPanel = new JPanel();

        //create components
        final JLabel loginLabel = new JLabel("join a network:");
        final JButton loginButton = new JButton("join");
        final JTextField ipField = new JTextField("network ip");
        final JTextField passField = new JTextField("network password");
        
        //bind methods to buttons
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Interface.login(ipField.getText(), passField.getText());
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

    //list of peers in the network
        //TODO: multiple selection
        //TODO: renders images
    public static JPanel userListPanel() {
    	JPanel userListPanel = new JPanel();
    	JLabel userLabel = new JLabel("users in network:");
    	JScrollPane userScroll = new JScrollPane();
    	
    	//userModel.addElement("please join a network");    	
    	JList<String> userList = new JList<String>(userModel);
   	
    	userScroll.setViewportView(userList);
       	userListPanel.add(userLabel);
       	userListPanel.add(userScroll);
    	
    	return userListPanel;
    }
    
    //list of files you can recover
        //TODO: multiple selection
        //TODO: renders images
    public static JPanel fileListPanel() {
    	JPanel fileListPanel = new JPanel();
    	JLabel fileLabel = new JLabel("files:");
    	JScrollPane fileScroll = new JScrollPane();
    	
    	JList<String> fileList = new JList<String>(fileModel);
   	
     	fileScroll.setViewportView(fileList);    	
    	fileListPanel.add(fileLabel);
    	fileListPanel.add(fileScroll);
      	
    	return fileListPanel;
    }

    //bind panels to frame and display the gui
    public static void startGui() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //start those intervals
                startIntervals(1000);

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
                frame.add(controlPanel(), BorderLayout.SOUTH);
                frame.add(userListPanel(), BorderLayout.WEST);
                frame.add(fileListPanel(), BorderLayout.EAST);
    
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
