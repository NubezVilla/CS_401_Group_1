package server;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import model.Conversation;
import model.GroupConversation;
import model.User;


public class Server {
	private static int port = 54927;
	private boolean isConnected;
	//FileManager manager
	//UserData userDataHandle
	private static ConcurrentHashMap<String, ClientHandler> activeUserList = new ConcurrentHashMap<>();
	//UserData class object
	private static ConcurrentHashMap<Integer, User> userData = new ConcurrentHashMap<>(); 
	//UserData class object
	private static ConcurrentHashMap<String, Conversation> conversations = new ConcurrentHashMap<>(); 	
	
	
	public static void main(String[] args) throws Exception {
		/* There has to be a lot of setup before the server can start listening.
		 * The server needs to load all user files into the data structures inside UserData.
		 */
		//manager.load()
		//userDataHandle.fillDataStructures()
		
		/*
		 * I need to simulate existing accounts and conversations
		 */
		//make a Conversation so simulate sending conversations
		Conversation conversation1 = new Conversation("user1", "user2");
		Conversation conversation2 = new Conversation("user2", "user3");
		Conversation conversation3 = new Conversation("user3", "user4");
		Conversation conversation4 = new Conversation("user4", "user5");
		Conversation conversation5 = new Conversation("user5", "user1");
		conversations.put(conversation1.getID(), conversation1);
		conversations.put(conversation2.getID(), conversation2);
		conversations.put(conversation3.getID(), conversation3);
		conversations.put(conversation4.getID(), conversation4);
		conversations.put(conversation5.getID(), conversation5);
		//add users to UserData to simulate existing users
		User user1 = new User("user1", "pass1");
		User user2 = new User("user2", "pass2");
		User user3 = new User("user3", "pass3");
		User user4 = new User("user4", "pass4");
		User user5 = new User("user5", "pass5");
		userData.put(user1.getLoginInfo().hashCode(), user1);
		userData.put(user2.getLoginInfo().hashCode(), user2);
		userData.put(user3.getLoginInfo().hashCode(), user3);
		userData.put(user4.getLoginInfo().hashCode(), user4);
		userData.put(user5.getLoginInfo().hashCode(), user5);
		
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
	
	public static void removeUser(String user) {
		activeUserList.remove(user);
	}
	
	public static ClientHandler getActiveClient(String user) {
		return activeUserList.get(user);
	}
	//UserData class method
	public static Conversation getConversation(String conversationID) {
		return conversations.get(conversationID);
	}
	
	public static User getUserData(int hashCode) {
		System.out.println("Passed in hashCode: " + hashCode);
		return userData.get(hashCode);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
