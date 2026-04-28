package modeltesting;

import model.Report;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ReportTest {

    @Test
    void constructorWithoutTimestampInitializesFields() {
        Report report = new Report("Issue found", "123");

        assertEquals("Issue found", report.getText());
        assertEquals("123", report.getSenderID());
        assertNotNull(report.getTimestamp());
        assertFalse(report.isResolved());
    }

    @Test
    void constructorWithTimestampInitializesFields() {
        Instant timestamp = Instant.parse("2024-01-01T10:00:00Z");
        Report report = new Report("Bug report", "456", timestamp);

        assertEquals("Bug report", report.getText());
        assertEquals("456", report.getSenderID());
        assertEquals(timestamp, report.getTimestamp());
        assertFalse(report.isResolved());
    }

    @Test
    void resolveTogglesResolvedState() {
        Report report = new Report("Test", "0000");

        // Initially false
        assertFalse(report.isResolved());

        // First toggle -> true
        report.resolve();
        assertTrue(report.isResolved());

        // Second toggle -> false
        report.resolve();
        assertFalse(report.isResolved());
    }

    @Test
    void multipleResolveCallsToggleStateCorrectly() {
        Report report = new Report("Test", "0000");

        for (int i = 0; i < 5; i++) {
            report.resolve();
        }
        assertTrue(report.isResolved());
    }

    @Test
    void evenNumberOfResolveCallsResultsInFalse() {
        Report report = new Report("Test", "0000");

        for (int i = 0; i < 4; i++) {
            report.resolve();
        }

        assertFalse(report.isResolved());
    }
}
