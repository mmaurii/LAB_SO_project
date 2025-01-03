import java.io.*;
import java.net.Socket;

/**
 * La classe Sender prende in input da console messaggi e comandi e li invia al server
 */
public class Sender extends Thread {
    Socket socket;
    private final String quitCommand = "quit";

    public Sender(Socket socket) {
        this.socket = socket;
    }

    /**
     * Rimane in attesa di messaggi dell'utente da console per inviarli al ClientHandler associato
     */
    @Override
    public void run() {
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        try {
            PrintWriter to = new PrintWriter(this.socket.getOutputStream(), true);
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
