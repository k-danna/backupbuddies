package backupbuddies.gui;

import javax.swing.ImageIcon;

import backupbuddies.gui.GuiMain;

public class ListModel implements Comparable<ListModel> {
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
	
	@Override
	public String toString(){
		return name +" ("+status+")";
	}

	@Override
	public int compareTo(ListModel other) {
		return name.compareTo(other.name);
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof ListModel))
			return false;
		ListModel other=(ListModel) o;
		return other.name.equals(name) && other.status.equals(status);
	}
	
	@Override
	public int hashCode(){
		return name.hashCode() * status.hashCode();
	}
}
