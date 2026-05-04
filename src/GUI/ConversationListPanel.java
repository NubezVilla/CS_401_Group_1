package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import client.DataModel;
import model.*;

public class ConversationListPanel extends JPanel {
	private ClientCalls client;
	private UserSearchBar userSearch;
	private JList<Conversation> conversationList;
	private UserDisplayComponent profile;
	private Runnable refreshMainScreen;
	
	public ConversationListPanel(ClientCalls client, ScreenNavigator controller, Runnable r) {
		this.client = client;
		refreshMainScreen = r;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		userSearch = new UserSearchBar(client, u->{
			Conversation temp = conversationExistsWith(u);
			if (temp == null) {
				if(JOptionPane.showConfirmDialog
						(null, "Start Conversation with " + u.getName() +"?", "Start Conversation", JOptionPane.YES_NO_OPTION)
						== 0) {
					temp = client.startNewConversation(u);
				}
				else { return; }
			}
			client.updateCurrentConversation(temp.getID());
			conversationList.setSelectedValue(temp, true);
			refreshMainScreen.run();
		});
		
		JTextField convoSearchBar = new HintTextField("Search Conversations");
		conversationSearchBarListners(convoSearchBar);
		
		conversationList = new JList<Conversation>(DataModel.getInstance().getConversationList());
		conversationList.setCellRenderer(new ConversationListCellRenderer(client));
		
		DataModel.getInstance().getConversationList().addListDataListener(new ListDataListener() {
		    private String selectedId;

		    private void rememberSelection() {
		        Conversation selected = conversationList.getSelectedValue();
		        selectedId = selected == null ? null : selected.getID();
		    }

		    private void restoreSelection() {
		        if (selectedId == null) return;

		        for (int i = 0; i < DataModel.getInstance().getConversationList().getSize(); i++) {
		            Conversation c = DataModel.getInstance().getConversationList().getElementAt(i);
		            if (c.getID().equals(selectedId)) {
		                conversationList.setSelectedIndex(i);
		                conversationList.ensureIndexIsVisible(i);
		                break;
		            }
		        }
		    }

		    @Override
		    public void contentsChanged(ListDataEvent e) {
		        restoreSelection();
		    }

		    @Override public void intervalAdded(ListDataEvent e) {}
		    @Override public void intervalRemoved(ListDataEvent e) {}
		});
		conversationList.addListSelectionListener(e -> {
			if (!conversationList.isSelectionEmpty()) {
				client.updateCurrentConversation(conversationList.getSelectedValue().getID());
				refreshMainScreen.run();
			}
		});
		JScrollPane listScroller = new JScrollPane(conversationList); 
		
		profile = new UserDisplayComponent(null, false);
		profile.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		JPopupMenu popup = new JPopupMenu();
		JMenuItem account = new JMenuItem("Show Account Information");
		account.addActionListener(f -> controller.show(Screen.Account));
		popup.add(account);
		profile.setOnClick(() -> {
				popup.show(profile, 0, -25);
		});
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0.01;
		add(userSearch, c);
		add(convoSearchBar, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		add(listScroller, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0.01;
		add(profile,c);
	}
	public UserDisplayComponent getProfile() {
		return profile;
	}

	
	private Conversation conversationExistsWith(User u) {
		HashSet<String> participants = new HashSet<String>();
		participants.add(u.getUserID());
		participants.add(DataModel.getInstance().getCurrentUser().getUserID());
		for(Object o : DataModel.getInstance().getConversationList().toArray()) {
			Conversation c = (Conversation) o;
			if (c.getParticipants().equals(participants)) {return c;}
		}
		return null;
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
			if (parseConversationName(DataModel.getInstance().getConversationList().getElementAt(i)).contains(term)){
				conversationModel.addElement(DataModel.getInstance().getConversationList().getElementAt(i));
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
			this.conversationList.setSelectedValue(conversationList.getSelectedValue(), true);
			refreshMainScreen.run();
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
	
	private String parseConversationName(Conversation c) {
		if (c.getClass() == GroupConversation.class) {
			return ((GroupConversation) c).getName();
		}
		else {
			return getOtherParticipantsName(c);
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
	
	public void updateSelectedConversation(Conversation c) {
		conversationList.setSelectedValue(c, true);
	}
}
