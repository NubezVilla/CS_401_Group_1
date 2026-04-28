package client;
import java.util.Map;

import javax.swing.DefaultListModel;

import GUI.DEBUGUserGenerator;
import GUI.MessageListModel;
import model.*;
//Singleton Data Model for read by GUI and write by Client
public class DataModel {
	//Eager Singleton instance
	private static DataModel uniqueInstance = new DataModel();
	//private data members for the model 
	private User currentUser;
	private Conversation currentConversation;
	//These have to be ListModels because they need to ping the GUI to update when they get updated
	//List Models need to be updated on the EDT, either talk to me or look up how that has to be handled. 
	private DefaultListModel<Conversation> conversationList;
	//Something in logic should make sure this is updated whenever the currentConversation gets
	//Messages added to it
	private MessageListModel currentConversationMessageList;
	private Map<String, User> userCache;

	protected DataModel() {
		conversationList = new DefaultListModel<Conversation>();
		currentConversationMessageList = new MessageListModel();
	}
	//Global access handle
	public static synchronized DataModel getInstance() {
		return uniqueInstance;
	}
	
	//Public getters, protected setters
	public User getCurrentUser() {
		return currentUser;
	}
	
	public DefaultListModel<Conversation> getConversationList(){
		return conversationList;
	}
	
	public MessageListModel getCurrentConversationMessageList(){
		return currentConversationMessageList;
	}
	
	public Conversation getCurrentConversation() {
		return currentConversation;
	}
	
	public Map<String, User>getUserCache(){
		return userCache;
	}
	//DEBUG: Public for GUI Testing
	public void setCurrentUser(User c) {
		currentUser = c;
	}
	//DEBUG: Hardcoded for gui testing purposes
	public void setConversationList() {
		User other1 =  new User();
		User other2 =  new User();
		User other3 =  new User();
		Conversation a = new Conversation(currentUser.getUserID(), other1.getUserID());
		Conversation b = new Conversation(currentUser.getUserID(), other2.getUserID());
		Conversation c = new Conversation(currentUser.getUserID(), other3.getUserID());
		a.addMessage(new Message("Hello", currentUser.getUserID()));
		a.addMessage(new Message("Hewwo", other1.getUserID()));
		b.addMessage(new Message("You're a bad man", currentUser.getUserID()));
		b.addMessage(new Message("Aint I a stinker", other2.getUserID()));
		c.addMessage(new Message("Hello There", currentUser.getUserID()));
		c.addMessage(new Message("General Kenobi", other3.getUserID()));
		conversationList.add(0, a);
		conversationList.add(1, b);
		conversationList.add(2, c);
		
	}
	//DEBUG: Public for GUI Testing 
	public void setCurrentConversation(int index) {
		currentConversation = conversationList.elementAt(index);
		currentConversationMessageList.setMessages(conversationList.elementAt(index).getMessages());
	}
	
	//DEBUG: Hardcoded for GUI Testing 
	public void addUserToCache() {
		userCache = DEBUGUserGenerator.generateUsers(1000);
	}
	
}
