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
	private ArrayList<Message> messages;
	private Instant createdAt;
	private Instant mostRecentMessage;
	//default constructor
	//we may need more constructors, not sure
	public Conversation() {
		conversationID = Integer.toString(++count);
		createdAt = Instant.now();
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
}
