package GUI;
import java.awt.CardLayout;
import java.awt.Component;

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
		for(Component c : container.getComponents()) {
			if(c.isVisible()) {
				if (c instanceof DisplayScreen) {
					((DisplayScreen) c).whenShown();
				}
			}
		}
	}
}
