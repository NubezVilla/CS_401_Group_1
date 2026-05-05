package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import model.Conversation;
import model.Message;
import model.RequestType;
import model.ResponseType;
import model.User;
import model.Wrapper; 
import server.Broadcast;

public class ConversationHandler {

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ResponseHandler responseHandle;
	private ClientHandler clientHandle;
	
	public ConversationHandler (ObjectOutputStream out, ObjectInputStream in, ClientHandler client) {
		this.out = out;
		this.in = in;
		this.responseHandle = new ResponseHandler(out, in);
		this.clientHandle = client;
	}
	
	void handleCreateConversation(Wrapper obj) {
		/* 
		 * This method must create a new conversation. A conversation
		 * involves 2 Users. The two User sockets must be able to send
		 * messages to each other. The socket for the clients
		 * are found in ClientList<ClientHandler, User> in the Server.
		 *  
		 */
		if (obj.getPayload() instanceof User) {
			User other = (User) obj.getPayload();
			Conversation newConvo = new Conversation(clientHandle.getUserID(), other.getUserID());
			UserData.getInstance().addConversation(newConvo);
			UserData.getInstance().getUserById(other.getUserID()).addConversation(newConvo.getID());
			sendResponse(newConvo, ResponseType.CREATE_CONVERSATION_SUCCESS);
		}

		
	}
	
	void handleCreateGroupConversation(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method must send a message to multiple Clients. Each 
		 * GroupConversation should have a list of participants.
		 * Get the UserIDs and the correct sockets you have to
		 * pass along the message.
		 */
		sendResponse(new Message("CREATING GROUP CONVERSATION", "Server"), ResponseType.GROUP_CREATION_SUCCESS);
		
	}
	
