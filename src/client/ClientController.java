package client;

import java.util.ArrayList;

import GUI.ClientCalls;
import client.Client.ClientRunner;
import model.*;

public class ClientController implements ClientCalls {
    private final ClientRunner runner;
    private final Object lock = new Object();
    private User loggedUser;
    private DataModel convo;
    private Wrapper response = null;

    private static final long RESPONSE_TIMEOUT_MS = 10_000;

    public ClientController(ClientRunner run) {
        runner = run;
        convo = new DataModel();
    }

    // ---------- Authentication ----------

    @Override
    public boolean loginAttempt(String username, String password) {
        LoginInfo information = new LoginInfo(username, password);
        runner.send(information, RequestType.LOGIN);

        Wrapper resp = waitForResponse();
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
        runner.send(loggedUser, RequestType.LOGOUT);

        Wrapper resp = waitForResponse();
        // Whether or not the server confirmed, we clear local state.
        loggedUser = null;
        convo.setCurrentUser(null);
    }

    // ---------- User lookup and search ----------

    @Override
    public User getUserByID(String id) {
        runner.send(id, RequestType.GET_USER_INFO);

        Wrapper resp = waitForResponse();
        if (resp == null) return null;
        if (resp.getResponseType() == ResponseType.USER_INFO_SENT) {
            return (User) resp.getPayload();
        }
        return null;
    }

    @Override
    public ArrayList<User> searchUsers(String matching) {
        runner.send(matching, RequestType.GET_USER_INFO);

        Wrapper resp = waitForResponse();
        if (resp == null) return new ArrayList<>();
        if (resp.getResponseType() == ResponseType.USER_INFO_SENT) {
            @SuppressWarnings("unchecked")
            ArrayList<User> result = (ArrayList<User>) resp.getPayload();
            return result;
        }
        return new ArrayList<>();
    }

    // ---------- User management ----------

    @Override
    public Boolean updateUser(User target, String newName, String newPosition,
                              String newUsername, String newPassword) {
    	User newData = target;
        newData.setLoginInfo(newUsername, newPassword);
        newData.setName(newName);
        newData.setPosition(newPosition);
        runner.send(target, RequestType.REGISTER);

        Wrapper resp = waitForResponse();
        if (resp == null) return false;
        if (resp.getResponseType() == ResponseType.REGISTER_USER_SUCCESS) {
            // Apply changes locally only on success
            target.setName(newName);
            target.setPosition(newPosition);
            target.setLoginInfo(newUsername, newPassword);
            return true;
        }
        return false;
    }

    @Override
    public Boolean createNewUser(String name, String position, String username, String password) {
        User thisUser = new User(username, password);
        thisUser.setName(name);
        thisUser.setPosition(position);
        runner.send(thisUser, RequestType.REGISTER);

        Wrapper resp = waitForResponse();
        if (resp == null) return false;
        return resp.getResponseType() == ResponseType.REGISTER_USER_SUCCESS;
    }

    @Override
    public Boolean createNewITUser(String name, String position, String username, String password) {
        User thisUser = new User(username, password);
        thisUser.setName(name);
        thisUser.setPosition(position);
        thisUser.setIT(true);
        runner.send(thisUser, RequestType.REGISTER);

        Wrapper resp = waitForResponse();
        if (resp == null) return false;
        return resp.getResponseType() == ResponseType.REGISTER_USER_SUCCESS;
    }

    // ---------- Conversations ----------

    @Override
    public void updateCurrentConversation(String id) {
        runner.send(id, RequestType.GET_CONVERSATION);

        Wrapper resp = waitForResponse();
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
        runner.send(other, RequestType.CREATE_CONVERSATION);

        Wrapper resp = waitForResponse();
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
        runner.send(c, RequestType.CREATE_GROUP_CONVERSATION);

        Wrapper resp = waitForResponse();
        if (resp == null) return null;
        if (resp.getResponseType() == ResponseType.GROUP_CREATION_SUCCESS) {
            GroupConversation group = (GroupConversation) resp.getPayload();
            convo.addConversationToList(group);
            return group;
        }
        return null;
    }

    // ---------- Group chat management ----------

    @Override
    public void addUserToGroupChat(User u) {
        Conversation current = convo.getCurrentConversation();
        if (!(current instanceof GroupConversation)) return;

        runner.send(u, RequestType.ADD_PARTICIPANT);

        Wrapper resp = waitForResponse();
        if (resp != null && resp.getResponseType() == ResponseType.ADD_PARTICIPANT_SUCCESS) {
            current.addParticipant(u.getUserID());;
        }
    }

    @Override
    public void removeUserFromGroupChat(User u) {
        Conversation current = convo.getCurrentConversation();
        if (!(current instanceof GroupConversation)) return;
        runner.send(u, RequestType.REMOVE_PARTICIPANT);

        Wrapper resp = waitForResponse();
        if (resp != null && resp.getResponseType() == ResponseType.REMOVE_PARTICIPANT_SUCCESS) {
            ((GroupConversation) current).removeParticipant(u.getUserID());
        }
    }

    @Override
    public void setGroupChatName(String name) {
        Conversation current = convo.getCurrentConversation();
        if (!(current instanceof GroupConversation)) return;
        
        runner.send(name, RequestType.CHANGE_GROUP_NAME);

        Wrapper resp = waitForResponse();
        if (resp != null && resp.getResponseType() == ResponseType.GROUP_NAME_CHANGED) {
            ((GroupConversation) current).setName(name);
        }
    }

    // ---------- Conversation logs ----------

    @Override
    public ArrayList<Conversation> queryConversationLogsByUser(User u) {
        runner.send(u, RequestType.FIND_CONVERSATION_BY_USER);

        Wrapper resp = waitForResponse();
        if (resp == null) return new ArrayList<>();
        if (resp.getResponseType() == ResponseType.CONVERSATIONS_FOUND) {
            @SuppressWarnings("unchecked")
            ArrayList<Conversation> result = (ArrayList<Conversation>) resp.getPayload();
            return result;
        }
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Conversation> queryConversationLogsByID(String id) {
        runner.send(id, RequestType.FIND_CONVERSATION_BY_ID);

        Wrapper resp = waitForResponse();
        if (resp == null) return new ArrayList<>();
        if (resp.getResponseType() == ResponseType.CONVERSATIONS_FOUND) {
            @SuppressWarnings("unchecked")
            ArrayList<Conversation> result = (ArrayList<Conversation>) resp.getPayload();
            return result;
        }
        return new ArrayList<>();
    }

    @Override
    public Conversation requestConversationLogById(String id) {
        runner.send(id, RequestType.GET_CONVERSATION_LOG);

        Wrapper resp = waitForResponse();
        if (resp == null) return null;
        if (resp.getResponseType() == ResponseType.CONVERSATION_LOG_RECEIVED) {
            return (Conversation) resp.getPayload();
        }
        return null;
    }

    @Override
    public void updateCurrentLog(String id) {
        convo.setCurrentLog(Integer.valueOf(id));
    }

    // ---------- Reader-side delivery ----------

    public void deliverResponse(Wrapper newResponse) {
        synchronized (lock) {
            response = newResponse;
            lock.notify();
        }
    }

    private Wrapper waitForResponse() {
        synchronized (lock) {
            long deadline = System.currentTimeMillis() + RESPONSE_TIMEOUT_MS;
            while (response == null) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) return null;
                try {
                    lock.wait(remaining);
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
}