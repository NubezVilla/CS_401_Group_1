package GUI;
import model.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import client.DataModel;

public class UserScreen extends JPanel implements DisplayScreen{
	private ScreenNavigator controller;
	private ClientCalls client;
	JPanel conversationListPane;
	JPanel conversationDisplayPane;
	ConversationDisplayPanel conversationDisplay;
	ConversationListPanel conversationList;
	
	public UserScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		setBackground(new Color(255, 243, 176));
		initalize();
	}
	
	private void initalize() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		conversationListPane = new JPanel(new BorderLayout());
		conversationList = new ConversationListPanel(client, controller, () -> refreshConversationDisplayPane());
		conversationListPane.add(conversationList, BorderLayout.CENTER);
		conversationDisplayPane = new JPanel(new BorderLayout());
		conversationDisplay = new ConversationDisplayPanel(client,() -> refreshConversationDisplayPane());
		conversationDisplayPane.add(buildConversationDisplayPane(), BorderLayout.CENTER);
		c.insets = new Insets(10,15,10,2);
		c.gridy = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 1;
		this.add(conversationListPane, c);
		c.insets = new Insets(10,2,10,15);
		c.gridx = 1;
		c.gridwidth = 3;
		c.weightx = 0.8;
		this.add(conversationDisplayPane, c);
	}
	
	
	
	
	private JPanel buildConversationDisplayPane() {
		JPanel pane = new JPanel(new GridBagLayout());
		JLabel empty = new JLabel("Select a Conversation from the left to view it");
		empty.setFont(Fonts.display);
		pane.add(empty);
		return pane;
	}
	

	
	private void refreshConversationDisplayPane() {
		if (DataModel.getInstance().getCurrentConversation() == null) {
			conversationDisplayPane.removeAll();
			conversationDisplayPane.add(buildConversationDisplayPane(), BorderLayout.CENTER);
			conversationList.updateSelectedConversation(null);
		}
		else if (conversationDisplayPane.getComponent(0) == conversationDisplay) {
	    		conversationDisplay.refresh();
	    		conversationList.updateSelectedConversation(DataModel.getInstance().getCurrentConversation());
	    }
	    else {
	    		conversationDisplayPane.removeAll();
	    		conversationDisplayPane.add(conversationDisplay, BorderLayout.CENTER);
	    		conversationDisplay.refresh();
	    		conversationList.updateSelectedConversation(DataModel.getInstance().getCurrentConversation());
	    }
		conversationDisplayPane.revalidate();
		conversationDisplayPane.repaint();
	}
	
	@Override
	public void whenShown() {
		conversationList.getProfile().setUser(DataModel.getInstance().getCurrentUser());
	}
}
