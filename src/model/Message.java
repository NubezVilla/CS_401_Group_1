package model;

import java.io.Serializable;
import java.time.Instant;

public class Message implements Serializable {
	private String text;
	private String senderID;
	private Instant timestamp;
	
	public Message (String text, String senderID) {
		this.text = text;
		this.senderID = senderID;
		timestamp = Instant.now();
	}
	
	public Message (String text, String senderID, Instant timestamp) {
		this.text = text;
		this.senderID = senderID;
		this.timestamp = timestamp;
	}
	
	public String getText() {
		return text;
	}
	
	public String getSenderID() {
		return senderID;
	}
	
	public Instant getTimestamp() {
		return timestamp;
	}
}
