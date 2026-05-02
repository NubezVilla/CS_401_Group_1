package server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

import model.*;

public class AuthenticateHandlerTest {
	
    private AuthenticateHandler authHandler;
    
    //these are to simulate sending bytes over a socket
    private ByteArrayOutputStream outputCatcher;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private User validTestUser;
    private LoginInfo validCredentials;

    @BeforeEach
    public void setUp() throws IOException {
     
        outputCatcher = new ByteArrayOutputStream();
        out = new ObjectOutputStream(outputCatcher);

        //i need to create an input stream
        //make an outputstream first to satisfy java
        ByteArrayOutputStream dummyOutputBytes = new ByteArrayOutputStream();
        ObjectOutputStream dummyOut = new ObjectOutputStream(dummyOutputBytes);
        dummyOut.flush(); 
        //then get the input stream
        ByteArrayInputStream dummyInBais = new ByteArrayInputStream(dummyOutputBytes.toByteArray());
        in = new ObjectInputStream(dummyInBais);

        authHandler = new AuthenticateHandler(out, in);

        validCredentials = new LoginInfo("admin", "password123");
        validTestUser = new User("admin", "password123"); 

        UserData mockDatabase = new UserData();
        mockDatabase.addUser(validTestUser);

        Server.setTestUserData(mockDatabase);
        /*** I NEED A DUMMY FILE MANAGER TO TEST ***/
        //use a dummy file manager class to test
        FileManager dummyFileManager = new FileManager() {
            @Override
            public synchronized void saveUser(User user) {}
            
            @Override
            public synchronized void saveConversation(Conversation conversation) {}
            
            @Override
            public synchronized void saveLogQueue(String userId, ArrayList<Message> logQueue) {}
        };
        
        Server.setTestFileManager(dummyFileManager);
    }
    
    /*** TESTING LOGGING IN ***/
    @Test
    public void testHandleLogin_Success() {
        //make a login request
        Wrapper loginRequest = new Wrapper(validCredentials, RequestType.LOGIN);

        boolean result = authHandler.handleLogin(loginRequest);

        //check that the credentials are correct
        //check that the account exists
        //check that the user is logged in
        assertTrue(result, "Handler should return true for valid credentials.");
        assertNotNull(authHandler.getUserAccount(), "User account should be populated.");
        assertTrue(authHandler.isLoggedIn(), "Internal loggedIn state should be true.");
    }

    @Test
    public void testHandleLogin_WrongPassword_Fails() {
        //send incorrect password
        LoginInfo badCredentials = new LoginInfo("admin", "wrongpassword");
        Wrapper loginRequest = new Wrapper(badCredentials, RequestType.LOGIN);

        boolean result = authHandler.handleLogin(loginRequest);

        //check that the user is not logged in
        //check that there is no user added to authHandler
        assertFalse(result, "Handler should reject incorrect passwords.");
        assertNull(authHandler.getUserAccount(), "User account should remain null.");
    }

    @Test
    public void testHandleLogin_InvalidPayload_Fails() {
        //send the wrong payload
        Wrapper badRequest = new Wrapper("This is a string, not credentials", RequestType.LOGIN);

        boolean result = authHandler.handleLogin(badRequest);

        //check that they are not logged in
        //check that the user s not added to authHandler
        assertFalse(result, "Handler should reject payloads that are not LoginInfo.");
        assertNull(authHandler.getUserAccount(), "User account should remain null.");
    }
    
    /*** TESTING LOGGING OUT ***/
    @Test
    public void testHandleLogout_InvalidPayload_Fails() {
        //send the wrong payload
        Wrapper badRequest = new Wrapper("Not a user object", RequestType.LOGOUT);
        Socket dummySocket = new Socket(); // Unconnected socket for testing
        ArrayList<Message> dummyLog = new ArrayList<>();

        boolean result = authHandler.handleLogout(badRequest, dummyLog, dummySocket);

        assertFalse(result, "Handler should fail logout if payload isn't a User.");
        assertFalse(dummySocket.isClosed(), "Socket should not be closed on failed logout.");
    }

    @Test
    public void testHandleLogout_Success() throws IOException {
        //i need a byte stream to test this method
    	//this is needed because logout expects to reads, user data and conversations
        ByteArrayOutputStream prepBaos = new ByteArrayOutputStream();
        ObjectOutputStream localOut = new ObjectOutputStream(prepBaos);
        
        //create the secondary payload the handler is waiting for
        ArrayList<Conversation> fakeConversations = new ArrayList<>();
        Wrapper secondaryPayload = new Wrapper(fakeConversations, ResponseType.SENDING_DATA);
        localOut.writeObject(secondaryPayload);
        localOut.flush();

        //create a custom handler equipped with our loaded input stream
        ByteArrayInputStream bais = new ByteArrayInputStream(prepBaos.toByteArray());
        ObjectInputStream loadedInput = new ObjectInputStream(bais);
        AuthenticateHandler customLogoutHandler = new AuthenticateHandler(out, loadedInput);

        //prepare the initial arguments
        Wrapper logoutRequest = new Wrapper(validTestUser, RequestType.LOGOUT);
        //i need a dummy socket to test that it gets closed
        Socket dummySocket = new Socket(); 
        //i need an array to copy the conversations and act as the logs
        ArrayList<Message> dummyLog = new ArrayList<>();

        boolean result = customLogoutHandler.handleLogout(logoutRequest, dummyLog, dummySocket);

        assertFalse(result, "isLoggedIn should be returned as false upon successful logout.");
        assertFalse(customLogoutHandler.isLoggedIn(), "Internal state should be false.");
        assertTrue(dummySocket.isClosed(), "Client socket should be closed after successful logout.");
    }
}