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

            if (line.equals("messages")) {
                readingMessages = true;
                continue;
            }

            if (!readingMessages) {
                String[] parts = line.split(",", 2);

                if (parts[0].equals("type")) type = parts[1];
                if (parts[0].equals("creatorID")) creator = parts[1];
                if (parts[0].equals("groupName")) groupName = parts[1];
                if (parts[0].equals("createdAt")) created = Instant.parse(parts[1]);

                if (parts[0].equals("participants")) {
                    String[] ids = parts[1].split(";");
                    for (String id : ids) {
                        participants.add(id.trim());
                    }
                }
            } else {
                String[] parts = line.split(",", 3);
                Message m = new Message(parts[2], parts[0], Instant.parse(parts[1]));
                messages.add(m);
            }
        }

        reader.close();

        Conversation base = new Conversation(created);

        for (String p : participants) base.addParticipant(p);
        for (Message m : messages) base.addMessage(m);

        if (type.equals("GC")) {
            GroupConversation gc = new GroupConversation(base, creator, created);
            gc.setName(groupName);
            return gc;
        }

        return base;
    }

    // save methods 

    public synchronized void saveUser(User user) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true));
        writer.write(userToCSV(user));
        writer.newLine();
        writer.close();
    }

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

    public synchronized void saveLogQueue(String userId, ArrayList<Message> logQueue) throws IOException {
        String fileName = LOG_FOLDER + "/" + userId + "_logs.csv";

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));

        for (Message m : logQueue) {
            writer.write(m.getSenderID() + "," + m.getTimestamp() + "," + m.getText());
            writer.newLine();
        }

        writer.close();
    }

    private String userToCSV(User user) {
        return user.getUserID() + ","
                + user.getLoginInfo().getUsername() + ","
                + user.getLoginInfo().getPassword() + ","
                + user.getName() + ","
                + user.getPosition() + ","
                + user.isIT();
    }
}