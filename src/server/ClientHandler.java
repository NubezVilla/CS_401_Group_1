package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

import model.Conversation;
import model.LoginInfo;
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
	private User userAccount;
	private Message msg;
	private ArrayList<Message> logQueue;
	private String activeConversationID;
	
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
	        
			while(!isLoggedIn) {
				
				Wrapper expectedLoginRequest = (Wrapper) in.readObject();
				isLoggedIn = handleLogin(out, expectedLoginRequest);
			
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
			            handleLogout(out, in, receivedObject);
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
			        //Alejandro
			        case UPDATE_ACTIVE_CONVERSATION:
			        	System.out.println("Updating active conversation");
			        	handleUpdatingActiveConversation(out, receivedObject);
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
	


	private boolean handleLogin(ObjectOutputStream out, Wrapper obj) {
		/* 
		 * The obj has the login credentials that must be checked.
		 * UserData should have all of the users in the system along 
		 * with their user names and passwords. 
		 */
		RequestType type = obj.getRequestType();
		boolean accountFound = false;
		if(type != RequestType.LOGIN) {
			msg = new Message("WRONG REQUEST TYPE\nNOT LOGGED IN", "Server");
			Wrapper objectToSend = new Wrapper(msg, ResponseType.LOGIN_FAIL);
			try {
				out.writeObject(objectToSend);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return accountFound;
		}
		else {
			if (!(obj.getPayload() instanceof LoginInfo)) {
		        msg = new Message("INVALID PAYLOAD: LOGIN EXPECTED", "Server");
		        Wrapper wrongPayloadResponse = new Wrapper(msg, ResponseType.LOGIN_FAIL);
		        try {
					out.writeObject(wrongPayloadResponse);
					out.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        return accountFound;
			}
			LoginInfo loginInfo = (LoginInfo) obj.getPayload();
			/*** NEED TO ADD ERROR HANDLING FOR INCORRECT PAYLOAD TYPES ***/
			//get the UserData form the UserData class later
			System.out.println("loginInfo code: " + loginInfo.hashCode());
			userAccount = Server.getUserData(loginInfo.hashCode());
			if(userAccount != null) {
				msg = new Message("LOGGING IN", "Server");
		        Wrapper response = new Wrapper(msg, ResponseType.LOGIN_SUCCESS);
		        isLoggedIn = true;
		        accountFound = true;
		        Server.registerActiveUser(userAccount.getUserID(), this);
		        /** THEIR ACCOUNT MUST BE LOADED INTO THE HANDLER **/
		        //userAccount = UserData.getUser();
		        try {
		            out.writeObject(response);
		            out.flush();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        /* 
		         * sendUserData needs to send the account information to the client.
		         * Three write outs will happen. 
		         * Send User account, Conversations, unread messages
		         */
		        sendUserData(userAccount);
		        return accountFound;
			}
			else
			{
				System.out.println("ACCOUNT NOT FOUND");
				msg = new Message("WRONG USER/PASS", "Server");
				Wrapper response = new Wrapper(msg, ResponseType.LOGIN_FAIL);
				try {
					out.writeObject(response);
					out.flush();
				} catch (IOException e) {
				        e.printStackTrace();
			    }
				    return accountFound;
			}
		}
	}
	
	private void handleLogout(ObjectOutputStream out, ObjectInputStream in, Wrapper obj) {
		/*
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
	        msg = new Message("INVALID LOGOUT PAYLOAD: USER REQUIRED", "Server");
	        Wrapper response = new Wrapper(msg, ResponseType.LOGOUT_FAIL);

	        try {
	            out.writeObject(response);
	            out.flush();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return;
		}
		else {
		    User updatedUser = (User) obj.getPayload(); //get user acccount
			userAccount = updatedUser; //update this User's profile
		}
    
	
		/*
		 * Assuming the read in for User is done correctly, read in
		 * the next package for the Conversations.
		 * This should be a list of Conversation objects that the user is 
		 * a participant.
		 */
		ArrayList<Conversation> updatedConversations = null;
		try {
			//assuming the next read is an Array
			Wrapper arrayObject = (Wrapper) in.readObject();
			if (!(arrayObject.getPayload() instanceof ArrayList<?>)) {
				msg = new Message("INVALID LOGOUT PAYLOAD: CONVERSATION LIST REQUIRED", "Server");
				Wrapper expectedArrayResponse = new Wrapper(msg, ResponseType.LOGOUT_FAIL);
	            out.writeObject(expectedArrayResponse);
	            out.flush();
	            return;
	        }
			else { updatedConversations = (ArrayList<Conversation>) arrayObject.getPayload();}
			}catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				msg = new Message("FAILED TO READ LOGOUT DATA", "Server");
				Wrapper caughtExceptionResponse = new Wrapper(msg, ResponseType.LOGOUT_FAIL);
				try {
		            out.writeObject(caughtExceptionResponse);
		            out.flush();
		        } catch (IOException ioException) {
		            ioException.printStackTrace();
		        }
				return;
			}
		//FileManager must be used to save files to disk
		//fileManager.saveLogs(logQueue[]);
		 try {
			 //Save updated user profile
			 Server.saveUserData(userAccount);
			 //Save each conversation
			 for (Conversation conversation : updatedConversations) {
				 Server.saveConversation(conversation);
			 }
			 //Save message logs
			 Server.saveLogQueue(userAccount.getUserID(), logQueue);
			 //Remove from active users
			 if (userAccount != null) {
				 Server.removeActiveUser(userAccount.getUserID());
			 }
			 isLoggedIn = false;
			 msg = new Message("LOGOUT SUCCESSFUL", "Server");
		     Wrapper logoutSuccessResponse = new Wrapper(msg, ResponseType.LOGOUT_SUCCESS);
		     out.writeObject(logoutSuccessResponse);
		     out.flush();
		     clientSocket.close();
		     } catch (IOException e) {
		        e.printStackTrace();

		        msg = new Message("ERROR SAVING LOGOUT DATA", "Server");
		        Wrapper response = new Wrapper(msg, ResponseType.LOGOUT_FAIL);

		        try {
		            out.writeObject(response);
		            out.flush();
		        } catch (IOException ioException) {
		            ioException.printStackTrace();
		        }
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
		 * is used to look up the file and load the contents.
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
		Message messageToSend = null;
		if (!(obj.getPayload() instanceof Message)) {
	        msg = new Message("INVALID PAYLOAD: MESSAGE", "Server");
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
			messageToSend = (Message) obj.getPayload();
		}
		System.out.println(messageToSend.getText());
		logQueue.add(messageToSend); //THIS LOG MUST BE SAVED TO DISK BEFORE LOGOUT
		
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
            msg = new Message("CONVERSATION NOT FOUND", "Server");
            Wrapper response = new Wrapper(msg, ResponseType.MESSAGE_NOT_SENT);
            try { 
            	out.writeObject(response);
            	out.flush();
            } catch (IOException e) {
            	e.printStackTrace();
            }
            return;
        }
		/* 
		 * The currentConversation is the current conversation that the user is viewing.
		 * Conversation has a list of participants to send a message to.
		 */
	    //save the message to the current conversation
	    //offline user will get the messages when they log in via Conversation object
	    currentConversation.addMessage(messageToSend);
		//get the list of participants from the conversation
		HashSet<String> conversationParticipants = currentConversation.getParticipants();
		//get the UserIDs so you can send a message to their ClientHandler.clientSocket
		for(String userID : conversationParticipants) {
			
			//if the participant is the sender, skip them
            if (userID.equals(this.getUserID())) {
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
				handler.sendToClient(new Wrapper(messageToSend, ResponseType.MESSAGE_SENT));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		msg = new Message("MESSAGE SENT", "Server");
		Wrapper objectToSend = new Wrapper(msg, ResponseType.MESSAGE_SENT);
		try {
			out.writeObject(objectToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
	//this is how to safely send to clients on threads
	public synchronized void sendToClient(Wrapper obj) throws IOException {
	    out.writeObject(obj);
	    out.flush();
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
	
	private void handleUpdatingActiveConversation(ObjectOutputStream out, Wrapper obj) {
		/*
		 * The User has switched to a different active conversation.
		 * The conversationID must be updated to be able to get
		 * the correct user to send messages to
		 */
		String updatedConversationID = (String) obj.getPayload();
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
	
	private void sendUserData(User userAccount) {
		/*
		 * I need to send the account information and conversation information to the 
		 * client.
		 * This will require to write outs
		 */
		//First, send the account information
		Wrapper sendUserAccount = new Wrapper(userAccount, ResponseType.SENDING_DATA);
		try {
			out.writeObject(sendUserAccount);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Next, i need to send a list of Conversations
		//I need to know what Conversations they belong to
		HashSet<String> conversationIDs = new HashSet<>();
		conversationIDs = userAccount.getConversations();
		ArrayList<Conversation> conversationsToSend = new ArrayList<>();
		for(String convoID : conversationIDs) {
			//i need the conversation in UserData
			conversationsToSend.add(Server.getConversation(convoID));
		}
		//send the Conversation data to the client
		Wrapper sendConversations = new Wrapper(conversationsToSend, ResponseType.SENDING_DATA);
		try {
			out.writeObject(sendConversations);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getUserID() {
		return userAccount.getUserID();
	}
	
	private User getUserAccount() {
		return userAccount;
	}
	
	
}//end of class
