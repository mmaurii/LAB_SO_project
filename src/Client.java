import java.io.IOException;
import java.net.Socket;

/**
 * La classe client si preoccupa di istanziare una socket, connettersi al server
 * e di gestire i thread Sender e Receiver
 */
public class Client {
    public static void main(String[] args) {
        // Impostazioni di default per host e porta
        String host = "127.0.0.1";
        int port = 9000;

        // Verifica se host e porta sono passati come argomenti
        if (args.length >= 2) {
            host = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Errore: inserisci un numero di porta.");
                return;
            }
        }

        // Gestione connessione
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connesso al server");

            // Creazione e avvio dei thread per invio e ricezione messaggi
            Sender sender = new Sender(socket);
            Receiver receiver = new Receiver(socket, sender);

            sender.start();
            receiver.start();

            // Attesa della fine dei thread
            sender.join();
            receiver.join();


            //client chiuso
            System.out.println("Client chiuso");
        } catch (IOException e) {
            System.err.println("Errore: Il server è irraggiungibile, " +
                    "riprovare cambiando host e numero di porta.");
        } catch (InterruptedException e) {
            System.err.println("Errore: Thread interrotto.");
        }
    }
}