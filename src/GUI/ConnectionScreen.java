package GUI;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ConnectionScreen extends JDialog{
	private JComboBox<String> selection;
	private JLabel ipSelectionLabel;
	private JLabel errorText;
	private JButton connect;
	private JProgressBar bar;
	private Socket socket;
	
	private static int portNumber = 54927;
	
	public ConnectionScreen() {
		setModal(true);
		setTitle("Server Connection");
		setLayout(new GridBagLayout());
		getContentPane().setBackground(new Color(255, 243, 176));
		setSize(350, 250);
		setLocationRelativeTo(null);
		Image icon;
		try {
			icon = ImageIO.read(new File("resources/icon/honeycombicon.png"));
			setIconImage(icon);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		ipSelectionLabel = new JLabel("Select a server to Connect to:");
		errorText =  new JLabel("Connection failed. Check the address and try again.");
		errorText.setFont(Fonts.error);
		errorText.setForeground(Color.red);
		errorText.setVisible(false);
		
		connect = new JButton("Connect");
		connect.setEnabled(false);
		connect.addActionListener(e->{
			showConnection();
			new SwingWorker<Socket, Void>() {
			    @Override
			    protected Socket doInBackground() throws Exception {
			        Thread.sleep(1000);

			        String ip = (String) selection.getSelectedItem();

			        if ("School".equals(ip)) {
			            ip = "134.154.33.22";
			        } else if ("Home".equals(ip)) {
			            ip = "10.0.0.165";
			        } else if ("Coffee Shop".equals(ip)) {
			            ip = "172.16.102.178";
			        }

			        return connectionAttempt(ip);
			    }

			    @Override
			    protected void done() {
			        try {
			            socket = get();
			            dispose();
			        } catch (Exception ex) {
			            showSelection(true);
			        }
			    }
			}.execute();
		});
		
		
		selection = new JComboBox<String>();
		selection.addActionListener(e-> {
			if (selection.getSelectedItem() != null) {
				connect.setEnabled(true);
			}
			else {
				connect.setEnabled(false);
			}
			if (selection.getSelectedIndex() == selection.getItemCount()-1) {
				selection.setSelectedItem("");
			}
		});
		selection.addItem("School");
		selection.addItem("Home");
		selection.addItem("Coffee Shop");
		selection.addItem("Custom (enter address)");
		selection.setEditable(true);
		
		bar = new JProgressBar();
		bar.setIndeterminate(true);
		bar.setSize(300, 45);
		
		showSelection(false);
		setVisible(true);
	}
	
	private void showSelection(boolean error) {
		getContentPane().removeAll();
		if(error) {errorText.setVisible(true);}
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		getContentPane().add(ipSelectionLabel, c);
		c.gridy = 1;
		getContentPane().add(errorText, c);
		c.gridy = 2;
		getContentPane().add(selection, c);
		c.gridy = 3;
		getContentPane().add(connect, c);
		getContentPane().revalidate();
		getContentPane().repaint();
	}
	
	private void showConnection() {
		getContentPane().removeAll();
		GridBagConstraints c = new GridBagConstraints();
		getContentPane().add(bar, c);
		getContentPane().revalidate();
	    getContentPane().repaint();
	}
	
	private Socket connectionAttempt(String ip) throws IOException {
	    return new Socket(ip, portNumber);
	}
	public Socket getSocket() {
	    return socket;
	}
}
