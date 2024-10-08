import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Receiver extends Thread {
    Socket s;
    Sender sender;

    public Receiver(Socket s, Sender sender) {
        this.s = s;
        this.sender = sender;
    }

    /**
     * Resta in ascolto di comandi inviati alla socket.
     * Esce dal ciclo quando viene inviato il comando quit.
     */
    @Override
    public void run() {
        try {
            BufferedReader from = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
            String line;
            while ((line = from.readLine()) != null) {
                System.out.println(line);
                //System.out.println("Received: " + line);
                if (line.equals("quit")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            sender.interrupt();
            System.out.println("Receiver closed.");
        }
    }
}
