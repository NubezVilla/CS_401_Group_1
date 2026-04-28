package GUI;

import java.awt.Insets;
import java.io.File;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import model.User;

public class UserDisplayComponent extends JPanel{
	private User thisUser;
	private JButton pfp;
	private JPanel info;
	private JLabel name;
	private JLabel position;
	private JLabel id;
	
	UserDisplayComponent(User u, boolean searchMode){
		thisUser = u;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setAlignmentX(LEFT_ALIGNMENT);
		pfp = new JButton();
		pfp.setMargin(new Insets(1, 0, 1, 3));
		pfp.setOpaque(false);
		pfp.setFocusable(false);
		pfp.setBorderPainted(false);
		pfp.setContentAreaFilled(false);
		info = new JPanel();
		info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
		name = new JLabel();
		position = new JLabel();
		id = new JLabel();
		name.setFont(Fonts.header);
		position.setFont(Fonts.display);
		position.setVisible(!searchMode);
		id.setFont(Fonts.error);
		id.setVisible(searchMode);
		info.add(name);
		info.add(position);
		info.setBorder(new EmptyBorder(0, 0, 0, 4));
		add(pfp);
		add(info);
		add(id);
		setVals();
	}
	
	
	
	private void setVals() {
		if (thisUser == null) {
			return;
		}
		char firstLetter = thisUser.getName().toUpperCase().charAt(0);
		File iconFile = new File("resources/avatars/" + firstLetter + ".png");
		ImageIcon icon = new ImageIcon(iconFile.getPath());
		icon.setImage(icon.getImage().getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH));
		pfp.setIcon(icon);
		name.setText(thisUser.getName());
		position.setText(thisUser.getPosition());
		id.setText(thisUser.getUserID());

	}
	
	public void setUser(User u) {
		thisUser = u;
		setVals();
	}
	
	
}
