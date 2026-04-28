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

public class ConversationTopBar extends JPanel {
	private JLabel name;
	private JButton addMember;
	private JButton removeMember;
	private JButton viewParticipants;
	private JPopupMenu participantsMenu;
	private ClientCalls client;
	//TODO add add and remove listeners/actions
	public ConversationTopBar(ClientCalls c) {
		client = c;
		name = new JLabel();
		addMember = new JButton("+");
		removeMember = new JButton("−");
		viewParticipants = new JButton("👥");
		viewParticipants.addActionListener(e -> participantsMenu.show(viewParticipants, 0, viewParticipants.getHeight()));
		participantsMenu = new JPopupMenu();
		
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setFont(Fonts.header);
		
		add(Box.createRigidArea(new Dimension(7,0)));
		add(name);
		add(Box.createHorizontalGlue());
		add(addMember);
		add(removeMember);
		add(viewParticipants);
		add(Box.createRigidArea(new Dimension(7,0)));
		
		
		
	}
	
	public void refresh() {
		name.setText(parseConversationName(DataModel.getInstance().getCurrentConversation()));
		if(DataModel.getInstance().getCurrentConversation().getClass() == GroupConversation.class) {
			addMember.setVisible(true);
			removeMember.setVisible(true);
		}
		else {
			addMember.setVisible(false);
			removeMember.setVisible(false);
		}
		participantsMenu.removeAll();
		for (String i : DataModel.getInstance().getCurrentConversation().getParticipants()) {
			participantsMenu.add(new UserDisplayComponent(client.getUserByID(i), false));
		}
	}
	
	private String getOtherParticipantsName(Conversation c) {
		String id = "";
		for (String i : c.getParticipants()) {
			if (!i.equals(DataModel.getInstance().getCurrentUser().getUserID())) {
				id = i;
			}
		}
		return client.getUserByID(id).getName();
	}
	
	private String parseConversationName(Conversation c) {
		if (c.getClass() == GroupConversation.class) {
			return ((GroupConversation) c).getName();
		}
		else {
			return getOtherParticipantsName(c);
		}
	}
}
