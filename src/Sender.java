import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                if (bf.ready()) {
                    String request = bf.readLine();
                    /*
                     * se il thread è stato interrotto mentre leggevamo l'input da tastiera, inviamo
                     * "quit" al server e usciamo
                     */
//                    if (Thread.interrupted()) {
//                        //to.println(quitCommand);
//                        break;
//                    }
                    // in caso contrario proseguiamo e spediamo l'input inserito
                    to.println(request);
                    if (request.equals(quitCommand)) {
                        break;
                    }
                }

                //controllo se il server si è disconnesso
//                if (Thread.interrupted()) {
////                    to.println(quitCommand);
////                    System.out.println("Il server si è disconnesso");
//                    break;
//                }
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
