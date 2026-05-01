package GUI;

import java.awt.Component;
import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import model.*;


public class MessageListCellRenderer extends JPanel implements ListCellRenderer<Message> {
	private JLabel messageBody;
	private JButton reportButton;
	private final int ICONHEIGHT = 48;
	private final int ICONWIDTH = 48;
	private final ClientCalls client;
	 
	public MessageListCellRenderer(ClientCalls client) {
		messageBody = new JLabel();
		reportButton = new JButton();
	    this.client = client;
	}
	@Override
	public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		setLayout(new OverlayLayout(this));
        setOpaque(false);
        
        messageBody.setOpaque(true);
        messageBody.setAlignmentX(0.5f);
        messageBody.setAlignmentY(0.5f);
        
        reportButton = new JButton(new ImageIcon(new ImageIcon("resources/icons/report.png").getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH)));
        reportButton.setVisible(false);
        reportButton.setFocusable(false);
        reportButton.setBorderPainted(false);
        reportButton.setContentAreaFilled(false);
        reportButton.setOpaque(false);
        reportButton.setAlignmentX(1.0f);
        reportButton.setAlignmentY(0.0f);
		User sender = client.getUserByID(value.getSenderID());
		
		//Icon making
		char firstLetter = sender.getName().toUpperCase().charAt(0);
		File iconFile = new File("resources/avatars/" + firstLetter + ".png");
		ImageIcon icon = new ImageIcon(iconFile.getPath());
		icon.setImage(icon.getImage().getScaledInstance(ICONWIDTH, ICONHEIGHT, java.awt.Image.SCALE_SMOOTH));
		
		//Timestamp formatting
		ZonedDateTime zdt = value.getTimestamp().atZone(ZoneId.systemDefault());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
		String formattedTimestamp = zdt.format(formatter);
		
		
		//Adding wrapping
		String formattedText = wrapText(value.getText());
		
		messageBody.setText("<html>" + sender.getName() + "  " + formattedTimestamp + "<br>" + formattedText + "</html>");
		messageBody.setIcon(icon);
		
		this.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        reportButton.setVisible(true);
		    }
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		    		reportButton.setVisible(false);
		    }
		});
		
		messageBody.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        reportButton.setVisible(true);
		    }
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		    		reportButton.setVisible(false);
		    }
		});
		
		messageBody.setFont(list.getFont());
		setEnabled(list.isEnabled());
	    
		add(reportButton);
		add(messageBody);
		return this;
	    
	}
	
	private String wrapText(String s) {
		String formattedText = "";
		int lengthCounter = 0;
		String[] words = s.split(" ");
		for (int i = 0; i < words.length; i++) {
			if (words[i].length() + lengthCounter < 100) {
				formattedText += words[i] + " ";
				lengthCounter += words[i].length() + 1;
			}
			else if (words[i].length() < 100) {
				lengthCounter = 0;
				formattedText += "<br>";
				formattedText += words[i] + " ";
				lengthCounter += words[i].length() + 1;
			}
			else {
				int printed = 0;
				while (printed < words[i].length()) {
					if (lengthCounter < 100) {
						formattedText += words[i].charAt(printed++);
						lengthCounter++;
					}
					else {
						formattedText += "<br>";
						lengthCounter = 0;
					}
				}
				formattedText += " ";
				lengthCounter++;
			}
		}
		
		return formattedText;
	}

}
