package GUI;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.User;

public class ActionBar extends JPanel {
	private JButton changeUserInfo;
	private JButton createNewUser;
	private JButton userView;
	private JButton itView;
	private ClientCalls client;
	private ScreenNavigator controller;
	
	public ActionBar(ClientCalls client, ScreenNavigator controller) {
		this.client = client;
		this.controller = controller;
		
		changeUserInfo =  new JButton("Change User Info");
		changeUserInfo.addActionListener(e-> doChangeUserInfo());
		createNewUser =  new JButton("Create New User");
		createNewUser.addActionListener(e -> doCreateNewUser());
		userView =  new JButton("Switch to User View");
		userView.addActionListener(e-> controller.show(Screen.User));
		itView =  new JButton("Switch to IT View");
		itView.addActionListener(e -> controller.show(Screen.IT));
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(changeUserInfo);
		add(createNewUser);
		add(userView);
		add(itView);
	}
	
	private void doChangeUserInfo() {
		JDialog searchPane = new JDialog((Frame)null, "Search For a User", true);
		searchPane.setLayout(new FlowLayout());
		searchPane.setLocationRelativeTo(changeUserInfo);
		
		searchPane.add(new UserSearchBar(client, u -> {
			JDialog userInfoPane = new JDialog((Frame)null, "Change User Info", true);
			userInfoPane.setLayout(new GridBagLayout());
			userInfoPane.setLocationRelativeTo(searchPane);
			GridBagConstraints c = new GridBagConstraints();
			JLabel idLabel = new JLabel("User ID:");
			JLabel nameLabel = new JLabel("Name:");
			JLabel positionLabel = new JLabel("Position:");
			JLabel usernameLabel = new JLabel("Username:");
			JLabel passwordLabel = new JLabel("Password:");
			JTextField idField = new JTextField(u.getUserID());
			idField.setEditable(false);
			JTextField nameField = new JTextField(u.getName());
			JTextField positionField = new JTextField(u.getPosition());
			JTextField usernameField = new HintTextField("New Username...");
			JTextField passwordField = new HintTextField("New Password...");
			
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e ->{
				userInfoPane.dispose();
			});
			JButton confirm = new JButton("Confirm");
			confirm.addActionListener(e-> {
				client.updateUser(u, nameField.getText(), positionField.getText(), usernameField.getText(), passwordField.getText());
				userInfoPane.dispose();
			});
			
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			userInfoPane.add(idLabel, c);
			c.gridy = 1;
			userInfoPane.add(nameLabel, c);
			c.gridy = 2;
			userInfoPane.add(positionLabel, c);
			c.gridy = 3;
			userInfoPane.add(usernameLabel, c);
			c.gridy = 4;
			userInfoPane.add(passwordLabel, c);
			c.gridy = 5;
			userInfoPane.add(cancel, c);
			c.gridy = 0;
			c.gridx = 1;
			c.gridwidth = 2;
			userInfoPane.add(idField, c);
			c.gridy = 1;
			userInfoPane.add(nameField, c);
			c.gridy = 2;
			userInfoPane.add(positionField, c);
			c.gridy = 3;
			userInfoPane.add(usernameField, c);
			c.gridy = 4;
			userInfoPane.add(passwordField, c);
			c.gridy = 5;
			c.gridx = 2;
			c.gridwidth = 1;
			userInfoPane.add(confirm, c);
			userInfoPane.pack();
			userInfoPane.setSize(userInfoPane.getWidth() + 15, userInfoPane.getHeight() + 15);
			userInfoPane.setVisible(true);
		}));
		JButton done = new JButton("Done");
		done.addActionListener(e -> searchPane.dispose());
		searchPane.add(done);
		searchPane.pack();
		searchPane.setVisible(true);
		
	}
	private void doCreateNewUser() {
		
	}
}
