package model;
import java.io.Serializable;
import java.util.HashSet;
//Important!! Must store and read users to/from file in ID order, as ID cannot be manually set. 
public class User implements Serializable {
	private static int count = 0;
	private String userID;
	private LoginInfo logininfo;
	private String realName;
	private String position;
	private boolean itUser;
	private HashSet<String> conversationIDs;
	private HashSet<String> unreadConversationIDs;
	
	//default constructor
	public User() {
		userID = Integer.toString(++count);
		itUser = false;
		conversationIDs = new HashSet<String>();
		unreadConversationIDs =  new HashSet<String>();
	}
	//overloaded constructor, main usecase
	public User(String un, String pw) {
		userID = Integer.toString(++count);
		logininfo = new LoginInfo(un, pw);
		itUser = false;
		conversationIDs = new HashSet<String>();
		unreadConversationIDs =  new HashSet<String>();
	}
	//convenience method for comparing
	public boolean compareLogin(User other) {
		return (this.getLoginInfo().equals(other.getLoginInfo()));
	}
	
	//Setters and Getters
	//Getters for everything, some setters intentionally omitted.
	public String getUserID() {
		return userID;
	}
	
	public LoginInfo getLoginInfo() {
		return logininfo;
	}
	
	public String getName() {
		return realName;
	}
	
	public String getPosition() {
		return position;
	}
	
	public boolean isIT() {
		return itUser;
	}
	
	public HashSet<String> getConversations(){
		return conversationIDs;
	}
	
	public HashSet<String> getUnreadConversations(){
		return unreadConversationIDs;
	}
	
	public void setLoginInfo(String un, String pw) {
		logininfo = new LoginInfo(un, pw);
	}
	
	public void setName(String n) {
		realName = n;
	}
	
	public void setPosition(String p) {
		position = p;
	}
	
	public void setIT(boolean v) {
		itUser = v;
	}
	
}
