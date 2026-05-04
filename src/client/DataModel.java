package client;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;

import GUI.ConversationListModel;
import GUI.MessageListModel;
import model.*;
//Singleton Data Model for read by GUI and write by Client
public class DataModel {
	//Eager Singleton instance
	private static DataModel uniqueInstance = new DataModel();
	//private data members for the model 
	private User currentUser;
	private Conversation currentConversation;
	private ConversationListModel conversationList;
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
		conversationList = new ConversationListModel();
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
	
	public ConversationListModel getConversationList(){
		return conversationList;
	}
	
	protected void addMessageToCurrent(Message m) {
		currentConversationMessageList.addMessage(m);
		addMessageToConversation(currentConversation, m);
	}
	
	public void addMessageToConversation(Conversation c, Message m) {
		conversationList.get(c).addMessage(m);
		conversationList.sortByRecentMessage();
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

	protected void setCurrentUser(User c) {
		currentUser = c;
	}

	protected void setConversationList(ArrayList<Conversation> conversations) {
		conversationList.removeAllElements();
		conversationList.addAll(conversations);
	}

	protected void addConversationToList(Conversation c) {
		conversationList.add(conversationList.getSize(), c);
		conversationList.sortByRecentMessage();
	}
	
	
	protected void setCurrentConversation(int index) {
		currentConversation = conversationList.getElementAt(index);
		currentConversationMessageList.setMessages(conversationList.getElementAt(index).getMessages());
	}
	
	protected void setCurrentLog(int index) {
		currentLog = logsList.elementAt(index);
		currentLogMessageList.setMessages(logsList.elementAt(index).getMessages());
	}
	
	public DefaultListModel<Conversation> getLogsList(){
		return logsList;
	}
	
	protected void addLogToList(Conversation c) {
		logsList.add(logsList.size(), c);
	}
	
	public MessageListModel getCurrentLogMessageList() {
		return currentLogMessageList;
	}
	
	public Conversation getCurrentLog() {
		return currentLog;
	}
	
	protected void emptyCurrentConversation() {
		currentConversation = null;
	}
	
	protected void addUserToCache(User u) {
		if (u != null) {
			userCache.put(u.getUserID(), u);
		}
	}
	
	public User getServerUser() {
		return serverUser;
	}
	
	//Should be called once on login
	protected void setServerUser(User u) {
		serverUser = u;
	}
	
	public Conversation getReportsConversation() {
		return reportsConversation;
	}
	
	//SHould be called once on login
	protected void setReportsConversation(Conversation c) {
		reportsConversation = c;
	}
	
}
