package server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Conversation;
import model.User;

public class UserDataTest {

    private UserData userData;

    @BeforeEach
    public void setUp() {
        userData = new UserData();
    }

    @Test
    public void testAddUserAndGetById() {
        User user = new User("testuser", "testpass");

        userData.addUser(user);

        assertEquals(user, userData.getUserById(user.getUserID()));
    }

    @Test
    public void testAddUserAndGetByLoginHash() {
        User user = new User("testuser", "testpass");

        userData.addUser(user);

        assertEquals(
            user,
            userData.getUserByLoginHash(user.getLoginInfo().hashCode())
        );
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        userData.addUser(user1);
        userData.addUser(user2);

        assertAll(
            () -> assertEquals(2, userData.getAllUsers().size()),
            () -> assertTrue(userData.getAllUsers().contains(user1)),
            () -> assertTrue(userData.getAllUsers().contains(user2))
        );
    }

    @Test
    public void testAddConversationAndGetConversation() {
        Conversation conversation = new Conversation();

        userData.addConversation(conversation);

        assertEquals(
            conversation,
            userData.getConversation(conversation.getID())
        );
    }

    @Test
    public void testUpdateUnreadMessage() {
        User user = new User("testuser", "testpass");
        Conversation conversation = new Conversation();

        userData.addUser(user);
        userData.addConversation(conversation);

        userData.updateUnreadMessage(user.getUserID(), conversation.getID());

        assertTrue(
            user.getUnreadConversations().contains(conversation.getID())
        );
    }

    @Test
    public void testAddNullUserDoesNotThrow() {
        assertDoesNotThrow(() -> userData.addUser(null));
        assertEquals(0, userData.getAllUsers().size());
    }

    @Test
    public void testAddNullConversationDoesNotThrow() {
        assertDoesNotThrow(() -> userData.addConversation(null));
        assertEquals(0, userData.getAllConversations().size());
    }
}