package GUI;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import client.DataModel;
import model.Message;

public class LogMessagePanel extends JPanel {
	private ClientCalls client;
	
	LogMessagePanel(ClientCalls c){
		client = c;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		DataModel.getInstance().getCurrentLogMessageList().addChangeListener(e -> {
			if (DataModel.getInstance().getCurrentLogMessageList().getMessages().size() != 0) {
				add(new MessageDisplayComponent(DataModel.getInstance().getCurrentLogMessageList().getNewestMessage(), client));
			}
		});
	}
	
	public void refresh() {
		removeAll();
		add(Box.createVerticalGlue());
		
		for (Message m : DataModel.getInstance().getCurrentLogMessageList().getMessages()) {
			add(new MessageDisplayComponent(m, client));
		}
		revalidate();
		repaint();
	}
}
