package GUI;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import java.util.*;
import model.Message;

public class MessageListModel {
	private final ArrayList<Message> messages = new ArrayList<>();
    private final EventListenerList listeners = new EventListenerList();

    public void addMessage(Message m) {
        messages.add(m);
        fireChanged();
    }

    public void setMessages(ArrayList<Message> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        fireChanged();
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }
    
    public Message getNewestMessage() {
    		return messages.get(messages.size()-1);
    }

    //copied code, listeners scare me
    public void addChangeListener(ChangeListener l) {
        listeners.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(ChangeListener.class, l);
    }

    private void fireChanged() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners.getListeners(ChangeListener.class)) {
            l.stateChanged(e);
        }
    }
}
