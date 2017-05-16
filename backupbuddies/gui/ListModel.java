package backupbuddies.gui;

import javax.swing.ImageIcon;

import backupbuddies.gui.GuiMain;

public class ListModel {
	private String name;
	private String status;
	
	public ListModel(String name, String status){
		this.name = name;
		this.status = status;
	}
	
	public String getName(){
		return name;
	}
	
	public String getStatus(){
		return status;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public String toString(){
		return name;
	}
}
