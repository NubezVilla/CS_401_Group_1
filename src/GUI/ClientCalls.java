package GUI;
import java.util.ArrayList;

import model.*;

public interface ClientCalls {
	
	/**Takes the passed ip string and attempts to connect to the server on that address.
	 * 
	 * @param ip the IP address to look for the server on
	 * @return true if the connection succeeded, false if it did not
	 */
	
	public boolean connectionAttempt(String ip); 
	
	/**Passes a un and pw to the client for a login attempt
	 * <p>
	 * If the login is successful, the data model should have a current user before this returns.
	 * @param username A string username
	 * @param password A string password
	 * @return true if login succeeded, false if login failed
	 */
	public boolean loginAttempt(String username, String password);
	
	/**Informs the server that the user is logging out, then closes the connection
	 * and logs out the user. Should send the server a copy of the current user for sync. 
	 */
	public void logoutAttempt();
	
	
	/**For a given ID, returns the User associated with it. 
	 * 
	 * @param id the ID of the user to be returned
	 * @return the user that has the ID requested
	 */
	public User getUserByID(String id);
	
	/**Searches the list of users for those matching the provided string. This should
	 * send the string/regex to the server for query against the server's list. 
	 * @param matching: a String that is matched against users' names, and IDs.
	 * @return A list of users matching the provided String
	 */
	public ArrayList<User> searchUsers(String matching);
	
	/**Responsible for two things:
	 * <br>
	 * Notifying the server that the current conversation has changed 
	 * <p>
	 * Updating Data Model's currentConversation and currentConversationMessage 
	 * to match the new ID. 
	 * @param id the id of the new current conversation
	 */
	public void updateCurrentConversation(String id);
	
	/**Fetches the next 200 messages from the conversation with the passed ID. 
	 * Fetched messages should be placed in the conversation in DataModel's list.
	 * @param id conversation to fetch messages from
	 */
	public void fetchMessages(String id);
	/**Takes the paramter text, makes it into a message, adds it to the current conversation,
	 * then passes it to the server. Server is responsible for updating unreadConversation list. 
	 * @param text to be sent
	 */
	public void sendMessage(String text);
	
	/** Updates target with the other four strings. 
	 * Should inform the server of a change attempt first. 
	 * Server may return fail if login info is already taken. 
	 * On fail, return false and do nothing. On success, return true and update. 
	 * <br>
	 * Note that any of the strings may be blank, and any blank strings should be ignored. 
	 * @param target user to update
	 * @param newName
	 * @param newPosition
	 * @param newUsername
	 * @param newPassword
	 * @return false if the user's login info is already taken, true otherwise
	 */
	public Boolean updateUser(User target, String newName, String newPosition, String newUsername, String newPassword);
	
	/**Requests the server to create a new user with the given information.
	 * Server may return fail if login info is already taken. 
	 * On fail, return false and do nothing. On success, return true and add the user to the local cache.
	 */
	public Boolean createNewUser(String name, String position, String username, String password);
	
	
	/**Same as above, except for an IT user. 
	 */
	public Boolean createNewITUser(String name, String position, String username, String password);
	
	/**Requests the server to start a new conversation with the current user.
	 * <br>
	 * Should add the new conversation to the currentUser's conversation list. 
	 * Server will add it to the other user's conversation list. 
	 * @Return the newly created conversation. 
	 * @param other user who is a part of the conversation
	 */
	public Conversation startNewConversation(User other);
	
	/**Requests the server to create a new conversation with 
	 * the passed conversation as the base. Server should give the conversation some default name;
	 * @param c the conversation to use as the base for constructing a new group conversation
	 * @return the newly created group conversation
	 */
	public GroupConversation startNewGroupConversation(Conversation c);
	
	/**Tells the server it should add the passed user to the current conversation. 
	 * Server should respond affirmatively, and local conversation should be updated too.
	 * <br> 
	 * Maybe should also add that user to the local cache
	 * @param u user to be added
	 */
	public void addUserToGroupChat(User u);
	
	/**Tells the server it should remove the passed user from the current conversation. 
	 * Server should respond affirmatively, and local conversation should be updated too.
	 * @param u user to be removed
	 */
	public void removeUserFromGroupChat(User u);
	
	/**Sets the current conversation (guaranteed to be a group chat)'s name  
	 * to the passed string. Tell the server to do the same blah blah 
	 * @param name
	 */
	public void setGroupChatName(String name);
	
	/**Asks the server for all conversation headers (conversation IDs and participants without messages)
	 * that the passed User is a part of, then returns the list. 
	 * @param u the user whose conversations should be returned
	 * @return the list of all conversations that the user is a part of
	 */
	public ArrayList<Conversation> queryConversationLogsByUser(User u);
	
	
	/**Asks the server for all conversation headers (conversation IDs and participants without messages)
	 * whose IDs begin with the passed string
	 * @param id to match against conversation logs
	 * @return the list of all conversation headers whose IDs begin with the passed string
	 */
	public ArrayList<Conversation> queryConversationLogsByID(String id);
	
	/**Asks the server for the full conversation log (all info and messages) 
	 * of the conversation with the passed id.
	 * @param id of the conversation to find
	 * @return the conversation log with the passed ID
	 */
	public Conversation requestConversationLogById(String id);
		
	/**updates the current log to the passed conversation log. 
	 * Should put the conversation in the logs data structure first. 
	 * <br>
	 * Needs to use setCurrentLog since setCurrentConversation goes to the conversationList, which needs to point elsewhere. 
	 * @param c conversation to be added to the logs list and made the current log
	 */
	public void updateCurrentLog(String id);
}
