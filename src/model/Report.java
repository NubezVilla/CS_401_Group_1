package model;

import java.time.Instant;

public class Report extends Message {
	private Boolean resolved;
	public Report (String text, String senderID) {
		super(text, senderID);
		resolved = false;
	}
	
	public Report (String text, String senderID, Instant timestamp) {
		super(text, senderID, timestamp);
		resolved = false;
	}
	
	public boolean isResolved() {
		return resolved;
	}
	
	public void resolve() {
		resolved = !resolved;
	}
}
