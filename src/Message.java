import java.time.LocalDateTime;

public class Message {
    private static int idCounter = 0;
    private int id;
    private String testo;
    private LocalDateTime dataInvio;

    public Messaggio(String testo) {
        this.id = ++idCounter;
        this.testo = testo;
        this.dataInvio = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getTesto() {
        return testo;
    }

    public LocalDateTime getDataInvio() {
        return dataInvio;
    }

    @Override
    public String toString() {
        return "ID: " + id + "\nTesto: " + testo + "\nData: " + dataInvio;
    }
}