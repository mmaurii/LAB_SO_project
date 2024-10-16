import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

/**
 * Classe server
 */
public class Server {
    private final HashSet<Topic> topics = new HashSet<>();
    private final List<ClientHandler> clients = new ArrayList<>();
    private Topic inspectedTopic = null;
    private boolean running = true;
    final int port;
    private ServerSocket serverSocket = null;
    LinkedList<Command> commandsBuffer = new LinkedList<>();
    private Boolean inspectedLock = false; // Oggetto di sincronizzazione

    public Server(int port) {
        this.port = port;
        System.out.println("probva");
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new SocketListener(this);
        this.commandLoop();
    }

    /**
     * @return restituisce true se il server è in fase di ispezione
     */
    public synchronized Boolean isInspectedLock() {
        return inspectedLock;
    }

    /**
     * Cambia lo stato di ispezione del server
     */
    private synchronized void changeStatusInspectedLock() {
        this.inspectedLock = !inspectedLock;
    }

    /**
     * Loop principale per il thread in ascolto dei comandi (main thread)
     */
    private void commandLoop() {
        Scanner input = new Scanner(System.in);
        while (running) {
            if (inspectedTopic == null) System.out.println("\n> Inserisci comando");
            else System.out.printf("Inserisci comando (ispezionando topic \"%s\")\n",
                    inspectedTopic.getTitle());
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
     * @return i topic presenti sul server
     */
    public HashSet<Topic> getTopics() {
        return topics;
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
            default -> System.out.printf("Comando non riconosciuto: %s\n", command);
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
            case "delete" -> delete(parameter);
            default -> System.out.printf("Comando non riconosciuto: %s\n", command);
        }
    }

    /**
     * Rimuove tutti i client connessi e arresta il server
     */
    private void quit() {
        running = false;
        System.out.println("Interruzione dei client connessi");
        for (ClientHandler client : this.clients) {
            System.out.printf("Interruzione client %s\n", client);
            client.quit();
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
        if (topics.isEmpty()) System.out.println("Non sono presenti topic");
        else {
            System.out.println("Topic presenti:");
            for (Topic t : topics) {
                System.out.printf("\t- %s\n", t.getTitle());
            }
        }
    }

    /**
     * Imposta il topic ispezionato se è presente
     *
     * @param topicName topic che si vuole ispezionare
     */
    private void inspect(String topicName) {
        if (topicName == null) {
            System.out.println("Inserisci il nome del topic da ispezionare.");
        } else {
            Topic topicToInspect = getTopicFromTitle(topicName);
            if (topicToInspect == null) {
                System.out.printf("Il topic %s non esiste.\n", topicName);
            } else {
                // entro in fase di ispezione
                changeStatusInspectedLock();
                inspectedTopic = topicToInspect;
                System.out.printf("Ispezionando il topic: %s\n", inspectedTopic.getTitle());
            }
        }
    }

    /**
     * Restituisce il topic dato il suo titolo
     *
     * @param title titolo del topic da restituire
     * @return il topic richiesto, null se non è stato trovato
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
     * Elenca tutti i messaggi nel topic della sezione interattiva
     */
    private void listAll() {
        if (inspectedTopic == null) {
            System.out.println("Nessun topic in fase di ispezione.");
            return;
        }

        ArrayList<Message> messages = inspectedTopic.getMessages();
        if (messages.isEmpty()) {
            System.out.println("Non ci sono messaggi");
        } else {
            System.out.printf("Sono stati inviati %s messaggi in questo topic.\n", messages.size());
            for (Message m : messages) {
                System.out.println(m.replyString());
            }
        }
    }

    /**
     * Termina la sessione interattiva
     */
    private void end() {
        // Resetta il topic ispezionato
        inspectedTopic = null;
        //processo tutti i comandi ricevuti durante l'ispezione
        executeOperation();
        // esco dalla fase di ispezione
        changeStatusInspectedLock();
    }

    /**
     * Esegue i comandi presenti sul commandBuffer
     */
    private void executeOperation() {
        for (Command command : commandsBuffer) {
            command.execute();
        }
        commandsBuffer.clear();
    }

    /**
     * @return restituisce il topic della sessione interattiva/ispezionato
     */
    public synchronized Topic getInspectedTopic() {
        return inspectedTopic;
    }

    /**
     * Cancella messaggi dal topic ispezionato
     *
     * @param parameter ID del messaggio che si vuole cancellare
     */
    private void delete(String parameter) {
        int id;
        try {
            id = Integer.parseInt(parameter);
            // da cambiare in base a come decidiamo di fare l'id
            ArrayList<Message> messages = inspectedTopic.getMessages();
            int initialSize = messages.size();
            // Costrutto simile all'Iterator per rimuovere con
            // sicurezza un elemento da una lista se soddisfa una condizione
            synchronized (messages) {
                messages.removeIf(m -> m.getID() == id);
            }

            for (ClientHandler ch : clients) {
                ch.delMessage(inspectedTopic, id);
            }
            // confronto le dimensioni della lista per capire se è stato cancellato un elemento
            if (initialSize == messages.size()) {
                System.out.printf("Messaggio con id %s non esiste\n", id);
            } else {
                System.out.println("Messaggio eliminato");
            }
        } catch (NumberFormatException e) {
            System.out.printf("%s non è un valore valido\n", parameter);
        }

    }

    /**
     * Aggiunge un topic se non è presente
     *
     * @param topic topic che si vuole aggiungere
     */
    public synchronized Topic addTopic(Topic topic) {
        for (Topic t : topics) {
            if (topic.equals(t)) {
                return t;
            }
        }

        topics.add(topic);
        return topic;
    }


    /**
     * Aggiunge un subscriber a un topic
     *
     * @param client subscriber da iscrivere
     * @param topic  topic a cui iscrivere il subscriber
     * @return il topic a cui si è iscritto il client, null se non è presente quel topic
     */
    public synchronized Topic addSubscriber(ClientHandler client, Topic topic) {
        for (Topic t : topics) {
            if (topic.equals(t)) {
                t.getClients().add(client);
                return t;
            }
        }
        return null;
    }

    /**
     * Aggiunge un commando al commandBuffer
     *
     * @param command comando da aggiungere
     */
    public synchronized void addCommand(Command command) {
        this.commandsBuffer.addLast(command);
    }

    public ServerSocket getSocket(){
        return serverSocket;
    }
    public void addClient(ClientHandler ch){
        this.clients.add(ch);
    }
    public boolean isRunning(){
        return running;
    }
}
