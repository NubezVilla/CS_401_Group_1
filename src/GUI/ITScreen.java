package GUI;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ITScreen extends JPanel {
	private ScreenNavigator controller;
	private ClientCalls client;
	
	public ITScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		initalize();
	}

		private void initalize() {
			JTextField test = new JTextField("What the fuck!");
			this.add(test);
			
			JButton next = new JButton("Next");
			next.addActionListener(e -> controller.show(Screen.Account));
			this.add(next);
	}
}
