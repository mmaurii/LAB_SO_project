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
    private final List<Message> messages;
    private PrintWriter clientPW;
    private boolean running = true;
    private final Object sendLock = new Object(); // Oggetto di sincronizzazione

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.messages = new ArrayList<>();
    }

    /**
     * Loop principale per la comunicazione tra client (tramite sender e receiver) e server
     */
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

    /**
     * Registra il client come publisher
     * @param parameter topic che si vuole pubblicare
     */
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

    /**
     * Registra il client come subscriber
     * @param parameter topic a cui ci si vuole iscrivere
     */
    private void subscribe(String parameter) {
        // false = publisher, true = subscriber
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

    /**
     * elenca i topic presenti
     */
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

    /**
     * Controlla se un comando è eseguibile solo dai publisher
     * @param command comando sui fare il controllo
     * @return true se il comando è eseguibile solo dal publisher
     */
    private boolean isPublisherCommand(String command) {
        // false = publisher, true = subscriber
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

    /**
     * Pubblica un messaggio sul topic del publisher
     * @param text messaggio da inviare
     */
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

    /**
     * Invia un messaggio a tutti i publisher iscritti al topic
     * @param message il messaggio che viene inviato
     */
    public void sendExecute(Message message) {
        // Invia il messaggio a tutti i subscriber
        for (ClientHandler c : topic.getClients()) {
            c.forward(message.toString());
        }
        addMessage(message);
    }

    /**
     * Invia una stringa al PrintWriter del ClientHandler
     * @param text testo da inviare al PrintWriter
     */
    private void forward(String text) {
        clientPW.println(text);
    }

    /**
     * Prova a eseguire il comando list se il server non è
     * in fase di ispezione
     */
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

    /**
     * Elenca tutti i messaggi che questo publisher ha
     * pubblicato sul topic
     */
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

    /**
     * Prova ad eseguire il comando listAll se il server non è in fase
     * di ispezione
     */
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

    /**
     * Elenca tutti i messaggi inviati sul topic
     */
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

    /**
     * Interrompe la connessione al server per questo client.
     */
    public synchronized void quit() {
        running = false;

        clientPW.println("quit");
        clientPW.close();
    }

    /**
     * Cancella un messaggio su un certo topic
     * @param id id del messaggio da cancellare
     * @param t topic dove si trova il messaggio da cancellare
     */
    public synchronized void delMessage(Topic t, int id) {
        // l'operazione non viene eseguita se il topic passato come parametro
        // è diverso da quello del client
        if (topic == t) {
            messages.removeIf(m -> m.getID() == id);
        }
    }

    /**
     * Aggiunge un messaggio sia alla lista dei messaggi inviati
     * dal client, che a quelli presenti sul topic del publisher
     * @param mess messaggio inviato
     */
    public synchronized void addMessage(Message mess) {
        //Salva il messaggio
        messages.add(mess);
        topic.addMessage(mess);
    }
}