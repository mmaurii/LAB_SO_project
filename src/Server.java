import java.util.*;

/**
 * La classe Server si occupa di fornire un interfaccia console con cui interfacciarsi
 * al server. Inoltre mette a disposizione le proprie risorse in maniera sicura e istanzia
 * durante l'inizializzazione un thread SocketListener.
 */
public class Server implements Runnable {
    private Boolean running = true;
    private SocketListener socketListener;
    private Resource resource = null;

    //definizione nomi comandi
    private final String deleteCommand = "delete";
    private final String quitCommand = "quit";
    private final String listAllCommand = "listall";
    private final String endCommand = "end";
    private final String inspectCommand = "inspect";
    private final String showCommand = "show";

    public Server(int portNumber) {
        this.resource = new Resource();
        this.socketListener = new SocketListener(this, portNumber);
        Thread socketListenerThread = new Thread(socketListener);
        socketListenerThread.start();
    }

    public Resource getResource() {
        return resource;
    }

    /**
     * Mette il thread in ascolto per eventuali comandi del server
     */
    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        while (running) {
            if (resource.inspectedTopicIsNull()) System.out.println("\n> Inserisci comando");
            else System.out.printf("> Inserisci comando (ispezionando topic \"%s\")\n",
                    resource.getInspectedTopic().getTitle());
            String command = input.nextLine();
            String[] parts = command.split(" ");
            if (resource.inspectedTopicIsNull()) {
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
        synchronized (running) {
            running = false;
        }
        System.out.println("Interruzione dei client connessi:");

        String result = resource.clientInterrupt();
        System.out.println(result);

        socketListener.close();
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
            Topic topicToInspect = resource.getTopicFromTitle(topicName);
            if (topicToInspect == null) {
                System.out.printf("Il topic %s non esiste.\n", topicName);
            } else {
                // entro in fase di ispezione
                resource.setInspectedTopic(topicToInspect);
                System.out.printf("Ispezionando il topic: %s\n", resource.getInspectedTopic().getTitle());
            }
        }
    }


    /**
     * Termina la fase di ispezione
     */
    private void end() {
        System.out.printf("Fine ispezione del topic %s.", resource.getInspectedTopic().getTitle());
        //esco dalla fase di ispezione
        resource.setInspectedTopic(null);
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

            String OpResult = resource.delete(id);

            System.out.println(OpResult);
        } catch (NumberFormatException e) {
            System.out.printf("%s non è un valore valido\n", parameter);
        }
    }

    /**
     * Mostra i topic al server, se ce ne sono
     */
    private void show() {
        String listOfTopics = resource.show();

        if (listOfTopics.isEmpty()) {
            System.out.println("Non sono presenti topic");
        } else {
            System.out.println("Topic presenti:");
            System.out.printf("\t- %s\n", listOfTopics);
        }
    }

    /**
     * Elenca tutti i messaggi nel topic selezionato col comando
     * inspect durante la fase di ispezione
     */
    public void listAll() {
        if(resource.inspectedTopicIsNull()) {
            System.out.println("Nessun topic in fase di ispezione.");
            return;
        }

        String result = resource.listAll(null);

        if (result.isEmpty()) {
            System.out.println("Non ci sono messaggi");
        } else {
            System.out.println(result);
        }

    }

    /**
     * Aggiunge un ClientHandler alla lista dei client connessi
     *
     * @param ch ClientHandler da aggiungere
     */
    public void addClient(ClientHandler ch) {
        resource.addClient(ch);
    }

    /**
     * @return true se il server è in esecuzione, false altrimenti
     */
    public boolean isRunning() {
        synchronized (running) {
            return running;
        }
    }
}
