import java.util.*;

/**
 * La classe Server si occupa di fornire un interfaccia console con cui interfacciarsi
 * al server. Inoltre mette a disposizione le proprie risorse in maniera sicura e istanzia
 * durante l'inizializzazione un thread SocketListener. Adibito alla gestione di nuove connessioni da parte di client.
 */
public class Server implements Runnable {
    private boolean running = true;
    private final Object runningLock = new Object();
    private final SocketListener socketListener;
    private final Resource resource;
    private final Thread socketListenerThread;

    //definizione comandi server
    private final String deleteCommand = "delete";
    private final String quitCommand = "quit";
    private final String listAllCommand = "listall";
    private final String endCommand = "end";
    private final String inspectCommand = "inspect";
    private final String showCommand = "show";
    //output var
    String outMesCommandWithOutParameters = "Questo comando non accetta parametri";

    public Server(int portNumber) {
        this.resource = new Resource();
        this.socketListener = new SocketListener(this, portNumber);
        socketListenerThread = new Thread(socketListener);
        socketListenerThread.setName("SocketListener");
        socketListenerThread.start();
    }

    /**
     * Mette il thread in ascolto per eventuali comandi in input da console
     */
    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        while (isRunning()) {
            if (resource.inspectedTopicIsNull()) {
                System.out.println("\n> Inserisci comando");
            } else {
                System.out.printf("> Inserisci comando (ispezionando topic \"%s\")\n",
                        resource.getInspectedTopicTitle());
            }

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

        try {
            socketListenerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Resource getResource() {
        return resource;
    }

    /**
     * Comandi per quando non si sta ispezionando un topic
     *
     * @param command   stringa contenente il comando, prima parte del input utente
     * @param parameter stringa contenente il valore per i comandi che hanno parametri
     */
    private void notInspecting(String command, String parameter) {
        switch (command) {
            case quitCommand -> {
                if (parameter != null) {
                    System.out.println(outMesCommandWithOutParameters);
                } else {
                    quit();
                }
            }
            case showCommand -> {
                if (parameter != null) {
                    System.out.println(outMesCommandWithOutParameters);
                } else {
                    show();
                }
            }
            case inspectCommand -> inspect(parameter);
            default -> System.out.printf("Comando non riconosciuto: %s\n", command);
        }
    }

    /**
     * Comandi per quando si sta ispezionando un topic
     *
     * @param command   stinga contenente il comando, prima parte del input utente
     * @param parameter stringa contenente il valore per i comandi che hanno parametri
     */
    private void inspecting(String command, String parameter) {
        switch (command) {
            case listAllCommand -> {
                if (parameter != null) {
                    System.out.println(outMesCommandWithOutParameters);
                } else {
                    listAll();
                }
            }
            case endCommand -> {
                if (parameter != null) {
                    System.out.println(outMesCommandWithOutParameters);
                } else {
                    end();
                }
            }
            case deleteCommand -> delete(parameter);
            default -> System.out.printf("Comando non riconosciuto: %s\n", command);
        }
    }

    /**
     * Chiude il thread in ascolto per nuove connessioni, termina tutte le connessioni già in essere coi client e arresta il server
     */
    private void quit() {
        //fermo questo thread e interrompo tutti quelli che compongono il server
        //rilasciando le risorse allocate
        synchronized (runningLock) {
            running = false;
        }

        socketListenerThread.interrupt();
        socketListener.close();
        System.out.println("Interruzione dei client connessi:");
        resource.removeAllClients();

    }

    /**
     * Imposta il topic ispezionato se è presente, altrimenti
     * segnala a console che non esiste il topic preso come parametro
     *
     * @param topicName stringa contenente il nome del topic che si vuole ispezionare
     */
    private void inspect(String topicName) {
        if (topicName == null) {
            System.out.println("Inserisci il nome del topic da ispezionare.");
        } else {
            Topic topicToInspect = resource.getTopicFromTitle(topicName);
            if (topicToInspect == null) {
                System.out.printf("Il topic %s non esiste.\n", topicName);
            } else {
                synchronized (topicToInspect.getTitle()) {
                    // entro in fase di ispezione
                    resource.setInspectedTopic(topicToInspect);
                    System.out.printf("Ispezionando il topic: %s\n", resource.getInspectedTopicTitle());
                }
            }
        }
    }


    /**
     * Termina la fase di ispezione
     */
    private void end() {
        synchronized (resource.getInspectedTopic().getTitle()) {
            System.out.printf("Fine ispezione del topic %s.", resource.getInspectedTopicTitle());
            //esco dalla fase di ispezione
            resource.setInspectedTopic(null);
        }
    }

    /**
     * Cancella il messaggio con l'id specificato dal topic ispezionato,
     * il metodo deve essere invocato solo durante la fase di ispezione
     *
     * @param parameter stringa contenente l'ID del messaggio che si vuole cancellare
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
     * Mostra i topic presenti nella console del server, se ce ne sono
     */
    private void show() {
        String listOfTopics = resource.show();

        if (listOfTopics.isEmpty()) {
            System.out.println("Non sono presenti topic");
        } else {
            System.out.println(listOfTopics);
        }
    }

    /**
     * Elenca tutti i messaggi nel topic in ispezione,
     * il metodo deve essere invocato solo durante la fase di ispezione
     */
    private void listAll() {
        if (resource.inspectedTopicIsNull()) {
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
     * @return true se il server non è in fase di chiusura, false altrimenti
     */
    public boolean isRunning() {
        synchronized (runningLock) {
            return running;
        }
    }
}
