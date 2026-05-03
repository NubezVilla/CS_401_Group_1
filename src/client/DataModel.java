package client;
import java.util.HashMap;
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
	
	private User serverUser;
	private Conversation reportsConversation;
	private DefaultListModel<Conversation> logsList;
	private Conversation currentLog;
	private MessageListModel currentLogMessageList;

	protected DataModel() {
		logsList = new DefaultListModel<Conversation>();
		currentLogMessageList = new MessageListModel();
		conversationList = new DefaultListModel<Conversation>();
		currentConversationMessageList = new MessageListModel();
		userCache = new HashMap<>();
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
	
	//Public for GUI Testing
	public void addMessageToCurrent(Message m) {
		currentConversationMessageList.addMessage(m);
		addMessageToConversation(currentConversation, m);
	}
	
	public void addMessageToConversation(Conversation c, Message m) {
		int index = conversationList.indexOf(c);
		conversationList.get(index).addMessage(m);
		conversationListSort();
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
		other1.setName("Tom Scott");
		User other2 =  new User();
		other2.setName("Victor Orban");
		User other3 =  new User();
		other3.setName("Daniel Adams");
		DataModel.getInstance().addUserToCache(other1);
		DataModel.getInstance().addUserToCache(other2);
		DataModel.getInstance().addUserToCache(other3);
		Conversation a = new Conversation(currentUser.getUserID(), other1.getUserID());
		Conversation b = new Conversation(currentUser.getUserID(), other2.getUserID());
		Conversation c = new Conversation(currentUser.getUserID(), other3.getUserID());
		GroupConversation d = new GroupConversation(c, currentUser.getUserID());
		a.addMessage(new Message("Hello", currentUser.getUserID()));
		a.addMessage(new Message("Hewwo", other1.getUserID()));
		b.addMessage(new Message("You're a bad man", currentUser.getUserID()));
		b.addMessage(new Message("Aint I a stinker", other2.getUserID()));
		c.addMessage(new Message("Hello There", currentUser.getUserID()));
		c.addMessage(new Message("General Kenobi", other3.getUserID()));
		addConversationToList(a);
		addConversationToList(b);
		addConversationToList(c);
		addConversationToList(d);
		
	}
	//DEBUG: Public for GUI Testing
	public void addConversationToList(Conversation c) {
		conversationList.add(conversationList.size(), c);
		conversationListSort();
	}
	
	private void conversationListSort() {
		Conversation last = conversationList.get(conversationList.size() - 1);
		for (int i = conversationList.size() - 2; i >= 0; i--) {
			if (last.getMostRecentMessageTimestamp().isAfter(conversationList.get(i).getMostRecentMessageTimestamp())) {
				conversationList.set(i+1, conversationList.get(i));
				conversationList.set(i, last);
			}
			last = conversationList.get(i);
		}
	}
	//DEBUG: Public for GUI Testing 
	public void setCurrentConversation(int index) {
		currentConversation = conversationList.elementAt(index);
		currentConversationMessageList.setMessages(conversationList.elementAt(index).getMessages());
	}
	
	//DEBUG: Public for GUI Testing 
	public void setCurrentLog(int index) {
		currentLog = logsList.elementAt(index);
		currentLogMessageList.setMessages(logsList.elementAt(index).getMessages());
	}
	
	public DefaultListModel<Conversation> getLogsList(){
		return logsList;
	}
	
	//DEBUG: Public for GUI Testing
	public void addLogToList(Conversation c) {
		logsList.add(logsList.size(), c);
	}
	
	public MessageListModel getCurrentLogMessageList() {
		return currentLogMessageList;
	}
	
	public Conversation getCurrentLog() {
		return currentLog;
	}
	
	public void emptyCurrentConversation() {
		currentConversation = null;
	}
	
	//DEBUG: Hardcoded for GUI Testing 
	public void addUserToCache(User u) {
		if (u != null) {
			userCache.put(u.getUserID(), u);
		}
		else {
			userCache.putAll(DEBUGUserGenerator.generateUsers(1000));
		}
		
	}
	
	public User getServerUser() {
		return serverUser;
	}
	
	//DEBUG: Public for GUI Testing
	//Should be called once on login
	public void setServerUser(User u) {
		serverUser = u;
	}
	
	public Conversation getReportsConversation() {
		return reportsConversation;
	}
	
	//DEBUG: Public for GUI Testing
	//SHould be called once on login
	public void setReportsConversation(Conversation c) {
		reportsConversation = c;
	}
	
}
