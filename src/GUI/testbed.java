package GUI;
import model.*;
import client.*;
public class testbed {

	public static void main(String[] args) {
		DEBUGClientController c = new DEBUGClientController();
		ConnectionScreen connect = new ConnectionScreen(c);
		MainWindow m = new MainWindow(c);
		
		m.startup();
	}

}
