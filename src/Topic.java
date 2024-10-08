import java.util.ArrayList;

public class Topic {
    private final String title;
    private final ArrayList<Message> messages = new ArrayList<>();
    private final ArrayList<ClientHandler> clients = new ArrayList<>();

    public Topic(String title) {
        this.title = title;
    }

    public ArrayList<ClientHandler> getClients() {
        return clients;
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
    public void addMessage(Message message) {
        messages.add(message);
    }

    /**
     * Controlla se due topic sono uguali (dato il titolo)
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
