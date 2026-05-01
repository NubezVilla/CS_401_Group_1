package GUI;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import model.User;

public class UserSearchBar extends HintTextField {
	private ClientCalls client;
	private JPopupMenu searchResponseMenu;
	private JPanel searchResponseContainer;
	Consumer<User> onSelect;
	
	private final int delay = 1000;
	private final int minChars = 3;
	
	public UserSearchBar(ClientCalls c, Consumer<User> function) {
		super("Search For User");
		client = c;
		onSelect = function;
		searchResponseContainer =  new JPanel();
		searchResponseMenu = new JPopupMenu();
		JScrollPane scroller = new JScrollPane(searchResponseContainer);
		scroller.setPreferredSize(new Dimension(300, 400));
		scroller.getVerticalScrollBar().setUnitIncrement(16);
		searchResponseContainer.setLayout(new BoxLayout(searchResponseContainer, BoxLayout.Y_AXIS));
		searchResponseMenu.add(scroller);
		
		Timer debounceTimer = new Timer(delay, e -> {
		    String text = getText();
		    doUserSearch(text);
		});
		debounceTimer.setRepeats(false);

		getDocument().addDocumentListener(new DocumentListener() {
		    private void handleChange() {
		        String text = getText();
		        if (text.length() >= minChars) {
		            debounceTimer.restart();
		        } else {
		            debounceTimer.stop(); 
		        }
		    }

		    @Override public void insertUpdate(DocumentEvent e) { handleChange(); }
		    @Override public void removeUpdate(DocumentEvent e) { handleChange(); }
		    @Override public void changedUpdate(DocumentEvent e) { handleChange(); }
		});
		this.setColumns(20);
	}
	
	private void doUserSearch(String text) {
		searchResponseContainer.removeAll();
		searchResponseContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		ArrayList<User> matches = client.searchUsers(text);
		if (matches.size() == 0) {
			JLabel feedback = new JLabel("No Matches Found");
			feedback.setFont(Fonts.error);
			searchResponseContainer.add(feedback);
			
		}
		else {
			for(User u : matches) {
				UserDisplayComponent temp = new UserDisplayComponent(u, true);
				temp.setOnClick(() ->{
						searchResponseMenu.setVisible(false);
						onSelect.accept(u);
				});
				searchResponseContainer.add(temp);
				searchResponseContainer.add(new JSeparator(JSeparator.HORIZONTAL));
			}
		}
		searchResponseMenu.show(this, this.getWidth(), this.getHeight());
	}
}
