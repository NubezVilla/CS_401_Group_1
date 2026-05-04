package server;

/* 
 * Broadcast is meant to let clients know of a change in the server.
 * For example, if a user logs in, every client that is online needs to know
 * that a user has come online, and who it is. 
 * Case 1: Login
 * Case 2: Logout
 * Case 3: Participant Removed
 * Case 4: Participant Added
 * 
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import model.Conversation;
import model.Message;
import model.ResponseType;
import model.User;
import model.Wrapper;

public class Broadcast {

    public void broadcastLogin(User user) {
        Message msg = new Message("USER LOGGED IN: " + user.getUserID(), "Server");
        Wrapper wrapper = new Wrapper(msg, ResponseType.USER_LOGGED_IN);
        sendToAllExcept(wrapper, user.getUserID());
    }

    public void broadcastLogout(User user) {
        Message msg = new Message("USER LOGGED OUT: " + user.getUserID(), "Server");
        Wrapper wrapper = new Wrapper(msg, ResponseType.USER_LOGGED_OUT);
        sendToAllExcept(wrapper, user.getUserID());
    }

    public void broadcastParticipantAdded(String conversationID, User user, String currentUserID) {
        Message msg = new Message(
            "PARTICIPANT ADDED: " + user.getUserID() + " TO " + conversationID,
            "Server"
        );
        Wrapper wrapper = new Wrapper(msg, ResponseType.PARTICIPANT_ADDED);

        sendToConversationParticipantsExcept(conversationID, wrapper, currentUserID);
    }

    public void broadcastParticipantRemoved(String conversationID, User user, String currentUserID) {
        Message msg = new Message(
            "PARTICIPANT REMOVED: " + user.getUserID() + " FROM " + conversationID,
            "Server"
        );
        Wrapper wrapper = new Wrapper(msg, ResponseType.PARTICIPANT_REMOVED);

        sendToConversationParticipantsExcept(conversationID, wrapper, currentUserID);
    }


    private void sendToAllExcept(Wrapper wrapper, String excludedUserID) {
        for (Map.Entry<String, ClientHandler> entry : Server.getActiveUserList().entrySet()) {
            String userID = entry.getKey();
            ClientHandler client = entry.getValue();

            if (userID.equals(excludedUserID)) {
                continue;
            }

            try {
                client.sendToClient(wrapper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    //this
    private void sendToConversationParticipantsExcept(String conversationID, Wrapper wrapper, String excludedUserID) {
        Conversation conversation = Server.getConversation(conversationID);

        if (conversation == null) {
            return;
        }

        HashSet<String> participants = conversation.getParticipants();

        for (String userID : participants) {
            // Skip the actor
            if (userID.equals(excludedUserID)) {
                continue;
            }

            ClientHandler handler = Server.getActiveClient(userID);

            // Skip offline users
            if (handler == null) {
                continue;
            }

            try {
                handler.sendToClient(wrapper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } 
    // Notifies all online participants that a new conversation was created.
    // The creator is skipped because they already initiated the action.  
    public void broadcastNewConversation(Conversation conversation, String creatorID) {
        Wrapper wrapper = new Wrapper(conversation, ResponseType.CONVERSATION_SENT);

        for (String userID : conversation.getParticipants()) {
            // Skip the user who created the conversation
            if (userID.equals(creatorID)) {
                continue;
            }

            ClientHandler handler = Server.getActiveClient(userID);

            // Only send to online users
            if (handler == null) {
                continue;
            }

            try {
                handler.sendToClient(wrapper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } 
}

