package backupbuddies.gui;

import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import backupbuddies.Main;

public class ListRenderer extends JLabel implements ListCellRenderer<ListModel> {
	 
	public ListRenderer() {
        setOpaque(true);
    }
    @Override
    public Component getListCellRendererComponent(JList<? extends ListModel> list, ListModel listmodel, int index,
        boolean isSelected, boolean cellHasFocus) {
          
        String status = listmodel.getStatus();
        ImageIcon imageIcon = new ImageIcon();
       // java.net.URL image = ("Desktop/pitures/person.png");
        if (status == "0"){
        	imageIcon = new ImageIcon(getClass().getResource("/backupbuddies/gui/assets/RedCircle.png"));
        }else if(status == "1"){
        	imageIcon = new ImageIcon(getClass().getResource("/backupbuddies/gui/assets/GreenCircle.png"));
        }else if(status == "2"){
        	imageIcon = new ImageIcon(getClass().getResource("/backupbuddies/gui/assets/YellowCircle.png"));
        }
         
        setIcon(imageIcon);
        setText(listmodel.getName());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
 
        
        return this;
    }
     
}
