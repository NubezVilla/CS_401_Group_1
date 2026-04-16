package model;

import java.time.Instant;

public class GroupConversation extends Conversation {
	private String groupName;
	private String creatorID;
	
	public GroupConversation(Conversation base, String creatorID) {
		for (String p : base.getParticipants()) {
			this.addParticipant(p);
		}
		this.creatorID = creatorID;
	}
	
	public GroupConversation(Conversation base, String creatorID, Instant created){
		super(created);
		for (String p : base.getParticipants()) {
			this.addParticipant(p);
		}
		this.creatorID = creatorID;
	}
	
	public String getName() {
		return groupName;
	}
	
	public String getCreator() {
		return creatorID;
	}
	
	public void setName(String  n) {
		groupName = n;
	}
	
	
	
}
