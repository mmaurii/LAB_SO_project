import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

/**
 * La classe ClientHandler gestisce la comunicazione con uno specifico client garantendo in questo modo
 * una corretta e sicura interazione con le risorse della classe Server.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Resource resource;
    // false = publisher, true = subscriber
    private Boolean publishORSubscribe = null;
    private Topic topic = null;
    //elenco di tutti i messaggi inviati da questo topic
    private final ArrayList<Message> messages;
    private PrintWriter clientPW;
    private volatile boolean running = true;

    //definizione nomi comandi
    private final String sendCommand = "send";
    private final String quitCommand = "quit";
    private final String listCommand = "list";
    private final String listAllCommand = "listall";
    private final String publishCommand = "publish";
    private final String subscribeCommand = "subscribe";
    private final String showCommand = "show";

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.resource = server.getResource();
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

            //controllo se c'è un comando del client da leggere
            while (running && clientMessage.hasNextLine()) {
                commandReciver(clientMessage);
            }
        } catch (IOException e) {
            System.err.println("ClientHandler IOException: " + e);
        } finally {
            closeSocket();
        }
    }

    /**
     * Ciclo principale per gestire la ricezione di comandi del client.
     *
     * @param clientMessage comando inviato dal client
     */
    private void commandReciver(Scanner clientMessage) {
        String request;
        String command;
        String parameter = "";

        if (Thread.currentThread().isInterrupted()) {
            // Gestione interruzione
            System.out.println("Thread interrotto.");
            clientPW.println("Chiusura server, sconnessione in corso");
            return;
        }

        // gestione comandi
        request = clientMessage.nextLine();
        if (request.indexOf(' ') == -1) {
            command = request;
        } else {
            command = request.substring(0, request.indexOf(' '));
            parameter = request.substring(request.indexOf(' ') + 1).toLowerCase().trim();
        }

        switch (command) {
            case publishCommand -> publish(parameter);
            case subscribeCommand -> subscribe(parameter);
            case showCommand -> show();
            case quitCommand -> quit();
            case sendCommand -> send(parameter);
            case listCommand -> list();
            case listAllCommand -> listAll();
            default -> clientPW.printf("Comando non riconosciuto: %s\n", command);
        }
    }

    /**
     * Chiude la socket e gestisce eventuali errori di chiusura.
     */
    private void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.out.println("Connessione terminata per il thread " + Thread.currentThread());
            } catch (IOException e) {
                System.err.println("Errore di chiusura socket: " + e.getMessage());
            }
        }
    }


    /**
     * Registra il client come publisher
     *
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
        this.topic = resource.addTopic(new Topic(parameter));
    }

    /**
     * Registra il client come subscriber
     *
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

        this.topic = resource.addSubscriber(this, new Topic(parameter));

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
        String listOfTopics = resource.show();

        if (listOfTopics.isEmpty()) {
            clientPW.println("Non sono presenti topic");
        } else {
            clientPW.println(listOfTopics);
        }
    }

    /**
     * Controlla se un comando è eseguibile solo dai publisher
     *
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

        if (command.equals(sendCommand)) {
            return true;
        }

        if (command.equals(listCommand)) {
            return true;
        }

        clientPW.println("Comando inesistente");
        return false;
    }

    /**
     * Pubblica un messaggio sul topic del publisher
     *
     * @param text messaggio da inviare
     */
    private void send(String text) {
        if (isPublisherCommand(sendCommand)) {
            if (text.isEmpty()) {
                clientPW.println("Non puoi inviare un messaggio vuoto");
            } else {
                //creo il messaggio
                Message mess = new Message(text);
                synchronized (resource) {
                    //controllo se il server è in fase di ispezione
                    // Controllo se il topic in ispezione è lo stesso del topic di questo client
                    if (resource.equalsInpectedTopic(topic)) {
                        // Messaggio in attesa
                        clientPW.printf("Messaggio \"%s\" in attesa. Il server è in fase d'ispezione.\n", text);
                        Command command = new Command(sendCommand, mess, this);
                        resource.addCommand(command);
                        return;
                    }
                    // Invia il messaggio a tutti i subscriber
                    sendExecute(mess);
                    clientPW.printf("Inviato messaggio \"%s\"\n", text);
                }
            }

        }
    }

    /**
     * Invia un messaggio a tutti i publisher iscritti al topic
     *
     * @param message il messaggio che viene inviato
     */
    public synchronized void sendExecute(Message message) {
        // Invia il messaggio a tutti i subscriber
        topic.forwardToAll(message);
        addMessage(message);
    }

    /**
     * Invia una stringa al PrintWriter del ClientHandler
     *
     * @param text testo da inviare al PrintWriter
     */
    public void forward(String text) {
        clientPW.println(text);
    }

    /**
     * Prova a eseguire il comando list se il server non è
     * in fase di ispezione
     */
    private void list() {
        if (isPublisherCommand(listCommand)) {
            //controllo se il server è in fase di ispezione
            synchronized (resource) {
                // Controllo se sono in ispezione e se il topic in ispezione è lo stesso del topic di questo client
                if (resource.equalsInpectedTopic(topic)) {
                    clientPW.printf("Comando \"list\" in attesa. Il server è in fase d'ispezione.\n");
                    resource.addCommand(new Command(listCommand, this));
                    return;
                }

                listExecute();
            }
        }
    }

    /**
     * Elenca tutti i messaggi che questo publisher ha
     * pubblicato sul topic
     */
    public synchronized void listExecute() {
        StringBuilder stringBuilder;
        synchronized (messages) {
            if (messages.isEmpty()) {
                clientPW.println("Non ci sono messaggi");
                return;
            }

            // funzionalità
            stringBuilder = new StringBuilder("Messaggi:");
            for (Message mess : messages) {
                stringBuilder.append(mess.replyString());
            }
        }
        clientPW.println(stringBuilder);
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

        synchronized (resource) {
            // Controllo se sono in ispezione e se il topic in ispezione è lo stesso del topic di questo client
            if (resource.equalsInpectedTopic(topic)) {
                clientPW.printf("Comando \"listall\" in attesa. Il server è in fase d'ispezione.\n");
                resource.addCommand(new Command(listAllCommand, this));
                return;
            }

            listallExecute();
        }
    }

    /**
     * Elenca tutti i messaggi inviati sul topic
     */
    public synchronized void listallExecute() {
        String result = resource.listAll(topic);

        if (result.isEmpty()) {
            System.out.println("Non ci sono messaggi");
        } else {
            clientPW.println(result);
        }
    }

    /**
     * Interrompe la connessione al server per questo client.
     */
    public synchronized void quit() {
        running = false; //meglio usare l'interrupt?
        clientPW.println("Terminata la connessione al server.");
        clientPW.close();
    }

    /**
     * Cancella un messaggio su un certo topic
     *
     * @param id id del messaggio da cancellare
     * @param t  topic dove si trova il messaggio da cancellare
     */
    public void delMessage(Topic t, int id) {           //synchronized
        //synchronized (messages) {//inutile
        // l'operazione non viene eseguita se il topic passato come parametro
        // è diverso da quello del client
        if (topic == t) {
            messages.removeIf(m -> m.getID() == id);
        }
        //}
    }

    /**
     * Aggiunge un messaggio sia alla lista dei messaggi inviati
     * dal client, che a quelli presenti sul topic del publisher
     *
     * @param mess messaggio inviato
     */
    private void addMessage(Message mess) {
        //Salva il messaggio
        synchronized (messages) {
            messages.add(mess);
            topic.addMessage(mess);
        }
    }
}