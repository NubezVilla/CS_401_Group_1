package GUI;
import java.util.ArrayList;

import model.*;

public interface ClientCalls {
	/**Passes a un and pw to the client for a login attempt
	 * <p>
	 * If the login is successful, the data model should have a current user before this returns.
	 * @param username A string username
	 * @param password A string password
	 * @return true if login succeeded, false if login failed
	 */
	public boolean loginAttempt(String username, String password);
	
	/**Searches the list of users for those matching the provided string. This should
	 * send the string/regex to the server for query against the server's list. 
	 * @param matching a String that is matched against users' names, usernames, and IDs.
	 * @return A list of users matching the provided String
	 */
	public ArrayList<User> searchUsers(String matching);
	
	/**For a given ID, returns the User associated with it. 
	 * 
	 * @param id the ID of the user to be returned
	 * @return the user that has the ID requested
	 */
	public User getUserByID(String id);
	
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
}
