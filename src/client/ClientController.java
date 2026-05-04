package client;

import java.util.ArrayList;

import GUI.ClientCalls;
import client.Client.ClientRunner;
import model.*;

public class ClientController implements ClientCalls {
    private final ClientRunner runner;
    private final Object requestLock = new Object();
    private final Object responseLock = new Object();
    private Wrapper response = null;

    private User loggedUser;
    private DataModel convo;
    private ClientUpdateListener updateListener;

    private static final long RESPONSE_TIMEOUT_MS = 10_000;

    public ClientController(ClientRunner run) {
        runner = run;
        convo = new DataModel();
    }

    public void setUpdateListener(ClientUpdateListener listener) {
        this.updateListener = listener;
    }

    public void handleIncoming(Wrapper data) {
        if (data == null || data.getResponseType() == null) return;

        switch (data.getResponseType()) {
            case LOGIN_SUCCESS: 
            case LOGIN_FAIL:
            case LOGOUT_SUCCESS:
            case LOGOUT_FAIL:
            case USER_INFO_SENT: //To search a specific user information
            case USER_INFO_NOT_SENT: //Wrong payload (not likely to happen because of the use of a gui)
            case REGISTER_USER_SUCCESS:
            case REGISTER_USER_FAIL: //Wrong payload or duplicate login info
            case CREATE_CONVERSATION_SUCCESS:
            case CREATE_CONVERSATION_FAIL: //Wrong payload
            case GROUP_CREATION_SUCCESS:
            case GROUP_CREATION_FAIL: //Wrong payload
            case ADD_PARTICIPANT_SUCCESS:
            case ADD_PARTICIPANT_FAIL: //Wrong payload or user is in the conversation or doesn't exist
            case REMOVE_PARTICIPANT_SUCCESS:
            case REMOVE_PARTICIPANT_FAIL: //Wrong payload or user isn't in the conversation or doesn't exist
            case GROUP_NAME_CHANGED:
           // case CONVERSATIONS_FOUND:
            case ACTIVE_CONVERSATION_UPDATED:
                deliverResponse(data);
                break;

            case CONVERSATION_SENT: //Send conversation to client
                handleIncomingConversation((Conversation) data.getPayload());
                break;
            case DATA_RECEIVED: //To update user information
                handleIncomingData(data.getPayload());
                break;
            case MESSAGE_SENT: //Send message to client
            case CONVERSATION_NOT_SENT:
            case DATA_NOT_RECEIVED:
                if (updateListener != null) {
                    updateListener.onAck(data.getResponseType(), data.getPayload());
                } else {
                    System.out.println("Ack: " + data.getResponseType());
                }
                break;

            case SENDING_MESSAGE:
            case SENDING_CONVERSATIONS:
                System.out.println("Server status: " + data.getResponseType());
                break;

            default:
                System.out.println("Unhandled response type: " + data.getResponseType());
                break;
        }
    }

    private void handleIncomingConversation(Conversation c) {
        if (c == null) return;
        convo.addConversationToList(c);
        if (updateListener != null) updateListener.onConversationReceived(c);
    }

    private void handleIncomingData(Object payload) {
        if (updateListener != null) updateListener.onDataReceived(payload);
    }

    public void deliverResponse(Wrapper newResponse) {
        synchronized (responseLock) {
            response = newResponse;
            responseLock.notifyAll();
        }
    }


    @Override
    public boolean loginAttempt(String username, String password) {
        LoginInfo information = new LoginInfo(username, password);
        Wrapper resp = sendAndWait(information, RequestType.LOGIN);
        if (resp == null) return false;
        if (resp.getResponseType() == ResponseType.LOGIN_SUCCESS) {
            loggedUser = (User) resp.getPayload();
            convo.setCurrentUser(loggedUser);
            return true;
        }
        return false;
    }

    @Override
    public void logoutAttempt() {
        if (loggedUser == null) return;
        sendAndWait(loggedUser, RequestType.LOGOUT);
        // Whether or not the server confirmed, we clear local state.
        loggedUser = null;
        convo.setCurrentUser(null);
    }

    
    @Override
    public User getUserByID(String id) {
        Wrapper resp = sendAndWait(id, RequestType.GET_USER_INFO);
        if (resp == null) return null;
        if (resp.getResponseType() == ResponseType.USER_INFO_SENT) {
            return (User) resp.getPayload();
        }
        return null;
    }

    @Override
    public ArrayList<User> searchUsers(String matching) {
        Wrapper resp = sendAndWait(matching, RequestType.GET_USER_INFO);
        if (resp == null) return new ArrayList<>();
        if (resp.getResponseType() == ResponseType.USER_INFO_SENT) {
            @SuppressWarnings("unchecked")
            ArrayList<User> result = (ArrayList<User>) resp.getPayload();
            return result;
        }
        return new ArrayList<>();
    }

    @Override
    public Boolean updateUser(User target, String newName, String newPosition,
                              String newUsername, String newPassword) {
        target.setLoginInfo(newUsername, newPassword);
        target.setName(newName);
        target.setPosition(newPosition);

        Wrapper resp = sendAndWait(target, RequestType.REGISTER);
        if (resp == null) return false;
        return resp.getResponseType() == ResponseType.REGISTER_USER_SUCCESS;
    }

    @Override
    public Boolean createNewUser(String name, String position, String username, String password) {
        User thisUser = new User(username, password);
        thisUser.setName(name);
        thisUser.setPosition(position);

        Wrapper resp = sendAndWait(thisUser, RequestType.REGISTER);
        if (resp == null) return false;
        return resp.getResponseType() == ResponseType.REGISTER_USER_SUCCESS;
    }

