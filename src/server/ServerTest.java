package server;

/*
 * I am not sure how to test the multi-threaded part of the server. 
 * So I will test that the contents inside the map are accurate
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.Socket;

import model.*;

public class ServerTest {

    @BeforeEach
    public void setUp() {
        //clear the list before each test to avoid bugs
        Server.getActiveUserList().clear();
    }

    @Test
    public void testRegisterActiveUser_Success() {
        //make a dummy socket to make a ClientHandler object
        Socket dummySocket = new Socket();
        ClientHandler testHandler = new ClientHandler(dummySocket);
        String testUserID = "user_123";
        Server.registerActiveUser(testUserID, testHandler);

        //assert that they were added to the list
        assertEquals(1, Server.getActiveUserList().size(), "Active user list should have 1 entry.");
        assertEquals(testHandler, Server.getActiveClient(testUserID), "Should retrieve the exact ClientHandler we registered.");
    }

    @Test
    public void testRemoveActiveUser_Success() {
        Socket dummySocket = new Socket();
        ClientHandler testHandler = new ClientHandler(dummySocket);
        String testUserID = "user_123";
        Server.registerActiveUser(testUserID, testHandler);

        Server.removeActiveUser(testUserID);
        
        assertEquals(0, Server.getActiveUserList().size(), "Active user list should be empty after removal.");
        assertNull(Server.getActiveClient(testUserID), "Retrieving a removed user should return null.");
    }

}