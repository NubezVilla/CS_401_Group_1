package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class ConversationDisplayPanel extends JPanel {
	private ConversationTopBar topBar;
	private ConversationMessagePanel messages;
	private JTextArea messageBox;
	private ClientCalls client;
	
	public ConversationDisplayPanel(ClientCalls client) {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		this.client = client;
		topBar = new ConversationTopBar(client);
		messages =  new ConversationMessagePanel(client);
		JPanel wrapper =  new JPanel(new BorderLayout());
		wrapper.add(messages, BorderLayout.SOUTH);
		JScrollPane scroller = new JScrollPane(wrapper);
		scroller.getVerticalScrollBar().setUnitIncrement(16);
		
		messages.addContainerListener(new ContainerAdapter() {
		    @Override
		    public void componentAdded(ContainerEvent e) {
		        SwingUtilities.invokeLater(() -> {
		            JScrollBar bar = scroller.getVerticalScrollBar();
		            bar.setValue(bar.getMaximum());
		        });
		    }
		});
		
		messageBox = new HintTextArea("Enter your message");
		messageBox.setLineWrap(true);
		messageBox.setMargin(new Insets(4,4,4,4));
		messageBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		installMessageSending(messageBox);
		
		c.gridx = 0;
		c.weightx = 1;
		c.gridy = 0;
		c.weighty = 0.05;
		c.fill = GridBagConstraints.BOTH;
		add(topBar, c);
		c.gridy = 1;
		c.weighty = 1.0;
		add(scroller, c);
		c.gridy = 2;
		c.weighty = 0.09;
		add(messageBox, c);
		
	}
	
	public void refresh() {
		topBar.refresh();
		messages.refresh();
	}
	
	private void installMessageSending(JTextArea messageField) {
		InputMap inputMap = messageField.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap actionMap = messageField.getActionMap();

		inputMap.put(KeyStroke.getKeyStroke("ENTER"), "sendMessage");
		inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
		actionMap.put("sendMessage", new AbstractAction() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        String text = messageField.getText().trim();
		        if (!text.isEmpty()) {
		            client.sendMessage(text);
		            messageField.setText("");
		        }
		    }
		});
	}
}
