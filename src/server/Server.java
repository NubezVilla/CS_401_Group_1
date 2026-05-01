package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import model.Conversation;
import model.GroupConversation;
import model.Message;
import model.User;


public class Server {
	private static int port = 54927;
	private boolean isConnected;
	private static FileManager fileManagerHandle;
	private static UserData userDataHandle;
	private static ConcurrentHashMap<String, ClientHandler> activeUserList = new ConcurrentHashMap<>();//userID, Client

	
	public static void main(String[] args) throws Exception {
		/* There has to be a lot of setup before the server can start listening.
		 * The server needs to load all user files into the data structures inside UserData.
		 */
		userDataHandle = new UserData();
		fileManagerHandle = new FileManager();
		//fileManagerHandle.loadData(userDataHandle);
		

		
		
		try (var listener = new ServerSocket(port)) {
			System.out.println("Server is running...");
	
            var clientThread = Executors.newFixedThreadPool(100);
            while (true) {
                clientThread.execute(new ClientHandler(listener.accept()));
            }
        }
	
    }//end main
	
	
	public static void registerActiveUser(String userID, ClientHandler client) {
		activeUserList.put(userID, client);
	}
	
	public static void removeActiveUser(String user) {
		activeUserList.remove(user);
	}
	
	public static ClientHandler getActiveClient(String user) {
		return activeUserList.get(user);
	}
	
	public static ConcurrentHashMap<String, ClientHandler> getActiveUserList() {
	    return activeUserList;
	}
	
	/*** FileManager class methods ***/
	public static void saveUserData(User user) {
		//save the user data to disk with the FileManager
		try {
			fileManagerHandle.saveUser(user);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveConversation(Conversation conversation) {
		//save to conversation to disk using FileManger
		try {
			fileManagerHandle.saveConversation(conversation);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveLogQueue(String userID, ArrayList<Message> logQueue) {
		try {
			fileManagerHandle.saveLogQueue(userID, logQueue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*** UserData class methods ***/
	public static User getUserData(int loginHashCode) {
		return userDataHandle.getUserByLoginHash(loginHashCode);
	}
	
	public static User getUserbyID(User user) {
		return userDataHandle.getUserById(user.getUserID());
	}
	
	public static Conversation getActiveConversation(String activeConversationID) {
		return userDataHandle.getConversation(activeConversationID);
	}
	
	public static void updateUnreadMessage(String userID, String activeConversationID) {
		userDataHandle.updateUnreadMessage(userID, activeConversationID);
	}
	

	/*** 
	 * TEST HELPER METHODS 
	 * These are for JUnit testing. 
	 * ***/
	public static void setTestUserData(UserData testData) {
		userDataHandle = testData;
	}
	
	public static void setTestFileManager(FileManager testManager) {
		fileManagerHandle = testManager;
	}
	
	
}
