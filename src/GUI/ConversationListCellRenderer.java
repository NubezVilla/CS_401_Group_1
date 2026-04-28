package GUI;

import java.awt.Component;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import client.DataModel;
import model.*;

public class ConversationListCellRenderer extends JLabel implements ListCellRenderer<Conversation> {
	private final int ICONHEIGHT = 32;
	private final int ICONWIDTH = 32;
	private final ClientCalls client;
	
	ConversationListCellRenderer(ClientCalls client){
		this.client = client;
		setOpaque(true);
	}
	@Override
	public Component getListCellRendererComponent(JList<? extends Conversation> list, Conversation value, int index,
			boolean isSelected, boolean cellHasFocus) {
		//Determine the name of the conversation
		String name = "";
		if (value.getClass() == GroupConversation.class) {
			name = ((GroupConversation) value).getName();
		}
		else {
			for(String p : value.getParticipants()) {
				if (p != DataModel.getInstance().getCurrentUser().getUserID()) {
					name = client.getUserByID(p).getName();
				}
			}
		}
		//Icon making
		char firstLetter = name.toUpperCase().charAt(0);
		File iconFile;
		if (DataModel.getInstance().getCurrentUser().getUnreadConversations().contains(value.getID())) {
			iconFile = new File("resources/badged_avatars/" + firstLetter + ".png");
		}
		else {
			iconFile = new File("resources/avatars/" + firstLetter + ".png");
		}
		ImageIcon icon = new ImageIcon(iconFile.getPath());
		icon.setImage(icon.getImage().getScaledInstance(ICONWIDTH, ICONHEIGHT, java.awt.Image.SCALE_SMOOTH));
		
		setIcon(icon);
		setText(name);
		
		//Everything below this is default
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
		return this;
	}

}
