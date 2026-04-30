package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.DataModel;

public class AccountScreen extends JPanel implements DisplayScreen{
	private ScreenNavigator controller;
	private ClientCalls client;
	
	private JTextField name;
	private JTextField position;
	private JTextField id;
	private ActionBar actionBar;
	
	
	
	public AccountScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		setLayout(new BorderLayout());
		setBackground(new Color(255, 243, 176));
		id = new JTextField();
		id.setEditable(false);
		name = new JTextField();
		position = new JTextField();
		actionBar = new ActionBar(client, c);
		initalize();
	}
	
	private void initalize() {
		JPanel userInfoPane = new JPanel();
		userInfoPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JLabel idLabel = new JLabel("User ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel positionLabel = new JLabel("Position:");
		
		JButton back = new JButton("Back");
		back.addActionListener(e ->{
			controller.show(Screen.User);
		});
		JButton confirm = new JButton("Confirm");
		confirm.addActionListener(e-> {
			if (client.updateUser(DataModel.getInstance().getCurrentUser(), name.getText(), position.getText(),"","")) { 
				JOptionPane.showMessageDialog(userInfoPane, "Information successfully Updated", "Update Success", JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JLabel badInfo = new JLabel("This should never happen");
				badInfo.setFont(Fonts.error);
				badInfo.setForeground(Color.red);
				JOptionPane.showMessageDialog(userInfoPane, badInfo, "Update Failed", JOptionPane.ERROR_MESSAGE);
			}
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
		userInfoPane.add(back, c);
		c.gridy = 0;
		c.gridx = 1;
		c.gridwidth = 2;
		userInfoPane.add(id, c);
		c.gridy = 1;
		userInfoPane.add(name, c);
		c.gridy = 2;
		userInfoPane.add(position, c);
		c.gridy = 3;
		c.gridx = 2;
		c.gridwidth = 1;
		userInfoPane.add(confirm, c);
		userInfoPane.setBackground(new Color(255, 243, 176));
		actionBar.setBackground(new Color(255, 243, 176));
		add(userInfoPane, BorderLayout.CENTER);
		add(actionBar, BorderLayout.WEST);
		
	}
	public void whenShown() {
		id.setText(DataModel.getInstance().getCurrentUser().getUserID());
		name.setText(DataModel.getInstance().getCurrentUser().getName());
		position.setText(DataModel.getInstance().getCurrentUser().getPosition());
		actionBar.setVisible(DataModel.getInstance().getCurrentUser().isIT());
	}
	
}
