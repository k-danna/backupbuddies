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
import java.util.*;

import backupbuddies.shared.Interface;

import static backupbuddies.Debug.*;


@SuppressWarnings("serial")
public class GuiMain extends JFrame {
  
    static JFrame frame;
    static JTextField saveDir = new JTextField();

    static ImageIcon statusRed = new ImageIcon("/assets/RedCircle.png");
    static ImageIcon statusYellow = new ImageIcon("/assets/YellowCircle.png");
    static ImageIcon statusGreen = new ImageIcon("/assets/GreenCircle.png");


    public static Map<String, Integer> debugReturnUsers() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("josh cena", 0);
        map.put("michael jordan", 1);
        return map;
    }

    public static Map<String, Integer> debugReturnFiles() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("file_one.txt", 0);
        map.put("file_two.java", 1);
        map.put("file_three.py", 2);
        return map;
    }

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
    	JButton userListRefresh = new JButton("refresh");
    	JScrollPane userScroll = new JScrollPane();
    	
    	//using DefaultListModel for easy overwriting
    	final DefaultListModel<String> model = new DefaultListModel<String>();
    	//model.addElement("please join a network");    	
    	JList<String> userList = new JList<String>(model);
   	
    	userListRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	//String[] newList = Interface.fetchUserList();

            	//DEBUG
                Map<String, Integer> newList = debugReturnUsers();
                
                model.removeAllElements();
                for (Map.Entry<String, Integer> entry : newList.entrySet()){
                    String key = entry.getKey();
                    int val = entry.getValue();
                    model.addElement(key);
                }    
            }
        });
   	
    	userScroll.setViewportView(userList);
       	userListPanel.add(userLabel);
       	userListPanel.add(userListRefresh);
       	userListPanel.add(userScroll);
    	
    	return userListPanel;
    }
    
    public static JPanel fileListPanel() {
    	JPanel fileListPanel = new JPanel();
    	JLabel fileLabel = new JLabel("files:");
    	JButton fileListRefresh = new JButton("refresh");
    	JScrollPane fileScroll = new JScrollPane();
    	
    	//using DefaultListModel for easy overwriting
    	final DefaultListModel<String> model = new DefaultListModel<String>();
    	JList<String> fileList = new JList<String>(model);
   	
    	fileListRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	//String[] newList = Interface.fetchFileList();

                //DEBUG:
                Map<String, Integer> newList = debugReturnFiles();
                
                model.removeAllElements();
                for (Map.Entry<String, Integer> entry : newList.entrySet()){
                    String key = entry.getKey();
                    int val = entry.getValue();
                    model.addElement(key);
                }    
            }
        });
    	
     	fileScroll.setViewportView(fileList);    	
    	fileListPanel.add(fileLabel);
    	fileListPanel.add(fileListRefresh);
    	fileListPanel.add(fileScroll);
      	
    	return fileListPanel;
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
