package modeltesting;

import model.*;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

class ConversationTest {

    @Test
    void defaultConstructorInitializesFieldsCorrectly() {
        Conversation conversation = new Conversation();

        assertNotNull(conversation.getID());
        assertNotNull(conversation.getCreated());
        assertNotNull(conversation.getParticipants());
        assertNotNull(conversation.getMessages());
        assertTrue(conversation.getParticipants().isEmpty());
        assertTrue(conversation.getMessages().isEmpty());
        assertNull(conversation.getMostRecentMessageTimestamp());
    }

    @Test
    void twoParticipantConstructorInitializesFieldsCorrectly() {
        Conversation conversation = new Conversation("user1", "user2");

        assertNotNull(conversation.getID());
        assertNotNull(conversation.getCreated());
        assertEquals(2, conversation.getParticipants().size());
        assertTrue(conversation.getParticipants().contains("user1"));
        assertTrue(conversation.getParticipants().contains("user2"));
        assertTrue(conversation.getMessages().isEmpty());
        assertNull(conversation.getMostRecentMessageTimestamp());
    }

    @Test
    void twoParticipantConstructorWithSameParticipantOnlyStoresOneParticipant() {
        Conversation conversation = new Conversation("user1", "user1");

        assertEquals(1, conversation.getParticipants().size());
        assertTrue(conversation.getParticipants().contains("user1"));
    }

    @Test
    void instantConstructorSetsCreatedAtToProvidedValue() {
        Instant created = Instant.parse("2024-01-01T12:00:00Z");

        Conversation conversation = new Conversation(created);

        assertEquals(created, conversation.getCreated());
        assertNotNull(conversation.getID());
        assertTrue(conversation.getParticipants().isEmpty());
        assertTrue(conversation.getMessages().isEmpty());
        assertNull(conversation.getMostRecentMessageTimestamp());
    }

    @Test
    void idsAreAssignedAndIncrease() {
        Conversation first = new Conversation();
        Conversation second = new Conversation();

        int firstId = Integer.parseInt(first.getID());
        int secondId = Integer.parseInt(second.getID());

        assertTrue(secondId > firstId);
    }

    @Test
    void addParticipantAddsParticipant() {
        Conversation conversation = new Conversation();

        conversation.addParticipant("user1");

        assertTrue(conversation.hasParticipant("user1"));
        assertEquals(1, conversation.getParticipants().size());
    }

    @Test
    void addParticipantDoesNotDuplicateExistingParticipant() {
        Conversation conversation = new Conversation();

        conversation.addParticipant("user1");
        conversation.addParticipant("user1");

        assertEquals(1, conversation.getParticipants().size());
    }

    @Test
    void removeParticipantRemovesExistingParticipant() {
        Conversation conversation = new Conversation("user1", "user2");

        conversation.removeParticipant("user1");

        assertFalse(conversation.hasParticipant("user1"));
        assertTrue(conversation.hasParticipant("user2"));
        assertEquals(1, conversation.getParticipants().size());
    }

    @Test
    void removeParticipantDoesNothingWhenParticipantDoesNotExist() {
        Conversation conversation = new Conversation("user1", "user2");

        conversation.removeParticipant("user3");

        assertEquals(2, conversation.getParticipants().size());
        assertTrue(conversation.hasParticipant("user1"));
        assertTrue(conversation.hasParticipant("user2"));
    }

    @Test
    void hasParticipantReturnsTrueWhenParticipantExists() {
        Conversation conversation = new Conversation("user1", "user2");

        assertTrue(conversation.hasParticipant("user1"));
    }

    @Test
    void hasParticipantReturnsFalseWhenParticipantDoesNotExist() {
        Conversation conversation = new Conversation("user1", "user2");

        assertFalse(conversation.hasParticipant("user3"));
    }

    @Test
    void addMessageAddsMessageToList() {
        Conversation conversation = new Conversation();
        Message message = new Message("Hello", "user1");

        conversation.addMessage(message);

        assertEquals(1, conversation.getMessages().size());
        assertSame(message, conversation.getMessages().get(0));
    }

    @Test
    void addMessageUpdatesMostRecentMessageTimestamp() {
        Conversation conversation = new Conversation();
        Instant timestamp = Instant.parse("2024-01-02T10:15:30Z");
        Message message = new Message("Hello", "user1", timestamp);

        conversation.addMessage(message);

        assertEquals(timestamp, conversation.getMostRecentMessageTimestamp());
    }

    @Test
    void addMultipleMessagesUpdatesMostRecentMessageTimestampToLastAddedMessage() {
        Conversation conversation = new Conversation();
        Message first = new Message("First", "user1", Instant.parse("2024-01-01T10:00:00Z"));
        Message second = new Message("Second", "user2", Instant.parse("2024-01-01T11:00:00Z"));

        conversation.addMessage(first);
        conversation.addMessage(second);

        assertEquals(2, conversation.getMessages().size());
        assertSame(first, conversation.getMessages().get(0));
        assertSame(second, conversation.getMessages().get(1));
        assertEquals(second.getTimestamp(), conversation.getMostRecentMessageTimestamp());
    }

    @Test
    void getParticipantsReturnsUnderlyingMutableSet() {
        Conversation conversation = new Conversation();

        HashSet<String> participants = conversation.getParticipants();
        participants.add("user1");

        assertTrue(conversation.hasParticipant("user1"));
    }

    @Test
    void getMessagesReturnsUnderlyingMutableList() {
        Conversation conversation = new Conversation();
        ArrayList<Message> messages = conversation.getMessages();
        Message message = new Message("Hello", "user1");

        messages.add(message);

        assertEquals(1, conversation.getMessages().size());
        assertSame(message, conversation.getMessages().get(0));
    }
}