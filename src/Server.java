import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/**
 * Classe server
 */
public class Server implements Runnable {
    private final HashSet<Topic> topics = new HashSet<>();
    private final List<Thread> clients = new ArrayList<>();
    private Topic inspectedTopic = null;
    private boolean running = true;
    final int port;
    private ServerSocket serverSocket = null;


    public Server(int port) {
        this.port = port;
        Thread serverThread = new Thread(this);
        serverThread.start();
        this.commandLoop();

    }

    /**
     * Loop principale per il thread in ascolto dei comandi (main thread)
     */
    private void commandLoop() {
        Scanner input = new Scanner(System.in);
        while (running) {
            if (inspectedTopic == null) System.out.println("\n> Inserisci comando");
            else System.out.println("Inserisci comando (ispezionando " + inspectedTopic.getTitle() + ")");
            String command = input.nextLine();
            String[] parts = command.split(" ");
            if (inspectedTopic == null) {
                if (parts.length == 1) notInspecting(parts[0], null);
                if (parts.length == 2) notInspecting(parts[0], parts[1]);
            } else {
                if (parts.length == 1) inspecting(parts[0], null);
                if (parts.length == 2) inspecting(parts[0], parts[1]);
            }
        }
    }

    /**
     * Funzione eseguita quando viene avviato il thread
     */
    @Override
    public void run() {
        create();
    }

    public HashSet<Topic> getTopics() {
        return topics;
    }

    /**
     * Avvio del server thread, in attesa di connessioni dai client
     */
    private void create() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server avviato");
            while (!Thread.interrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nuova connessione da " + clientSocket.getInetAddress());
                    if (!Thread.interrupted()) {
                        // crea un nuovo thread per il nuovo socket
                        Thread handlerThread = new Thread(new ClientHandler(clientSocket, this));
                        handlerThread.start();
                        this.clients.add(handlerThread);
                    } else {
                        serverSocket.close();
                        break;
                    }
                } catch (SocketException e) {
                    if (!running) {
                        System.out.println("Server arrestato.");
                        break;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
            this.serverSocket.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Comandi per quando non si sta ispezionando un topic
     *
     * @param command   comando, prima parte del input utente
     * @param parameter valore per i comandi che hanno parametri
     */
    private void notInspecting(String command, String parameter) {
        switch (command) {
            case "quit" -> quit();
            case "show" -> show();
            case "inspect" -> inspect(parameter);
            default -> System.err.println("Comando non riconosciuto: " + command);
        }
    }

    /**
     * Comandi per quando si sta ispezionando un topic
     *
     * @param command   comando, prima parte del input utente
     * @param parameter valore per i comandi che hanno parametri
     */
    private void inspecting(String command, String parameter) {
        switch (command) {
            case "listall" -> listAll();
            case "end" -> end();
            case "delete" -> delete(Integer.parseInt(parameter));
            default -> System.err.println("Comando non riconosciuto: " + command);
        }
    }

    /**
     * Rimuove tutti i client connessi e arresta il server
     */
    private void quit() {
        running = false;
        System.out.println("Interruzione dei client connessi");
        for (Thread client : this.clients) {
            System.out.println("Interruzione client " + client);
            /*
             * client.interrupt() non è bloccante; una volta inviato il segnale
             * di interruzione proseguiamo con l'esecuzione, senza aspettare che "child"
             * termini
             */
            client.interrupt();
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra i topic, se ce ne sono
     */
    private void show() {
        if (topics.isEmpty()) System.err.println("Non sono presenti topic");
        else {
            System.out.println("Topics:");
            for (Topic t : topics) {
                System.out.println("\t" + "- " + t.getTitle());
            }
        }
    }

    /**
     * Imposta il topic ispezionato se è presente
     *
     * @param topic topic che si vuole ispezionare
     */
    private void inspect(String topic) {
        if (topic == null) {
            System.err.println("Inserisci topic da ispezionare");
        } else {
            inspectedTopic = getTopicFromTitle(topic);
            if (inspectedTopic == null) System.err.println("Topic " + topic + " non esiste");
        }
    }

    /**
     * Restituisce il topic dato il titolo
     *
     * @param title titolo del topic da restituire
     * @return Topic
     */
    private Topic getTopicFromTitle(String title) {
        for (Topic t : topics) {
            if (Objects.equals(t.getTitle(), title)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Elenca i messaggi in un topic, se ce ne sono
     */
    public void listAll() {
        ArrayList<Message> messages = inspectedTopic.getMessages();
        if (messages.isEmpty()) {
            System.err.println("Non ci sono messaggi");
        } else {
            System.out.println("Messaggi:");
            for (Message m : inspectedTopic.getMessages()) {
                System.out.println("\t" + "- " + m.getID() + "\n" + "\t" + m.getText());

            }
        }
    }

    /**
     * Termina la sessione interattiva
     */
    public void end() {
        inspectedTopic = null;
    }

    /**
     * Cancella messaggi dal topic ispezionato
     *
     * @param id ID del messaggio che si vuole cancellare
     */
    private void delete(int id) {
        // da cambiare in base a come decidiamo di fare l'id
        ArrayList<Message> messages = inspectedTopic.getMessages();
        int initialSize = messages.size();
        // Costrutto simile all'Iterator per rimuovere con
        // sicurezza un elemento da una lista se soddisfa una condizione
        synchronized (messages) {
            messages.removeIf(m -> m.getID() == id);
        }
        // confronto le dimensioni della lista per capire se è stato cancellato un elemento
        if (initialSize == messages.size()) {
            System.err.println("Messaggio con id " + id + " non esiste");
        } else System.out.println("Messaggio eliminato");

    }

    // Aggiunge un nuovo topic
    public synchronized Topic addTopic(Topic topic) {
        for (Topic t : topics) {
            if (topic.equals(t)) {
                return t;
            }
        }

        topics.add(topic);
        return topic;
    }

    // Aggiunge un messaggio a un topic
    public synchronized void addMessageToTopic(Topic topic, Message message) {
        List<Message> messages = topic.getMessages();
        if (messages != null) {
            messages.add(message);
            //notifySubscribers(topic, message); // Notifica i subscriber
        }
    }

    private void notifySubscribers(String topic, Message message) {
    }

    // Aggiunge un subscriber per un determinato topic
    public synchronized Topic addSubscriber(ClientHandler client, Topic topic) {
        for (Topic t : topics) {
            if (topic.equals(t)) {
                t.getClients().add(client);
                return t;
            }
        }
        return null;
    }
}
