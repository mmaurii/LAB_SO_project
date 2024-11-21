import java.io.*;
import java.net.Socket;

/**
 * La classe Sender prende in input da console messaggi e comandi e li invia al server
 */
public class Sender extends Thread {
    Socket s;
    private final String quitCommand = "quit";

    public Sender(Socket s) {
        this.s = s;
    }

    /**
     * Invia al ClientHandler l'input da tastiera dell'utente
     */
    @Override
    public void run() {
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        try {
            PrintWriter to = new PrintWriter(this.s.getOutputStream(), true);
            while (!Thread.interrupted()) {
                    String request = bf.readLine();
                    /*
                     * se il thread è stato interrotto mentre leggevamo l'input da tastiera, inviamo
                     * "quit" al server e usciamo
                     */
                    if (Thread.interrupted()) {
                        break;
                    }
                    // in caso contrario proseguiamo e spediamo l'input inserito
                    to.println(request);
                    if (request.equals(quitCommand)) {
                        break;
                    }
            }
            System.out.println("Connessione terminata");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
