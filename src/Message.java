import java.time.LocalDateTime;

public class Message {
    private static int idCounter = 0;
    private int id;
    private String text;
    private LocalDateTime sendDate;

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
        return "ID: " + id + "\nTesto: " + text + "\nData: " + sendDate;
    }
}