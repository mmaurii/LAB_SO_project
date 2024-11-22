import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * La classe Receiver sta in ascolto sulla socket e attende che arrivino messaggi dal server per
 * presentarli poi a console
 */
public class Receiver extends Thread {
    Socket s;
    Sender sender;
    private final String quitCommand = "quit";

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
                if (line.equals(quitCommand)) {
                    System.out.println("Il server si e' disconneso");
                    break;
                }
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Il server si e' impropriamente disconnesso");
        } finally {
            sender.interrupt();
        }
    }
}
