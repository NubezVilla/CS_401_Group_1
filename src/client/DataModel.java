package client;
import model.*;
//Singleton Data Model for read by GUI and write by Client
public class DataModel {
	//Singleton instance
	private static DataModel uniqueInstance = new DataModel();
	//private data members for the model 
	private User currentUser;
	//Constructor, initialize data members
	protected DataModel() {
		currentUser = null;
	}
	//Global access handle
	public static synchronized DataModel getInstance() {
		return uniqueInstance;
	}
	
	//Public getters, protected setters
	public User getCurrentUser() {
		return currentUser;
	}
	
	protected void setCurrentUser(User c) {
		currentUser = c;
	}
}
