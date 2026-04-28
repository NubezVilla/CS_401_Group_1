package model;
import java.io.Serializable;

public class Wrapper implements Serializable{
	Object payload;
	RequestType requestID;
	ResponseType responseID;
	
	
	public Wrapper(Object payload, RequestType requestID) {
		this.payload = payload;
		this.requestID = requestID;
	}
	
	public Wrapper (Object payload, ResponseType responseID) {
		this.payload = payload;
		this.responseID = responseID;
	}
	public Object getPayload() {
		return payload;
	}
	
	public RequestType getRequestType() {
		return requestID;
	}
	
	public ResponseType getResponseType() {
		return responseID;
	}
}
