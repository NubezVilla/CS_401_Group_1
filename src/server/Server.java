package server;

import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import model.GroupConversation;
import model.User;


public class Server {
	private static int port = 54927;
	private boolean isConnected;
	//FileManager manager
	//UserData userDataHandle
	private static ConcurrentHashMap<ClientHandler, User> userList;
	
	
	public static void main(String[] args) throws Exception {
		/* There has to be a lot of setup before the server can start listening.
		 * The server needs to load all user files into the data structures inside UserData.
		 */
		//manager.load()
		//userDataHandle.fillDataStructures()
		
		try (var listener = new ServerSocket(port)) {
			System.out.println("Server is running...");
	
            var clientThread = Executors.newFixedThreadPool(100);
            while (true) {
                clientThread.execute(new ClientHandler(listener.accept()));
            }
        }
		//save()
    }
}
