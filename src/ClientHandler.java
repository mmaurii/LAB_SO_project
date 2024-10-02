import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    // false = publisher, true = subscriber
    private Boolean publishORSubscribe = null;
    private Topic topic = null;
    private List<Message> messages;
    private PrintWriter clientPW;
    private boolean running = true;
    private final Object sendLock = new Object(); // Oggetto di sincronizzazione

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
                System.out.printf("<Ricevuto comando \"%s\">\n", request);
                String command;
                String parameter = "";
                if (request.indexOf(' ') == -1) {
                    command = request;
                } else {
                    command = request.substring(0, request.indexOf(' '));
                    parameter = request.substring(request.indexOf(' ') + 1).toLowerCase().trim();
                }
                switch (command) {
                    case "publish" -> publish(parameter);
                    case "subscribe" -> subscribe(parameter);
                    case "show" -> show();
                    case "quit" -> quit();
                    case "send" -> send(parameter);
                    case "list" -> list();
                    case "listall" -> listAll();
                    default -> clientPW.printf("Comando non riconosciuto: %s\n", command);
                }
            }

        } catch (IOException e) {
            System.err.println("ClientHandler IOException: " + e);
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
        if (publishORSubscribe != null) {
            clientPW.println("Non puoi più eseguire questo comando");
            return;
        }

        if (Objects.equals(parameter, "")) {
            clientPW.println("Inserisci il topic");
            return;
        }

        clientPW.printf("Registrato come publisher al topic %s\n", parameter);
        publishORSubscribe = false;
        this.topic = server.addTopic(new Topic(parameter));
    }

    // false = publisher, true = subscriber
    private void subscribe(String parameter) {
        if (publishORSubscribe != null) {
            clientPW.println("Non puoi più eseguire questo comando");
            return;
        }

        if (Objects.equals(parameter, "")) {
            clientPW.println("Inserisci il topic");
            return;
        }

        this.topic = server.addSubscriber(this, new Topic(parameter));

        if (topic == null) {
            clientPW.println("Il topic inserito non esiste");
            return;
        }
        clientPW.printf("Registrato come subscriber al topic %s\n", parameter);
        publishORSubscribe = true;
    }

    private void show() {
        HashSet<Topic> lTopic = server.getTopics();
        StringBuilder output;

        if (!lTopic.isEmpty()) {
            output = new StringBuilder("Topic presenti:");
            for (Topic t : lTopic) {
                output.append("\n\t- ").append(t.getTitle());
            }
        } else {
            output = new StringBuilder("Non ci sono topic disponibili");
        }

        clientPW.println(output);
    }

    // false = publisher, true = subscriber
    private boolean isPublisherCommand(String command) {
        if (publishORSubscribe == null) {
            clientPW.println("Devi essere registrato come publisher o subscriber per inviare questo comando");
            return false;
        }

        if (publishORSubscribe) {
            clientPW.println("Devi essere registrato come publisher per inviare questo comando");
            return false;
        }

        if (command.equals("send")) return true;
        if (command.equals("list")) return true;

        clientPW.println("Comando inesistente");
        return false;
    }

    private void send(String text) {
        if (isPublisherCommand("send")) {
            if (text.isEmpty()) {
                clientPW.println("Non puoi inviare un messaggio vuoto");
            } else {
//                synchronized (sendLock) {
                //creo il messaggio
                Message mess = new Message(text);
                //controllo se il server è in fase di ispezione
                if (server.isInspectedLock()) {
                    // Controlla se il topic in ispezione è lo stesso del topic di questo client
                    if (server.getInspectedTopic().equals(topic)) {
                        // Messaggio in attesa
                        clientPW.printf("Messaggio \"%s\" in attesa. Il server è in fase d'ispezione.\n", text);
                        Command command = new Command("send", mess, this);
                        server.addCommand(command);
                        return;
                    }
                }
                // Invia il messaggio a tutti i subscriber
                sendExecute(mess);
                clientPW.printf("Inviato messaggio \"%s\"\n", text);
//                }
            }
        }
    }

    public void sendExecute(Message message) {
        // Invia il messaggio a tutti i subscriber
        for (ClientHandler c : topic.getClients()) {
            c.forward(message.toString());
        }

        addMessage(message);
    }

    private void forward(String text) {
        clientPW.println(text);
    }

    private void list() {
        if (isPublisherCommand("list")) {
            //controllo se il server è in fase di ispezione
            if (server.isInspectedLock()) {
                // Controlla se il topic in ispezione è lo stesso del topic di questo client
                if (server.getInspectedTopic().equals(topic)) {
                    clientPW.printf("Comando \"list\" in attesa. Il server è in fase d'ispezione.\n");
                    server.addCommand(new Command("list", this));
                    return;
                }
            }
            listExecute();
        }
    }

    public void listExecute() {
        if (messages.isEmpty()) {
            clientPW.println("Non ci sono messaggi");
            return;
        }
        // funzionalità
        clientPW.println("Messaggi:");
        for (Message mess : messages) {
            clientPW.println(mess.replyString());
        }
    }

    private void listAll() {
        if (topic == null) {
            clientPW.println("Devi registrarti come publisher o subscriber " +
                    "prima di poter eseguire questo comando");
            return;
        }
        if (server.isInspectedLock()) {
            // Controlla se il topic in ispezione è lo stesso del topic di questo client
            if (server.getInspectedTopic().equals(topic)) {
                clientPW.printf("Comando \"listall\" in attesa. Il server è in fase d'ispezione.\n");
                server.addCommand(new Command("listall", this));
                return;
            }
        }
        listallExecute();
    }

    public void listallExecute() {
        if (topic.getMessages().isEmpty()) {
            clientPW.println("Non ci sono messaggi");
            return;
        }
        // funzionalità
        clientPW.println("Messaggi:");
        for (Message mess : topic.getMessages()) {
            clientPW.println(mess.replyString());
        }
    }

    public synchronized void quit() {
        running = false;

        clientPW.println("quit");
        clientPW.close();
    }

    public synchronized void delMessage(Topic t, int id) {
        if (topic == t) {
            messages.removeIf(m -> m.getID() == id);
        }
    }

    public synchronized void addMessage(Message mess) {
        //Salva il messaggio
        messages.add(mess);
        topic.addMessage(mess);
    }
}