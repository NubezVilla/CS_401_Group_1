package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.DataModel;

public class ITScreen extends JPanel implements DisplayScreen{
	private ScreenNavigator controller;
	private ClientCalls client;
	JPanel logListPane;
	JPanel logDisplayPane;
	LogDisplayPanel logDisplay;
	LogListPanel logList;
	
	public ITScreen(ScreenNavigator c, ClientCalls client) {
		controller = c;
		this.client = client;
		setBackground(new Color(232, 171, 39));
		initalize();
	}
	
	private void initalize() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		logListPane = new JPanel(new BorderLayout());
		logList = new LogListPanel(client, controller, () -> refreshConversationDisplayPane());
		logListPane.add(logList, BorderLayout.CENTER);
		logDisplayPane = new JPanel(new BorderLayout());
		logDisplay = new LogDisplayPanel(client);
		logDisplayPane.add(buildConversationDisplayPane(), BorderLayout.CENTER);
		c.insets = new Insets(10,15,10,2);
		c.gridy = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 1;
		this.add(logListPane, c);
		c.insets = new Insets(10,2,10,15);
		c.gridx = 1;
		c.gridwidth = 3;
		c.weightx = 0.8;
		this.add(logDisplayPane, c);
	}
	
	
	
	
	private JPanel buildConversationDisplayPane() {
		JPanel pane = new JPanel(new GridBagLayout());
		JLabel empty = new JLabel("<html>Select a Log from the left to view it<br>Or search for logs if you have none previously viewed.");
		empty.setFont(Fonts.display);
		pane.add(empty);
		return pane;
	}
	

	
	private void refreshConversationDisplayPane() {
		if (DataModel.getInstance().getCurrentLog() == null) {
			logDisplayPane.removeAll();
			logDisplayPane.add(buildConversationDisplayPane(), BorderLayout.CENTER);
		}
		else if (logDisplayPane.getComponent(0) == logDisplay) {
			logDisplay.refresh();
	    		logList.updateSelectedConversation(DataModel.getInstance().getCurrentLog());
	    }
	    else {
	    		logDisplayPane.removeAll();
	    		logDisplayPane.add(logDisplay, BorderLayout.CENTER);
	    		logDisplay.refresh();
	    		logList.updateSelectedConversation(DataModel.getInstance().getCurrentLog());
	    }
		logDisplayPane.revalidate();
		logDisplayPane.repaint();
	}
	
	@Override
	public void whenShown() {
		logList.getProfile().setUser(DataModel.getInstance().getCurrentUser());
	}
}
