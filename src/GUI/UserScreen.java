package GUI;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UserScreen extends JPanel {
	private ScreenNavigator controller;
	private ClientCalls client;
	
	public UserScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		initalize();
	}
	
	private void initalize() {
		JTextField test = new JTextField("Just Do it!!");
		this.add(test);
		
		JButton next = new JButton("Next");
		next.addActionListener(e -> controller.show(Screen.IT));
		this.add(next);
	}
}
