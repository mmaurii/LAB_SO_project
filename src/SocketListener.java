import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * La classe SocketListener permette di mettersi in ascolto su una socket per eventuali
 * nuovi collegamenti da parte di un client. Una volta stabilita la connessione la
 * comunicazione viene lasciata in gestione a un nuovo thread appositamente istanziato
 * dalla classe ClientHandler
 */
public class SocketListener implements Runnable {
    private final Server server;
    private final ServerSocket serverSocket;

    public SocketListener(Server server, int port) {
        this.server = server;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Avvio del server thread, in attesa di connessioni dai client
     */
    @Override
    public void run() {
        try {
            System.out.println("Server avviato");
            while (!Thread.interrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.printf("Nuova connessione da %s\n", clientSocket.getInetAddress());
                    if (!Thread.interrupted()) {
                        // crea un nuovo thread che gestisca la comunicazione istanziata
                        ClientHandler ch = new ClientHandler(clientSocket, server);
                        new Thread(ch).start();
                        server.addClient(ch);
                    } else {
                        serverSocket.close();
                        break;
                    }
                } catch (SocketException e) {
                    if (!server.isRunning()) {
                        System.out.println("Server arrestato.");
                        break;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
            serverSocket.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Chiude la socket su cui il server è in ascolto per nuovi collegamenti di client
     */
    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
