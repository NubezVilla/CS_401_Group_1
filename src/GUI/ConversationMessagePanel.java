package GUI;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import client.DataModel;
import model.Message;

public class ConversationMessagePanel extends JPanel {
	private ClientCalls client;
	
	ConversationMessagePanel(ClientCalls c){
		client = c;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		DataModel.getInstance().getCurrentConversationMessageList().addChangeListener(e -> {
			add(new MessageDisplayComponent(DataModel.getInstance().getCurrentConversationMessageList().getNewestMessage(), client));
		});
	}
	
	public void refresh() {
		removeAll();
		add(Box.createVerticalGlue());
		if (DataModel.getInstance().getCurrentConversationMessageList().getMessages().size() < 200) {
			client.fetchMessages(DataModel.getInstance().getCurrentConversation().getID());
		}
		
		for (Message m : DataModel.getInstance().getCurrentConversationMessageList().getMessages()) {
			
			add(new MessageDisplayComponent(m, client));
		}
		revalidate();
		repaint();
	}
	
}
