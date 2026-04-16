package model;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
//Important!! Must read Conversations from file in ID order, as ID cannot be manually set. 
public class Conversation implements Serializable {
	private static int count = 0;
	private String conversationID;
	private HashSet<String> participantIDs;
	protected ArrayList<Message> messages;
	private Instant createdAt;
	private Instant mostRecentMessage;
	//default constructor
	public Conversation() {
		conversationID = Integer.toString(++count);
		createdAt = Instant.now();
		participantIDs = new HashSet<String>();
		messages = new ArrayList<Message>();
		mostRecentMessage = null;
	}
	//pass both members of the conversation
	public Conversation(String p1, String p2) {
		conversationID = Integer.toString(++count);
		createdAt = Instant.now();
		participantIDs = new HashSet<String>();
		participantIDs.add(p1);
		participantIDs.add(p2);
		messages = new ArrayList<Message>();
		mostRecentMessage = null;
	}
	//Constructor for rebuilding conversations 
	public Conversation(Instant i) {
		conversationID = Integer.toString(++count);
		createdAt = i;
		participantIDs = new HashSet<String>();
		messages = new ArrayList<Message>();
		mostRecentMessage = null;
	}

	public String getID() {
		return conversationID;
	}
	
	public HashSet<String> getParticipants(){
		return participantIDs;
	}
	
	public ArrayList<Message> getMessages(){
		return messages;
	}
	
	public Instant getCreated() {
		return createdAt;
	}
	
	public Instant getMostRecentMessageTimestamp() {
		return mostRecentMessage;
	}
	
	//Convenience Methods
	public void addParticipant(String id) {
		participantIDs.add(id);
	}
	
	public void removeParticipant(String id) {
		participantIDs.remove(id);
	}
	
	public Boolean hasParticipant(String id) {
		return participantIDs.contains(id);
	}
	//Automatically updates mostRecentMessage
	public void addMessage(Message m) {
		messages.add(m);
		mostRecentMessage = m.getTimestamp();
	}
}
