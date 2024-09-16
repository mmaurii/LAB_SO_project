import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Publisher extends Client implements Runnable {
    private Topic topic;

    public Publisher(Socket s, Topic topic) {
        super(s);
        this.topic = topic;
    }

    public Topic getTopic() {
        return topic;
    }

    @Override
    synchronized public void run() {//adattare a publisher
        try {
            Scanner from = new Scanner(this.socket.getInputStream());
            System.out.println("Messaggi:");
            while (true) {
                String response = from.nextLine();
                System.out.println(response);
                if (response.equals("quit")) {
                    break;
                }

            }
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            System.out.println("Receiver closed.");
        }
    }

    // Invia un messaggio al server attraverso il client
    public void inviaMessaggio(String testoMessaggio, PrintWriter output) {
        output.println("send " + topic.getTitle() + " " + testoMessaggio);
    }

    // Richiede la lista dei messaggi inviati da questo publisher su questo topic
    public void richiediMessaggiPersonali(PrintWriter output) {
        output.println("list " + topic.getTitle());
    }

    // Richiede la lista di tutti i messaggi inviati su questo topic
    public void richiediMessaggiTotali(PrintWriter output) {
        output.println("listall " + topic.getTitle());
    }
}