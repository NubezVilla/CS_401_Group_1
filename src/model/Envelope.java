package model;
import java.io.Serializable;

public class Envelope implements Serializable{
	Message thisMessage;
	ConversationHeader cHeader;
	
	public Envelope(Message information, ConversationHeader c) {
		thisMessage = information;
		cHeader = c;
	}
	
	public Message getMessage() {
		return thisMessage;
	}
	
	public String getID() {
		return cHeader.getID();
	}

	public ConversationHeader getHeader() {
		return cHeader;
	}
}
