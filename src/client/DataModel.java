package client;
import javax.swing.DefaultListModel;
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
	//DEBUG: Public for GUI Testing
	public void setCurrentUser(User c) {
		currentUser = c;
	}
	
	public void setConversationList() {
		
	}
	//DEBUG: Public for GUI Testing 
	public void setCurrentConversation(int index) {
		currentConversation = conversationList.elementAt(index);
		currentConversationMessageList.setMessages(conversationList.elementAt(index).getMessages());
	}
	
}
