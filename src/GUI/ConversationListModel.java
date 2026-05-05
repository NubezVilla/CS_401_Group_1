package GUI;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.AbstractListModel;

import model.Conversation;

public class ConversationListModel extends AbstractListModel<Conversation> {
	private ArrayList<Conversation> listModel = new ArrayList<Conversation>();
	
	@Override
	public int getSize() {
		return listModel.size();
	}
	
	public int size() {
		return listModel.size();
	}

	@Override
	public Conversation getElementAt(int index) {
		return listModel.get(index);
	}
	public void sortByRecentMessage() {
		listModel.sort(
            Comparator.comparing(Conversation::getMostRecentMessageTimestamp).reversed()
        );

        if (!listModel.isEmpty()) {
            fireContentsChanged(this, 0, listModel.size() - 1);
        }
    }
	
	public Conversation get(Conversation c) {
		for(Conversation i : listModel) {
			if (i.equals(c)) {
				return i;
			};
		}
		return null;
	}

	public void set(int i, Conversation element) {
		listModel.set(i, element);
		fireContentsChanged(this, i, i);
		
	}
	
	public void add(int i, Conversation element) {
		listModel.add(i, element);
		fireContentsChanged(this, i, i);
	}

	public Object[] toArray() {
		return listModel.toArray();
	}

	public void removeElement(Conversation currentConversation) {
		int i = listModel.indexOf(currentConversation);
		listModel.remove(currentConversation);
		fireContentsChanged(this, i, i);
		
	}

	public void removeAllElements() {
		listModel.clear();
		
	}

	public void addAll(ArrayList<Conversation> conversations) {
		listModel.addAll(conversations);
		
	}
	
//	Added
	public Conversation findConversation(String id) {
		for(int x = 0; x < listModel.size(); x++) {
			if(listModel.get(x).getID().equals(id)) {
				return listModel.get(x);
			}
		}
		return null;
	}
	public int findConversationIndex(String id) {
		for(int x = 0; x < listModel.size(); x++) {
			if(listModel.get(x).getID().equals(id)) {
				return x;
			}
		}
		return -1;
	}

}
