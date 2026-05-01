package modeltesting;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import model.Message;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void constructorWithTextAndSenderIDSetsFields() {
        Message message = new Message("Hello", "user123");

        assertEquals("Hello", message.getText());
        assertEquals("user123", message.getSenderID());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void constructorWithTextSenderIDAndTimestampSetsAllFields() {
        Instant timestamp = Instant.parse("2024-01-01T12:00:00Z");

        Message message = new Message("Hi there", "user456", timestamp);

        assertEquals("Hi there", message.getText());
        assertEquals("user456", message.getSenderID());
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    void timestampFromTwoArgConstructorIsSetToCurrentTime() {
        Instant before = Instant.now();
        Message message = new Message("Test", "sender");
        Instant after = Instant.now();

        assertNotNull(message.getTimestamp());
        assertFalse(message.getTimestamp().isBefore(before));
        assertFalse(message.getTimestamp().isAfter(after));
    }
}