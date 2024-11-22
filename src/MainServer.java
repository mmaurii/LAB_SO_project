/**
 * La classe MainServer prende in input le informazioni necessarie a instanziare una nuova socket TCP
 * e avvia un nuovo Server sulla socket specificata mettendosi poi in attesa che termini.
 */
public class MainServer {
    public static void main(String[] args) {
        int portNumber = 9000;

        // Verifica se la porta è passata come parametro
        if (args.length >= 1) {
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Errore: inserisci un numero di porta per la socket.");
                return;
            }
        }

        Server server = new Server(portNumber);
        Thread threadServer = new Thread(server);
        threadServer.setName("server");
        threadServer.start();

        try {
            threadServer.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}