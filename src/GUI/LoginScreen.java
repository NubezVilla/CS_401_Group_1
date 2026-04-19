package GUI;

import client.DataModel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

public class LoginScreen extends JPanel {
	private ScreenNavigator controller;
	private ClientCalls client;
	
	public LoginScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		initalize();
	}
	
	private void initalize() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		
		JLabel title = new JLabel("PLACEHOLDER");
		JLabel usernameLabel = new JLabel("Username:");
		JLabel passwordLabel = new JLabel("Password:");
		JTextField usernameField = new JTextField();
		JTextField passwordField = new JTextField();
		JButton login = new JButton("Login");
		
		login.addActionListener(e-> client.loginAttempt(usernameField.getText(), passwordField.getText()));
		
		
		
		JButton debugNext = new JButton("DEBUG: Pass Login");
		debugNext.addActionListener(e -> controller.show(Screen.User));
		this.add(debugNext);
	}
}
