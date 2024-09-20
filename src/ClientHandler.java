import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    // false = publisher, true = subscriber
    private Boolean publishORSubscribe = null;
    private Topic topic = null;
    private List<Message> messages;
    private PrintWriter clientPW;
    private boolean running = true;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.messages = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            Scanner clientMessage = new Scanner(socket.getInputStream());
            clientPW = new PrintWriter(socket.getOutputStream(), true);

            //controllo che ci sia un comando del client da leggere
            while (running && clientMessage.hasNextLine()) {
                if (Thread.currentThread().isInterrupted()) {
                    // Gestione interruzione
                    System.out.println("Thread interrotto.");
                    clientPW.println("Chiusura server, sconnessione in corso");
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
                    case "publish" -> publish(parameter);
                    case "subscribe" -> subscribe(parameter);
                    case "show" -> show();
                    case "quit" -> quit();

                    case "send" -> send(parameter);
                    case "list" -> list();

                    case "listall" -> listAll();
                    default -> clientPW.println("Comando invalido");
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

    private void publish(String parameter) {
        if (publishORSubscribe == null && !Objects.equals(parameter, "")) {
            clientPW.printf("Registrato publisher a topic %s\n", parameter);
            publishORSubscribe = false;
            // <pubblicare il topic>

            this.topic = server.addTopic(new Topic(parameter));

            clientPW.printf("Pubblicato: %s\n", parameter);
        } else {
            clientPW.println("Comando invalido");
        }
    }

    private void subscribe(String parameter) {
        if (publishORSubscribe == null && !Objects.equals(parameter, "")) {
            this.topic = server.addSubscriber(this, new Topic(parameter));

            //controllo l'esistenza del topic
            if (topic == null) {
                clientPW.println("Il topic inserito non esiste");
            } else {
                clientPW.printf("Registrato subscriber a topic %s\n", parameter);
                clientPW.printf("Iscritto: %s\n", parameter);
                publishORSubscribe = true;
            }
        } else {
            clientPW.println("Comando invalido");
        }
    }

    private void show() {
        HashSet<Topic> lTopic = server.getTopics();
        String output = "";

        if (!lTopic.isEmpty()) {
            output = "Lista dei topic presenti:";
            for (Topic t : lTopic) {
                output += "\n\t- " + t.getTitle();
            }
        } else {
            output = "non ci sono topic disponibili";
        }

        clientPW.println(output);
    }

    // false = publisher, true = subscriber
    private boolean isPublisherCommand(String command) {
        if (command.equals("send") && !publishORSubscribe) return true;
        if (command.equals("list") && !publishORSubscribe) return true;

        clientPW.println("Comando invalido");
        return false;
    }

    private void send(String text) {
        if (isPublisherCommand("send")) {
            if (text.isEmpty()) {
                clientPW.println("Manca il parametro");
            } else {
                // funzionalità
                Message mess = new Message(text);

                //invio il messaccio a tutti i subscriber
                for (ClientHandler c : topic.getClients()) {
                    c.sendToClient(mess);
                }

                //salvo il messaggio
                messages.add(mess);
                topic.getMessages().add(mess);
            }
        }
    }

    public void sendToClient(Message message) {
        clientPW.println(message.toString());
    }

    private void list() {
        if (isPublisherCommand("list")) {
            // funzionalità
            for (Message mess : messages) {
                clientPW.println(mess.toString());
            }
        }
    }

    private void listAll() {
        // funzionalità
        for (Message mess : topic.getMessages()) {
            clientPW.println(mess.toString());
        }
    }
    public void quit(){
        running = false;
        clientPW.println("quit");
    }
}