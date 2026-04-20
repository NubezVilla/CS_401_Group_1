package GUI;
import model.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

import client.DataModel;

public class UserScreen extends JPanel {
	private ScreenNavigator controller;
	private ClientCalls client;
	
	public UserScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		initalize();
	}
	
	private void initalize() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		JPanel conversationListPane = buildConversationListPane();
		JPanel conversationDisplayPane = buildConversationDisplayPane();
	}
	
	
	private JPanel buildConversationListPane() {
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		
		JTextField userSearchBar = new JTextField("Search For Users");
		JTextField convoSearchBar = new JTextField("Search Conversations");
		
		JList<Conversation> conversationList = new JList<Conversation>(DataModel.getInstance().getConversationList());
		JScrollPane listScroller = new JScrollPane(conversationList); 
		
		JLabel profile = new JLabel("This doesn't work right now");
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0.01;
		pane.add(userSearchBar, c);
		pane.add(convoSearchBar, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.75;
		pane.add(listScroller, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0.01;
		pane.add(profile,c);
		return pane;
	}
	
	private JPanel buildConversationDisplayPane() {
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		if (DataModel.getInstance().getCurrentConversation() == null) {
			JLabel empty = new JLabel("Select a Conversation from the left to view it");
			empty.setFont(Fonts.display);
			pane.add(empty, c);
		}
		else {
			if (DataModel.getInstance().getCurrentConversation().getClass() == GroupConversation.class) {
				GroupConversation temp = (GroupConversation) DataModel.getInstance().getCurrentConversation();
				JLabel name = new JLabel(temp.getName());
				JButton addUser = new JButton("AU");
				JButton removeUser = new JButton("RU");
				
				//add stuff
			}
			else {
				JLabel name = new JLabel(getOtherParticipantsName());
			}
			JButton viewParticipants = new JButton("VP");
			
			Conversation temp = DataModel.getInstance().getCurrentConversation();
			if (DataModel.getInstance().getCurrentConversationMessageList().size() < 200) {
				client.fetchMessages(temp.getID());
			}
			JList<Message> messageList = new JList<Message>(DataModel.getInstance().getCurrentConversationMessageList());
			JScrollPane scroller = new JScrollPane(messageList);
			
			JTextField messageBox = new JTextField("Enter your message");
			
			
		}
			
			
		
		
		return pane;
	}
	
	private String getOtherParticipantsName() {
		String id = "";
		for (String i : DataModel.getInstance().getCurrentConversation().getParticipants()) {
			if (!i.equals(DataModel.getInstance().getCurrentUser().getUserID())) {
				id = i;
			}
		}
		return client.getUserByID(id).getName();
	}
}
