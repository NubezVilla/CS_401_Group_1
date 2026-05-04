package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class LogDisplayPanel extends JPanel {
	private LogTopBar topBar;
	private LogMessagePanel messages;
	
	public LogDisplayPanel(ClientCalls client) {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		topBar = new LogTopBar(client);
		messages =  new LogMessagePanel(client);
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
		scroller.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 2, Color.black));
		
		c.gridx = 0;
		c.weightx = 1;
		c.gridy = 0;
		c.weighty = 0.05;
		c.fill = GridBagConstraints.BOTH;
		add(topBar, c);
		c.gridy = 1;
		c.weighty = 1.0;
		add(scroller, c);
		
		setBackground(new Color(255, 243, 176));
		
	}
	
	public void refresh() {
		topBar.refresh();
		messages.refresh();
	}
}
