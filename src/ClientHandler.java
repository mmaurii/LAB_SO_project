import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    // 0 = publisher, 1 = subscriber
    private Integer mode = null;
    private Topic topic = null;

    public ClientHandler(Socket socket, Server server) {
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
                if (request.indexOf(' ') == -1) {
                    command = request;
                } else {
                    command = request.substring(0, request.indexOf(' '));
                    parameter = request.substring(request.indexOf(' ') + 1);
                }
                System.out.println(command);
                switch (command) {
                    case "publish" -> publish(parameter, clientReply);
                    case "subscribe" -> subscribe(parameter, clientReply);
                    case "show" -> show(clientReply);
                    case "quit" -> {
                        running = false;
                        clientReply.println("quit");
                    }

                    case "send" -> send(parameter, clientReply);
                    case "list" -> list(clientReply);

                    case "listall" -> listAll(clientReply);
                    default -> clientReply.println("Comando invalido");
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

    private void publish(String parameter, PrintWriter reply) {
        if (mode == null && !Objects.equals(parameter, "")) {
            reply.printf("Registrato publisher a topic %s\n", parameter);
            mode = 0;
            // <pubblicare il topic>

            reply.printf("Pubblicato: %s\n", parameter);
        } else {
            reply.println("Comando invalido");
        }
    }

    private void subscribe(String parameter, PrintWriter reply) {
        if (mode == null && !Objects.equals(parameter, "")) {
            reply.printf("Registrato subscriber a topic %s\n", parameter);
            mode = 1;

            reply.printf("Iscritto: %s\n", parameter);
        } else {
            reply.println("Comando invalido");
        }
    }

    private void show(PrintWriter reply) {
        List<Topic> lTopic = server.getTopics();
        String output = "";

        if(!lTopic.isEmpty()) {
            output = "Lista dei topic presenti:";
            for (Topic t : lTopic) {
                output += "\n\t- " + t.getTitle();
            }
        }else {
            output = "non ci sono topic disponibili";
        }

        reply.println(output);
    }

    // 0 = publisher, 1 = subscriber
    private boolean isPublisherCommand(String command, PrintWriter reply) {
        if (command.equals("send") && mode == 0) return true;
        if (command.equals("list") && mode == 0) return true;

        reply.println("Comando invalido");
        return false;
    }

    private void send(String message, PrintWriter reply) {
        if (isPublisherCommand("send", reply)) {
            if (message.isEmpty()) {
                reply.println("Manca il parametro");
            } else {
                // funzionalità
            }
        }
    }

    private void list(PrintWriter reply) {
        if (isPublisherCommand("list", reply)) {
            // funzionalità
        }
    }

    private void listAll(PrintWriter reply) {
        // funzionalità
    }
}