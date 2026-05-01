package server;

import java.io.IOException;
import java.io.ObjectOutputStream;

import model.Conversation;
import model.Message;
import model.RequestType;
import model.ResponseType;
import model.User;
import model.Wrapper;

public class ConversationHandler {

	private ObjectOutputStream out;
	
	public ConversationHandler (ObjectOutputStream out) {
		this.out = out;
	}
	
	void handleCreateConversation(Wrapper obj) {
		/* 
		 * This method must create a new conversation. A conversation
		 * involves 2 Users. The two User sockets must be able to send
		 * messages to each other. The socket for the clients
		 * are found in ClientList<ClientHandler, User> in the Server.
		 *  
		 */
		sendResponse( new Message("CREAING CONVERSATION", "Server"), ResponseType.CREATE_CONVERSATION_SUCCESS);
		
		
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
	
	void handleGetConversation(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method retrieves a conversation from a file. The conversationID
		 * is used to look up the file and load the contents.
		 */
		
		sendResponse(new Message("GETTING CONVERSATION", "Server"), ResponseType.CONVERSATION_SENT);
		
	}
	
	void handleAddParticipant(Wrapper obj, String activeConversationID) {
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
		
		Conversation currentConversation = Server.getActiveConversation(activeConversationID);
		if (currentConversation == null) {
            sendResponse(new Message("CONVERSATION NOT FOUND", "Server"), ResponseType.ADD_PARTICIPANT_FAIL);
            return;
        }
		
		User incomingUser = (User) obj.getPayload();
		User userToAdd = Server.getUserbyID(incomingUser);

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

		// Update the canonical server-side User object
		userToAdd.getConversations().add(activeConversationID);

		// If the added user is online, send them the updated conversation
		ClientHandler addedUserHandler = Server.getActiveClient(userToAddID);
		if (addedUserHandler != null) {
			try {
				addedUserHandler.sendToClient(new Wrapper(currentConversation, ResponseType.SENDING_DATA));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		sendResponse(new Message("ADDING PARTICIPANT", "Server"), ResponseType.ADD_PARTICIPANT_SUCCESS);
		
	}
	
	
	void handleRemoveParticipant(Wrapper obj, String activeConversationID) {
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
		Conversation activeConversation = Server.getActiveConversation(activeConversationID);
		
		//I know which conversation they are part of
		//I have to remove the from the participant list
		//check that the conversation exists
		if(activeConversation == null) {
            sendResponse(new Message("CONVERSATION DNE", "Server"), ResponseType.REMOVE_PARTICIPANT_FAIL);
            return;
        }
		
		//get the user from the Existing data to update
		User removingUser = Server.getUserbyID(userToRemove);
		
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
	    
	    sendResponse(new Message("REMOVING PARTICIPANT", "Server"), ResponseType.REMOVE_PARTICIPANT_SUCCESS);
		
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
