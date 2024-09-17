import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private Integer mode;
    public ClientHandler(Socket socket,Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
                Scanner clientMessage = new Scanner(socket.getInputStream());
                PrintWriter clientReply = new PrintWriter(socket.getOutputStream(), true)
        ) {

            boolean running = true;
            while (running) {
                if (Thread.currentThread().isInterrupted()) {
                    // Gestione interruzione
                    System.out.println("Thread interrotto.");
                    clientReply.println("Chiusura server, sconnessione in corso");
                    break;
                }
                    // gestione comandi
                    String request = clientMessage.nextLine();
                    System.out.println("Request: " + request);
                    String command;
                    String parameter = "";
                    if (request.indexOf(' ') == -1){
                        command = request;
                    } else {
                        command = request.substring(0,request.indexOf(' '));
                        parameter = request.substring(request.indexOf(' ')+1);
                    }
                    clientReply.printf("Command: %s, Parameter: %s",command,parameter);
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
