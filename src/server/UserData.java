package server; 


import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import model.Conversation;
import model.User;


public class UserData { 

    private ConcurrentHashMap<Integer, User> usersByLoginHash;
    private ConcurrentHashMap<String, User> usersById;
    private ConcurrentHashMap<String, Conversation> conversations;

    public UserData() {
        usersByLoginHash = new ConcurrentHashMap<>();
        usersById = new ConcurrentHashMap<>();
        conversations = new ConcurrentHashMap<>();
    }

    public void addUser(User user) {
        if (user == null) return;

        usersById.put(user.getUserID(), user);
        usersByLoginHash.put(user.getLoginInfo().hashCode(), user);
    }

    public User getUserByLoginHash(int loginHash) {
        return usersByLoginHash.get(loginHash);
    }

    public User getUserById(String userId) {
        return usersById.get(userId);
    }

    public ArrayList<User> getAllUsers() {
        return new ArrayList<>(usersById.values());
    }

    public void addConversation(Conversation conversation) {
        if (conversation == null) return;

        conversations.put(conversation.getID(), conversation); 
    }

    public Conversation getConversation(String conversationId) {
        return conversations.get(conversationId);
    }

    public ArrayList<Conversation> getAllConversations() {
        return new ArrayList<>(conversations.values());
    }

    public void updateUnreadMessage(String userId, String conversationId) {
        User user = usersById.get(userId);

        if (user != null) {
            user.getUnreadConversations().add(conversationId);
        }
    }
}