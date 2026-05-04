package client;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;


public class Client {
	private static int port = 54927;
	private static String address = "";
	private static boolean isConnected = false;
	private static int timeOut = 30;
	private static ClientController client;
	public static void main(String[] args) throws Exception {
		// This is to establish a connection to the server first
		try(Socket socket = new Socket(address, port)){
			isConnected = !isConnected;
			ClientListener listener = new ClientListener(socket, client);
			new Thread(listener).start();
			
		}
	}
	
	private static class ClientListener implements Runnable{
		private final Socket clientSocket;
		private ClientController myClient;
		
		public ClientListener(Socket sock, ClientController client) {
			clientSocket = sock;
			myClient = client;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			// We create an ObjectInputStream to listen for any oncoming data
			InputStream listen = null;
			ObjectInputStream objectListen = null;
			try {
				listen = clientSocket.getInputStream();
				objectListen = new ObjectInputStream(listen);
			} catch (IOException e){
				e.printStackTrace();
			}
			while(true) {
				try {
					Wrapper data = (Wrapper) objectListen.readObject();
					Object load = data.getPayload();
					
					/* Based on what the wrapper gives back, we need to create a case statement for each case
					Note to self: Ask what requestTypes should be used
					So pipeline wise, it should look like below
					Server sends data -> client recieves and breaks down data based on type -> clientController takes data and updates values 
					*/
					
					switch(data.getRequestType()) {
					case LOGIN:
						break;
					case LOGOUT:
						return;
					case GET_USER_INFO:
						break;
					case CREATE_CONVERSATION:
						break;
					case CREATE_GROUP_CONVERSATION:
						break;
					case GET_CONVERSATION:
						break;
					case ADD_PARTICIPANT:
						break;
					case REMOVE_PARTICIPANT:
						break;
					case GET_MESSAGES:
						break;
					case GET_NEW_MESSAGES:
						break;
					default:
						break;
					}
				} catch (IOException e) {
					if(clientSocket.isClosed()) {
						System.out.println("Client has closed connection");
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					try {
						clientSocket.close();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
	}
	
	
	
	
	
	
	
	
}
