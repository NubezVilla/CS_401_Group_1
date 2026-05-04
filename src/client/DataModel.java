package client;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	private DefaultListModel<Conversation> conversationList;
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
	
	protected void addMessageToCurrent(Message m) {
		currentConversationMessageList.addMessage(m);
		addMessageToConversation(currentConversation, m);
	}
	
	protected void addMessageToConversation(Conversation c, Message m) {
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

	protected void setCurrentUser(User c) {
		currentUser = c;
	}

	protected void setConversationList(ArrayList<Conversation> conversations) {
		conversationList.removeAllElements();
		conversationList.addAll(conversations);
	}

	protected void addConversationToList(Conversation c) {
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
	
	protected void setCurrentConversation(int index) {
		currentConversation = conversationList.elementAt(index);
		currentConversationMessageList.setMessages(conversationList.elementAt(index).getMessages());
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
