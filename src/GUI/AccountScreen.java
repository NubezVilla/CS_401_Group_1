package GUI;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AccountScreen extends JPanel {
	private ScreenNavigator controller;
	private ClientCalls client;
	
	public AccountScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		initalize();
	}
	
	private void initalize() {
		JTextField test = new JTextField("Rip!");
		this.add(test);
		
		JButton next = new JButton("Next");
		next.addActionListener(e -> controller.show(Screen.Login));
		this.add(next);
	}
}
