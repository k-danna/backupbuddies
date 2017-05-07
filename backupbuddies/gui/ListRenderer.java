package backupbuddies.gui;

import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import backupbuddies.Main;

public class ListRenderer extends JLabel implements ListCellRenderer<ListModel> {
	 
    @Override
    public Component getListCellRendererComponent(JList<? extends ListModel> list, ListModel listmodel, int index,
        boolean isSelected, boolean cellHasFocus) {
          
        String status = listmodel.getStatus();
       // java.net.URL image = ("Desktop/pitures/person.png");
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/backupbuddies/gui/assets/RedCircle.png"));
         
        setIcon(imageIcon);
        setText(listmodel.getName());
         
        return this;
    }
     
}
