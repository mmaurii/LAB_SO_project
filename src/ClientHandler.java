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

    // Getter per sendLock
    public Object getSendLock() {
        return sendLock;
    }

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
                    default -> clientPW.println("Comando invalido");
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
        if (publishORSubscribe == null && !Objects.equals(parameter, "")) {

            clientPW.printf("Registrato come publisher al topic %s\n", parameter);
            publishORSubscribe = false;
            this.topic = server.addTopic(new Topic(parameter));

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
                clientPW.printf("Registrato come subscriber al topic %s\n", parameter);
                publishORSubscribe = true;
            }
        } else {
            clientPW.println("Comando invalido");
        }
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
            clientPW.println("Comando invalido");
            return false;
        }

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
                synchronized (sendLock) {
                    // Controlla se il topic in ispezione è lo stesso del topic attuale
                    if (server.getInspectedTopic() != null && server.getInspectedTopic().equals(topic)) {
                        // Messaggio in attesa
                        clientPW.printf("Messaggio \"%s\" in attesa. Il server è in fase d'ispezione.\n", text);

//                        try {
////                             Mette in attesa il thread del client
//                            sendLock.wait();
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
//                            clientPW.println("Invio messaggio interrotto.");
//                            return;
//                        }
                    }

                    //creo il messaggio
                    Message mess = new Message(text);
                    //controllo se il server è in fase di ispezione
                    if (server.isInspectedLock()) {
                        Command command = new Command("send", mess, this);
                        server.addCommand(command);
                    } else {
                        // Invia il messaggio a tutti i subscriber
                        sendToClient(mess);

                        addMessage(mess);

                        clientPW.printf("Inviato messaggio \"%s\"\n", text);
                    }
                }
            }
        }
    }


    public synchronized void sendToClient(Message message) {
        // Invia il messaggio a tutti i subscriber
        for (ClientHandler c : topic.getClients()) {
            c.forward(message.toString());
        }

        addMessage(message);
    }

    private void forward(String text){
        clientPW.println(text);
    }

    public void list() {
        if (isPublisherCommand("list")) {
            //controllo se il server è in fase di ispezione
            if (server.isInspectedLock()) {
                server.addCommand(new Command("list", this));
            } else {
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
        }
    }

    public void listAll() {
        if (server.isInspectedLock()) {

            server.addCommand(new Command("listall", this));
        } else {
            if (topic == null) {
                clientPW.println("Comando invalido");
                return;
            }
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
    }

    public void quit() {
        running = false;
        clientPW.println("quit");
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