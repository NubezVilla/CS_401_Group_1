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
    private static final long RESPONSE_TIMEOUT_MS = 10_000;

    public ClientController(ClientRunner run) {
        runner = run;
        convo = new DataModel();
    }
    
    public void parseWrapper(Wrapper data) {
        if (data == null || data.getResponseType() == null) return;

        switch (data.getResponseType()) {
            case LOGIN_SUCCESS, LOGIN_FAIL: 
            case LOGOUT_SUCCESS, LOGOUT_FAIL:
            case REGISTER_USER_SUCCESS, REGISTER_USER_FAIL:
            case USER_INFO_SENT, USER_INFO_NOT_SENT: 
            case MESSAGE_SENT, MESSAGE_NOT_SENT:
            case ADD_PARTICIPANT_SUCCESS, ADD_PARTICIPANT_FAIL: 
            case REMOVE_PARTICIPANT_SUCCESS, REMOVE_PARTICIPANT_FAIL: 
            case GROUP_CREATION_SUCCESS, GROUP_CREATION_FAIL:
            case CREATE_CONVERSATION_SUCCESS, CREATE_CONVERSATION_FAIL: 
            case CONVERSATION_SENT, CONVERSATION_NOT_SENT:
            case ACTIVE_CONVERSATION_UPDATED:
            case UPDATED_USER_RECEIVED, UPDATED_USER_NOT_RECEIVED:
            case CONVERSATION_LOG_QUERY_RESULT:
            case LIST_OF_USERS_SIMILAR:
            	deliverResponse(data);
            	break;
            	
            case SENDING_MESSAGE:
            	getMessage((Envelope) data.getPayload());
            	break;
            case GROUP_NAME_CHANGED:
            	changeGroupName((GroupConversation) data.getPayload());
            	break;
            case SENDING_CONVERSATIONS:
            	Object conversations = data.getPayload();
            	if(conversations instanceof ArrayList<?> list) {
            		ArrayList<Conversation> result = new ArrayList<>();
            		for(Object o: list) {
            			if(o instanceof Conversation c) {
            				result.add(c);
            			}
            		}
            		getConversations(result);
            	}
            	break;
            case SENDING_ACTIVE_USERS:
            	Object users = data.getPayload();
            	if(users instanceof ArrayList<?> list) {
            		ArrayList<User> result = new ArrayList<>();
            		for(Object o: list) {
            			if(o instanceof User u) {
            				result.add(u);
            			}
            		}
            		getActiveUsers(result);
            	}
            	break;
            case USER_LOGGED_IN, USER_LOGGED_OUT:
            	updateActiveUsers(data);
            	break;
            case PARTICIPANT_ADDED, PARTICIPANT_REMOVED:
            	groupConversationParticipantChanged(data);
            	break;
            default:
                System.out.println("Unhandled response type: " + data.getResponseType());
                break;
        }
    }
    
    
    
    
    @Override
    public boolean loginAttempt(String username, String password) {
        LoginInfo information = new LoginInfo(username, password);
        Wrapper resp = sendAndWait(information, RequestType.LOGIN);
        if(resp == null) {
        	return false;
        }
        if(resp.getResponseType() == ResponseType.LOGIN_SUCCESS) {
            loggedUser = (User) resp.getPayload();
            convo.setCurrentUser(loggedUser);
            return true;
        }
        return false;
    }

    
    
    
    @Override
    public void logoutAttempt() {
        if(loggedUser == null) return;
        Wrapper resp = sendAndWait(loggedUser, RequestType.LOGOUT);
        if(resp.getResponseType() != ResponseType.LOGOUT_SUCCESS){
        	if(resp.getResponseType() == ResponseType.LOGOUT_FAIL) {
        		return;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.LOGOUT_SUCCESS) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return;
        	}
        }
        loggedUser = null;
        convo.setCurrentUser(null);
    }
    

    
    
    @Override
    public Boolean createNewUser(String name, String position, String username, String password) {
        LoginInfo thisUser = new LoginInfo(username, password);
        thisUser.setName(name);
        thisUser.setPosition(position);
        Wrapper resp = sendAndWait(thisUser, RequestType.REGISTER);
        if(resp == null) {
        	return false;
        }
        if(resp.getResponseType() == ResponseType.REGISTER_USER_SUCCESS) {
            loggedUser = (User) resp.getPayload();
            convo.setCurrentUser(loggedUser);
            return true;
        }
        return false;
    }
    
    
    
    
    @Override
    public Boolean createNewITUser(String name, String position, String username, String password) {
        User thisUser = new User(username, password);
        thisUser.setName(name);
        thisUser.setPosition(position);
        Wrapper resp = sendAndWait(thisUser, RequestType.REGISTER);
        if(resp == null) {
        	return false;
        }
        if(resp.getResponseType() == ResponseType.REGISTER_USER_SUCCESS) {
            loggedUser = (User) resp.getPayload();
            convo.setCurrentUser(loggedUser);
            return true;
        }
        return false;
    }
    
    
    
    
    @Override
    public User getUserByID(String id) {
        Wrapper resp = sendAndWait(id, RequestType.GET_USER_INFO);
        if(resp.getResponseType() != ResponseType.USER_INFO_SENT){
        	if(resp.getResponseType() == ResponseType.USER_INFO_NOT_SENT) {
        		return null;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.USER_INFO_SENT) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return null;
        	}
        }
        return (User)resp.getPayload();
    }
    
    
    
    
    @Override
    public void sendMessage(String text) {
    	Message newMessage = new Message(text, convo.getCurrentUser().getUserID());
    	Wrapper resp = sendAndWait(newMessage, RequestType.SEND_MESSAGE);
        if(resp.getResponseType() != ResponseType.MESSAGE_SENT){
        	if(resp.getResponseType() == ResponseType.MESSAGE_NOT_SENT) {
        		return;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.MESSAGE_SENT) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return;
        	}
        }
        convo.getCurrentConversation().addMessage(newMessage);
    }
    
    
    
    
    public void getMessage(Envelope E) {
    	convo.addMessageToConversation(convo.getConversationAtIndex(convo.findConversationIndex(E.getID())), E.getMessage());
    	loggedUser.addUnreadConversation(E.getID());
    }
    
    
    
    
    @Override
    public void addUserToGroupChat(User u) {
        if(!(convo.getCurrentConversation() instanceof GroupConversation)) {
        	return;
        }
        Wrapper resp = sendAndWait(u, RequestType.ADD_PARTICIPANT);
        if(resp.getResponseType() != ResponseType.ADD_PARTICIPANT_SUCCESS){
        	if(resp.getResponseType() == ResponseType.ADD_PARTICIPANT_FAIL) {
        		return;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.ADD_PARTICIPANT_SUCCESS) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return;
        	}
        }
        convo.getCurrentConversation().addParticipant(u.getUserID());;
    }
    
    
    

    @Override
    public void removeUserFromGroupChat(User u) {
    	if(!(convo.getCurrentConversation() instanceof GroupConversation)) {
        	return;
        }
        Wrapper resp = sendAndWait(u, RequestType.REMOVE_PARTICIPANT);
        if(resp.getResponseType() != ResponseType.REMOVE_PARTICIPANT_SUCCESS){
        	if(resp.getResponseType() == ResponseType.REMOVE_PARTICIPANT_FAIL) {
        		return;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.REMOVE_PARTICIPANT_SUCCESS) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return;
        	}
        }
        convo.getCurrentConversation().removeParticipant(u.getUserID());;
    }
    
    
    
    
    @Override
    public GroupConversation startNewGroupConversation(Conversation c) {
        Wrapper resp = sendAndWait(c, RequestType.CREATE_GROUP_CONVERSATION);
        if(resp.getResponseType() != ResponseType.GROUP_CREATION_SUCCESS){
        	if(resp.getResponseType() == ResponseType.GROUP_CREATION_FAIL) {
        		return null;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.GROUP_CREATION_SUCCESS) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return null;
        	}
        }
        GroupConversation newConversation =(GroupConversation) resp.getPayload();
        convo.addConversationToList(newConversation);
        return newConversation;
    }
    
    
    
    
    @Override
    public Conversation startNewConversation(User other) {
    	Conversation newConversation = new Conversation(loggedUser.getUserID(), other.getUserID());
    	Wrapper resp = sendAndWait(other, RequestType.CREATE_CONVERSATION);
        if(resp.getResponseType() != ResponseType.CREATE_CONVERSATION_SUCCESS){
        	if(resp.getResponseType() == ResponseType.CREATE_CONVERSATION_FAIL) {
        		return null;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.CREATE_CONVERSATION_SUCCESS) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return null;
        	}
        }
        convo.addConversationToList(newConversation);
        return newConversation;
    }

    

    
    @Override
    public Conversation requestConversationLogById(String id) {
    	Wrapper resp = sendAndWait(id, RequestType.GET_CONVERSATION);
        if(resp.getResponseType() != ResponseType.CONVERSATION_SENT){
        	if(resp.getResponseType() == ResponseType.CONVERSATION_NOT_SENT) {
        		return null;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.CONVERSATION_SENT) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return null;
        	}
        }
        Conversation requestedConversation = (Conversation) resp.getPayload();
        convo.setCurrentLog(requestedConversation);
        return requestedConversation;
    }
    
    
    
    
    
    @Override
    public void updateCurrentConversation(String id) {
        Wrapper resp = sendAndWait(id, RequestType.GET_CONVERSATION);
        if(resp.getResponseType() != ResponseType.ACTIVE_CONVERSATION_UPDATED){
        	while(resp != null && resp.getResponseType() != ResponseType.ACTIVE_CONVERSATION_UPDATED) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return;
        	}
        }
        convo.setCurrentConversation(convo.findConversationIndex(id));
    }


    
    
    
    public void getConversations(ArrayList<Conversation> Ar){
    	for(int x = 0; x < Ar.size(); x++) {
    		convo.addConversationToList(Ar.get(x));
    	}
    }
    
    
    
    
    public void getActiveUsers(ArrayList<User> Ar) {
    	convo.addActiveUsers(Ar);
    }
    
    
    
    
    @Override
    public Boolean updateUser(User target, String newName, String newPosition, String newUsername, String newPassword) {
        User newUser = target;
    	newUser.setLoginInfo(newUsername, newPassword);
        newUser.setName(newName);
        newUser.setPosition(newPosition);
        Wrapper resp = sendAndWait(newUser, RequestType.UPDATE_USER_INFO);
        if(resp.getResponseType() != ResponseType.UPDATED_USER_RECEIVED){
        	if(resp.getResponseType() == ResponseType.UPDATED_USER_NOT_RECEIVED) {
        		return null;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.UPDATED_USER_RECEIVED) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return null;
        	}
        }
        target.setLoginInfo(newUsername, newPassword);
        target.setName(newName);
        target.setPosition(newPosition);
        return true;
    }
    
    
    
    
    public void updateActiveUsers(Wrapper w) {
    	if(w.getResponseType() == ResponseType.USER_LOGGED_IN) {
    		User ActiveUser = (User)w.getPayload();
    		convo.addActiveUser(ActiveUser);
    	}
    	else if(w.getResponseType() == ResponseType.USER_LOGGED_OUT) {
    		User InactiveUser = (User)w.getPayload();
    		convo.removeActiveUser(InactiveUser);
    	}
    }
    
    
    
    
    public void groupConversationParticipantChanged(Wrapper c) {
        GroupConversation participantChanged = (GroupConversation) c.getPayload();
        String ID = participantChanged.getID();
        int idx = convo.findConversationIndex(ID);
        convo.getConversationAtIndex(idx).clearParticipants();
        convo.getConversationAtIndex(idx).addParticipants(participantChanged.getParticipants());	
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
    
    
    
    
    public void changeGroupName(GroupConversation c) {
    	String ID = c.getID();
    	int idx = convo.findConversationIndex(ID);
    	GroupConversation changedName = convo.getGroupConversationAtIndex(idx);
    	if(convo != null) {
    		changedName.setName(c.getName());
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
    public ArrayList<User> searchUsers(String matching) {
        Wrapper resp = sendAndWait(matching, RequestType.GET_USER_INFO);
        if(resp == null) {
        	return new ArrayList<>();
        }
        if(resp.getResponseType() == ResponseType.USER_INFO_SENT) {
            @SuppressWarnings("unchecked")
            ArrayList<User> result = (ArrayList<User>) resp.getPayload();
            return result;
        }
        return new ArrayList<>();
    }

    
    
    
    @Override
    public void updateCurrentLog(String id) {
    	Wrapper resp = sendAndWait(id, RequestType.GET_CONVERSATION);
        if(resp.getResponseType() != ResponseType.CONVERSATION_SENT){
        	if(resp.getResponseType() == ResponseType.CONVERSATION_NOT_SENT) {
        		return ;
        	}
        	while(resp != null && resp.getResponseType() != ResponseType.CONVERSATION_SENT) {
        		resp = waitForResponse();
        	}
        	if(resp == null) {
        		return;
        	}
        }
        if(resp.getPayload() instanceof GroupConversation) {
        	GroupConversation groupLog = (GroupConversation) resp.getPayload();
        	convo.setCurrentLog(groupLog);
        	
        }
        else {
        	Conversation groupLog = (Conversation) resp.getPayload();
        	convo.setCurrentLog(groupLog);
        }
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

    public void deliverResponse(Wrapper newResponse) {
        synchronized (responseLock) {
            response = newResponse;
            responseLock.notifyAll();
        }
    }
}