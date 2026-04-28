package GUI;

import javax.swing.*;
import java.awt.CardLayout;

public class MainWindow extends JFrame {
	private JPanel contentPane;
	private CardLayout layout;
	private ScreenNavigator controller;
	
	public MainWindow (ClientCalls c) {
		contentPane = new JPanel();
		this.setContentPane(contentPane);
		layout = new CardLayout();
		controller =  new ScreenNavigator(layout, contentPane);
		contentPane.setLayout(layout);
		this.setTitle("Hyve");
		intializeScreens(c);
		
	}
	private void intializeScreens(ClientCalls c) {
		LoginScreen login = new LoginScreen(controller, c);
		UserScreen user = new UserScreen(controller, c);
		ITScreen it = new ITScreen(controller, c);
		AccountScreen account = new AccountScreen(controller, c);
		
		contentPane.add(login, Screen.Login.name());
		contentPane.add(user, Screen.User.name());
		contentPane.add(it, Screen.IT.name());
		contentPane.add(account, Screen.Account.name());
		
		controller.show(Screen.Login);
	}
	
	public void startup() {
		setSize(1000, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		validate();
		setVisible(true);
	}

}
