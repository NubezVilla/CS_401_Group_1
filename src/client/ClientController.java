package client;
import java.util.ArrayList;

import GUI.ClientCalls;
import model.User;
public class ClientController implements ClientCalls {

	@Override
	public boolean loginAttempt(String username, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<User> searchUsers(String matching) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUserByID(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCurrentConversation(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fetchMessages(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean updateUser(User target, String newName, String newPosition, String newUsername, String newPassword) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean createNewUser(String name, String position, String username, String password) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
