package GUI;
import java.awt.CardLayout;

import javax.swing.*;
public class ScreenNavigator {
	private JPanel container;
	private CardLayout layout;
	
	public ScreenNavigator(CardLayout layout, JPanel container) {
		this.layout = layout;
		this.container = container;
	}
	
	public void show(Screen s) {
		layout.show(container, s.name());
	}
}
