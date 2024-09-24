import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private static int idCounter = 0;
    private int id;
    private String text;
    private LocalDateTime sendDate;
    final String DATE_TIME_FORMAT = "dd/MM/yyyy - kk:mm:ss";

    public Message(String text) {
        this.id = ++idCounter;
        this.text = text;
        this.sendDate = LocalDateTime.now();
    }

    public int getID() {
        return id;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    @Override
    public String toString() {
        return "ID: " + id + "\nTesto: " + text + "\nData: " +
                sendDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public String replyString() {
        return String.format("\t- ID: %s\n\t  Testo: %s\n\t  Data: %s",
                id, text, sendDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
    }
}