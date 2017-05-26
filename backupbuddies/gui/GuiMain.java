package backupbuddies.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.lang.*;

import backupbuddies.gui.ListModel;
import backupbuddies.shared.IInterface;

import static backupbuddies.Debug.*;

@SuppressWarnings("serial")
public class GuiMain extends JFrame {
  
    //load assets, lists etc before creating the gui
    static JFrame frame = new JFrame("BackupBuddies");
    static JTextField saveDir = new JTextField();
    static final DefaultListModel<String> userModel = new DefaultListModel<String>();
    static final DefaultListModel<String> fileModel = new DefaultListModel<String>();
    //static DefaultListModel<String> files = new DefaultListModel<String>();
    
    static DefaultListModel<ListModel> filetest = new DefaultListModel<ListModel>();
    static DefaultListModel<ListModel> usertest = new DefaultListModel<ListModel>();
    static JList<ListModel> allFiles = new JList<ListModel>();
    static JList<ListModel> allUsers = new JList<ListModel>();   
    
    //holds the all indices selected by the user
    static DefaultListModel<String> lastFileState = new DefaultListModel<String>();
    static DefaultListModel<String> lastUserState = new DefaultListModel<String>();
    
    static DefaultListModel<ListModel> debug = new DefaultListModel<ListModel>();
    
    static final JTextArea log = new JTextArea(5, 20);
    static List<String> prevEvents = new ArrayList<>();
    
    static ImageIcon statusRed = new ImageIcon("bin/backupbuddies/gui/assets/RedCircle.png");
    static ImageIcon statusYellow = new ImageIcon("bin/backupbuddies/backupbuddies/gui/assets/YellowCircle.png");
    static ImageIcon statusGreen = new ImageIcon("bin/backupbuddies/backupbuddies/gui/assets/GreenCircle.png");
    static JList<ListModel> userMap = fetchAndProcess("users");
    static JList<ListModel> fileMap = fetchAndProcess("files");
    
    //populate the window
    static Container contentPane = frame.getContentPane();
    static JPanel loginPanel = loginPanel();            
    static JPanel controlPanel = controlPanel();
    static JScrollPane userListPanel = userListPanel();
    static JScrollPane fileListPanel = fileListPanel("");
    static JPanel selectUsersPanel = selectUsersPanel();
    static JPanel selectFilesPanel = selectFilesPanel();
    static JPanel searchPanel = searchPanel();
    static JPanel varsPanel = varsPanel();
    static JPanel storagePanel = storagePanel();
    static JPanel logPanel = logPanel();
    
    static Map<Component, List<Integer>> panelLocs = new HashMap<Component, List<Integer>>();

    //process lists returned from networking
        //NOTE: to speed this up we can just do it in the interface methods
            //iteration already occurs there
    public static JList<ListModel> fetchAndProcess(String type) {
        //get data
        JList<ListModel> map = new JList<ListModel>(); 
        //debug = new DefaultListModel<>();
        if (type.equals("users")) debug = IInterface.INSTANCE.fetchUserList();
        else if (type.equals("files")) debug = IInterface.INSTANCE.fetchFileList();
  
        return map;
    }

