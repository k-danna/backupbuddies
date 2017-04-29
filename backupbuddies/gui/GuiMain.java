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
import java.util.Map;
import java.util.HashMap;
import java.lang.*;

//do not import util.*
//there is a Timer class in util and swing that conflict

import backupbuddies.shared.Interface;
import static backupbuddies.Debug.*;

@SuppressWarnings("serial")
public class GuiMain extends JFrame {
  
    static JFrame frame;
    static JTextField saveDir = new JTextField();
    //using DefaultListModel for easy overwriting
    static final DefaultListModel<String> userModel = new DefaultListModel<String>();
    static final DefaultListModel<String> fileModel = new DefaultListModel<String>();

    //load img assets
    static ImageIcon statusRed = new ImageIcon("/assets/RedCircle.png");
    static ImageIcon statusYellow = new ImageIcon("/assets/YellowCircle.png");
    static ImageIcon statusGreen = new ImageIcon("/assets/GreenCircle.png");

    public static void startIntervals(int interval) {
        ActionListener refreshLists = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                //refresh user list
            	//String[] uList = Interface.fetchUserList();
            	//DEBUG
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
        Timer timer = new Timer(interval, refreshLists);
        timer.setRepeats(true);
        timer.start();
    }

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

    public static void setDirAndDownload() {
        //FIXME: need to have a list of uploaded files to choose from
        String fileToGet = "filename";
        if (saveDir.getText().equals("")) {
            setSaveDir();
        }
        Interface.downloadFile(fileToGet, saveDir.getText());
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
                frame.add(filePanel(), BorderLayout.SOUTH);
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
