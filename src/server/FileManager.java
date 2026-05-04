package server;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;

import model.Conversation;
import model.GroupConversation;
import model.Message;
import model.User;

public class FileManager {

    private static final String DATA_FOLDER = "data";
    private static final String USERS_FILE = "data/users.csv";
    private static final String CONVERSATION_FOLDER = "data/conversations";
    private static final String LOG_FOLDER = "data/logs";

    public FileManager() {
        createRequiredFolders();
    }

    private void createRequiredFolders() {
        new File(DATA_FOLDER).mkdirs();
        new File(CONVERSATION_FOLDER).mkdirs();
        new File(LOG_FOLDER).mkdirs();
    } 
    // Load data 
    public synchronized void loadData(UserData userData) throws IOException {
        loadUsers(userData);
        loadConversations(userData);
    }
    // Reads users.csv and reconstructs User objects.
    // Adds each user into UserData for login and system use.
    private void loadUsers(UserData userData) throws IOException {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine(); // skip header

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(",");

            String username = parts[1];
            String password = parts[2];
            String name = parts[3];
            String position = parts[4];
            boolean isIT = Boolean.parseBoolean(parts[5]);

            User user = new User(username, password);
            user.setName(name);
            user.setPosition(position);
            user.setIT(isIT);

            userData.addUser(user);
        }

        reader.close();
    } 
    
    // Iterates through all conversation files and rebuilds Conversation objects.
	// Each file represents one conversation stored on disk.
    private void loadConversations(UserData userData) throws IOException {
        File folder = new File(CONVERSATION_FOLDER);
        if (!folder.exists()) return;

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.getName().endsWith(".csv")) continue;

            Conversation conversation = readConversation(file);
            if (conversation != null) {
                userData.addConversation(conversation);
            }
        }
    }
    /* Reads a single conversation file and reconstructs:
     * participants
     * messages
     * metadata (type, creator, group name)
     * Returns either a Conversation or GroupConversation object. 
    */
    private Conversation readConversation(File file) throws IOException {
    	BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        String type = "DM";
        String creator = "";
        String groupName = "NAMEUNSET";
        Instant created = Instant.now();

        ArrayList<String> participants = new ArrayList<>();
        ArrayList<Message> messages = new ArrayList<>();

        boolean readingMessages = false;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }

            if (line.equals("messages")) {
                readingMessages = true;
                continue;
            }

            if (!readingMessages) {
                String[] parts = line.split(",", 2);

                if (parts.length < 2) {
                    continue;
                }

                String key = parts[0];
                String value = parts[1];

                if (key.equals("type")) {
                    type = value;
                } else if (key.equals("creatorID")) {
                    creator = value;
                } else if (key.equals("groupName")) {
                    groupName = value;
                } else if (key.equals("createdAt")) {
                    created = Instant.parse(value);
                } else if (key.equals("participants")) {
                    String[] ids = value.split(";");

                    for (String id : ids) {
                        if (!id.trim().isEmpty()) {
                            participants.add(id.trim());
                        }
                    }
                }
            } else {
                String[] parts = line.split(",", 3);

                if (parts.length < 3) {
                    continue;
                }

                Message m = new Message(parts[2], parts[0], Instant.parse(parts[1]));
                messages.add(m);
            }
        }

        reader.close();

        Conversation base = new Conversation(created);

        for (String p : participants) {
            base.addParticipant(p);
        }

        if (type.equals("GC")) {
            GroupConversation gc = new GroupConversation(base, creator, created);
            gc.setName(groupName);

            for (Message m : messages) {
                gc.addMessage(m);
            }

            return gc;
        }

        for (Message m : messages) {
            base.addMessage(m);
        }

        return base;
    }
    
 // Saves user data back to users.csv.
 // If the user already exists, replace that row instead of adding a duplicate.
 public synchronized void saveUser(User user) throws IOException {
     File file = new File(USERS_FILE);

     ArrayList<String> lines = new ArrayList<>();
     boolean userUpdated = false;

     if (file.exists()) {
         try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
             String line;

             while ((line = reader.readLine()) != null) {
                 if (line.startsWith(user.getUserID() + ",")) {
                     lines.add(userToCSV(user));
                     userUpdated = true;
                 } else {
                     lines.add(line);
                 }
             }
         }
     }

     // If this user was not already in the file, add them as a new row.
     if (!userUpdated) {
         if (lines.isEmpty()) {
             lines.add("userId,username,password,realName,position,isIT");
         }

         lines.add(userToCSV(user));
     }

     // Rewrite the whole file with the updated user row.
     try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, false))) {
         for (String line : lines) {
             writer.write(line);
             writer.newLine();
         }
     }
 }
    	    
    // Saves a conversation to disk
    // Overwrites existing file to maintain latest state
    // Handles both direct messages (DM) and group conversations (GC)
    public synchronized void saveConversation(Conversation conversation) throws IOException {
        String fileName = CONVERSATION_FOLDER + "/" + conversation.getID() + ".csv";

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));

        if (conversation instanceof GroupConversation) {
            GroupConversation gc = (GroupConversation) conversation;

            writer.write("type,GC");
            writer.newLine();

            writer.write("creatorID," + gc.getCreator());
            writer.newLine();

            writer.write("groupName," + gc.getName());
            writer.newLine();
        } else {
            writer.write("type,DM");
            writer.newLine();
        }

        writer.write("conversationId," + conversation.getID());
        writer.newLine();

        writer.write("createdAt," + conversation.getCreated());
        writer.newLine();

        writer.write("participants," + String.join(";", conversation.getParticipants()));
        writer.newLine();

        writer.write("messages");
        writer.newLine();

        for (Message m : conversation.getMessages()) {
            writer.write(m.getSenderID() + "," + m.getTimestamp() + "," + m.getText());
            writer.newLine();
        }

        writer.close();
    }
    // Writes logQueue messages to a log file
    // Each user has a separate log file for tracking activity
    // Logs are appended to preserve history
    public synchronized void saveLogQueue(String userId, ArrayList<Message> logQueue) throws IOException {
        String fileName = LOG_FOLDER + "/" + userId + "_logs.csv";

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

        for (Message m : logQueue) {
            writer.write(m.getSenderID() + "," + m.getTimestamp() + "," + m.getText());
            writer.newLine();
        }

        writer.close();
    }
    // Converts a User object into CSV format for storage in users.csv
    private String userToCSV(User user) {
        return user.getUserID() + ","
                + user.getLoginInfo().getUserName() + ","
                + user.getLoginInfo().getPassword() + ","
                + user.getName() + ","
                + user.getPosition() + ","
                + user.isIT();
    }
}