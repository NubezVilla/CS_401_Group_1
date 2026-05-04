package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
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

import client.DataModel;
import model.Conversation;
import model.GroupConversation;
import model.User;

public class LogListPanel extends JPanel {

	private ClientCalls client;
	private UserSearchBar userSearch;
	private JList<Conversation> logsList;
	//TODO maybe add a search through current logs
	private JList<Conversation> runningSearchLogList;
	private UserDisplayComponent profile;
	private Runnable refreshMainScreen;
	
	public LogListPanel(ClientCalls client, ScreenNavigator controller, Runnable r) {
		this.client = client;
		refreshMainScreen = r;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		userSearch = new UserSearchBar(client, u->{
			JDialog searchReturns = new JDialog();
			searchReturns.setLayout(new BoxLayout(searchReturns, BoxLayout.Y_AXIS));
			searchReturns.setLocationRelativeTo(null);
			ArrayList<Conversation> usersConversations = client.queryConversationLogsByUser(u);
			if (usersConversations.size() == 0) {
				JLabel error = new JLabel("No conversations found");
				error.setFont(Fonts.error);
				error.setAlignmentX(CENTER_ALIGNMENT);
				searchReturns.add(error);
			}
			else {
				JLabel instruction = new JLabel("Select a log from the following list");
				instruction.setFont(Fonts.display);
				instruction.setAlignmentX(CENTER_ALIGNMENT);
				searchReturns.add(instruction);
				
				DefaultListModel<Conversation> responseModel = new DefaultListModel<Conversation>();
				responseModel.addAll(usersConversations);
				JList<Conversation> responseList = new JList<Conversation>(responseModel);
				responseList.addListSelectionListener(e ->{
					client.updateCurrentLog(responseList.getSelectedValue().getID());
					searchReturns.dispose();
				});
				searchReturns.add(responseList);
			}
			JButton close = new JButton("Close");
			close.addActionListener(e -> searchReturns.dispose());
			close.setAlignmentX(CENTER_ALIGNMENT);
			searchReturns.add(close);
			searchReturns.pack();
			searchReturns.setSize(searchReturns.getWidth() + 10, searchReturns.getHeight() + 10);
			searchReturns.setVisible(true);
		});
		
		JTextField convoSearchBar = new HintTextField("Search Log by ID");
		conversationSearchBarListners(convoSearchBar);
		
		logsList = new JList<Conversation>(DataModel.getInstance().getLogsList());
		logsList.setCellRenderer(new LogListCellRenderer(client));
		logsList.addListSelectionListener(e -> {
			if (!logsList.isSelectionEmpty()) {
				client.updateCurrentLog(logsList.getSelectedValue().getID());
				refreshMainScreen.run();
			}
		});
		JScrollPane listScroller = new JScrollPane(logsList); 
		
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
	
	
	private void conversationSearchBarListners(JTextField searchField) {
		int delay = 1;;

		Timer debounceTimer = new Timer(delay, e -> {
		    String text = searchField.getText();
		    doConversationSearch(searchField, text);
		});
		debounceTimer.setRepeats(false);

		searchField.getDocument().addDocumentListener(new DocumentListener() {
		    private void handleChange() {
		        String text = searchField.getText();
		        if (text.length() > 0) {
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
		DefaultListModel<Conversation> searchLogsModel = new DefaultListModel<Conversation>(); 
		searchLogsModel.addAll(client.queryConversationLogsByID(term));
		if (searchLogsModel.getSize() == 0) {
			JLabel feedback = new JLabel("No Matches Found");
			feedback.setFont(Fonts.error);
			toReturn.add(feedback);
			toReturn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			return toReturn;
		}
		JList<Conversation> logsList = new JList<Conversation>(searchLogsModel);
		logsList.setCellRenderer(new LogListCellRenderer(client));
		logsList.addListSelectionListener(e -> {
			client.updateCurrentLog(logsList.getSelectedValue().getID());
			this.logsList.setSelectedValue(logsList.getSelectedValue(), true);
			refreshMainScreen.run();
			menu.setVisible(false);
			source.setText("");
			source.grabFocus();
			source.transferFocus();
		});
		
		JScrollPane scroller = new JScrollPane(logsList);
		scroller.setPreferredSize(new Dimension(150, 250));
		toReturn.add(scroller);
		return toReturn;
	}
	
	
	public void updateSelectedConversation(Conversation c) {
		logsList.setSelectedValue(c, true);
	}
}
