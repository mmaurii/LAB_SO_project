import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    private Socket socket;
    private HashMap<String, String> response = new HashMap<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        // Risposte
        response.put("important", "Incredibly important bit of response about everything");
        response.put("random", "Random bit of response about something");
        response.put("shadow", "The outer part of a shadow is called the penumbra");
    }

    @Override
    public void run() {
        try (
                Scanner clientMessage = new Scanner(socket.getInputStream());
                PrintWriter clientReply = new PrintWriter(socket.getOutputStream(), true)
        ) {

            boolean closed = false;
            while (!closed) {
                if (Thread.currentThread().isInterrupted()) {
                    // Gestione interruzione
                    System.out.println("Thread interrotto.");
                    clientReply.println("Chiusura server, sconnessione in corso");
                    break;
                }

                // Controllo input da parte del client

                String request = clientMessage.nextLine();
                System.out.println("Request: " + request);
                String[] parts = request.split(" ");

                switch (parts[0]) {
                    case "quit":
                        closed = true;
                        break;

                    case "info":
                        if (parts.length > 1) {
                            String key = parts[1];
                            String res = response.getOrDefault(key, "Errore, no info");
                            clientReply.println(res);
                        } else {
                            clientReply.println("Nessun parametro fornito");
                        }
                        break;

                    default:
                        clientReply.println("Comando sconosciuto");
                        break;
                }

            }

        } catch (IOException e) {
            System.err.println("ClientHandler: IOException caught: " + e);
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                System.out.println("Connessione terminata per il thread " + Thread.currentThread());
            } catch (IOException e) {
                System.err.println("Errore di chiusura socket: " + e.getMessage());
            }
        }
    }
}
