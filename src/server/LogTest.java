package server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LogTest {

    @Test
    public void testToCSVContainsUserConversationAndAction() {
        Log log = new Log("user123", "convo456", "SENT_MESSAGE");

        String csv = log.toCSV();

        assertAll(
            () -> assertTrue(csv.contains("user123")),
            () -> assertTrue(csv.contains("convo456")),
            () -> assertTrue(csv.contains("SENT_MESSAGE"))
        );
    }

    @Test
    public void testToCSVFormatHasFourFields() {
        Log log = new Log("user123", "convo456", "SENT_MESSAGE");

        String csv = log.toCSV();
        String[] parts = csv.split(",", 4);

        assertEquals(4, parts.length);
    }

    @Test
    public void testToCSVStartsWithExpectedValues() {
        Log log = new Log("user123", "convo456", "SENT_MESSAGE");

        String csv = log.toCSV();

        assertTrue(csv.startsWith("user123,convo456,SENT_MESSAGE,"));
    }
}