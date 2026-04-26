package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import model.LoginInfo;
import model.Message;
import model.RequestType;
import model.User;
import model.Wrapper;

public class ClientHandler implements Runnable {
	private Socket clientSocket;
	private RequestType request;
	private Server server;
	private boolean isLoggedIn;
	private User userAccount;
	private Message msg;
	private Message logQueue[];
	private int conversationID;
	User acc = new User("User1", "1234");//username, password
	
	public ClientHandler(Socket socket) {
		this.clientSocket = socket;
	}
	
	
	//The run() method is what will execute after the thread spawns
	@Override
	public void run() {
		System.out.println("Connected: " + clientSocket);
		//simulate user accounts to test login 
		
		
		
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
	        ObjectOutputStream out = new ObjectOutputStream(outputStream);
	        //input
	        InputStream inputStream = clientSocket.getInputStream();
	        ObjectInputStream in = new ObjectInputStream(inputStream);
			while(true) {
			
		        
		        Wrapper receivedObject = (Wrapper) in.readObject();
		        
		       //the first thing the client handler should do is check the request type
		        RequestType request = receivedObject.getRequestType();
		        switch(request) {
		        	//Alejandro
			        case LOGIN:
			            System.out.println("Logging in");
			            handleLogin(out, receivedObject);
			            break;
		
			        //Alejandro
			        case LOGOUT:
			            System.out.println("Logging out");
			            handleLogout(out, receivedObject);
			            break;
		
			        //Riya
			        case REGISTER:
			            System.out.println("Registering user");
			            handleRegister(out, receivedObject);
			            break;
			        //Riya
			        case GET_USER_INFO:
			            System.out.println("Getting user info");
			            handleGetUserInfo(out, receivedObject);
			            break;
		
			        //Riya
			        case CREATE_CONVERSATION:
			            System.out.println("Creating conversation");
			            handleCreateConversation(out, receivedObject);
			            break;
		
			        //Riya
			        case CREATE_GROUP_CONVERSATION:
			            System.out.println("Creating group conversation");
			            handleCreateGroupConversation(out, receivedObject);
			            break;
		
			        //Alejandro
			        case GET_CONVERSATION:
			            System.out.println("Getting conversation");
			            handleGetConversation(out, receivedObject);
			            break;
		
			        //Alejandro
			        case ADD_PARTICIPANT:
			            System.out.println("Adding participant");
			            handleAddParticipant(out, receivedObject);
			            break;
		
			        //Alejandro
			        case REMOVE_PARTICIPANT:
			            System.out.println("Removing participant");
			            handleRemoveParticipant(out, receivedObject);
			            break;
		
			        //Alejandro
			        case SEND_MESSAGE:
			            System.out.println("Sending message");
			            handleSendMessage(out, receivedObject);
			            break;
		
			        //Riya    
			        case GET_MESSAGES:
			            System.out.println("Getting messages");
			            handleGetMessages(out, receivedObject);
			            break;
		
			        //Riya    
			        case GET_NEW_MESSAGES:
			            System.out.println("Getting new messages");
			            handleGetNewMessages(out, receivedObject);
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
	
	private void handleLogin(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * The obj has the login credentials that must be checked.
		 * UserData should have all of the users in the system along 
		 * with their user names and passwords. 
		 */
		//check hash codes for equivalence
		LoginInfo existingAccountInfo = acc.getLoginInfo();
		LoginInfo loginRequest = (LoginInfo) obj.getPayload();
		boolean loginSuccess = existingAccountInfo.equals(loginRequest);
		System.out.println("Login success: " + loginSuccess);
		msg = new Message("LOGGING IN", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.LOGIN);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void handleLogout(ObjectOutputStream out, Wrapper obj) {
		/*
		 * This method must save all of the users data to a file.
		 * The user data includes all messages that have been sent
		 * since start up to logs, conversations, new conversations, 
		 * user profile if it was updated, etc.
		 * It must close the socket and send a successful logout response.
		 */
		msg = new Message("LOGGIN OUT", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.LOGOUT);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO 
			e.printStackTrace();
		}
	}
	
	private void handleRegister(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method must create a new user. The user
		 * must be added to UserData. The new user information
		 * must be saved to file as well. This can be done here
		 * or during logout?
		 */
		msg = new Message("REGISTERING NEW USER", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.REGISTER);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void handleGetUserInfo(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method retrieves a User's information from UserData. 
		 */
		msg = new Message("RETRIEVING USER INFO", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.GET_USER_INFO);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void handleCreateConversation(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method must create a new conversation. A conversation
		 * involves 2 Users. The two User sockets must be able to send
		 * messages to each other. The socket for the clients
		 * are found in ClientList<ClientHandler, User> in the Server.
		 *  
		 */
		msg = new Message("CREAING CONVERSATION", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.CREATE_CONVERSATION);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void handleCreateGroupConversation(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method must send a message to multiple Clients. Each 
		 * GroupConversation should have a list of participants.
		 * Get the UserIDs and the correct sockets you have to
		 * pass along the message.
		 */
		msg = new Message("CREATING GROUP CONVERSATION", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.CREATE_GROUP_CONVERSATION);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void handleGetConversation(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method retrieves a conversation from a file. The conversationID
		 * is used to look up the file and load the contents. This method
		 * will have to use a separate thread for the work and return that
		 * work back to send the conversation to the correct User. 
		 * The whole conversation should not be sent.
		 */
		msg = new Message("GETTING CONVERSATION", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.GET_CONVERSATION);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void handleAddParticipant(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method should add a User to a GroupConversation. The UserID and 
		 * their ClientHandler are in the ClientList in the Server. 
		 * The UserID should be added to the participant list in the 
		 * GroupConversation.
		 */
		msg = new Message("ADDING PARTICIPANT", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.ADD_PARTICIPANT);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void handleRemoveParticipant(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method must remove a participant from a GroupConversation. 
		 * The UserID and their ClientHandler are in the 
		 * ClientList in the Server. The UserID should be removed
		 * from the participant list in the GroupConversation.
		 */
		msg = new Message("REMOVING PARTICIPANT", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.REMOVE_PARTICIPANT);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void handleSendMessage(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method has to re-route the message to the right user.
		 * It will need to go through the userList in the Server.
		 * This method needs to store the message that is being sent
		 * into the logQueue[] array to save the messages to a log later.
		 */
		msg = new Message("message recieved, back to you", "ServerID");
    	Wrapper objectToSend = new Wrapper(msg, RequestType.SEND_MESSAGE);
    	try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
	private void handleGetMessages(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * This method gets the messages of a conversation to send to a User.
		 * The ConversationID should be used to get the messages for that
		 * Conversation.  
		 */
		msg = new Message("GETTING MESSAGES", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.GET_MESSAGES);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void handleGetNewMessages(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * I am not sure the Server will need this method??
		 * This method will send unread messages to a User when they 
		 * switch to the correct conversation.  
		 */
		msg = new Message("GETTING NEW MESSAGES", "Server");
		Wrapper objectToSend = new Wrapper(msg, RequestType.GET_NEW_MESSAGES);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
}//end of class
