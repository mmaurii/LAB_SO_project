import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class SocketListener implements Runnable {
    private final Server server;

    public SocketListener(Server server) {
        Thread listenerThread = new Thread(this);
        listenerThread.start();
        this.server = server;
    }

    /**
     * Avvio del server thread, in attesa di connessioni dai client
     */
    public void run() {
        try {
            System.out.println("Server avviato");
            while (!Thread.interrupted()) {
                try {
                    Socket clientSocket = server.getSocket().accept();
                    System.out.printf("Nuova connessione da %s\n", clientSocket.getInetAddress());
                    if (!Thread.interrupted()) {
                        // crea un nuovo thread per il nuovo socket
                        ClientHandler ch = new ClientHandler(clientSocket, server);
                        new Thread(ch).start();
                        server.addClient(ch);
                    } else {
                        server.getSocket().close();
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
            server.getSocket().close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
