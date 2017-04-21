package backupbuddies.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SetupGui extends JFrame{
	
	public SetupGui() throws HeadlessException{
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
	                up.addActionListener(new ActionListener(){
	                	public void actionPerformed(ActionEvent e){
	                		System.out.printf("Upload\n");
	                	}
	                } );
	                
	                
	                // download button
	                JButton down = new JButton("Download");
	                down.setBounds(500, 550, 175, 50);
	                panel.add(down);
	                down.addActionListener(new ActionListener(){
	                	public void actionPerformed(ActionEvent e){
	                		System.out.printf("Download\n");
	                	}
	                } );
	                
	                // username label/textfield
	                JLabel userLabel = new JLabel("Username:");
	                userLabel.setBounds(10, 10, 150, 25);
	                panel.add(userLabel);
	                
	                JTextField userText = new JTextField(20);
	                userText.setBounds(10, 30, 150, 25);
	                panel.add(userText);
	                
	                // password label/textfield
	                JLabel passLabel = new JLabel("Password:");
	                passLabel.setBounds(10, 50, 150, 25);
	                panel.add(passLabel);
	                
	                JTextField passText = new JTextField(20);
	                passText.setBounds(10, 70, 150, 25);
	                panel.add(passText);
	                
	                
	                // submit button for username/pass
	                JButton submit = new JButton("Submit");
	                submit.setBounds(10, 100, 150, 25);
	                panel.add(submit);
	                submit.addActionListener(new ActionListener(){
	                	public void actionPerformed(ActionEvent e){
	                		String user = userText.getText();
	                		String pass = passText.getText();
	                		System.out.printf("user: %s\n", user);
	                		System.out.printf("pass: %s\n", pass);
	                	}
	                } );
	            }
	        });
	}
}