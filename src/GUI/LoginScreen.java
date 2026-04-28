package GUI;

import client.DataModel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

public class LoginScreen extends JPanel{
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
		setBackground(new Color(255, 243, 176));
		JLabel title = new JLabel("HYVE");
		title.setFont(Fonts.title);
		title.setForeground(Color.ORANGE);
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setVerticalAlignment(JLabel.CENTER);
		
		JLabel errortext = new JLabel("<html>The username and password entered do not match.<br>Please try again or contact IT.</html>");
		errortext.setFont(Fonts.error);
		errortext.setForeground(Color.red);
		errortext.setHorizontalAlignment(JLabel.LEFT);
		errortext.setVisible(false);
		
		JLabel usernameLabel = new JLabel("Username:");
		usernameLabel.setFont(Fonts.display);
		usernameLabel.setHorizontalAlignment(JLabel.LEFT);
		
		JLabel passwordLabel = new JLabel("Password:");
		passwordLabel.setFont(Fonts.display);
		passwordLabel.setHorizontalAlignment(JLabel.LEFT);
		
		JTextField usernameField = new JTextField();
		JTextField passwordField = new JTextField();
		JButton login = new JButton("Login");
		
		login.addActionListener(e-> {
			if (client.loginAttempt(usernameField.getText(), passwordField.getText()))
			{
				if (DataModel.getInstance().getCurrentUser().isIT()) {
					controller.show(Screen.IT);
				}
				else {controller.show(Screen.User);}
			}
			else {
				usernameField.setText("");
				passwordField.setText("");
				errortext.setVisible(true);
			}
			});
		
		
		
		JButton debugNext = new JButton("DEBUG: Pass Login");
		debugNext.addActionListener(e -> controller.show(Screen.User));
		c.fill =  GridBagConstraints.BOTH;
		c.weighty = 0.5;
		this.add(title, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0.01;
		this.add(errortext, c);
		this.add(usernameLabel, c);
		this.add(usernameField, c);
		this.add(passwordLabel, c);
		this.add(passwordField, c);
		this.add(login, c);
		
		c.weighty = 0.5;
		
		this.add(Box.createGlue(), c);
		
		//DEBUG
		c.weighty = 0.1;
		this.add(debugNext, c);
		
	}
}