    //updates ui on interval
    public static void startIntervals(int interval) {  	
        ActionListener updateUI = new ActionListener() {       
            public void actionPerformed(ActionEvent e) {
            	IInterface.INSTANCE.saveNetwork();
                userMap = fetchAndProcess("users");
                fileMap = fetchAndProcess("files");
            	updateFileSelection();
            	updateUserSelection();
            	
            	
                
                //FIXME: this gets slower as more events are added
                    //prevArray --> int (length of last returned array)
                    //change to check length of returned array
                    //append the last (len(events) - prevLength) elements to log
                        //if this is negative they cleared the event log
                            //only reset prevArraysize variable

                List<String> events = IInterface.INSTANCE.getEventLog();
                for (String event : events) {
                    if (!prevEvents.contains(event)) {
                        log.append(event + "\n");
                        log.setCaretPosition(log.getDocument().getLength());
                    }
                }
                prevEvents = events;

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
            IInterface.INSTANCE.setStoragePath(saveDir.getText());
        }
    }
    
    //user selects a file and it uploads to network
    public static void chooseAndUpload() {
        JFileChooser browser = new JFileChooser();
        browser.setMultiSelectionEnabled(true);
        browser.setDialogTitle("choose files to upload");
        if (browser.showOpenDialog(frame) == 
                JFileChooser.APPROVE_OPTION) {

            //File[] files = browser.getSelectedFiles();
            //for (File f : files) {
            //    System.out.printf("%s\n", f.toPath());
            //}

        	int[] selected = allUsers.getSelectedIndices();
        	for( int i=0; i<selected.length; i++){       		        	
        		IInterface.INSTANCE.uploadFile(browser.getSelectedFiles(),
                    allUsers.getModel().getElementAt(selected[i]).getName());
        	}
        }
    }

    //user downloads a file to save directory (and chooses if not set)
    public static void setDirAndDownload() {
        //FIXME: need to have a list of uploaded files to choose from
        //String fileToGet = "test.txt";
        if (saveDir.getText().equals("")) {
            setSaveDir();
        }

		int[] selected = allFiles.getSelectedIndices();
        for(int i=0; i<selected.length; i++){
        	//System.out.printf("Index: %d %s\n", i, hi.getModel().getElementAt(selected[i]).getName());
        	IInterface.INSTANCE.downloadFile(allFiles.getModel().getElementAt(selected[i]).getName(), saveDir.getText());
        }
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
        BoxLayout layout = new BoxLayout(loginPanel, BoxLayout.Y_AXIS);
        loginPanel.setLayout(layout);

        //create components
        final JLabel loginLabel = new JLabel("join a network:");
        final JButton loginButton = new JButton("join");
        final JTextField ipField = new JTextField("network ip");
        final JTextField passField = new JTextField("network password");
        
        //bind methods to buttons
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IInterface.INSTANCE.login(ipField.getText(), passField.getText());
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
        //loginPanel.setComponentOrientation(
        //        ComponentOrientation.LEFT_TO_RIGHT);

        return loginPanel;    
    }

    //list of peers in the network
        //TODO: multiple selection
        //TODO: renders images
    public static JScrollPane userListPanel() {
    	usertest = (IInterface.INSTANCE.fetchUserList());    	
    	allUsers.setModel(usertest);
   
        allUsers.addMouseListener(new MouseAdapter(){
        	@Override
        	public void mouseClicked(MouseEvent e){
        		int selectedItem = allUsers.getSelectedIndex();
        		lastUserState.addElement(Integer.toString(selectedItem));
        	}
        });
    	
        allUsers.setCellRenderer(new ListRenderer());
        JScrollPane pane = new JScrollPane(allUsers);
        pane.setPreferredSize(new Dimension(300, 100));
        
        return pane;
    }

    //list of files you can recover
        //TODO: multiple selection
        //TODO: renders images
    public static JScrollPane fileListPanel(String search) {
    	//filetest = (Interface.INSTANCE.fetchFileList());   	
        allFiles.setModel(filetest);      
        allFiles.addMouseListener(new MouseAdapter(){
        	@Override
        	public void mouseClicked(MouseEvent e){
        		int selectedItem = allFiles.getSelectedIndex();
        		lastFileState.addElement(Integer.toString(selectedItem));
        	}
        });
        allFiles.setCellRenderer(new ListRenderer());
        JScrollPane pane = new JScrollPane(allFiles);
        pane.setPreferredSize(new Dimension(300, 100));
       
        return pane;
       
    }

    public static void fileSearch(String search){
    	int cap = debug.getSize();
        filetest.clear();
        for(int i=0; i<cap; i++){
        	ListModel model = debug.elementAt(i);
        	String name = model.getName();
      
        	if(name.indexOf(search) != -1){
        	    ListModel add = new ListModel(model.getName(), model.getStatus());
        	    filetest.addElement(add);
                
        	}
        }
    }
    
    public static JPanel searchPanel() {
    	JPanel panel = new JPanel();
    	JLabel label = new JLabel("search for file:");
        JTextField search = new JTextField("", 10);
        fileSearch(search.getText());
        search.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                search.setText("");
            }
        });
        
        search.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				System.out.printf("changed\n");
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				fileSearch(search.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				fileSearch(search.getText());				
			}
        });
        
        panel.add(label);
        panel.add(search);
        return panel;
    }

    public static JPanel varsPanel() {
        //create panel
        final JPanel panel = new JPanel();

        //create components
        final JLabel varsPanelLabel = new JLabel("enter encryption key:");
        final JButton lockPassButton = new JButton("confirm key");
        final JTextField keyField = new JTextField("encryption key");
        
        //bind methods to buttons
        lockPassButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IInterface.INSTANCE.setEncryptKey(keyField.getText());
            }
        });
        keyField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                keyField.setText("");
            }
        });

        /*int min = 0;
        int max = 1000;
        int init = 1;
        final JLabel sliderLabel = new JLabel("storage:");
        final JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, init);
        slider.setMajorTickSpacing(max / 10);
        slider.setPaintTicks(true);

        final JLabel currStorageLabel = new JLabel(String.valueOf(slider.getValue()) + " GB");

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!slider.getValueIsAdjusting()) {
                    currStorageLabel.setText(String.valueOf(slider.getValue()) + " GB");
                    IInterface.INSTANCE.setStorageSpace(slider.getValue());
                }
            }
        });*/


        //add components to panel and specify orientation
        panel.add(varsPanelLabel);
        panel.add(keyField);
        panel.add(lockPassButton);
        //panel.add(sliderLabel);
        //panel.add(slider);
        //panel.add(currStorageLabel);
        panel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);

        return panel;    
    }
    
    public static JPanel storagePanel(){
    	JPanel panel = new JPanel();
    	
    	int min = 0;
        int max = 1000;
        int init = 1;
        final JLabel sliderLabel = new JLabel("storage:");
        final JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, init);
        slider.setMajorTickSpacing(max / 10);
        slider.setPaintTicks(true);

        final JLabel currStorageLabel = new JLabel(String.valueOf(slider.getValue()) + " GB");

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!slider.getValueIsAdjusting()) {
                    currStorageLabel.setText(String.valueOf(slider.getValue()) + " GB");
                    IInterface.INSTANCE.setStorageSpace(slider.getValue());
                }
            }
        });
        
        panel.add(sliderLabel);
        panel.add(slider);
        panel.add(currStorageLabel);
        panel.setComponentOrientation(
        		ComponentOrientation.LEFT_TO_RIGHT);
        
        return panel;
    }

    public static JPanel selectUsersPanel() {
        //create panel
        final JPanel panel = new JPanel();

        final JLabel selectUser = new JLabel("users: ");
        final JButton selectAllButton = new JButton("select all");
        final JButton selectNoneButton = new JButton("select none");
        //bind methods to buttons
        selectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.printf("[*] selecting all\n");
                for(int i=0; i < (allUsers.getModel().getSize()); i++){
                	lastUserState.addElement(Integer.toString(i));
                }
            }
        });
        selectNoneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.printf("[*] selecting none\n");
                lastUserState.clear();
            }
        });
        panel.add(selectUser);
        panel.add(selectAllButton);
        panel.add(selectNoneButton);
        return panel;
    }

    public static JPanel selectFilesPanel() {
        //create panel
        final JPanel panel = new JPanel();

        final JLabel selectFiles = new JLabel("files: ");
        final JButton selectAllButton = new JButton("select all");
        final JButton selectNoneButton = new JButton("select none");
        //bind methods to buttons
        selectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.printf("[*] selecting all\n");
                for(int i=0; i < (allFiles.getModel().getSize()); i++){
                	lastFileState.addElement(Integer.toString(i));
                }
            }
        });
        selectNoneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.printf("[*] selecting none\n");
                lastFileState.clear();
            }
        });
        panel.add(selectFiles);
        panel.add(selectAllButton);
        panel.add(selectNoneButton);
        return panel;
    }
    
    public static void updateFileSelection(){
    	for(int i=0; i<lastFileState.getSize(); i++){
    		allFiles.addSelectionInterval(Integer.parseInt(lastFileState.elementAt(i)),
                                          Integer.parseInt(lastFileState.elementAt(i)));
    	}
    }
    public static void updateUserSelection(){
    	for(int i=0; i<lastUserState.getSize(); i++){
    		allUsers.addSelectionInterval(Integer.parseInt(lastUserState.elementAt(i)),
    				                      Integer.parseInt(lastUserState.elementAt(i)));
    	}
    }
    

    public static JPanel logPanel() {
        //create panel
        final JPanel panel = new JPanel();

        //create components
        final JLabel logLabel = new JLabel("event log");
        log.setEditable(false);

        //log.append(text + newline)

        panel.add(logLabel);
        panel.add(log);
        return panel;
    }

    public static SpringLayout frameLayout() {
        SpringLayout layout = new SpringLayout();
        //set locations for each panel
        for (Component panel : panelLocs.keySet()) {
            layout.putConstraint(SpringLayout.NORTH, panel, 
                    panelLocs.get(panel).get(1), SpringLayout.NORTH, contentPane);
            layout.putConstraint(SpringLayout.WEST, panel, 
                    panelLocs.get(panel).get(0), SpringLayout.WEST, contentPane);
        }

        return layout;
    }

    //bind panels to frame and display the gui
    public static void startGui() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	//load network
            	IInterface.INSTANCE.loadNetwork();
            	
                //start those intervals
                startIntervals(500);

                //create the window and center it on screen
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);

                //set locations for each panel
                panelLocs.put(loginPanel,       Arrays.asList(50, 5));
                panelLocs.put(userListPanel,    Arrays.asList(50, 200));
                panelLocs.put(fileListPanel,    Arrays.asList(450, 200));
                panelLocs.put(searchPanel,      Arrays.asList(450, 160));
                panelLocs.put(selectFilesPanel, Arrays.asList(450, 300));
                panelLocs.put(selectUsersPanel, Arrays.asList(50, 300));
                panelLocs.put(controlPanel,     Arrays.asList(350, 525));
                panelLocs.put(varsPanel,        Arrays.asList(50, 100));
                panelLocs.put(storagePanel,     Arrays.asList(450, 100));
                panelLocs.put(logPanel,         Arrays.asList(5, 400));

                //confirm layout
                contentPane.setLayout(frameLayout());
                
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);

                for (Component panel : panelLocs.keySet()) {
                    contentPane.add(panel);
                }

                //display the window
                frame.validate();
                frame.repaint();
                frame.setVisible(true);

            }
        });
    }
  
}
