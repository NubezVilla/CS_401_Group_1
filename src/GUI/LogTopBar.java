package GUI;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import client.DataModel;
import model.Conversation;
import model.GroupConversation;
import model.User;

public class LogTopBar extends JPanel {
	private JLabel name;
	private JLabel id;
	private JButton viewParticipants;
	private JPopupMenu participantsMenu;
	private ClientCalls client;
	public LogTopBar(ClientCalls c) {
		client = c;
		name = new JLabel();
		id = new JLabel();
		viewParticipants = new JButton("👥");
		viewParticipants.addActionListener(e -> participantsMenu.show(viewParticipants, 0, viewParticipants.getHeight()));
		participantsMenu = new JPopupMenu();
		
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setFont(Fonts.header);
		
		add(Box.createRigidArea(new Dimension(7,0)));
		add(name);
		add(Box.createRigidArea(new Dimension(7,0)));
		add(id);
		add(Box.createHorizontalGlue());
		add(viewParticipants);
		add(Box.createRigidArea(new Dimension(7,0)));
		setBackground(new Color(255, 243, 176));
		
	}
	
	public void refresh() {
		name.setText(parseConversationName(DataModel.getInstance().getCurrentLog()));
		id.setText(DataModel.getInstance().getCurrentLog().getID());
		participantsMenu.removeAll();
		for (String i : DataModel.getInstance().getCurrentLog().getParticipants()) {
			User u = client.getUserByID(i);
			participantsMenu.add(new UserDisplayComponent(u, false));
		}
	}
	
	private String parseConversationName(Conversation c) {
		if (c.getClass() == GroupConversation.class) {
			return ((GroupConversation) c).getName();
		}
		else {
			Object[] ids = c.getParticipants().toArray();
			return client.getUserByID((String) ids[0]).getName() + " and " + client.getUserByID((String) ids[1]).getName() + " Conversation";
		}
	}
	
}
