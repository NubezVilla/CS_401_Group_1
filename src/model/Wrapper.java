package model;
import java.io.Serializable;

public class Wrapper implements Serializable{
	Object payload;
	RequestType requestID;
	
	public Wrapper(Object payload, RequestType requestID) {
		this.payload = payload;
		this.requestID = requestID;
	}
	
	public Object getPayload() {
		return payload;
	}
	
	public RequestType getRequestID() {
		return requestID;
	}
}
