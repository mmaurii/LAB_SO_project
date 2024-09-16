public class Publisher {
    private Topic topic;

    public Publisher(Topic topic) {
        this.topic = topic;
    }

    public Topic getTopic() {
        return topic;
    }

    // Invia un messaggio al server attraverso il client
    public void inviaMessaggio(String testoMessaggio, PrintWriter output) {
        output.println("send " + topic.getNome() + " " + testoMessaggio);
    }

    // Richiede la lista dei messaggi inviati da questo publisher su questo topic
    public void richiediMessaggiPersonali(PrintWriter output) {
        output.println("list " + topic.getNome());
    }

    // Richiede la lista di tutti i messaggi inviati su questo topic
    public void richiediMessaggiTotali(PrintWriter output) {
        output.println("listall " + topic.getNome());
    }
}