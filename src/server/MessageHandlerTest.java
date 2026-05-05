//package server;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import java.io.*;
//import java.util.ArrayList;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import model.*;
//
//public class MessageHandlerTest {
//
//	//create the message handle
//    private MessageHandler messageHandler;
//    /*
//     * I need to simulate an input and output stream.
//     * out sends out bytes over a socket.
//     * in reads in bytes over a socket.
//     * I need to simulate a byte stream in and out
//    */
//    //THIS IS HOW TO SIMULATE A BYTE STREAM
//    private ByteArrayOutputStream outputCatcher;
//    //this is my output stream
//    private ObjectOutputStream out;
//
//    //create local variables to test MessageHandler methods
//    private User sender;
//    private User recipient;
//    private Conversation activeConversation;
//    private ArrayList<Message> logQueue;
//
//    /*** BEGIN SET UP ***/
//    
//    @BeforeEach
//    public void setUp() throws IOException {
//        //this lets me simulate sending objects over a socket
//    	//out needs this to pretend to be writing to a socket
//        outputCatcher = new ByteArrayOutputStream();
//        //get the output stream like normal
//        out = new ObjectOutputStream(outputCatcher);
//
//        //create the message handler
//        messageHandler = new MessageHandler(out);
//        //make a logQueue to simulate saving to a queue
//        logQueue = new ArrayList<>();
//
//
//        // Create users
//        sender = new User("sender", "pass123"); 
//        recipient = new User("recipient", "pass123");
//
//        // Create a conversation and add both users to it
//        activeConversation = new Conversation();
//        activeConversation.addParticipant(sender.getUserID());
//        activeConversation.addParticipant(recipient.getUserID());
//
//        // Update the users' internal conversation lists
//        sender.getConversations().add(activeConversation.getID());
//        recipient.getConversations().add(activeConversation.getID());
//
//        // Create the fake database to populate it
//        UserData mockDatabase = new UserData();
//        mockDatabase.addUser(sender);
//        mockDatabase.addUser(recipient);
//        mockDatabase.addConversation(activeConversation);
//
//        // Inject into the Server
//        Server.setTestUserData(mockDatabase);
//    }
//
//   
//    /*** TESTING SENDING MESSAGE ***/
//    @Test
//    public void testHandleSendMessage_OfflineRecipient_Success() {
//        //create a message to send and wrap it
//        Message msgPayload = new Message("Hello World!", sender.getUserID());
//        Wrapper request = new Wrapper(msgPayload, RequestType.SEND_MESSAGE);
//        String convoId = activeConversation.getID();
//
//        messageHandler.handleSendMessage(request, logQueue, convoId, sender.getUserID());
//
//        //check that the log is added to the queue
//        assertEquals(1, logQueue.size(), "Message should be saved to the log queue.");
//        
//        //the message should be added to the conversation when sent
//        assertEquals(1, activeConversation.getMessages().size(), "Message should be added to the Conversation.");
//        
//        //if the user is offline, then add the message to their unreadCoversations list
//        assertTrue(recipient.getUnreadConversations().contains(convoId), "Offline recipient should get an unread notification.");
//    }
//
//    @Test
//    public void testHandleSendMessage_InvalidPayload_Fails() {
//        //send an incorrect payload 
//        Wrapper badRequest = new Wrapper("This is just text, not a message object", RequestType.SEND_MESSAGE);
//        String convoId = activeConversation.getID();
//
//        messageHandler.handleSendMessage(badRequest, logQueue, convoId, sender.getUserID());
//
//        //the wrong payload was sent, the message should not be added 
//        assertTrue(logQueue.isEmpty(), "Log queue should remain empty on invalid payload.");
//        assertTrue(activeConversation.getMessages().isEmpty(), "Conversation should not receive a message.");
//    }
//
//    @Test
//    public void testHandleSendMessage_ConversationNotFound_Fails() {
//        //send a message to a non-existant conversation
//        Message msgPayload = new Message("Hello void", sender.getUserID());
//        Wrapper request = new Wrapper(msgPayload, RequestType.SEND_MESSAGE);
//        String badConvoId = "999999"; 
//
//        messageHandler.handleSendMessage(request, logQueue, badConvoId, sender.getUserID());
//
//        //check to make sure at was not added to the queue
//        assertEquals(1, logQueue.size(), "Message hits log queue before validation check.");
//        //check that the invalid conversation doesn't exist
//        assertTrue(activeConversation.getMessages().isEmpty(), "No message should be routed to the valid conversation.");
//    }
//}