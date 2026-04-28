package GUI;

import model.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import client.DataModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class MessageDisplayComponent extends JPanel {
    private final Message message;
    private final ClientCalls client;
    private final boolean isOwnMessage;

    private final JPanel bubblePanel;
    private final JButton actionButton;
    private final JButton senderIcon;

    public MessageDisplayComponent(Message message, ClientCalls client) {
        this.message = message;
        this.client = client;
        this.isOwnMessage = message.getSenderID().equals(DataModel.getInstance().getCurrentUser().getUserID());

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(4, 8, 4, 8));

        bubblePanel = buildBubble();
        actionButton = buildActionButton();
        senderIcon = buildSenderIcon();

        JPanel row = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        
        JPanel iconBox = buildUpwardsBox(senderIcon);
        JPanel actionBox = buildUpwardsBox(actionButton);
        
        
        if(!isOwnMessage) {
        		container.add(actionBox, BorderLayout.EAST);
        		container.add(iconBox, BorderLayout.WEST);
        }
        container.add(bubblePanel, BorderLayout.CENTER);
        

        row.add(container);
        add(row, BorderLayout.CENTER);

        if(!isOwnMessage) {
        		installHoverBehavior();
        }
    }
    
    private JPanel buildUpwardsBox(Component c) {
    		JPanel upwardsBox = new JPanel();
        upwardsBox.setLayout(new BoxLayout(upwardsBox, BoxLayout.Y_AXIS));
        upwardsBox.add(c);
        upwardsBox.add(Box.createVerticalGlue());
        return upwardsBox;
    }

    private JPanel buildBubble() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        panel.setBackground(isOwnMessage ? new Color(220, 248, 198) : new Color(187, 234, 242));

        String senderName = client.getUserByID(message.getSenderID()).getName();

        JLabel headerLabel = new JLabel(senderName + "  •  " + timestampFormat(message.getTimestamp()));
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 12f));

        JTextArea textArea = new JTextArea(message.getText());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setOpaque(false);
        textArea.setBorder(null);
        textArea.setFont(textArea.getFont().deriveFont(13f));

        int maxTextWidth = 300;
        textArea.setSize(new Dimension(maxTextWidth, Short.MAX_VALUE));
        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(textArea, BorderLayout.CENTER);

        return panel;
    }

    private JButton buildActionButton() {
        JButton button = new JButton("🏴");
        button.setVisible(false);
        button.setFocusable(false);
        button.setMargin(new Insets(0, 4, 0, 4));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setToolTipText("Report Message");

        JPopupMenu menu = new JPopupMenu();

        JMenuItem reportItem = new JMenuItem("Report Message");
        reportItem.addActionListener(e -> {
            //TODO Implement reporting
        });

        menu.add(reportItem);

        button.addActionListener(e -> menu.show(button, 0, button.getHeight()));
        return button;
    }
    
    private JButton  buildSenderIcon() {
    		JButton button = new JButton();
    		button.setMargin(new Insets(1, 0, 1, 3));
    		button.setOpaque(false);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
    		char firstLetter = client.getUserByID(message.getSenderID()).getName().toUpperCase().charAt(0);
		File iconFile = new File("resources/avatars/" + firstLetter + ".png");
		ImageIcon icon = new ImageIcon(iconFile.getPath());
		icon.setImage(icon.getImage().getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH));
		button.setIcon(icon);
		return button;
    }

    private void installHoverBehavior() {
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                actionButton.setVisible(true);
                revalidate();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), MessageDisplayComponent.this);
                if (!MessageDisplayComponent.this.contains(p)) {
                    actionButton.setVisible(false);
                    revalidate();
                    repaint();
                }
            }
        };

        addMouseListener(hoverAdapter);
        bubblePanel.addMouseListener(hoverAdapter);
        actionButton.addMouseListener(hoverAdapter);

        for (Component child : bubblePanel.getComponents()) {
            child.addMouseListener(hoverAdapter);
        }
    }

    private static String timestampFormat(Instant instant) {
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        LocalDate date = zdt.toLocalDate();
        LocalDate today = LocalDate.now();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        if (date.equals(today)) {
            return zdt.format(timeFormatter);
        } else if (date.equals(today.minusDays(1))) {
            return "Yesterday at " + zdt.format(timeFormatter);
        } else {
            return zdt.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"));
        }
    }

}