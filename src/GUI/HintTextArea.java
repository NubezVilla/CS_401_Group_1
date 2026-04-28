package GUI;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextArea;

public class HintTextArea extends JTextArea implements FocusListener{
	private final String hint;
	private boolean showingHint;

	public HintTextArea(final String hint) {
		super(hint);
	    this.hint = hint;
	    this.showingHint = true;
	    super.addFocusListener(this);
	    setFont(Fonts.display);
	    setForeground(Color.GRAY);
	}

	@Override
	public void focusGained(FocusEvent e) {
	    if(this.getText().isEmpty()) {
	    		super.setText("");
	    		showingHint = false;
	    }
	}
	@Override
	public void focusLost(FocusEvent e) {
		if(this.getText().isEmpty()) {
			showingHint = true;
			super.setText(hint); 
		}
	}

	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}
}
