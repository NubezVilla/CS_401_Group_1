package GUI;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import model.*;

public class LogListCellRenderer extends JLabel implements ListCellRenderer<Conversation> {
	private ClientCalls client;
	
	public LogListCellRenderer(ClientCalls c) {
		client = c;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Conversation> list, Conversation value, int index, boolean isSelected,
			boolean cellHasFocus) {
		String conversationID = "ID: " + value.getID();
		String participants = "";
		if (value.getClass() == GroupConversation.class) {
			participants += "Group Conversation with:";
			for (String id : value.getParticipants()) {
				participants += "<br>  " + client.getUserByID(id).getName();
			}
			participants += "";
		}
		else {
			Object[] ids = value.getParticipants().toArray();
			participants += "Conversation with " + client.getUserByID((String)ids[0]).getName()
					+ " and " + client.getUserByID((String)ids[1]).getName();
		}
		setText("<html>" + conversationID + "<br>" + participants + "</html");
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
