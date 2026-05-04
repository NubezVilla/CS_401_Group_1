package client;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Client {
	private static int port = 54927;
	private static String address = "";
	private static boolean isConnected = false;
	private static int timeOut = 30;
	private static ClientController client;
	public static BlockingQueue<Wrapper> wrappedObjects;
	
	public static void main(String[] args) throws Exception {
		// This is to establish a connection to the server first
		try(Socket socket = new Socket(address, port)){
			wrappedObjects = new LinkedBlockingQueue<>();
			isConnected = !isConnected;
			ClientListener listener = new ClientListener(socket, client);
			new Thread(listener).start();
			ClientRunner runner = new ClientRunner(socket, wrappedObjects);
			client = new ClientController(runner);
			new Thread(runner).start();
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
					/* Based on what the wrapper gives back, we need to create a case statement for each case
					Note to self: Ask what requestTypes should be used
					So pipeline wise, it should look like below
					Server sends data -> client recieves and breaks down data based on type -> clientController takes data and updates values 
					*/
					
					switch(data.getResponseType()) {
					case LOGIN_SUCCESS:
						myClient.deliverResponse(data);
						break;
					case LOGIN_FAIL:
						myClient.deliverResponse(data);
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
	
	
	public static class ClientRunner implements Runnable{
		private final Socket clientSocket;
		private final BlockingQueue<Wrapper> queue;
		private OutputStream run;
		private ObjectOutputStream objectRun;
		
		public ClientRunner(Socket sock, BlockingQueue<Wrapper> wrappedObjects) {
			clientSocket = sock;
			queue = wrappedObjects;
			try {
				run = clientSocket.getOutputStream();
				objectRun = new ObjectOutputStream(run);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				while(true) {		//Change true to a value that would go false when the server sends a successfull logout message
					Wrapper object = null;
						object = queue.take();
						objectRun.writeObject(object);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try { objectRun.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void send(Object thisObject, RequestType ID) {
			Wrapper newData = new Wrapper(thisObject, ID);
			try {
				queue.put(newData);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	
	
}
