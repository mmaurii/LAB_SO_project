import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Classe server
 */
public class Server implements Runnable {
    private final List<Topic> topics = new ArrayList<>();
    private final List<Client> clients = new ArrayList<>();
    private Topic inspectedTopic = null;
    private boolean running = true;

    public static void main(String[] args) {
        Server server = new Server();
        // Valori temporanei
        server.insertMessage(new Topic("cibo"));
        server.insertMessage(new Topic("musica"));
        server.insertMessage(new Topic("sport"));

        Thread serverThread = new Thread(server);
        serverThread.start();

        server.loop();
    }

    /**
     * Loop principale per il thread in ascolto dei comandi (main thread)
     */
    private void loop() {
        Scanner input = new Scanner(System.in);
        while (running) {
            if (inspectedTopic == null) {
                System.out.println("\n> Inserisci comando");
            } else {
                System.out.println("Inserisci comando (ispezionando " +
                        inspectedTopic.getTitle() + ")");
            }
            String command = input.nextLine();
            String[] parts = command.split(" ");

            if (inspectedTopic == null) {
                notInspecting(parts,command);
            } else {
                inspecting(parts,command);
            }
        }
    }

    /**
     * Gestione comandi default
     *
     * @param command comando diviso
     * @param commandString comando iniziale
     */
    private void notInspecting(String[] command, String commandString){
        if (command.length == 1 && Objects.equals(command[0], "quit")) {
            quit();
        } else if (command.length == 1 && Objects.equals(command[0], "show")) {
            show();
        } else if (command.length == 2 && Objects.equals(command[0], "inspect")) {
            inspect(command[1]);
        } else {
            System.err.printf("Comando non riconosciuto: %s\n", commandString);
        }
    }

    /**
     * Gestione comandi sessione interattiva
     *
     * @param command comando diviso
     * @param commandString comando iniziale
     */
    private void inspecting(String[] command, String commandString){
        if (command.length == 1 && Objects.equals(command[0], "listall")) {
            listAll();
        } else if (command.length == 1 && Objects.equals(command[0], "end")) {
            end();
        } else if (command.length == 2 && Objects.equals(command[0], "delete")) {
            delete(Integer.parseInt(command[1]));
        } else {
            System.err.printf("Comando non riconosciuto: %s\n", commandString);
        }
    }

    /**
     * Funzione eseguita quando viene avviato il thread
     */
    @Override
    public void run() {
        create(9000);
    }

    /**
     * Avvio del server thread, resta in attesa di connessioni dai client
     *
     * @param port porta del server
     */
    private void create(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server avviato");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione da " + clientSocket.getInetAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Rimuove tutti i client connessi e arresta il server
     */
    private void quit() {
        for (Client c : clients) {
            c.disconnect();
        }
        running = false;
        System.exit(0);
    }

    /**
     * Mostra i topic, se ce ne sono
     */
    private void show() {
        if (topics.isEmpty()) {
            System.err.println("Non sono presenti topic");
        } else {
            System.out.println("Topic:");
            for (Topic t : topics) {
                System.out.println("\t" + t.getTitle());
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
            if (inspectedTopic == null) {
                System.err.println("Topic " + topic + " non esiste");
            }
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
                System.out.println("\t" + m.getText());
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
        messages.removeIf(m -> m.getID() == id);
        // confronto le dimensioni della lista per capire se è stato cancellato un elemento
        if (initialSize == messages.size()) {
            System.err.println("Messaggio con id " + id + " non esiste");
        } else {
            System.out.println("Messaggio eliminato");
        }
    }

    // funzione temporanea
    public void insertMessage(Topic topic) {
        topics.add(topic);
    }

    // funzione da testare col publisher

    /**
     * Ricezione messaggi dai Publisher
     *
     * @param publisher publisher che ha inviato il comando
     * @param command   string inviata dal publisher
     */
    public void receiveMessage(Publisher publisher, String command) {
        String topicTitle = publisher.getTopic().getTitle();
        String[] parts = command.split(" ", 2);
        String message = parts[1];

        for (Topic topic : topics) {
            if (topic.getTitle().equals(topicTitle)) {
                // Se c'è il topic aggiungo il messaggio alla lista
                topic.addMessage(message);
                return;
            }
        }
        // Se non c'è il topic, aggiungo un nuovo topic
        Topic t = new Topic(topicTitle);
        t.addMessage(message);
        topics.add(t);
    }

}
