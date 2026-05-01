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
import model.RequestType;
import model.ResponseType;
import model.User;
import model.Wrapper;

public class AuthenticateHandler {

    // Session state
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isLoggedIn;
    private User userAccount;

    public AuthenticateHandler(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;
        this.isLoggedIn = false;
        this.userAccount = null;
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

        // Success
        isLoggedIn = true;
        
        /* 
         * sendUserData needs to send the account information to the client.
         * Three write outs will happen. 
         * Send User account, Conversations, unread messages
         */
        sendUserData(userAccount);
        sendResponse(new Message("LOGGING IN", "Server"), ResponseType.LOGIN_SUCCESS);
        return true;
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
			}
			isLoggedIn = false;
			sendResponse(new Message("LOGOUT SUCCESSFUL", "Server"), ResponseType.LOGOUT_SUCCESS);
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			sendResponse(new Message("ERROR SAVING LOGOUT DATA", "Server"), ResponseType.LOGOUT_FAIL);
		}
		return isLoggedIn;
    }
 
	void handleRegister(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method must create a new user. The user
		 * must be added to UserData. The new user information
		 * must be saved to file as well. This can be done here
		 * or during logout?
		 */
		sendResponse(new Message("REGISTERING NEW USER", "Server"), ResponseType.REGISTER_USER_SUCCESS);
		
	}

    
	void handleGetUserInfo(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method retrieves a User's information from UserData. 
		 */
		sendResponse(new Message("RETRIEVING USER INFO", "Server"), ResponseType.USER_INFO_SENT);
		
	}
    
    
    // Getters so ClientHandler can sync state after login
    public boolean isLoggedIn() { return isLoggedIn; }
    public User getUserAccount() { return userAccount; }

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



    //this sends the User data and conversation data to the client
	private void sendUserData(User userAccount) {
		/*
		 * I need to send the account information and conversation information to the 
		 * client.
		 * This will require two write outs
		 */
		//First, send the account information
		Wrapper sendUserAccount = new Wrapper(userAccount, ResponseType.SENDING_DATA);
		try {
			out.writeObject(sendUserAccount);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Next, i need to send a list of Conversations
		//I need to know what Conversations they belong to
		HashSet<String> conversationIDs = new HashSet<>();
		conversationIDs = userAccount.getConversations();
		ArrayList<Conversation> conversationsToSend = new ArrayList<>();
		for(String convoID : conversationIDs) {
			//i need the conversation in UserData
			conversationsToSend.add(Server.getActiveConversation(convoID));
		}
		//send the Conversation data to the client
		Wrapper sendConversations = new Wrapper(conversationsToSend, ResponseType.SENDING_DATA);
		try {
			out.writeObject(sendConversations);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}