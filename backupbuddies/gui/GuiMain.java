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
  
    //colors
    //static final Color colorBlue = new Color(53, 129, 184);
    static final Color colorBlue = new Color(80, 80, 80);
    static final Color textColor = new Color(220, 220, 220);
    static final Color buttonTextColor = new Color(40, 40, 40);
    static final Color listColor = new Color(255, 255, 255);
    static final Color backgroundColor = new Color(92, 146, 194);
    static final Color colorGray = new Color(20, 20, 20);
    static final Font font = new Font("Papyrus", Font.BOLD, 18);

    //load assets, lists etc before creating the gui
    static JFrame frame = new JFrame("BackupBuddies");
    static JTextField saveDir = new JTextField();
    static final DefaultListModel<String> userModel = new DefaultListModel<String>();
    static final DefaultListModel<String> fileModel = new DefaultListModel<String>();
    static final DefaultListModel<ListModel> files = new DefaultListModel<ListModel>();
    
    static DefaultListModel<ListModel> filetest = new DefaultListModel<ListModel>();
    static DefaultListModel<ListModel> usertest = new DefaultListModel<ListModel>();
    static JList<ListModel> allFiles = new JList<ListModel>();
    static JList<ListModel> allUsers = new JList<ListModel>();
    
    //holds the all indices selected by the user
    static DefaultListModel<String> lastFileState = new DefaultListModel<String>();
    static DefaultListModel<String> lastUserState = new DefaultListModel<String>();
    
    static DefaultListModel<ListModel> debug = new DefaultListModel<ListModel>();
    
    static final JTextArea log = new JTextArea(5, 23);
    static List<String> prevEvents = new ArrayList<>();
    
    static ImageIcon statusRed = new ImageIcon("bin/backupbuddies/gui/assets/RedderCircle.png");
    static ImageIcon statusYellow = new ImageIcon("bin/backupbuddies/backupbuddies/gui/assets/YellowerCircle.png");
    static ImageIcon statusGreen = new ImageIcon("bin/backupbuddies/backupbuddies/gui/assets/GreenerCircle.png");
    static JList<ListModel> userMap = fetchAndProcess("users");
    static JList<ListModel> fileMap = fetchAndProcess("files");
    static boolean firstSearch = false;
    static String globalSearch = "";
    
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
            	fileSearch(globalSearch);
            	
                if(firstSearch == false){
                	fileSearch("");
                	firstSearch = true;
                }
                int[] selected = new int[lastFileState.getSize()];
                for(int i=0; i<lastFileState.getSize(); i++){
                	selected[i] = Integer.parseInt(lastFileState.getElementAt(i));
                }
                allFiles.setSelectedIndices(selected);
             //   fileSearch(globalSearch);
                
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
        //fileSearch("");
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
    	JFrame failedUpload = new JFrame();
        JPanel controlPanel = new JPanel();
        GridLayout layout = new GridLayout(2, 1, 0, 10);
        controlPanel.setLayout(layout);
        
        //create components
        JLabel fileLabel = new JLabel("backup your files");
        JButton uploadButton = new JButton("upload");
        JButton downloadButton = new JButton("download");
        JButton pathButton = new JButton("save to...");

        downloadButton.setForeground(buttonTextColor);
        uploadButton.setForeground(buttonTextColor);
    
        //set button colors
        //uploadButton.setForeground(colorGreen); //text color
        //uploadButton.setBackground(Color.GRAY);
        //uploadButton.setContentAreaFilled(false);
        //uploadButton.setOpaque(true);

        //bind methods to buttons
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if (allUsers.getSelectedIndex() == -1){
            		System.out.printf("please select a user\n");
            		JOptionPane.showMessageDialog(failedUpload,
            				"please select a user");
            	}else{
                    chooseAndUpload();
            	}
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
        controlPanel.setPreferredSize(new Dimension(250, 150));
        //controlPanel.add(fileLabel);
        controlPanel.add(uploadButton);
        controlPanel.add(downloadButton);
        //controlPanel.add(pathButton);
        //controlPanel.setComponentOrientation(
        //        ComponentOrientation.LEFT_TO_RIGHT);

        return controlPanel;
    }

    //allows user to input ip and pass and connect to network
    public static JPanel loginPanel() {
        //create panel
        final JPanel loginPanel = new JPanel();
        BoxLayout layout = new BoxLayout(loginPanel, BoxLayout.Y_AXIS);
        loginPanel.setLayout(layout);

        //create components
        final JLabel loginLabel = new JLabel("Join a Network:");
        final JButton loginButton = new JButton("Join");
        final JTextField ipField = new JTextField("network ip...", 21);
        final JTextField passField = new JTextField("network password...");
        ipField.setEnabled(false);
        passField.setEnabled(false);

        ipField.setBackground(listColor);
        passField.setBackground(listColor);
        loginButton.setForeground(buttonTextColor);
        
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
                ipField.setEnabled(true);
                ipField.requestFocus();
            }
        });
        passField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                passField.setText("");
                passField.setEnabled(true);
                passField.requestFocus();
            }
        });

       // loginButton.setBorder(new RoundedBorder(10));
        //add components to panel and specify orientation
       // loginButton.setOpaque(false);
       // loginButton.setBorderPainted(false);
       // loginButton.setFocusPainted(false);
        //loginButton.setForeground(Color.BLUE);
        //loginButton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        loginLabel.setForeground(textColor);
        loginLabel.setFont(font);
        loginPanel.add(loginLabel);
        loginPanel.add(ipField);
        loginPanel.add(passField);
        loginPanel.add(loginButton);

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
        pane.setPreferredSize(new Dimension(250, 440));
        //allUsers.setSelectionBackground(Color.green);
        allUsers.setBackground(listColor);
        return pane;
    }

    //list of files you can recover
        //TODO: multiple selection
        //TODO: renders images
    public static JScrollPane fileListPanel(String search) {
    	filetest = (IInterface.INSTANCE.fetchFileList());   	
    	//allFiles.setModel(filetest);
        allFiles.setModel(files);
        for(int i=0; i< files.size(); i++){
        	System.out.printf("%s\n", files.getElementAt(i));
        }
    	/*for(int i=0; i< filetest.size(); i++){
        	System.out.printf("%s\n", filetest.getElementAt(i));
        }*/
        allFiles.addMouseListener(new MouseAdapter(){
        	@Override
        	public void mouseClicked(MouseEvent e){
        		int selectedItem = allFiles.getSelectedIndex();
        		lastFileState.addElement(Integer.toString(selectedItem));
        	}
        });
        allFiles.setCellRenderer(new ListRenderer());
        JScrollPane pane = new JScrollPane(allFiles);
        pane.setPreferredSize(new Dimension(250, 440));
        //allFiles.setSelectionBackground(Color.green);
        allFiles.setBackground(listColor);
       
        return pane;
       
    }

    public static void fileSearch(String search){
    	//int cap = debug.getSize();
    	int cap = filetest.getSize();
        files.clear();
        for(int i=0; i<cap; i++){
        	//ListModel model = debug.elementAt(i);
        	ListModel model = filetest.elementAt(i);
        	String name = model.getName();
      
        	if(name.indexOf(search) != -1){
        	    ListModel add = new ListModel(model.getName(), model.getStatus());
        	    //filetest.addElement(add);
                files.addElement(add);;
        	}
        }
    }
    
    public static JPanel searchPanel() {
    	JPanel panel = new JPanel();
    	JLabel label = new JLabel("Search:");
        JTextField search = new JTextField("search...", 12);
        search.setEnabled(false);
       // fileSearch(search.getText());
        search.setBackground(listColor);
        search.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                search.setText("");
                search.setEnabled(true);
                search.requestFocus();
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
				globalSearch = search.getText();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				fileSearch(search.getText());	
				globalSearch = search.getText();
			}
        });
        label.setForeground(textColor);
        label.setFont(font);
        panel.add(label);
        panel.add(search);
        return panel;
    }

    public static JPanel varsPanel() {
        //create panel
        final JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        
        //create components
        final JLabel varsPanelLabel = new JLabel("Enter Encryption Key:");
        final JButton lockPassButton = new JButton("confirm key");
        final JTextField keyField = new JTextField("key...",10);
        keyField.setEnabled(false);
        keyField.setBackground(listColor);

        lockPassButton.setForeground(buttonTextColor);

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
                keyField.setEnabled(true);
                keyField.requestFocus();
            }
        });

        //add components to panel and specify orientation
        varsPanelLabel.setForeground(textColor);
        varsPanelLabel.setFont(font);
        panel.add(varsPanelLabel);
        panel.add(keyField);
        panel.add(lockPassButton);
        panel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);

        return panel;    
    }
    
    public static JPanel storagePanel(){
    	JPanel panel = new JPanel();
    	
        GridLayout layout = new GridLayout(2, 2, 0, 0);
        panel.setLayout(layout);
        panel.setPreferredSize(new Dimension(280, 50));

    	int min = 0;
        int max = 100;
        int init = 1;
        final JLabel sliderLabel = new JLabel("Storage:");
        final JLabel positionLabel = new JLabel("");
        sliderLabel.setForeground(textColor);
        sliderLabel.setFont(font);
        final JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, init);
        slider.setPreferredSize(new Dimension(200, 30));
        slider.setMajorTickSpacing(max / 10);
        slider.setPaintTicks(true);

        final JLabel currStorageLabel = new JLabel(String.valueOf(slider.getValue()) + " GB");
        currStorageLabel.setForeground(textColor);
        currStorageLabel.setFont(font);

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
        panel.add(positionLabel);
        panel.add(slider);
        panel.add(currStorageLabel);
        panel.setComponentOrientation(
        		ComponentOrientation.LEFT_TO_RIGHT);
        
        return panel;
    }

    public static JPanel selectUsersPanel() {
        //create panel
        final JPanel panel = new JPanel();

        final JLabel selectUser = new JLabel("Select Peer: ");
        final JButton selectAllButton = new JButton("all");
        final JButton selectNoneButton = new JButton("none");

        selectAllButton.setForeground(buttonTextColor);
        selectNoneButton.setForeground(buttonTextColor);

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
        selectUser.setForeground(textColor);
        selectUser.setFont(font);
        panel.add(selectUser);
        panel.add(selectAllButton);
        panel.add(selectNoneButton);
        return panel;
    }

    public static JPanel selectFilesPanel() {
        //create panel
        final JPanel panel = new JPanel();

        final JLabel selectFiles = new JLabel("Select File: ");
        final JButton selectAllButton = new JButton("all");
        final JButton selectNoneButton = new JButton("none");

        selectAllButton.setForeground(buttonTextColor);
        selectNoneButton.setForeground(buttonTextColor);

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
        selectFiles.setForeground(textColor);
        selectFiles.setFont(font);
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
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        //create components
        final JLabel logLabel = new JLabel("Event Log:");
        logLabel.setForeground(textColor);
        logLabel.setFont(font);
        log.setEditable(false);

        //log.append(text + newline)

        log.setBackground(listColor);

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

    public static JPanel leftColorPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(colorBlue);
        panel.setPreferredSize(new Dimension(310, 600));
        return panel;
    }

    public static JPanel linePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(colorGray);
        panel.setPreferredSize(new Dimension(318, 600));
        return panel;
    }

    public static JPanel topColorPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(colorGray);
        panel.setPreferredSize(new Dimension(1000, 100));
        return panel;
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

                //add color panel

                //left column locations
                panelLocs.put(loginPanel,       Arrays.asList(30, 10));
                panelLocs.put(varsPanel,        Arrays.asList(30, 120));
                panelLocs.put(controlPanel,     Arrays.asList(30, 210));
                panelLocs.put(logPanel,         Arrays.asList(30, 425));
                panelLocs.put(storagePanel,     Arrays.asList(30, 365));
                //middle column locatinos
                panelLocs.put(userListPanel,    Arrays.asList(370, 80));
                panelLocs.put(selectUsersPanel, Arrays.asList(350, 40));
                //right column locations
                panelLocs.put(searchPanel,      Arrays.asList(650, 10));
                panelLocs.put(selectFilesPanel, Arrays.asList(650, 40));
                panelLocs.put(fileListPanel,    Arrays.asList(670, 80));

                //confirm layout
                contentPane.setLayout(frameLayout());
                
                frame.setSize(1000, 600);
                frame.setLocationRelativeTo(null);

                for (Component panel : panelLocs.keySet()) {
                    contentPane.add(panel);
                }
                contentPane.add(leftColorPanel());
                contentPane.add(linePanel());
                //contentPane.add(topColorPanel());

                //set background color
                Color globalColor = colorBlue;
                //background stuff
                frame.getContentPane().setBackground(backgroundColor);
                searchPanel.setBackground(backgroundColor);
                selectFilesPanel.setBackground(backgroundColor);
                selectUsersPanel.setBackground(backgroundColor);

                //left panel stuff
                loginPanel.setBackground(globalColor);
                varsPanel.setBackground(globalColor);
                storagePanel.setBackground(globalColor);
                logPanel.setBackground(globalColor);
                controlPanel.setBackground(globalColor);

                //display the window
                frame.validate();
                frame.repaint();
                frame.setVisible(true);

            }
        });
    }
  
}
