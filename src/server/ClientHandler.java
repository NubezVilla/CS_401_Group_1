package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;



import model.Message;
import model.RequestType;
import model.ResponseType;
import model.User;
import model.Wrapper;

public class ClientHandler implements Runnable {
	//networking
	private Socket clientSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in; 
	//User Data
	private boolean isLoggedIn = false;
	private boolean isIT = false;
	private User userAccount;
	private Message msg;
	private ArrayList<Message> logQueue;
	private String activeConversationID;
	//Handles
	AuthenticateHandler authenticateHandle;
	MessageHandler messageHandle;
	ConversationHandler conversationHandle; 
	
	
	public ClientHandler(Socket socket) {
		this.clientSocket = socket;
	}
	
	
	//The run() method is what will execute after the thread spawns
	@Override
	public void run() {
		System.out.println("Connected: " + clientSocket);

		
		try {
			/*
			 * I have to test receiving objects over the socket.
			 * I have to be able to get the type of request from the wrapper
			 * and be able to appropriately handle the request. 
			 */
			//get the object output stream
			//this is to send objects over the socket
			//output
	        OutputStream outputStream = clientSocket.getOutputStream();
	        out = new ObjectOutputStream(outputStream);
	        //input
	        InputStream inputStream = clientSocket.getInputStream();
	        in = new ObjectInputStream(inputStream);
	        
	        logQueue = new ArrayList<>();
			
			/*
			 * Login must be its own while loop. 
			 * It must check for isLoggedIn and the type.
			 */
	        //make the necessary handle objects
	        authenticateHandle = new AuthenticateHandler(out, in);
			messageHandle = new MessageHandler(out);
	        conversationHandle = new ConversationHandler(out, in);
			while(!isLoggedIn) {
				
				Wrapper expectedLoginRequest = (Wrapper) in.readObject();
				//check login credentials
				this.isLoggedIn = authenticateHandle.handleLogin(expectedLoginRequest);
				//if credentials correct, get their account
				if(isLoggedIn) {
					this.userAccount = authenticateHandle.getUserAccount();
					//check if the user is an IT user
					isIT = userAccount.isIT();
					Server.registerActiveUser(userAccount.getUserID(), this);
					Broadcast broadcast = new Broadcast();
					broadcast.broadcastLogin(userAccount);
				}
				
			}
			
			
			
			/*** SHOULD ONLY EXECUTE AFTER LOGGING IN ***/
			while(isLoggedIn) {
		        Wrapper receivedObject = (Wrapper) in.readObject();
		        
		       //the first thing the client handler should do is check the request type
		        RequestType request = receivedObject.getRequestType();
		        switch(request) {
		        /*
		         * THIS CASE IS BEING HANDLED OUTISDE THIS WHILE STATEMENT
		         * A USER ONLY NEEDS TO LOGIN ONCE.
		        	//Alejandro
			        case LOGIN:
			            System.out.println("Logging in");
			            handleLogin(out, receivedObject);
			            break;
		         */
		        
			        //Alejandro
			        case LOGOUT:
			            System.out.println("Logging out");
			            this.isLoggedIn = authenticateHandle.handleLogout(receivedObject, logQueue, this.clientSocket);
			            break;
		
			        //Riya
			        case REGISTER:
			            System.out.println("Registering user");
			            authenticateHandle.handleRegister(out, receivedObject);
			            break;
			        //Riya
			        case GET_USER_INFO:
			            System.out.println("Getting user info");
			            authenticateHandle.handleGetUserInfo(out, receivedObject);
			            break;
		
			        //Riya
			        case CREATE_CONVERSATION:
			            System.out.println("Creating conversation");
			            conversationHandle.handleCreateConversation(receivedObject, userAccount.getUserID());
			            break;
		
			        //Riya
			        case CREATE_GROUP_CONVERSATION:
			            System.out.println("Creating group conversation");
			            conversationHandle.handleCreateGroupConversation(out, receivedObject, userAccount.getUserID());
			            break;
		
			        //Alejandro
			        case GET_CONVERSATION:
			            System.out.println("Getting conversation");
			            conversationHandle.handleGetConversation(receivedObject, activeConversationID, isIT);
			            break;
		
			        //Alejandro
			        case ADD_PARTICIPANT:
			            System.out.println("Adding participant");
			            conversationHandle.handleAddParticipant(receivedObject, activeConversationID, this.userAccount.getUserID());
			            break;
		
			        //Alejandro
			        case REMOVE_PARTICIPANT:
			            System.out.println("Removing participant");
			            conversationHandle.handleRemoveParticipant(receivedObject, activeConversationID, this.userAccount.getUserID());
			            break;
		
			        //Alejandro
			        case SEND_MESSAGE:
			            System.out.println("Sending message");
			            messageHandle.handleSendMessage(receivedObject, logQueue, activeConversationID, userAccount.getUserID());
			            break;
		
			            /*** DISCUSS WITH GROUP REGARDING GET_MESSAGES AND GET_NEW_MESSAGES ***/
			        //Riya    
			       /* case GET_MESSAGES:
			            System.out.println("Getting messages");
			            messageHandle.handleGetMessages(out, receivedObject);
			            break;
		
			        //Riya    
			        case GET_NEW_MESSAGES:
			            System.out.println("Getting new messages");
			            messageHandle.handleGetNewMessages(out, receivedObject);
			            break;
			        */ 
			        //Alejandro
			        case UPDATE_ACTIVE_CONVERSATION:
			        	System.out.println("Updating active conversation");
			        	handleUpdatingActiveConversation(out, receivedObject);
			        	break;
			        	
			        //Riya
			        case QUERY_CONVERSATION_LOG:
			            System.out.println("Querying conversation log");
			            conversationHandle.handleQueryConversationLog(receivedObject, isIT);
			            break;
			         
			        case REQUEST_CONVERSATION_LOG:
			            System.out.println("Requesting full conversation log");
			            conversationHandle.handleRequestConversationLog(receivedObject, isIT);
			            break;
	
			        default:
			            System.out.println("Invalid Request");
			            break;
		        }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//end of run
	

	/** SEE AuthenticateHandler.java **/
	//private boolean handleLogin(ObjectOutputStream out, Wrapper obj)
	//private void handleLogout(ObjectOutputStream out, ObjectInputStream in, Wrapper obj)
	//private void handleRegister(ObjectOutputStream out, Wrapper obj)
	//private void handleGetUserInfo(ObjectOutputStream out, Wrapper obj)
	
	/** SEE ConversationHandler.java **/
	//private void handleCreateConversation(ObjectOutputStream out, Wrapper obj)
	//private void handleCreateGroupConversation(ObjectOutputStream out, Wrapper obj)
	//private void handleGetConversation(ObjectOutputStream out, Wrapper obj)
	//private void handleAddParticipant(ObjectOutputStream out, Wrapper obj)
	//private void handleRemoveParticipant(ObjectOutputStream out, Wrapper obj)
	
	/** SEE MessageHandler.java **/
	//private void handleSendMessage(ObjectOutputStream out, Wrapper obj)
	//private void handleGetMessages(ObjectOutputStream out, Wrapper obj)
	//private void handleGetNewMessages(ObjectOutputStream out, Wrapper obj)
	
	private void handleUpdatingActiveConversation(ObjectOutputStream out, Wrapper obj) {
		/*
		 * The User has switched to a different active conversation.
		 * The conversationID must be updated to be able to get
		 * the correct user to send messages to
		 */
		String updatedConversationID = null;
		if (!(obj.getPayload() instanceof String)) {
	        msg = new Message("INVALID PAYLOAD: ", "Server");
	        Wrapper incorrectPayloadResponse = new Wrapper(msg, ResponseType.MESSAGE_NOT_SENT);
	        try {
	            out.writeObject(incorrectPayloadResponse);
	            out.flush();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return;
		}
		else {
			updatedConversationID = (String) obj.getPayload();
		}
		activeConversationID = updatedConversationID;
		msg = new Message("ACTIVE CONVERSATION UPDATED", "Server");
		Wrapper objectToSend = new Wrapper(msg, ResponseType.ACTIVE_CONVERSATION_UPDATED);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/*** END REQUEST HANDLING ***/
	
	/*** HELPER METHODS FOR CLIENT ***/
	
	//this is how to safely send to clients on threads
	public synchronized void sendToClient(Wrapper obj) throws IOException {
	    out.writeObject(obj);
	    out.flush();
	}
	
	public String getUserID() {
		return userAccount.getUserID();
	}
	
	public User getUserAccount() {
		return userAccount;
	}
	
	
}//end of class
