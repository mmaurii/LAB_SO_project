import java.util.ArrayList;
import java.util.HashSet;

/**
 * La classe definisce un argomento di discussione su cui
 * possonono esssere scambiati messaggi
 */
public class Topic {
    //nome del topic
    private final String title;
    //lista di tutti i messaggi che sono stati scambiati su questo topic
    private final ArrayList<Message> messages = new ArrayList<>();
    final Object messagesLock = new Object();
    //lista di tutti subscribers che hanno scelto questo topic
    private final HashSet<ClientHandler> subscribers = new HashSet<>();
    final Object subscribersLock = new Object();

    public Topic(String title) {
        this.title = title;
    }

    /**
     * @return titolo del topic
     */
    public String getTitle() {
        return title;
    }


    public String getStringMessages(){
        synchronized (messagesLock) {
            if (messages.isEmpty()) {
                return "";
            } else {
                StringBuilder sb = new StringBuilder();

                sb.append("Sono stati inviati " + messages.size() + " messaggi in questo topic.\n");
                sb.append("MESSAGGI:");
                for (Message m : messages) {
                    sb.append(m.replyString());
                }

                return sb.toString();
            }
        }
    }

    /**
     * @param message messaggio da aggiungere al topic
     */
    public void addMessage(Message message) {
        synchronized (messagesLock) {
            messages.add(message);
        }
    }

    /**
     * Controlla se due topic sono uguali in base al titolo
     */
    @Override
    public synchronized boolean equals(Object obj) {
        if (obj instanceof Topic) {
            return ((Topic) obj).title.equals(title);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString(){
        return String.format("%s: %s",title,messages);
    }

    public boolean removeMessage(int id) {
        synchronized (messagesLock) {
            return messages.removeIf(m -> m.getID() == id);
        }
    }

    public void addSubscriber(ClientHandler client) {
        synchronized (subscribersLock){
            subscribers.add(client);
        }
    }

    public void forwardToAll(Message message) {
        synchronized (subscribersLock) {
            for (ClientHandler c : subscribers) {
                c.forward("Nuovo messaggio pubblicato");
                c.forward(message.toString());
            }
        }
    }
}
