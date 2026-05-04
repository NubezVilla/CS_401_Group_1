package modeltesting;

import model.User;
import model.LoginInfo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void defaultConstructorInitializesFieldsCorrectly() {
        User user = new User();

        assertNotNull(user.getUserID());
        assertNull(user.getLoginInfo());
        assertNull(user.getName());
        assertNull(user.getPosition());
        assertFalse(user.isIT());
        assertNotNull(user.getConversations());
        assertNotNull(user.getUnreadConversations());
        assertTrue(user.getConversations().isEmpty());
        assertTrue(user.getUnreadConversations().isEmpty());
    }

    @Test
    void overloadedConstructorInitializesLoginInfoAndDefaults() {
        User user = new User("alice", "secret");

        assertNotNull(user.getUserID());
        assertNotNull(user.getLoginInfo());
        assertEquals(new LoginInfo("alice", "secret"), user.getLoginInfo());
        assertNull(user.getName());
        assertNull(user.getPosition());
        assertFalse(user.isIT());
        assertNotNull(user.getConversations());
        assertNotNull(user.getUnreadConversations());
        assertTrue(user.getConversations().isEmpty());
        assertTrue(user.getUnreadConversations().isEmpty());
    }

    @Test
    void userIDsAreAssignedAndIncrease() {
        User first = new User();
        User second = new User();

        int firstId = Integer.parseInt(first.getUserID());
        int secondId = Integer.parseInt(second.getUserID());

        assertTrue(secondId > firstId);
    }

    @Test
    void compareLoginReturnsTrueForSameLoginInfo() {
        User user1 = new User("bob", "pw123");
        User user2 = new User("bob", "pw123");

        assertTrue(user1.compareLogin(user2));
    }

    @Test
    void compareLoginReturnsFalseForDifferentLoginInfo() {
        User user1 = new User("bob", "pw123");
        User user2 = new User("bob", "differentPw");

        assertFalse(user1.compareLogin(user2));
    }

    @Test
    void setLoginInfoUpdatesLoginInfo() {
        User user = new User("oldUser", "oldPw");

        user.setLoginInfo("newUser", "newPw");

        assertEquals(new LoginInfo("newUser", "newPw"), user.getLoginInfo());
    }

    @Test
    void setNameUpdatesRealName() {
        User user = new User();

        user.setName("Alice Smith");

        assertEquals("Alice Smith", user.getName());
    }

    @Test
    void setPositionUpdatesPosition() {
        User user = new User();

        user.setPosition("Manager");

        assertEquals("Manager", user.getPosition());
    }

    @Test
    void setITTrueUpdatesFlag() {
        User user = new User();

        user.setIT(true);

        assertTrue(user.isIT());
    }

    @Test
    void setITFalseUpdatesFlag() {
        User user = new User();
        user.setIT(true);

        user.setIT(false);

        assertFalse(user.isIT());
    }

    @Test
    void getConversationsReturnsMutableConversationSet() {
        User user = new User();

        HashSet<String> conversations = user.getConversations();
        conversations.add("conv1");

        assertTrue(user.getConversations().contains("conv1"));
        assertEquals(1, user.getConversations().size());
    }

    @Test
    void getUnreadConversationsReturnsMutableUnreadConversationSet() {
        User user = new User();

        HashSet<String> unreadConversations = user.getUnreadConversations();
        unreadConversations.add("conv2");

        assertTrue(user.getUnreadConversations().contains("conv2"));
        assertEquals(1, user.getUnreadConversations().size());
    }

    @Test
    void conversationAndUnreadConversationSetsAreIndependent() {
        User user = new User();

        user.getConversations().add("conv1");
        user.getUnreadConversations().add("conv2");

        assertTrue(user.getConversations().contains("conv1"));
        assertFalse(user.getConversations().contains("conv2"));
        assertTrue(user.getUnreadConversations().contains("conv2"));
        assertFalse(user.getUnreadConversations().contains("conv1"));
    }
}
