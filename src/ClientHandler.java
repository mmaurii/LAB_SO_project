import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/**
 * La classe ClientHandler gestisce la comunicazione con uno specifico client garantendo in questo modo
 * una corretta e sicura interazione con le risorse della classe Resource.
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
    private boolean running = true;
    private final Object runningLock = new Object();

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
     * Loop principale per la comunicazione tra client e server
     */
    @Override
    public void run() {
        try {
            BufferedReader clientMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientPW = new PrintWriter(socket.getOutputStream(), true);
            String request;

            //controllo se c'è un comando del client da leggere e che la comunicazione non sia stata interrotta
            while (isRunning() && (request = clientMessage.readLine()) != null) {
                String command;
                String parameter = "";

                if (Thread.currentThread().isInterrupted()) {
                    // Gestione interruzione
                    System.out.println("Thread interrotto.");
                    clientPW.println("Chiusura server, sconnessione in corso");
                    return;
                }

                //elaborazione input
                if (request.indexOf(' ') == -1) {
                    command = request;
                } else {
                    command = request.substring(0, request.indexOf(' '));
                    parameter = request.substring(request.indexOf(' ') + 1).toLowerCase().trim();
                }

                // gestione comandi
                switch (command) {
                    case publishCommand -> publish(parameter);
                    case subscribeCommand -> subscribe(parameter);
                    case showCommand -> show(parameter);
                    case quitCommand -> {
                        if (!Objects.equals(parameter, "")) {
                            clientPW.println("Questo comando non accetta parametri");
                        } else {
                            quit();
                            System.out.println("Interruzione client " + this);
                        }
                    }
                    case sendCommand -> send(parameter);
                    case listCommand -> list(parameter);
                    case listAllCommand -> listAll(parameter);
                    default -> clientPW.printf("Comando non riconosciuto: %s\n", command);
                }
            }
            clientPW.close();
            clientMessage.close();
        } catch (SocketException se) {
            //controllo se l'errore si è verificato perchè la comunicazione è stata interrotta da un comando quit
            if (isRunning()) {
                System.err.println("ClientHandler SocketException: " + se);
                System.out.println("Il client " + this + " si è impropriamente disconnesso");
                System.out.println("Rilascio tutte le risorse associate al client...");
                quit();
            }
        } catch (IOException e) {
            System.err.println("ClientHandler IOException: " + e);
        } finally {
            releaseResources();
//            if (!socket.isClosed()) {
//                closeSocket();
//            }
        }
    }

    /**
     * Chiude la socket e gestisce eventuali errori di chiusura.
     */
    private void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Errore di chiusura socket: " + e.getMessage());
            }
        }
    }


    /**
     * Registra il client associato come publisher
     *
     * @param parameter stringa contenente il nome del topic su cui si vuole scrivere
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
        // false = publisher, true = subscriber
        publishORSubscribe = false;
        this.topic = resource.addTopic(new Topic(parameter));
    }

    /**
     * Registra il client associato come subscriber
     *
     * @param parameter stringa contenente il nome del topic a cui ci si vuole iscrivere
     */
    private void subscribe(String parameter) {
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
        // false = publisher, true = subscriber
        publishORSubscribe = true;
    }

    /**
     * elenca i topic presenti sul server, parameter deve essere vuoto perchè il metodo vada a buon fine
     *
     * @param parameter stringa contenente eventuali parametri ricevuti col comando show
     */
    private void show(String parameter) {
        if (!Objects.equals(parameter, "")) {
            clientPW.println("Questo comando non accetta parametri");
            return;
        }
        String listOfTopics = resource.show();

        if (listOfTopics.isEmpty()) {
            clientPW.println("Non sono presenti topic");
        } else {
            clientPW.println(listOfTopics);
        }
    }

    /**
     * Controlla se un comando è eseguibile dai publisher
     *
     * @param command stringa contente il nome del comando su cui fare il controllo
     * @return true se il comando è eseguibile dai publisher, false alrimenti
     */
    private boolean isPublisherOrSubscribeCommand(String command) {
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
     * Pubblica un messaggio corrispondente a "text" sul topic del publisher,
     * se il server è in fase di ispezione mette il comando in coda per essere eseguito al termine dell'ispezione
     *
     * @param text stringa contenente il messaggio da inviare
     */
    private void send(String text) {
        if (isPublisherOrSubscribeCommand(sendCommand)) {
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
     * Invia un messaggio a tutti i subscriber iscritti al topic a cui appartiene questo publisher
     *
     * @param message messaggio da inviare
     */
    public void sendExecute(Message message) {
        // Invia il messaggio a tutti i subscriber
        topic.forwardToAll(message);
        addMessage(message);
    }

    /**
     * Invia una stringa al client associato al ClientHandler
     *
     * @param text stringa contennete il testo da inviare al client
     */
    public synchronized void forward(String text) {
        clientPW.println(text);
    }

    /**
     * Manda un elenco di tutti i messaggi, che il publisher associato a questo ClientHandler
     * ha inviato su questo topic, al publier che li ha inviati.
     * Se il server è in fase di ispezione mette il comando in coda per essere eseguito al termine dell'ispezione.
     * parameter deve essere vuoto perchè il metodo vada a buon fine
     *
     * @param parameter stringa contenente eventuali parametri ricevuti col comando list
     */
    private void list(String parameter) {
        if (!Objects.equals(parameter, "")) {
            clientPW.println("Questo comando non accetta parametri");
            return;
        }

        if (isPublisherOrSubscribeCommand(listCommand)) {
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
     * Manda un elenco di tutti i messaggi, che il publisher associato a questo ClientHandler
     * ha inviato su questo topic, al publier che li ha inviati
     */
    public void listExecute() {
        StringBuilder stringBuilder;
        if (messages.isEmpty()) {
            clientPW.println("Non ci sono messaggi");
            return;
        }

        stringBuilder = new StringBuilder("Messaggi inviati da questo client:");
        for (Message mess : messages) {
            stringBuilder.append(mess.replyString());
        }
        clientPW.println(stringBuilder);
    }

    /**
     * Manda un elenco di tutti i messaggi scambiati su questo topic al client associato a questo ClientHandler.
     * Se il server è in fase di ispezione mette il comando in coda per essere eseguito al termine dell'ispezione.
     * Parameter deve essere vuoto perchè il metodo vada a buon fine
     *
     * @param parameter stringa contenente eventuali parametri ricevuti col comando listAll
     */
    private void listAll(String parameter) {
        if (!Objects.equals(parameter, "")) {
            clientPW.println("Questo comando non accetta parametri");
            return;
        }

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
     * Manda un elenco di tutti i messaggi scambiati su questo topic al client associato a questo ClientHandler.
     */
    public void listallExecute() {
        String result = resource.listAll(topic);

        if (result.isEmpty()) {
            System.out.println("Non ci sono messaggi");
        } else {
            clientPW.println(result);
        }
    }

    /**
     * Interrompe la connessione al server per il client associato al ClientHandler.
     */
    public synchronized void quit() {
        synchronized (runningLock) {
            running = false;
        }
        clientPW.println("Server Disconnesso");
        closeSocket();
    }

    /**
     * Rilascia tutte le risorse associate a questo ClientHandler
     */
    private void releaseResources() {
        if (topic != null) {
            topic.removeSubscriber(this);
        }
        resource.removeClient(this);
    }

    /**
     * verifica in maniera sincrona se la variabile running è vera o falsa
     *
     * @return true se running è vera false altrimenti
     */
    private Boolean isRunning() {
        synchronized (runningLock) {
            return running;
        }
    }

    /**
     * Cancella un messaggio relativo a un certo topic dalla copia locale di questo ClientHandler se presente
     *
     * @param t  topic dove si trova il messaggio da cancellare
     * @param id del messaggio da cancellare
     */
    public void delMessage(Topic t, int id) {
        // l'operazione non viene eseguita se il topic passato come parametro
        // è diverso da quello del client
        if (topic == t) {
            messages.removeIf(m -> m.getID() == id);
        }
    }

    /**
     * Aggiunge un messaggio sia alla lista locale dei messaggi inviati
     * dal client associato a questo ClientHandler, che a quelli presenti sul topic del publisher
     *
     * @param mess messaggio ricevuto dal client associato a questo ClientHandler
     */
    private void addMessage(Message mess) {
        //Salva il messaggio
        messages.add(mess);
        topic.addMessage(mess);
    }
}