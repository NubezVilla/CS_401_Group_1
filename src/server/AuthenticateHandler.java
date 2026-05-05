package server;
 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
 
import model.Conversation;
import model.LoginInfo;
import model.Message;
import model.ResponseType;
import model.User;
import model.Wrapper;

public class AuthenticateHandler {

    
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isLoggedIn;
    private User userAccount;
    private ResponseHandler responseHandle;
    
    public AuthenticateHandler(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;
        this.isLoggedIn = false;
        this.userAccount = null;
        this.responseHandle = new ResponseHandler(out, in);
    }

    boolean handleLogin(Wrapper obj) {
    	/* 
		 * The obj has the login credentials that must be checked.
		 * UserData should have all of the users in the system along 
		 * with their user names and passwords. 
		 */
 
        // Check correct payload type
        if (!(obj.getPayload() instanceof LoginInfo)) {
            sendResponse(new Message("INVALID PAYLOAD: LOGIN EXPECTED", "Server"), ResponseType.LOGIN_FAIL);
            return isLoggedIn;
        }
		/*** NEED TO ADD ERROR HANDLING FOR INCORRECT PAYLOAD TYPES ***/
		//get the UserData form the UserData class later
        LoginInfo loginInfo = (LoginInfo) obj.getPayload();
        /** THEIR ACCOUNT MUST BE LOADED INTO THE HANDLER **/
        //userAccount = UserData.getUser();
        userAccount = Server.getUserData(loginInfo.hashCode());

        // Check credentials
        if (userAccount == null) {
            sendResponse(new Message("WRONG USER/PASS", "Server"), ResponseType.LOGIN_FAIL);
            return isLoggedIn;
        }
        
        /*** HANDLE RESPONSE ***/
        //here is where I would loop until the client gets a successful login
        //send the user account first
        Wrapper sendUserAccount = new Wrapper(userAccount, ResponseType.LOGIN_SUCCESS);
        //send the user account and a LOGIN_SUCCESS
        sendPayload(sendUserAccount);
        //boolean userSent = responseHandle.sendWithRetry(sendUserAccount, ResponseType.DATA_RECEIVED);

        /*if (!userSent) {
            sendResponse(new Message("FAILED TO SEND USER DATA", "Server"), ResponseType.LOGIN_FAIL);
            return false;
        }*/

        //send the conversation data second
        ArrayList<Conversation> conversationsToSend = new ArrayList<>();
        HashSet<String> conversationIDs = userAccount.getConversations();

        for (String convoID : conversationIDs) {
        	Conversation conversationToAdd = Server.getConversation(convoID);
        	if(conversationToAdd != null) {
        		conversationsToSend.add(conversationToAdd);
        	}
            
        }

        Wrapper sendConversations = new Wrapper(conversationsToSend, ResponseType.CONVERSATION_SENT);
        //send the conversation data
        sendPayload(sendConversations);
        //i also need to send the active client list so the client logging in know who is online
        ArrayList<String> activeUsers = Server.getActiveUserIDs();
        Wrapper activeUsersPayload = new Wrapper(activeUsers, ResponseType.USER_INFO_SENT);
        sendPayload(activeUsersPayload);
        /*
        boolean conversationsSent = responseHandle.sendWithRetry(sendConversations, ResponseType.DATA_RECEIVED);

        if (!conversationsSent) {
            sendResponse(new Message("FAILED TO SEND CONVERSATION DATA", "Server"), ResponseType.LOGIN_FAIL);
            return isLoggedIn;
        }*/

        // Success
        isLoggedIn = true;
        //sendResponse(new Message("LOGGING IN", "Server"), ResponseType.LOGIN_SUCCESS);
        return isLoggedIn;
    }

	
    boolean handleLogout(Wrapper obj, ArrayList<Message> logQueue, Socket clientSocket) {
		/*
		 * This method must save all of the users data to a file.
		 * The user data includes all messages that have been sent
		 * since start up to logs, conversations, new conversations, 
		 * user profile if it was updated, etc.
		 * It must close the socket and send a successful logout response.
		 * I need to get the data from the obj
		 * this has be User Data, Conversation Data, and Message Data
		 * A Conversation object should have a list of messages and participants
		 * I might need to read two times to be sent User Data and Conversation data
		 * The first read happens in the switch, and redirects it here. The first
		 * read I will assume is the User account information
		 * It will be more efficient to only save the account if there was a change. 
		*/
		//read in User object data first
		if (!(obj.getPayload() instanceof User)) {
            sendResponse(new Message("INVALID LOGOUT PAYLOAD: USER REQUIRED", "Server"), ResponseType.LOGOUT_FAIL);
            return isLoggedIn;
		}
		else {
		    User updatedUser = (User) obj.getPayload(); //get user account
			userAccount = updatedUser; //update this User's profile
		}
 
		/*
		 * Assuming the read in for User is done correctly, read in
		 * the next package for the Conversations.
		 * This should be a list of Conversation objects that the user is 
		 * a participant.
		 */
		ArrayList<Conversation> updatedConversations = null;
		try {
			//assuming the next read is an Array
			Wrapper arrayObject = (Wrapper) in.readObject();
			if (!(arrayObject.getPayload() instanceof ArrayList<?>)) {
				sendResponse(new Message("INVALID LOGOUT PAYLOAD: CONVERSATION LIST REQUIRED", "Server"), ResponseType.LOGOUT_FAIL);
	            return isLoggedIn;
	        }
			else { updatedConversations = (ArrayList<Conversation>) arrayObject.getPayload(); }
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			sendResponse(new Message("FAILED TO READ LOGOUT DATA", "Server"), ResponseType.LOGOUT_FAIL);
			return isLoggedIn;
		}
 
		try {
			//Save updated user profile
			Server.saveUserData(userAccount);
			//Save each conversation
			for (Conversation conversation : updatedConversations) {
				Server.saveConversation(conversation);
			}
			//Save message logs
			Server.saveLogQueue(userAccount.getUserID(), logQueue);
			//Remove from active users
			if (userAccount != null) {
				Server.removeActiveUser(userAccount.getUserID());
				Broadcast broadcast = new Broadcast();
				broadcast.broadcastLogout(userAccount);
			}
			isLoggedIn = false;
			sendResponse(new Message("LOGOUT SUCCESSFUL", "Server"), ResponseType.LOGOUT_SUCCESS);
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			sendResponse(new Message("ERROR SAVING LOGOUT DATA", "Server"), ResponseType.LOGOUT_FAIL);
			return isLoggedIn;
		}
		return isLoggedIn;
    }
 
