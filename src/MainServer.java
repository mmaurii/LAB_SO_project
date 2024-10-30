/**
 * La classe MainServer prende in input le informazioni necessarie a instanziare una nuova socket TCP
 * e avvia un nuovo Server sulla socket specificata mettendosi poi in attesa che termini.
 */
public class MainServer {
    public static void main(String[] args) {
        final int portNumber = 9000;
        String localHost = "127.0.0.1";

                /*
        // Verifica se host e porta sono passati come argomenti (opzionale)
        if (args.length >= 2) {
            host = args[0];
            try {
                portNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Errore: inserisci un numero di porta.");
                return;
            }
        }
         */

        Server server = new Server(portNumber);
        Thread threadServer = new Thread(server);
        threadServer.start();

        try {
            threadServer.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}