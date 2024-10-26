import java.util.*;

/**
 * Classe server
 */
public class Server implements Runnable {
    private final HashSet<Topic> topics = new HashSet<>();
    private final HashSet<ClientHandler> clients = new HashSet<>();
    private Topic inspectedTopic = null;
    private boolean running = true;
    LinkedList<Command> commandsBuffer = new LinkedList<>();
    private Boolean inspectedLock = false; // Oggetto di sincronizzazione
    private SocketListener socketListener;

    //definizione nomi comandi
    private final String deleteCommand = "delete";
    private final String quitCommand = "quit";
    private final String listAllCommand = "listall";
    private final String endCommand = "end";
    private final String inspectCommand = "inspect";
    private final String showCommand = "show";

    public Server(int portNumber) {
        this.socketListener = new SocketListener(this, portNumber);
        Thread socketListenerThread = new Thread(socketListener);
        socketListenerThread.start();
    }

    /**
     * @return restituisce true se il server è in fase di ispezione, false altrimenti
     */
    public Boolean isInspectedLock() {
        return inspectedLock;
    }

    /**
     * Mette il thread in ascolto per eventuali comandi del server
     */
    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        while (running) {
            if (inspectedTopic == null) System.out.println("\n> Inserisci comando");
            else System.out.printf("> Inserisci comando (ispezionando topic \"%s\")\n",
                    inspectedTopic.getTitle());
            String command = input.nextLine();
            String[] parts = command.split(" ");
            if (inspectedTopic == null) {
                if (parts.length == 1) notInspecting(parts[0], null);
                if (parts.length == 2) notInspecting(parts[0], parts[1]);
            } else {
                // fase di ispezione
                if (parts.length == 1) inspecting(parts[0], null);
                if (parts.length == 2) inspecting(parts[0], parts[1]);
            }
        }
    }

    /**
     * @return i topic presenti sul server
     */
    public HashSet<Topic> getTopics() {
        synchronized (topics) {
            return topics;
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
            case quitCommand -> quit();
            case showCommand -> show();
            case inspectCommand -> inspect(parameter);
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
            case listAllCommand -> listAll();
            case endCommand -> end();
            case deleteCommand -> delete(parameter);
            default -> System.out.printf("Comando non riconosciuto: %s\n", command);
        }
    }

    /**
     * Chiude tutte le connessioni coi client e arresta il server
     */
    private void quit() {
        running = false;
        System.out.println("Interruzione dei client connessi");
        synchronized (clients) {
            for (ClientHandler client : this.clients) {
                System.out.printf("Interruzione client %s\n", client);
                client.quit();
            }
        }

        socketListener.close();
    }

    /**
     * Mostra i topic al server, se ce ne sono
     */
    private void show() {
        synchronized (topics) {
            if (topics.isEmpty()) {
                System.out.println("Non sono presenti topic");
            } else {
                System.out.println("Topic presenti:");
                for (Topic t : topics) {
                    System.out.printf("\t- %s\n", t.getTitle());
                }
            }
        }
    }

    /**
     * Imposta il topic ispezionato se è presente, altrimenti
     * segnala al server che non esiste il topic inserito
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
                synchronized (this) {
                    this.inspectedLock = !inspectedLock;
                    inspectedTopic = topicToInspect;
                }
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
        synchronized (topics) {
            for (Topic t : topics) {
                if (Objects.equals(t.getTitle(), title)) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * Elenca tutti i messaggi nel topic selezionato col comando
     * inspect durante la fase di ispezione
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
            System.out.printf("Sono stati inviati %s messaggi in questo topic.", messages.size());
            for (Message m : messages) {
                System.out.println(m.replyString());
            }
        }
    }

    /**
     * Termina la fase di ispezione
     */
    private void end() {
        System.out.printf("Fine ispezione del topic %s.", inspectedTopic.getTitle());

        synchronized (this) {
            // Resetta il topic ispezionato
            inspectedTopic = null;
            //processo tutti i comandi ricevuti durante l'ispezione
            executeOperation();
            // esco dalla fase di ispezione
            this.inspectedLock = !inspectedLock;
        }
    }

    /**
     * Esegue i comandi presenti sul commandBuffer
     * che sono stati ricevuti durante la fase di ispezione
     */
    private void executeOperation() {
        synchronized (commandsBuffer) {
            for (Command command : commandsBuffer) {
                command.execute();
            }
            commandsBuffer.clear();
        }
    }

    /**
     * @return restituisce il topic che sta venendo ispezionato
     */
    public Topic getInspectedTopic() {
        return inspectedTopic;
    }

    /**
     * Cancella il messaggio con l'id specificato dal topic ispezionato
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
            synchronized (messages) {//inutile
                messages.removeIf(m -> m.getID() == id);
            }

            synchronized (clients) {
                for (ClientHandler ch : clients) {
                    ch.delMessage(inspectedTopic, id);
                }
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
        synchronized (topics) {
            for (Topic t : topics) {
                if (topic.equals(t)) {
                    return t;
                }
            }

            topics.add(topic);
            return topic;
        }
    }


    /**
     * Aggiunge un subscriber a un topic
     *
     * @param client subscriber da iscrivere
     * @param topic  topic a cui iscrivere il subscriber
     * @return il topic a cui si è iscritto il client, null se non è presente quel topic
     */
    public synchronized Topic addSubscriber(ClientHandler client, Topic topic) {
        synchronized (topics) {
            for (Topic t : topics) {
                if (topic.equals(t)) {
                    synchronized (t.getClients()) {
                        t.getClients().add(client);
                    }
                    return t;
                }
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
        synchronized (commandsBuffer) {
            this.commandsBuffer.addLast(command);
        }
    }

    /**
     * Aggiunge un ClientHandler alla lista dei client connessi
     *
     * @param ch ClientHandler da aggiungere
     */
    public void addClient(ClientHandler ch) {
        synchronized (clients) {
            this.clients.add(ch);
        }
    }

    /**
     * @return true se il server è in esecuzione, false altrimenti
     */
    public boolean isRunning() {
        return running;
    }
}
