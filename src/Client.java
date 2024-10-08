import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.err.println("Usage: java Client <host> <port>");
//            return;
//        }

//        String host = args[0];
        String host = "127.0.0.1";
//        int port = Integer.parseInt(args[1]);
        int port = 9000;

        try {
            Socket s = new Socket(host, port);
            System.out.println("Connesso al server");

            /*
             * Delega la gestione di input/output a due thread separati, uno per inviare
             * messaggi e uno per leggerli
             */
            Sender sender = new Sender(s);
            Receiver receiver = new Receiver(s, sender);
            sender.start();
            receiver.start();

            try {
                /* rimane in attesa che sender e receiver terminino la loro esecuzione */
                sender.join();
                receiver.join();
                s.close();
                System.out.println("Socket closed.");
            } catch (InterruptedException e) {
                System.err.println("Thread Interrotto");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: inserisci un numero di porta valido");
        } catch (IOException e) {
            System.out.println("Error: Il server è irraggiungibile provare cambiando host e port number");
        }
    }
}