	void handleRegister(ObjectOutputStream out, Wrapper obj) {
		/*
	     * IT-only method for creating a new user.
	     * Payload is expected to be a User object.
	     */

	    if (userAccount == null || !userAccount.isIT()) {
	        sendResponse(new Message("UNAUTHORIZED USER", "Server"), ResponseType.REGISTER_USER_FAIL);
	        return;
	    }

	    if (!(obj.getPayload() instanceof User)) {
	        sendResponse(new Message("INVALID PAYLOAD: USER REQUIRED", "Server"), ResponseType.REGISTER_USER_FAIL);
	        return;
	    }

	    User newUser = (User) obj.getPayload();

	    for (User existingUser : Server.getAllUsers()) {
	        if (existingUser.getLoginInfo().equals(newUser.getLoginInfo())) {
	            sendResponse(new Message("DUPLICATE USER", "Server"), ResponseType.REGISTER_USER_FAIL);
	            return;
	        }
	    }

	    Server.addUserData(newUser);
	    Server.saveUserData(newUser);

	    sendResponse(new Message("REGISTERED NEW USER", "Server"), ResponseType.REGISTER_USER_SUCCESS);
	} 
    
		

    
	void handleGetUserInfo(ObjectOutputStream out, Wrapper obj) {
		/*
	     * Retrieves user information from UserData.
	     * Payload can be either:
	     * 1. User object
	     * 2. String userID
	     */

	    String userID;

	    if (obj.getPayload() instanceof User) {
	        User requestedUser = (User) obj.getPayload();
	        userID = requestedUser.getUserID();
	    } else if (obj.getPayload() instanceof String) {
	        userID = (String) obj.getPayload();
	    } else {
	        sendResponse(new Message("INVALID PAYLOAD: USER OR USER ID REQUIRED", "Server"),
	                ResponseType.USER_INFO_NOT_SENT);
	        return;
	    }

	    User foundUser = Server.getUserByIdString(userID);

	    if (foundUser == null) {
	        sendResponse(new Message("USER NOT FOUND", "Server"),
	                ResponseType.USER_INFO_NOT_SENT);
	        return;
	    }

	    try {
	        Wrapper response = new Wrapper(foundUser, ResponseType.USER_INFO_SENT);
	        out.writeObject(response);
	        out.flush();
	    } catch (IOException e) {
	        e.printStackTrace();
	        sendResponse(new Message("FAILED TO SEND USER INFO", "Server"),
	                ResponseType.USER_INFO_NOT_SENT);
	    } 
	} 
    
    // Getters so ClientHandler can sync state after login
    public boolean isLoggedIn() { return isLoggedIn; }
    public User getUserAccount() { return userAccount; }
    
    // Private helper to reduce repetitive try/catch blocks
    private void sendPayload(Wrapper payload) {
        try {
            out.writeObject(payload);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Private helper to reduce repetitive try/catch blocks
    private void sendResponse(Message msg, ResponseType responseType) {
        try {
            Wrapper response = new Wrapper(msg, responseType);
            out.writeObject(response);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}