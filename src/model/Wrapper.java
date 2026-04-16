package model;
import java.io.Serializable;

public class Wrapper implements Serializable{
	Object payload;
	int requestID;
	
	public Wrapper(Object payload, int requestID) {
		this.payload = payload;
		this.requestID = requestID;
	}
	
	public Object getPayload() {
		return payload;
	}
	
	public int getRequestID() {
		return requestID;
	}
}
