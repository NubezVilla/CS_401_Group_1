package GUI;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AccountScreen extends JPanel {
	private ScreenNavigator controller;
	private ClientCalls client;
	
	private JTextField name;
	private JTextField position;
	private JTextField id;
	private JTextField username;
	private JTextField password;
	private ActionBar actionBar;
	
	
	
	public AccountScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		setBackground(new Color(255, 243, 176));
		initalize();
	}
	
	private void initalize() {

		actionBar = new ActionBar(client, controller);
		add(actionBar);
	}
}
