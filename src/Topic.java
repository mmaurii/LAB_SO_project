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
    //lista di tutti subscribers che hanno scelto questo topic
    private final HashSet<ClientHandler> subscribers = new HashSet<>();

    public Topic(String title) {
        this.title = title;
    }

    public HashSet<ClientHandler> getSubscribers() {
        return subscribers;
    }
    /**
     * @return titolo del topic
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return restituisce i messaggi pubblicati su quel topic
     */
    public ArrayList<Message> getMessages() {
        return messages;
    }

    /**
     * @param message messaggio da aggiungere al topic
     */
    public synchronized void addMessage(Message message) {
        messages.add(message);
    }

    /**
     * Controlla se due topic sono uguali in base al titolo
     */
    @Override
    public boolean equals(Object obj) {
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
}