    @Override
    public Boolean createNewITUser(String name, String position, String username, String password) {
        User thisUser = new User(username, password);
        thisUser.setName(name);
        thisUser.setPosition(position);
        thisUser.setIT(true);

        Wrapper resp = sendAndWait(thisUser, RequestType.REGISTER);
        if (resp == null) return false;
        return resp.getResponseType() == ResponseType.REGISTER_USER_SUCCESS;
    }


    @Override
    public void updateCurrentConversation(String id) {
        Wrapper resp = sendAndWait(id, RequestType.GET_CONVERSATION);
        if (resp != null && resp.getResponseType() == ResponseType.ACTIVE_CONVERSATION_UPDATED) {
            Conversation conversation = (Conversation) resp.getPayload();
            convo.setCurrentConversation(Integer.valueOf(conversation.getID()));
        }
    }

    @Override
    public void sendMessage(String text) {
        Message newMessage = new Message(text, convo.getCurrentUser().getUserID());
        convo.getCurrentConversation().addMessage(newMessage);
        runner.send(newMessage, RequestType.SEND_MESSAGE);
    }

    @Override
    public Conversation startNewConversation(User other) {
        Wrapper resp = sendAndWait(other, RequestType.CREATE_CONVERSATION);
        if (resp == null) return null;
        if (resp.getResponseType() == ResponseType.CREATE_CONVERSATION_SUCCESS) {
            Conversation newConvo = (Conversation) resp.getPayload();
            convo.addConversationToList(newConvo);
            return newConvo;
        }
        return null;
    }

    @Override
    public GroupConversation startNewGroupConversation(Conversation c) {
        Wrapper resp = sendAndWait(c, RequestType.CREATE_GROUP_CONVERSATION);
        if (resp == null) return null;
        if (resp.getResponseType() == ResponseType.GROUP_CREATION_SUCCESS) {
            GroupConversation group = (GroupConversation) resp.getPayload();
            convo.addConversationToList(group);
            return group;
        }
        return null;
    }


    @Override
    public void addUserToGroupChat(User u) {
        Conversation current = convo.getCurrentConversation();
        if (!(current instanceof GroupConversation)) return;

        Wrapper resp = sendAndWait(u, RequestType.ADD_PARTICIPANT);
        if (resp != null && resp.getResponseType() == ResponseType.ADD_PARTICIPANT_SUCCESS) {
            current.addParticipant(u.getUserID());
        }
    }

    @Override
    public void removeUserFromGroupChat(User u) {
        Conversation current = convo.getCurrentConversation();
        if (!(current instanceof GroupConversation)) return;

        Wrapper resp = sendAndWait(u, RequestType.REMOVE_PARTICIPANT);
        if (resp != null && resp.getResponseType() == ResponseType.REMOVE_PARTICIPANT_SUCCESS) {
            ((GroupConversation) current).removeParticipant(u.getUserID());
        }
    }

    @Override
    public void setGroupChatName(String name) {
        Conversation current = convo.getCurrentConversation();
        if (!(current instanceof GroupConversation)) return;

        Wrapper resp = sendAndWait(name, RequestType.CHANGE_GROUP_NAME);
        if (resp != null && resp.getResponseType() == ResponseType.GROUP_NAME_CHANGED) {
            ((GroupConversation) current).setName(name);
        }
    }


    @Override
    public ArrayList<Conversation> queryConversationLogsByUser(User u) {
        Wrapper resp = sendAndWait(u, RequestType.QUERY_CONVERSATION_LOG_BY_USER);
        if (resp == null) return new ArrayList<>();
        if (resp.getResponseType() == ResponseType.CONVERSATION_LOG_QUERY_RESULT) {
            @SuppressWarnings("unchecked")
            ArrayList<Conversation> result = (ArrayList<Conversation>) resp.getPayload();
            return result;
        }
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Conversation> queryConversationLogsByID(String id) {
        Wrapper resp = sendAndWait(id, RequestType.QUERY_CONVERSATION_LOG_BY_ID); 
        if (resp == null) return new ArrayList<>();
        if (resp.getResponseType() == ResponseType.CONVERSATION_LOG_QUERY_RESULT) {
            @SuppressWarnings("unchecked")
            ArrayList<Conversation> result = (ArrayList<Conversation>) resp.getPayload();
            return result;
        }
        return new ArrayList<>();
    }

    @Override
    public Conversation requestConversationLogById(String id) {
        Wrapper resp = sendAndWait(id, RequestType.REQUEST_CONVERSATION_LOG);
        if (resp == null) return null;
        if (resp.getResponseType() == ResponseType.CONVERSATION_SENT) {
            return (Conversation) resp.getPayload();
        }
        return null;
    }

    @Override
    public void updateCurrentLog(String id) {
        convo.setCurrentLog(Integer.valueOf(id));
    }

    private Wrapper sendAndWait(Object payload, RequestType type) {
        synchronized (requestLock) {
            synchronized (responseLock) {
                response = null;
            }
            runner.send(payload, type);
            return waitForResponse();
        }
    }

    private Wrapper waitForResponse() {
        synchronized (responseLock) {
            long deadline = System.currentTimeMillis() + RESPONSE_TIMEOUT_MS;
            while (response == null) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) return null;
                try {
                    responseLock.wait(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            Wrapper newResponse = response;
            response = null;
            return newResponse;
        }
    }


    public interface ClientUpdateListener {
        void onConversationReceived(Conversation c);
        void onDataReceived(Object payload);
        void onAck(ResponseType type, Object payload);
    }
}