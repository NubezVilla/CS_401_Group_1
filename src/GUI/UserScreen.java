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
	
	public UserScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		setBackground(new Color(255, 243, 176));
		initalize();
	}
	
	private void initalize() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		conversationListPane = buildConversationListPane();
		conversationDisplayPane = new JPanel(new BorderLayout());
		conversationDisplay = new ConversationDisplayPanel(client);
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
	
	
	private JPanel buildConversationListPane() {
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		
		JTextField userSearchBar = new HintTextField("Search for Users");
		
		JTextField convoSearchBar = new HintTextField("Search Conversations");
		conversationSearchBarListners(convoSearchBar);
		JList<Conversation> conversationList = new JList<Conversation>(DataModel.getInstance().getConversationList());
		conversationList.setCellRenderer(new ConversationListCellRenderer(client));
		conversationList.addListSelectionListener(e -> {
			client.updateCurrentConversation(conversationList.getSelectedValue().getID());
			refreshConversationDisplayPane();
		});
		
		
		JScrollPane listScroller = new JScrollPane(conversationList); 
		
		UserDisplayComponent profile = new UserDisplayComponent(null, false);
		profile.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		JPopupMenu popup = new JPopupMenu();
		JMenuItem account = new JMenuItem("Show Account Information");
		account.addActionListener(f -> controller.show(Screen.Account));
		popup.add(account);
		profile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				popup.show(profile, 0, -25);
			}
		});
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0.01;
		pane.add(userSearchBar, c);
		pane.add(convoSearchBar, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		pane.add(listScroller, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0.01;
		pane.add(profile,c);
		return pane;
	}
	
	private JPanel buildConversationDisplayPane() {
		JPanel pane = new JPanel(new GridBagLayout());
		JLabel empty = new JLabel("Select a Conversation from the left to view it");
		empty.setFont(Fonts.display);
		pane.add(empty);
		return pane;
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
	
	private void refreshConversationDisplayPane() {
	    if (conversationDisplayPane.getComponent(0) == conversationDisplay) {
	    		conversationDisplay.refresh();
	    }
	    else {
	    		conversationDisplayPane.removeAll();
	    		conversationDisplayPane.add(conversationDisplay);
	    		conversationDisplay.refresh();
	    }
	}
	
	private String parseConversationName(Conversation c) {
		if (c.getClass() == GroupConversation.class) {
			return ((GroupConversation) c).getName();
		}
		else {
			return getOtherParticipantsName(c);
		}
	}
	
	private void conversationSearchBarListners(JTextField searchField) {
		int delay = 1000;
		int minChars = 3;

		Timer debounceTimer = new Timer(delay, e -> {
		    String text = searchField.getText();
		    doConversationSearch(searchField, text);
		});
		debounceTimer.setRepeats(false);

		searchField.getDocument().addDocumentListener(new DocumentListener() {
		    private void handleChange() {
		        String text = searchField.getText();
		        if (text.length() >= minChars) {
		            debounceTimer.restart();
		        } else {
		            debounceTimer.stop(); 
		        }
		    }

		    @Override public void insertUpdate(DocumentEvent e) { handleChange(); }
		    @Override public void removeUpdate(DocumentEvent e) { handleChange(); }
		    @Override public void changedUpdate(DocumentEvent e) { handleChange(); }
		});
	}
	
	private void doConversationSearch(JTextField point, String term) {
		JPopupMenu popup = new JPopupMenu();
		popup.add(conversationSearcher(term, point, popup));
		popup.show(point, point.getWidth(), point.getHeight());
	}
	
	private JPanel conversationSearcher(String term, JTextField source, JPopupMenu menu) {
		JPanel toReturn = new JPanel();
		DefaultListModel<Conversation> conversationModel = new DefaultListModel<Conversation>(); 
		for (int i = 0; i < DataModel.getInstance().getConversationList().getSize(); i++) {
			if (parseConversationName(DataModel.getInstance().getConversationList().get(i)).contains(term)){
				conversationModel.addElement(DataModel.getInstance().getConversationList().get(i));
			}
		}
		if (conversationModel.getSize() == 0) {
			JLabel feedback = new JLabel("No Matches Found");
			feedback.setFont(Fonts.error);
			toReturn.add(feedback);
			toReturn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			return toReturn;
		}
		JList<Conversation> conversationList = new JList<Conversation>(conversationModel);
		conversationList.setCellRenderer(new ConversationListCellRenderer(client));
		conversationList.addListSelectionListener(e -> {
			client.updateCurrentConversation(conversationList.getSelectedValue().getID());
			refreshConversationDisplayPane();
			menu.setVisible(false);
			source.setText("");
			source.grabFocus();
			source.transferFocus();
		});
		
		JScrollPane scroller = new JScrollPane(conversationList);
		scroller.setPreferredSize(new Dimension(150, 250));
		toReturn.add(scroller);
		return toReturn;
	}
	


	@Override
	public void whenShown() {
		Component[] components = conversationListPane.getComponents();
		for (int i = 0; i < components.length; i++) {
			if(components[i].getClass() == UserDisplayComponent.class) {
				((UserDisplayComponent) components[i]).setUser(DataModel.getInstance().getCurrentUser());
				break;
			}
		}
		
	}
}
