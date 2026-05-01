package modeltesting;

import model.*;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class GroupConversationTest {

    @Test
    void constructorCopiesParticipantsFromBaseConversation() {
        Conversation base = new Conversation();
        base.addParticipant("user1");
        base.addParticipant("user2");

        GroupConversation group = new GroupConversation(base, "creator1");

        assertTrue(group.hasParticipant("user1"));
        assertTrue(group.hasParticipant("user2"));
        assertEquals(2, group.getParticipants().size());
    }

    @Test
    void constructorSetsCreatorID() {
        Conversation base = new Conversation("user1", "user2");

        GroupConversation group = new GroupConversation(base, "creator1");

        assertEquals("creator1", group.getCreator());
    }

    @Test
    void constructorWithoutCreatedUsesCurrentTime() {
        Conversation base = new Conversation("user1", "user2");
        Instant before = Instant.now();

        GroupConversation group = new GroupConversation(base, "creator1");

        Instant after = Instant.now();

        assertNotNull(group.getCreated());
        assertFalse(group.getCreated().isBefore(before));
        assertFalse(group.getCreated().isAfter(after));
    }

    @Test
    void constructorWithCreatedSetsCreatedTimestamp() {
        Conversation base = new Conversation("user1", "user2");
        Instant created = Instant.parse("2024-01-01T12:00:00Z");

        GroupConversation group = new GroupConversation(base, "creator1", created);

        assertEquals(created, group.getCreated());
    }

    @Test
    void constructorWithCreatedCopiesParticipantsFromBaseConversation() {
        Conversation base = new Conversation();
        base.addParticipant("user1");
        base.addParticipant("user2");
        Instant created = Instant.parse("2024-01-01T12:00:00Z");

        GroupConversation group = new GroupConversation(base, "creator1", created);

        assertTrue(group.hasParticipant("user1"));
        assertTrue(group.hasParticipant("user2"));
        assertEquals(2, group.getParticipants().size());
    }

    @Test
    void groupStartsWithNoName() {
        Conversation base = new Conversation("user1", "user2");

        GroupConversation group = new GroupConversation(base, "creator1");

        assertNull(group.getName());
    }

    @Test
    void setNameUpdatesGroupName() {
        Conversation base = new Conversation("user1", "user2");
        GroupConversation group = new GroupConversation(base, "creator1");

        group.setName("Team Chat");

        assertEquals("Team Chat", group.getName());
    }

    @Test
    void setNameCanOverwriteExistingName() {
        Conversation base = new Conversation("user1", "user2");
        GroupConversation group = new GroupConversation(base, "creator1");

        group.setName("Old Name");
        group.setName("New Name");

        assertEquals("New Name", group.getName());
    }

    @Test
    void modifyingBaseConversationAfterConstructionDoesNotChangeGroupParticipants() {
        Conversation base = new Conversation();
        base.addParticipant("user1");

        GroupConversation group = new GroupConversation(base, "creator1");
        base.addParticipant("user2");

        assertTrue(group.hasParticipant("user1"));
        assertFalse(group.hasParticipant("user2"));
        assertEquals(1, group.getParticipants().size());
    }

    @Test
    void groupConversationHasItsOwnConversationID() {
        Conversation base = new Conversation("user1", "user2");

        GroupConversation group = new GroupConversation(base, "creator1");

        assertNotNull(group.getID());
    }

    @Test
    void groupConversationStartsWithNoMessages() {
        Conversation base = new Conversation("user1", "user2");

        GroupConversation group = new GroupConversation(base, "creator1");

        assertNotNull(group.getMessages());
        assertTrue(group.getMessages().isEmpty());
        assertNull(group.getMostRecentMessageTimestamp());
    }
}
