package server; 

import java.io.Serializable;
import java.time.LocalDateTime;

	public final class Log implements Serializable {
		private static final long serialVersionUID = 1L;

	    private final String userId;
	    private final String conversationId;
	    private final String action;
	    private final String timestamp;

	    public Log(String userId, String conversationId, String action) {
	        this.userId = userId;
	        this.conversationId = conversationId;
	        this.action = action;
	        this.timestamp = LocalDateTime.now().toString();
	    }

	    public String toCSV() {
	        return userId + "," + conversationId + "," + action + "," + timestamp;
	    }
	}
