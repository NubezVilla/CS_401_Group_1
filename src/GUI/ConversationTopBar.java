package GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import client.DataModel;
import model.Conversation;
import model.GroupConversation;
import model.User;

public class ConversationTopBar extends JPanel {
	private JLabel name;
	private JButton addMember;
	private JButton removeMember;
	private JButton viewParticipants;
	private JButton convertToGroup;
	private JButton modifyInfo;
	private JButton leaveGroupChat;
	private JPopupMenu participantsMenu;
	private JPopupMenu removeParticipantsMenu;
	private ClientCalls client;
	private Runnable refreshMainPage;
	public ConversationTopBar(ClientCalls c, Runnable r) {
		client = c;
		refreshMainPage = r;
		name = new JLabel();
		modifyInfo = new JButton("🛈");
		modifyInfo.addActionListener(e -> setupModifyInfoDialog());
		leaveGroupChat = new JButton("↩");
		leaveGroupChat.addActionListener(e ->{
			if(JOptionPane.showConfirmDialog(null, 
					"Are you sure you want to leave?\nYou won't be able to rejoin unless you are added back.",
					"Leave Group Conversation",
					JOptionPane.YES_NO_OPTION) == 0) {
				client.removeUserFromGroupChat(DataModel.getInstance().getCurrentUser());
				client.updateCurrentConversation(null);
				refreshMainPage.run();
			}
		});
		addMember = new JButton("+");
		addMember.addActionListener(e -> setupAddUserDialog());
		removeMember = new JButton("−");
		removeMember.addActionListener(e->removeParticipantsMenu.show(removeMember, 0, removeMember.getHeight()));
		convertToGroup = new JButton("Ⓖ");
		convertToGroup.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(null,
					"Start Group Conversation with " + getOtherParticipantsName(DataModel.getInstance().getCurrentConversation()),
					"GC Creation Confirmation",
					JOptionPane.YES_NO_OPTION) == 0) {
				client.updateCurrentConversation(client.startNewGroupConversation(DataModel.getInstance().getCurrentConversation()).getID());
				refreshMainPage.run();
			}
		});
		viewParticipants = new JButton("👥");
		viewParticipants.addActionListener(e -> participantsMenu.show(viewParticipants, 0, viewParticipants.getHeight()));
		participantsMenu = new JPopupMenu();
		removeParticipantsMenu = new JPopupMenu();
		
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setFont(Fonts.header);
		
		add(Box.createRigidArea(new Dimension(7,0)));
		add(name);
		add(Box.createHorizontalGlue());
		add(leaveGroupChat);
		add(modifyInfo);
		add(addMember);
		add(removeMember);
		add(convertToGroup);
		add(viewParticipants);
		add(Box.createRigidArea(new Dimension(7,0)));
		setBackground(new Color(255, 243, 176));
		
		
	}
	
	public void refresh() {
		name.setText(parseConversationName(DataModel.getInstance().getCurrentConversation()));
		if(DataModel.getInstance().getCurrentConversation().getClass() == GroupConversation.class) {
			convertToGroup.setVisible(false);
			leaveGroupChat.setVisible(true);
			if (((GroupConversation)DataModel.getInstance().getCurrentConversation()).getCreator() ==
					DataModel.getInstance().getCurrentUser().getUserID()) {
				modifyInfo.setVisible(true);
				addMember.setVisible(true);
				removeMember.setVisible(true);
			}
			else {
				modifyInfo.setVisible(false);
				addMember.setVisible(false);
				removeMember.setVisible(false);
			}
		}
		else {
			leaveGroupChat.setVisible(false);
			modifyInfo.setVisible(false);
			addMember.setVisible(false);
			removeMember.setVisible(false);
			convertToGroup.setVisible(true);
		}
		participantsMenu.removeAll();
		removeParticipantsMenu.removeAll();
		for (String i : DataModel.getInstance().getCurrentConversation().getParticipants()) {
			User u = client.getUserByID(i);
			participantsMenu.add(new UserDisplayComponent(u, false));
			UserDisplayComponent temp = new UserDisplayComponent(u, false);
			temp.setOnClick(() -> {
					client.removeUserFromGroupChat(u);
					removeParticipantsMenu.setVisible(false);
					refresh();
			});
			removeParticipantsMenu.add(temp);
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
	
	private void setupAddUserDialog() {
		JDialog userAddDialog = new JDialog();
		userAddDialog.setTitle("Add A User");
		userAddDialog.setLayout(new GridBagLayout());
		UserDisplayComponent user = new UserDisplayComponent(null, false);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> userAddDialog.dispose());
		JButton confirm = new JButton("Add User");
		confirm.setEnabled(false);
		confirm.addActionListener(e ->{
				client.addUserToGroupChat(user.getUser());
				refresh();
				userAddDialog.dispose();
		});
		UserSearchBar searchbar = new UserSearchBar(client, u->{
			user.setUser(u);
			user.setVisible(true);
			confirm.setEnabled(true);
			userAddDialog.pack();
			userAddDialog.setSize(userAddDialog.getWidth() + 10, userAddDialog.getHeight() + 10);
		});
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		userAddDialog.add(searchbar, c);
		c.gridy = 1;
		userAddDialog.add(user, c);
		c.gridy = 2;
		c.gridwidth = 1;
		userAddDialog.add(cancel, c);
		c.gridx = 1;
		userAddDialog.add(confirm, c);
		userAddDialog.pack();
		userAddDialog.setSize(userAddDialog.getWidth() + 10, userAddDialog.getHeight() + 10);
		userAddDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		userAddDialog.setLocationRelativeTo(null);
		userAddDialog.setVisible(true);
	
	}
	
	private void setupModifyInfoDialog() {
		JDialog infoDialog = new JDialog();
		infoDialog.setTitle("Add A User");
		infoDialog.setLayout(new GridBagLayout());
		
		JLabel nameLabel = new JLabel("Group Name:");
		JTextField nameField = new JTextField(parseConversationName(DataModel.getInstance().getCurrentConversation()));
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> infoDialog.dispose());
		JButton confirm = new JButton("Update Name");
		confirm.addActionListener(e ->{
			client.setGroupChatName(nameField.getText());
			infoDialog.dispose();
		});
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0; 
		infoDialog.add(nameLabel, c);
		c.gridx = 1;
		infoDialog.add(nameField, c);
		c.gridy = 1;
		c.gridx = 0;
		infoDialog.add(cancel, c);
		c.gridx = 1;
		infoDialog.add(confirm, c);
		
		infoDialog.pack();
		infoDialog.setSize(infoDialog.getWidth() + 10,  infoDialog.getHeight() + 10);
		infoDialog.setLocationRelativeTo(null);
		infoDialog.setVisible(true);
		
		
	}
}
