import java.util.ArrayList;
import java.util.HashSet;

/**
 * La classe definisce un argomento di discussione su cui
 * possonono esssere scambiati messaggi, e a cui i subscribers possono iscriversi per riceverli
 */
public class Topic {
    //nome del topic
    private final String title;
    //lista di tutti i messaggi che sono stati scambiati su questo topic
    private final ArrayList<Message> messages = new ArrayList<>();
    final Object messagesLock = new Object();
    //lista di tutti i subscribers che hanno scelto questo topic
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


    /**
     * Il metodo restituisce un elenco di tutti i messaggi scambiati su questo topic
     * e il numero di messaggi scambiati
     *
     * @return una stringa pronta per l'output contenente il numero di messaggi scambiati e l'elenco dei relativi messaggi
     */
    public String getStringMessages(){
        synchronized (messagesLock) {
            if (messages.isEmpty()) {
                return "";
            } else {
                StringBuilder sb = new StringBuilder();

                sb.append("Sono stati inviati " + messages.size() + " messaggi in questo topic.\n");
                sb.append("Messaggi inviati:");
                for (Message m : messages) {
                    sb.append(m.replyString());
                }

                return sb.toString();
            }
        }
    }

    /**
     * Aggiunge un messaggio a questo topic
     *
     * @param message messaggio da aggiungere al topic
     */
    public void addMessage(Message message) {
        synchronized (messagesLock) {
            messages.add(message);
        }
    }

    /**
     * Rimuove il messaggio con l'id preso come parametro da questo topic
     *
     * @param id intero e univoco relativo al messaggio da eliminare
     * @return true se l'elemento è stato eliminato
     */
    public boolean removeMessage(int id) {
        synchronized (messagesLock) {
            return messages.removeIf(m -> m.getID() == id);
        }
    }

    /**
     * Aggiunge un subscriber a questo topic
     * @param subscriber subscriber da aggiungere
     */
    public void addSubscriber(ClientHandler subscriber) {
        synchronized (subscribersLock){
            subscribers.add(subscriber);
        }
    }

    /**
     * Invia un messaggio a tutti i subscribers di questo topic
     *
     * @param message da inviare
     */
    public void forwardToAll(Message message) {
        synchronized (subscribersLock) {
            for (ClientHandler c : subscribers) {
                c.forward("Nuovo messaggio pubblicato");
                c.forward(message.toString());
            }
        }
    }

    /**
     * Rimuove un subscriber dall'elenco dei subscribers iscritti a questo topic
     * @param subscriber da rimuovere
     */
    public void removeSubscriber(ClientHandler subscriber) {
        synchronized (subscribersLock) {
            subscribers.remove(subscriber);
        }
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
}
