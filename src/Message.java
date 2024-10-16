import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * La classe definisce un messaggio di testo all'interno della comunicazione tra publisher e subscriber
 */
public class Message {
    private static int idCounter = 0;
    private final int id;
    private final String text;
    private final LocalDateTime sendDate;
    final String DATE_TIME_FORMAT = "dd/MM/yyyy - kk:mm:ss";

    public Message(String text) {
        synchronized (Message.class) {
            this.id = ++idCounter;
        }
        this.text = text;
        this.sendDate = LocalDateTime.now();
    }

    /**
     * @return restituisce l'id del messaggio
     */
    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return "ID: " + id + "\nTesto: " + text + "\nData: " +
                sendDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    /**
     * @return restituisce la stringa formattata, usata
     * nelle risposte ai comandi inviati
     */
    public String replyString() {
        return String.format("\t- ID: %s\n\t  Testo: %s\n\t  Data: %s",
                id, text, sendDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
    }
}