package client;
import model.*;
import GUI.MainWindow;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		Socket socket = new Socket(address, port);
	    isConnected = true;
	    wrappedObjects = new LinkedBlockingQueue<>();
	    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
	    out.flush();
	    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
	    ClientRunner runner = new ClientRunner(socket, out, wrappedObjects);
	    client = new ClientController(runner);
	    ClientListener listener = new ClientListener(socket, in, client);
	    Thread listenerThread = new Thread(listener, "ClientListener");
	    Thread runnerThread = new Thread(runner, "ClientRunner");
	    listenerThread.start();
	    runnerThread.start();
	    MainWindow m = new MainWindow(client);
	    m.startup();
	    listenerThread.join();
	    runnerThread.join();
	    
	    
	    try { socket.close(); } catch (IOException ignored) {}
	}
	
	
	
	private static class ClientListener implements Runnable {
	    private final Socket clientSocket;
	    private final ObjectInputStream objectListen;
	    private final ClientController myClient;

	    public ClientListener(Socket sock, ObjectInputStream in, ClientController controller) {
	        this.clientSocket = sock;
	        this.objectListen = in;
	        this.myClient = controller;
	    }

	    @Override
	    public void run() {
	        try {
	            while (true) {
	                Wrapper data;
	                try {
	                    data = (Wrapper) objectListen.readObject();
	                } catch (IOException e) {
	                    if (clientSocket.isClosed()) {
	                        System.out.println("Client has closed connection");
	                    } else {
	                        e.printStackTrace();
	                    }
	                    return;
	                } catch (ClassNotFoundException e) {
	                    e.printStackTrace();
	                    continue;
	                }

	                myClient.parseWrapper(data);
	            }
	        } finally {
	            try { clientSocket.close(); } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}


	public static class ClientRunner implements Runnable {
	    private final Socket clientSocket;
	    private final ObjectOutputStream objectRun;
	    private final BlockingQueue<Wrapper> queue;

	    public ClientRunner(Socket sock, ObjectOutputStream out,
	                        BlockingQueue<Wrapper> wrappedObjects) {
	        this.clientSocket = sock;
	        this.objectRun = out;
	        this.queue = wrappedObjects;
	    }

	    @Override
	    public void run() {
	        try {
	            while (!clientSocket.isClosed()) {
	                Wrapper object = queue.take();
	                objectRun.writeObject(object);
	                objectRun.flush();
	                objectRun.reset();
	            }
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        } catch (IOException e) {
	            if (!clientSocket.isClosed()) e.printStackTrace();
	        } finally {
	            try { objectRun.close(); } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }

	    public void send(Object thisObject, RequestType ID) {
	        Wrapper newData = new Wrapper(thisObject, ID);
	        try {
	            queue.put(newData);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    }

		public void send(Object thisObject, ResponseType ID) {
			Wrapper newData = new Wrapper(thisObject, ID);
	        try {
	            queue.put(newData);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
		}
	}
}