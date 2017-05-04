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
  
    //load assets, lists etc before creating the gui
    static JFrame frame;
    static JTextField saveDir = new JTextField();
    static final DefaultListModel<String> userModel = new DefaultListModel<String>();
    static final DefaultListModel<String> fileModel = new DefaultListModel<String>();
    static ImageIcon statusRed = new ImageIcon("gui/assets/RedCircle.png");
    static ImageIcon statusYellow = new ImageIcon("gui/assets/YellowCircle.png");
    static ImageIcon statusGreen = new ImageIcon("gui/assets/GreenCircle.png");
    static Map<String, ImageIcon> userMap = fetchAndProcess("users");
    static Map<String, ImageIcon> fileMap = fetchAndProcess("files");

    //process lists returned from networking
        //NOTE: to speed this up we can just do it in the interface methods
            //iteration already occurs there
    public static Map<String, ImageIcon> fetchAndProcess(String type) {
        //get data
        Map<String, Integer> map = new HashMap<String, Integer>(); 
        if (type.equals("users")) map = Interface.fetchUserList();
        else if (type.equals("files")) map = Interface.fetchFileList();
        
        //replace int with img
        Map<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();
        for  (Map.Entry<String, Integer> entry : map.entrySet()) {
            switch (entry.getValue()) {
                case 0: iconMap.put(entry.getKey(), statusRed); break;
                case 1: iconMap.put(entry.getKey(), statusGreen); break;
                case 2: iconMap.put(entry.getKey(), statusYellow); break;
                default: iconMap.put(entry.getKey(), statusRed); break;
            }
        }
        return iconMap;
    }

    //updates ui on interval
    public static void startIntervals(int interval) {
        ActionListener updateUI = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userMap = fetchAndProcess("users");
                userMap = fetchAndProcess("files");
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

    public static class UserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, 
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setIcon(userMap.get((String)value));
            label.setHorizontalTextPosition(JLabel.RIGHT);
            return label;
        }
    }

    public static class FileListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, 
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setIcon(fileMap.get((String)value));
            label.setHorizontalTextPosition(JLabel.RIGHT);
            return label;
        }
    }

    //list of peers in the network
        //TODO: multiple selection
        //TODO: renders images
    public static JScrollPane userListPanel() {
        userMap = fetchAndProcess("users");
        JList list = new JList(userMap.keySet().toArray());
        list.setCellRenderer(new UserListRenderer());
        JScrollPane pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(300, 100));
        return pane;
    }
    
    //list of files you can recover
        //TODO: multiple selection
        //TODO: renders images
    public static JScrollPane fileListPanel() {
        fileMap = fetchAndProcess("files");
        JList list = new JList(fileMap.keySet().toArray());
        list.setCellRenderer(new FileListRenderer());
        JScrollPane pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(300, 100));
        return pane;
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
                Container contentPane = frame.getContentPane();
                SpringLayout layout = new SpringLayout();
                contentPane.setLayout(layout);
                
                //these values are used to center despite pack() overriding
                frame.setSize(700, 300);
                //frame.setLocationRelativeTo(null);

                //FIXME: migrate to SpringLayout
                    //this uses the easy yet terrible BorderLayout to
                        //prototype each panel

                //populate the window
                JPanel loginPanel = new JPanel();
                JPanel controlPanel = new JPanel();
                JScrollPane userListPanel = new JScrollPane();
                JScrollPane fileListPanel = new JScrollPane();
                
                loginPanel = loginPanel();            
                controlPanel = controlPanel();
                userListPanel = userListPanel();
                fileListPanel = fileListPanel();
                                
                contentPane.add(loginPanel);
                contentPane.add(controlPanel);
                contentPane.add(userListPanel);
                contentPane.add(fileListPanel);
                
                //set locations for each panel
                layout.putConstraint(SpringLayout.NORTH, loginPanel, 5,
                		             SpringLayout.NORTH, contentPane);
                layout.putConstraint(SpringLayout.WEST, loginPanel, 5,
   		                             SpringLayout.WEST, contentPane);               
                
                layout.putConstraint(SpringLayout.WEST, userListPanel, 5,
   		                             SpringLayout.WEST, contentPane);
                layout.putConstraint(SpringLayout.NORTH, userListPanel, 5,
                                     SpringLayout.SOUTH, loginPanel);
                
                layout.putConstraint(SpringLayout.NORTH, controlPanel, 5,
                                     SpringLayout.SOUTH, userListPanel);
                layout.putConstraint(SpringLayout.WEST, controlPanel, 5,
                                     SpringLayout.WEST, contentPane);
                
                layout.putConstraint(SpringLayout.WEST, fileListPanel, 20,
   		                             SpringLayout.EAST, userListPanel);
                layout.putConstraint(SpringLayout.NORTH, fileListPanel, 5,
                                     SpringLayout.SOUTH, loginPanel);
                
                //display the window
                    //pack - layout manager auto sizes and auto locates
                        //fixes size issue with insets/border of frame
                        //aka use minimum frame size to display the content
                //frame.pack();
                frame.setVisible(true);

            }
        });
    }
  
}
