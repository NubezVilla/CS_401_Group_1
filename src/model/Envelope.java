package model;
import java.io.Serializable;

public class Envelope implements Serializable{
	Message thisMessage;
	String conversationID;
	
	public Envelope(Message information, String ID) {
		thisMessage = information;
		conversationID = ID;
	}
	
	public Message getMessage() {
		return thisMessage;
	}
	
	public String getID() {
		return conversationID;
	}
	
	public void setMessage(Message m) {
		thisMessage = m;
	}
	
	public void setID(String ID) {
		conversationID = ID;
	}
}
