package server;

import java.io.IOException; 
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;

import model.Conversation;
import model.Message;
import model.RequestType;
import model.ResponseType;
import model.Wrapper;

public class MessageHandler {

	private ObjectOutputStream out;
	
	public MessageHandler(ObjectOutputStream out) {
		this.out = out;
	}
	
	void handleGetMessages(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method gets the messages of a conversation to send to a User.
		 * The ConversationID should be used to get the messages for that
		 * Conversation.  
		 */ 
		
		 if (!(obj.getPayload() instanceof String)) {
		        sendResponse(new Message("INVALID PAYLOAD: CONVERSATION ID REQUIRED", "Server"),
		                ResponseType.MESSAGE_NOT_SENT);
		        return;
		    }

		    String conversationID = (String) obj.getPayload();

		    Conversation conversation = Server.getConversation(conversationID);

		    if (conversation == null) {
		        sendResponse(new Message("CONVERSATION NOT FOUND", "Server"),
		                ResponseType.MESSAGE_NOT_SENT);
		        return;
		    }

		    ArrayList<Message> messages = conversation.getMessages();

		    try {
		        Wrapper response = new Wrapper(messages, ResponseType.SENDING_DATA);
		        out.writeObject(response);
		        out.flush();
		    } catch (IOException e) {
		        e.printStackTrace();
		        sendResponse(new Message("FAILED TO SEND MESSAGES", "Server"),
		                ResponseType.MESSAGE_NOT_SENT);
		    }
		} 
	
	void handleGetNewMessages(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * I am not sure the Server will need this method??
		 * This method will send unread messages to a User when they 
		 * switch to the correct conversation.  
		 */
		if (!(obj.getPayload() instanceof String)) {
	        sendResponse(new Message("INVALID PAYLOAD: CONVERSATION ID REQUIRED", "Server"),
	                ResponseType.MESSAGE_NOT_SENT);
	        return;
	    }

	    String conversationID = (String) obj.getPayload();

	    Conversation conversation = Server.getConversation(conversationID);

	    if (conversation == null) {
	        sendResponse(new Message("CONVERSATION NOT FOUND", "Server"),
	                ResponseType.MESSAGE_NOT_SENT);
	        return;
	    }

	    ArrayList<Message> messages = conversation.getMessages();

	    try {
	        Wrapper response = new Wrapper(messages, ResponseType.SENDING_DATA);
	        out.writeObject(response);
	        out.flush();
	    } catch (IOException e) {
	        e.printStackTrace();
	        sendResponse(new Message("FAILED TO SEND NEW MESSAGES", "Server"),
	                ResponseType.MESSAGE_NOT_SENT);
	    }
	}
	
	
	void handleSendMessage(Wrapper obj, ArrayList<Message> logQueue, String activeConversationID, String senderID) {
		/* 
		 * This method has to re-route the message to the right user.
		 * It will need to go through the userList in the Server.
		 * This method needs to store the message that is being sent
		 * into the logQueue[] array to save the messages to a log later.
		 */
		Message messageToSend = null;

		if (!(obj.getPayload() instanceof Message)) {
			sendResponse(new Message("INVALID PAYLOAD: MESSAGE", "Server"), ResponseType.MESSAGE_NOT_SENT);
	        return;
		}
		else {
			messageToSend = (Message) obj.getPayload();
		}
		logQueue.add(messageToSend); 
		// Auto-save logs when queue reaches threshold
		if (logQueue.size() >= 10) {
		    Server.saveLogQueue(senderID, logQueue);
		    logQueue.clear();
		} 
		//THIS LOG MUST BE SAVED TO DISK BEFORE LOGOUT
		
		/* Search the ConversationList in UserData to find the conversation
		 * that the user is a part of.
		 * Get the participants from the Conversation object.
		 * Then get the UserID's from the Participant list
		 * to find their ClientHandlers in the userList in the Server.
		 * the userList<User, ClientHandler> has each active user and their
		 * clientHandler. Each ClientHandler has a socket to write to.
		 */
		
		/*
		 * for(each conversation in ConversationList) {
		 * 	check conversationID with ConversationList.conversationID
		 *  if the correct ID is found
		 *    get the participants of that Conversation
		 *    Get each Users ClientSocket to write to
		 *    send the message through their socket
		 */
		//first use conversationID to find the correct conversation
		//a map in the Server currently holds all the conversations
		Conversation currentConversation = Server.getConversation(activeConversationID);
		
	    if (currentConversation == null) {
	    	sendResponse(new Message("CONVERSATION NOT FOUND", "Server"), ResponseType.MESSAGE_NOT_SENT);
            return;
        }
		/* 
		 * The currentConversation is the current conversation that the user is viewing.
		 * Conversation has a list of participants to send a message to.
		 */
	    //save the message to the current conversation
	    //offline user will get the messages when they log in via Conversation object
	    currentConversation.addMessage(messageToSend); 
	    Server.saveConversation(currentConversation);
		//get the list of participants from the conversation
		HashSet<String> conversationParticipants = currentConversation.getParticipants();
		//get the UserIDs so you can send a message to their ClientHandler.clientSocket
		for(String userID : conversationParticipants) {
			
			//if the participant is the sender, skip them
            if (userID.equals(senderID)) {
                continue;
            }
			//get an activeClient by passing userID into activeClient(user)
			ClientHandler handler = Server.getActiveClient(userID); //returns client handle associated with userID
			//if the handler is the user is offline
			//you must save unread messages to send to the User when they log in
            if (handler == null) {
            	
            	//save the message to the map in the server
            	Server.updateUnreadMessage(userID, activeConversationID);
                continue;
            }
          
			//get their socket to write to
            try {
				handler.sendToClient(new Wrapper(messageToSend, ResponseType.SENDING_MESSAGE));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sendResponse(new Message("MESSAGE SENT", "Server"), ResponseType.MESSAGE_SENT);
    	
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