	void handleGetConversation(Wrapper obj, String activeConversationID) {
		/* find the conversation they are looking for and 
		 * send it to the client
		 *  */
		
		// check that it is the correct payload
		if (!(obj.getPayload() instanceof String)) {
			sendResponse(new Message("INVALID PAYLOAD: STRING", "Server"), ResponseType.CONVERSATION_NOT_SENT);
			return;
		}
		//get the conversationID from the payload
		String conversationIDRequest = (String) obj.getPayload();
		//find the conversation that needs to be sent
		Conversation requestedConversation = Server.getConversation(conversationIDRequest);
		//make sure the conversation exists
		if(requestedConversation == null)
		{
			
			sendResponse(new Message("CONVERSATION DOES NOT EXIST", "Server"), ResponseType.CONVERSATION_NOT_SENT);
			return;
		}
		Wrapper sendConversation = new Wrapper(requestedConversation, ResponseType.CONVERSATION_SENT);
		//send the payload
		System.out.println("I was right");
		try {
			clientHandle.sendToClient(sendConversation);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void handleAddParticipant(Wrapper obj, String activeConversationID, String currentUserID) {
		/* 
		 * This method should add a User to a GroupConversation. The UserID and 
		 * their ClientHandler are in the ClientList in the Server. 
		 * The UserID should be added to the participant list in the 
		 * GroupConversation.
		 */
		// check that it is the correct payload
		if (!(obj.getPayload() instanceof User)) {
			sendResponse(new Message("INVALID PAYLOAD: USER REQUIRED", "Server"), ResponseType.ADD_PARTICIPANT_FAIL);
			return;
		}
		//check to make sure that the conversation the correct conversation is active
		if (activeConversationID == null) {
            sendResponse(new Message("NO ACTIVE CONVERSATION SELECTED", "Server"), ResponseType.ADD_PARTICIPANT_FAIL);
            return;
        }
		
		Conversation currentConversation = Server.getConversation(activeConversationID); 
		
		if (currentConversation == null) {
            sendResponse(new Message("CONVERSATION NOT FOUND", "Server"), ResponseType.ADD_PARTICIPANT_FAIL);
            return;
        }
		
		User incomingUser = (User) obj.getPayload();
		User userToAdd = Server.getUserbyID(incomingUser.getUserID());

		if (userToAdd == null) {
            sendResponse(new Message("USER NOT FOUND", "Server"), ResponseType.ADD_PARTICIPANT_FAIL);
            return;
        }
		
		String userToAddID = userToAdd.getUserID();

		// Prevent duplicate participants
		if (currentConversation.hasParticipant(userToAddID)) {
            sendResponse(new Message("USER IS ALREADY A PARTICIPANT", "Server"), ResponseType.ADD_PARTICIPANT_FAIL);
            return;
        }
		
		
		// Add the user to the conversation
		currentConversation.addParticipant(userToAddID);

		// Update the server-side User object
		userToAdd.getConversations().add(activeConversationID); 
		
		// Save the updated conversation so participant changes are persisted
		Server.saveConversation(currentConversation);
		Server.saveUserData(userToAdd);

		// If the added user is online, send them the updated conversation
		ClientHandler addedUserHandler = Server.getActiveClient(userToAddID);
		if (addedUserHandler != null) {
			try {
				addedUserHandler.sendToClient(new Wrapper(currentConversation, ResponseType.CONVERSATION_SENT));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		sendResponse(new Message("ADDING PARTICIPANT", "Server"), ResponseType.ADD_PARTICIPANT_SUCCESS);
		Broadcast broadcast = new Broadcast();
		broadcast.broadcastParticipantAdded(activeConversationID, userToAdd, currentUserID);
	}
	
	
	void handleRemoveParticipant(Wrapper obj, String activeConversationID, String currentUserID) {
		/* 
		 * This method must remove a participant from a GroupConversation. 
		 * The UserID and their ClientHandler are in the 
		 * ClientList in the Server. The UserID should be removed
		 * from the participant list in the GroupConversation.
		 */
		//the activeConversationID should be the group chat
		//the object from socket should be a User
		//check that the right payload was sent
		User userToRemove = null;
		if (!(obj.getPayload() instanceof User)) {
            sendResponse(new Message("INVALID PAYLOAD: USER", "Server"), ResponseType.REMOVE_PARTICIPANT_FAIL);
            return;
        } else {
            userToRemove = (User) obj.getPayload();
        }
		//I know who must be removed now
		//I need the conversation that they are part of to remove them
		Conversation activeConversation = Server.getConversation(activeConversationID);
		
		//I know which conversation they are part of
		//I have to remove the from the participant list
		//check that the conversation exists
		if(activeConversation == null) {
            sendResponse(new Message("CONVERSATION DNE", "Server"), ResponseType.REMOVE_PARTICIPANT_FAIL);
            return;
        }
		
		//get the user from the Existing data to update
		User removingUser = Server.getUserbyID(userToRemove.getUserID());
		
		if (removingUser == null) {
            sendResponse(new Message("USER NOT FOUND", "Server"), ResponseType.REMOVE_PARTICIPANT_FAIL);
            return;
        }
		
	    String userToRemoveID = removingUser.getUserID();

	    // Check that the user is actually in the conversation
	    if (!activeConversation.hasParticipant(userToRemoveID)) {
            sendResponse(new Message("USER NOT IN CONVERSATION", "Server"), ResponseType.REMOVE_PARTICIPANT_FAIL);
            return;
        }
	    
	  
	    //remove from the conversation
	    //this should be holding a reference to the object in the Map
	    //so it can change the contents inside the map
	    activeConversation.removeParticipant(userToRemoveID);

	    //remove the conversation from the user's conversation list
	    removingUser.getConversations().remove(activeConversationID);

	    //also remove from unread list if present
	    removingUser.getUnreadConversations().remove(activeConversationID); 
	    
	    //update conversation and user data
	    Server.saveConversation(activeConversation);
	    Server.saveUserData(removingUser); 
	    
	    //notify affected online clients
	    Broadcast broadcast = new Broadcast();
	    broadcast.broadcastParticipantRemoved(activeConversationID, removingUser, currentUserID);
	    
	    sendResponse(new Message("REMOVING PARTICIPANT", "Server"), ResponseType.REMOVE_PARTICIPANT_SUCCESS);
		
	}
	
	
    // Private helper to reduce repetitive try/catch blocks
    private void sendPayload(Wrapper payload) {
        try {
        		clientHandle.sendToClient(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	// Private helper to reduce repetitive try/catch blocks
	private void sendResponse(Object o, ResponseType responseType) {
	    try {
	        Wrapper response = new Wrapper(o, responseType);
	        clientHandle.sendToClient(response);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
	
	
}
