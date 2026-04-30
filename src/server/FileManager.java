package server; 

import java.io.BufferedWriter; 
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import model.Conversation;
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

		    public synchronized void saveUser(User user) throws IOException {
		        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
		            writer.write(userToCSV(user));
		            writer.newLine();
		        }
		    }

		    public synchronized void saveConversation(Conversation conversation) throws IOException {
		        String fileName = CONVERSATION_FOLDER + "/" + conversation.getID() + ".csv"; 

		        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
		            writer.write("conversationId," + conversation.getID());
		            writer.newLine();

		            writer.write("participants," + conversation.getParticipants());
		            writer.newLine();

		            writer.write("messages");
		            writer.newLine();

		            for (Message message : conversation.getMessages()) {
		                writer.write(messageToCSV(message));
		                writer.newLine();
		            }
		        }
		    }

		    public synchronized void saveLogQueue(String userId, ArrayList<Message> logQueue) throws IOException {
		        String fileName = LOG_FOLDER + "/" + userId + "_logs.csv";

		        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
		            for (Message message : logQueue) {
		                writer.write(messageToCSV(message));
		                writer.newLine();
		            }
		        }
		    }

		    private String userToCSV(User user) {
		        return user.getUserID() + ","
		                + cleanText(user.getName())
		                + "," + cleanText(user.getPosition())
		                + "," + user.isIT(); 
		    }

		    private String messageToCSV(Message message) {
		        return cleanText(message.getSenderID()) 
		                + "," + message.getTimestamp()
		                + "," + cleanText(message.getText());
		    }

		    private String cleanText(Object value) {
		        if (value == null) return "";
		        return value.toString().replace(",", " ").replace("\n", " ");
		    }
		}	