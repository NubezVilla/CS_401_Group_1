package model;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;

public class ConversationHeader implements Serializable{
	private boolean isGroup;
	private String conversationID;
	private HashSet<String> participantIDs;
	private String groupName;
	private String groupCreator;
	private Instant createdAt;
	
	public ConversationHeader(Conversation c) {
		conversationID = c.getID();
		participantIDs = c.getParticipants();
		groupName = null;
		groupCreator = null;
		isGroup = false;
		createdAt = c.getCreated();
	}
	
	public ConversationHeader(GroupConversation c) {
		conversationID = c.getID();
		participantIDs = c.getParticipants();
		groupName = c.getName();
		groupCreator = c.getCreator();
		isGroup = true;
		createdAt = c.getCreated();
	}
	
	public String getID() {
		return conversationID;
	}
	
	public HashSet<String> getParticipants(){
		return participantIDs;
	}
	
	public boolean isGroup() {
		return isGroup;
	}
	
	public String getName() {
		return groupName;
	}
	public String getCreator() {
		return groupCreator;
	}
	public Instant getCreatedAt() {
		return createdAt;
	}
}
