package server; 

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Conversation;
import model.GroupConversation;
import model.LoginInfo;
import model.Message;
import model.User;


public class FileManagerTest {
	private FileManager fileManager;

    @BeforeEach
    public void setUp() {
        fileManager = new FileManager();
    }

    @Test
    public void testSaveConversation_CreatesConversationFile() throws IOException {
        Conversation conversation = new Conversation();
        conversation.addParticipant("1");
        conversation.addParticipant("2");
        conversation.addMessage(new Message("Hello", "1"));

        fileManager.saveConversation(conversation);

        File savedFile = new File("data/conversations/" + conversation.getID() + ".csv");

        assertTrue(savedFile.exists(), "Conversation file should be created.");
    }

    @Test
    public void testSaveGroupConversation_SavesGroupMetadata() throws IOException {
        Conversation base = new Conversation();
        base.addParticipant("1");
        base.addParticipant("2");

        GroupConversation groupConversation = new GroupConversation(base, "1");
        groupConversation.setName("Project Team");

        fileManager.saveConversation(groupConversation);

        File savedFile = new File("data/conversations/" + groupConversation.getID() + ".csv");
        String content = Files.readString(savedFile.toPath());

        assertTrue(content.contains("type,GC"), "Group conversation should save GC type.");
        assertTrue(content.contains("creatorID,1"), "Group conversation should save creator ID.");
        assertTrue(content.contains("groupName,Project Team"), "Group conversation should save group name.");
    }

    @Test
    public void testSaveLogQueue_CreatesLogFile() throws IOException {
        ArrayList<Message> logQueue = new ArrayList<>();
        logQueue.add(new Message("Test log message", "1"));

        fileManager.saveLogQueue("1", logQueue);

        File logFile = new File("data/logs/1_logs.csv");

        assertTrue(logFile.exists(), "Log file should be created.");
    }

    @Test
    public void testLoadData_LoadsUsersIntoUserData() throws IOException {
        UserData userData = new UserData();

        fileManager.loadData(userData);

        User user = userData.getUserByLoginHash(new LoginInfo("user1", "pass1").hashCode());

        assertNotNull(user, "User from users.csv should load into UserData.");
        assertEquals("Allison Ray", user.getName(), "Loaded user should have correct name.");
    }
}

