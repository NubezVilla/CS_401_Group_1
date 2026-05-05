package server;
 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

import client.DataModel;
import model.Conversation;
import model.LoginInfo;
import model.Message;
import model.RequestType;
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
        boolean userSent = responseHandle.sendWithRetry(sendUserAccount, ResponseType.LOGIN_SUCCESS);
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
        Wrapper sendConversations = new Wrapper(conversationsToSend, ResponseType.SENDING_CONVERSATIONS);
        //send the conversation data
        sendPayload(sendConversations);
        //No you don't, we have no logic for this. - Jack
        //i also need to send the active client list so the client logging in know who is online
//        ArrayList<String> activeUsers = Server.getActiveUserIDs();
//        Wrapper activeUsersPayload = new Wrapper(activeUsers, ResponseType.USER_INFO_SENT);
//        sendPayload(activeUsersPayload);


        // Success
        isLoggedIn = true;
        return isLoggedIn;
    }

	
    boolean handleLogout(Wrapper obj, ArrayList<Message> logQueue, Socket clientSocket) {
		/*Jack: No reason to save conversations from user on logout.
		 * Just the user's information. Messages are to be stored (at least 
		 * in cache) as soon as they are sent. Rewriting this to 
		 * only deal with user stuff. 
		 * 
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
 
 
		try {
			//Save updated user profile
			Server.saveUserData(userAccount);
			//Save each conversation
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
			return isLoggedIn;
		}
		return isLoggedIn;
    }
 
	void handleRegister(ObjectOutputStream out, Wrapper obj, boolean isIT) {
		if (obj.getPayload() instanceof ArrayList<?>) {
			ArrayList<String> userInfo = (ArrayList<String>) obj.getPayload();
			User newUser = new User(userInfo.get(0), userInfo.get(1));
			newUser.setName(userInfo.get(2));
			newUser.setPosition(userInfo.get(3));
			newUser.setIT(isIT);
			for (User existingUser : Server.getAllUsers()) {
				if (existingUser.getLoginInfo().equals(newUser.getLoginInfo())) {
					sendResponse(new Message("DUPLICATE USER", "Server"), ResponseType.REGISTER_USER_FAIL);
					return;
				}
			}
			UserData.getInstance().addUser(newUser);
			Server.saveUserData(newUser);
			sendResponse(newUser, ResponseType.REGISTER_USER_SUCCESS);
		}
	} 
    
	void handleGetUserInfo(ObjectOutputStream out, Wrapper obj) {
		if (obj.getPayload() instanceof String) {
			User requested = UserData.getInstance().getUserById((String)obj.getPayload());
			if (requested == null) {
		        sendResponse(new Message("USER NOT FOUND", "Server"),
		                ResponseType.USER_INFO_NOT_SENT);
		        return;
		    }
			sendResponse(requested, ResponseType.USER_INFO_SENT);
		}
		//Should include error handling, not worrying about it right now. 
	}
	
	void handleSearchSimilarUsers(ObjectOutputStream out, Wrapper obj) {
		if (obj.getPayload() instanceof String) {
			String matching = (String) obj.getPayload();
			ArrayList<User> response = new ArrayList<User>();
			for(User u : UserData.getInstance().getAllUsers()) {
				if (u.getName().contains(matching) || 
						u.getUserID().startsWith(matching)) {
					response.add(u);
				}
				//exclude self
				if(u.equals(userAccount)) {
					response.remove(u);
				}
			}
			
			sendResponse(response, ResponseType.USER_INFO_SENT);
		}
	}
   
	void handleUpdateUserInfo(Wrapper obj) {
		if (obj.getPayload() instanceof User) {
			User changes = (User)obj.getPayload();
			User working = UserData.getInstance().getUserById(((User)obj.getPayload()).getUserID());
			if (!changes.getName().isEmpty()) {
				working.setName(changes.getName());
			}
			if (!changes.getPosition().isEmpty()) {
				working.setPosition(changes.getPosition());
			}
			String newUN = working.getLoginInfo().getUserName();
			if (!changes.getLoginInfo().getUserName().isEmpty()) {
				newUN = changes.getLoginInfo().getUserName();
			}
			String newPW =  working.getLoginInfo().getPassword();
			if (!changes.getLoginInfo().getPassword().isEmpty()) {
				newPW = changes.getLoginInfo().getPassword();
			}
			working.setLoginInfo(newUN, newPW);
			Server.saveUserData(working);
			
			sendResponse(null, ResponseType.UPDATED_USER_RECEIVED);
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
    private void sendResponse(Object o, ResponseType responseType) {
        try {
            Wrapper response = new Wrapper(o, responseType);
            out.writeObject(response);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}