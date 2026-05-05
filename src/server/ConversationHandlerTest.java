//package server;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import java.io.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//import model.*;
//
//public class ConversationHandlerTest {
//
//    private ConversationHandler conversationHandler;
//    
//    //this is how to simulate a socket
//    private ByteArrayOutputStream outputCatcher;
//    private ObjectOutputStream out;
//    private ObjectInputStream in;
//
//    private User testUserToModify;
//    private User testHostUser;
//    private Conversation activeConversation;
//
//    @BeforeEach
//    public void setUp() throws IOException {
//       
//        outputCatcher = new ByteArrayOutputStream();
//        out = new ObjectOutputStream(outputCatcher);
//        //i need to create an input stream
//        //make an outputstream first to satisfy java
//        ByteArrayOutputStream dummyOutputBytes = new ByteArrayOutputStream();
//        ObjectOutputStream dummyOut = new ObjectOutputStream(dummyOutputBytes);
//        dummyOut.flush(); 
//        //then get the input stream
//        ByteArrayInputStream dummyInBais = new ByteArrayInputStream(dummyOutputBytes.toByteArray());
//        in = new ObjectInputStream(dummyInBais);
//
//        conversationHandler = new ConversationHandler(out, in);
//        
//        testUserToModify = new User("newGuy", "password123"); 
//        testHostUser = new User("host", "password123");
//
//        activeConversation = new Conversation();
//        activeConversation.addParticipant(testHostUser.getUserID());
//        testHostUser.getConversations().add(activeConversation.getID());
//
//        //create a mock database for testing
//        UserData mockDatabase = new UserData();
//        mockDatabase.addUser(testUserToModify);
//        mockDatabase.addUser(testHostUser);
//        mockDatabase.addConversation(activeConversation);
//
//        Server.setTestUserData(mockDatabase);
//    }
//
//
//    /*** TEST ADDING PARTICIPANTS ***/
//
//    @Test
//    public void testHandleAddParticipant_Success() {
//        //make the right payload for this method
//        Wrapper request = new Wrapper(testUserToModify, RequestType.ADD_PARTICIPANT);
//        String convoId = activeConversation.getID();
//        String currentUser = testHostUser.getUserID();
//
//        conversationHandler.handleAddParticipant(request, convoId, currentUser);
//
//        assertTrue(activeConversation.hasParticipant(testUserToModify.getUserID()), "Conversation should contain the new user's ID.");
//        assertTrue(testUserToModify.getConversations().contains(convoId), "User's conversation list should contain the new Conversation ID.");
//    }
//
//    @Test
//    public void testHandleAddParticipant_InvalidPayload_Fails() {
//        //make the wrong payload for this method
//        Wrapper badRequest = new Wrapper("Not a user object", RequestType.ADD_PARTICIPANT);
//        String convoId = activeConversation.getID();
//        String currentUser = testHostUser.getUserID();
//
//        conversationHandler.handleAddParticipant(badRequest, convoId, currentUser);
//
//        assertFalse(activeConversation.hasParticipant(testUserToModify.getUserID()), "Should not add participant on invalid payload.");
//    }
//
//    @Test
//    public void testHandleAddParticipant_DuplicateUser_Fails() {
//        //send the right payload
//        Wrapper request = new Wrapper(testHostUser, RequestType.ADD_PARTICIPANT);
//        String convoId = activeConversation.getID();
//        String currentUser = testHostUser.getUserID();
//
//        conversationHandler.handleAddParticipant(request, convoId, currentUser);
//
//        //test that the same participant is not added twice
//        assertEquals(1, activeConversation.getParticipants().size(), "Participant list should not increase when adding a duplicate.");
//    }
//
//
//    /*** TESTING REMOVING PARTICIPANTS ***/
//    
//    @Test
//    public void testHandleRemoveParticipant_Success() {
//        //add the user to the conversation to remove
//        String convoId = activeConversation.getID();
//        String currentUser = testHostUser.getUserID();
//        activeConversation.addParticipant(testUserToModify.getUserID());
//        testUserToModify.getConversations().add(convoId);
//
//        Wrapper request = new Wrapper(testUserToModify, RequestType.REMOVE_PARTICIPANT);
//        
//        conversationHandler.handleRemoveParticipant(request, convoId, currentUser);
//
//        //test that the added user has been removed
//        assertFalse(activeConversation.hasParticipant(testUserToModify.getUserID()), "User ID should be removed from the conversation.");
//        assertFalse(testUserToModify.getConversations().contains(convoId), "Conversation ID should be removed from the user.");
//    }
//
//    @Test
//    public void testHandleRemoveParticipant_UserNotInConversation_Fails() {
//        //send a payload to remove non-participant user
//        Wrapper request = new Wrapper(testUserToModify, RequestType.REMOVE_PARTICIPANT);
//        String convoId = activeConversation.getID();
//        String currentUser = testHostUser.getUserID();
//        
//        int size = activeConversation.getParticipants().size();
//
//        conversationHandler.handleRemoveParticipant(request, convoId, currentUser);
//
//        //test that the participant size did not change because user DNE in conversation
//        assertEquals(size, activeConversation.getParticipants().size(), "Participant list size should not change if user wasn't in it.");
//    }
//    
//    /*** TESTING GETTING CONVERSATIONS ***/
//
//    @Test
//    public void testHandleGetConversation_NotIT_Fails() {
//        //make the right payload
//        Wrapper request = new Wrapper(activeConversation.getID(), RequestType.GET_CONVERSATION);
//        boolean isIT = false;
//        
//        assertDoesNotThrow(() -> {
//            conversationHandler.handleGetConversation(request, activeConversation.getID(), isIT);
//        }, "Should safely abort and send UNAUTHORIZED message without exceptions.");
//    }
//
//    @Test
//    public void testHandleGetConversation_InvalidPayload_Fails() {
//        //send the wrong data type
//        Wrapper badRequest = new Wrapper(12345, RequestType.GET_CONVERSATION);
//        boolean isIT = true;
//        
//        assertDoesNotThrow(() -> {
//            conversationHandler.handleGetConversation(badRequest, activeConversation.getID(), isIT);
//        }, "Should safely abort and send INVALID PAYLOAD message without exceptions.");
//    }
//
//    @Test
//    public void testHandleGetConversation_NotFound_Fails() {
//        //send conversationID that does not exist
//        Wrapper badRequest = new Wrapper("999999", RequestType.GET_CONVERSATION);
//        boolean isIT = true;
//        
//        assertDoesNotThrow(() -> {
//            conversationHandler.handleGetConversation(badRequest, activeConversation.getID(), isIT);
//        }, "Should safely abort and send DOES NOT EXIST message without exceptions.");
//    }
//
//    @Test
//    public void testHandleGetConversation_Success() throws IOException {
//        //send the right data
//        //this will require making mock input stream
//    	//see setUp to set the input stream up
//    	ByteArrayOutputStream prepBaos = new ByteArrayOutputStream();
//        ObjectOutputStream prepOos = new ObjectOutputStream(prepBaos);
//        
//        //send the right data
//        Wrapper clientAck = new Wrapper(new Message("GOT IT", "Client"), ResponseType.CONVERSATION_SENT);
//        prepOos.writeObject(clientAck);
//        prepOos.flush();
//
//        //simulate sending the data out of the socket
//        ByteArrayInputStream bais = new ByteArrayInputStream(prepBaos.toByteArray());
//        ObjectInputStream loadedMockIn = new ObjectInputStream(bais);
//        ConversationHandler customHandler = new ConversationHandler(out, loadedMockIn);
//
//        //send the request
//        Wrapper request = new Wrapper(activeConversation.getID(), RequestType.GET_CONVERSATION);
//        boolean isIT = true;
//
//        assertDoesNotThrow(() -> {
//            customHandler.handleGetConversation(request, activeConversation.getID(), isIT);
//        }, "Handler should successfully send conversation and read the ACK without throwing exceptions.");
//    }
//    
//    
//    
//}