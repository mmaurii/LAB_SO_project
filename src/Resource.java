import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Questa classe viene usata per memorizzare i dati relativi alle comunicazioni sul server
 * e per gestirne l'accesso in questo modo viene garantito che non ci siano problemi di concorrenza
 */
public class Resource {
    //set di tutti i topic che sono stati creati sul server
    private final HashSet<Topic> topics = new HashSet<>();
    //elenco di tutti i client connessi al server sia subscriber che publisher
    private final HashSet<ClientHandler> clients = new HashSet<>();
    //rappresenta il topic in fase di ispezione se non null
    private Topic inspectedTopic = null;
    //oggetto di sincronizzazione per la variabile inspectedTopic
    final Object inspectedObjectsLock = new Object();
    //Buffer per i comandi in attesa durante la fase di ispezione
    final LinkedList<Command> commandsBuffer = new LinkedList<>();

    /**
     * Mostra i topic al server, se ce ne sono
     */
    public String show() {
        synchronized (topics) {
            if (topics.isEmpty()) {
                return "";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Topic presenti:\n");
                for (Topic t : topics) {
                    sb.append("\t- " + t.getTitle() + "\n");
                }
                return sb.toString();
            }
        }
    }

    /**
     * Restituisce il topic dato il suo titolo
     *
     * @param title titolo del topic da restituire
     * @return il topic richiesto, null se non è stato trovato
     */
    public Topic getTopicFromTitle(String title) {
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
     * Ritorna una stringa per l'output contenente tutti i messaggi nel topic preso come parametro
     * o "" se non ci sono messaggi.
     *
     * @param topic di cui restituire tutti i messaggi
     * @return La stringa con tutti i messaggi del topic in ispezione se il topic è null.
     * Se non si è in ispezione si solleverà un NullPointerException
     * @throws NullPointerException se non si è in fase di ispezione
     */
    public String listAll(Topic topic) {

        if (topic == null) {
            if (inspectedTopic != null) {
                return inspectedTopic.getStringMessages();
            } else {
                throw new NullPointerException("the parameter topic is null can only be used during the server inspection");
            }
        }

        return topic.getStringMessages();
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
     * @throws NullPointerException se non si è in fase di ispezione
     */
    public String getInspectedTopicTitle() {
        synchronized (inspectedObjectsLock) {
            if (inspectedTopic != null) {
                return inspectedTopic.getTitle();
            } else {
                throw new NullPointerException("inspectedTopic is null this method can only be called during the server inspection");
            }
        }
    }

    /**
     * Cancella il messaggio con l'id specificato dal topic ispezionato
     *
     * @param id del messaggio che si vuole cancellare
     * @throws NullPointerException se non si è in fase di ispezione
     */
    public String delete(int id) {
        if (inspectedTopic == null) {
            throw new NullPointerException("inspectedTopic is null this method can only be called during the server inspection");
        }

        synchronized (clients) {
            for (ClientHandler ch : clients) {
                ch.delMessage(inspectedTopic, id);
            }
        }

        boolean removed = inspectedTopic.removeMessage(id);

        // confronto le dimensioni della lista per capire se è stato cancellato un elemento
        if (removed) {
            return "Messaggio eliminato";
        } else {
            return "Messaggio con id " + id + " non esiste\n";
        }
    }


    /**
     * Aggiunge un topic se non è presente
     *
     * @param topic topic che si vuole aggiungere
     */
    public Topic addTopic(Topic topic) {
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
    public Topic addSubscriber(ClientHandler client, Topic topic) {
        synchronized (topics) {
            for (Topic t : topics) {
                if (topic.equals(t)) {
                    t.addSubscriber(client);
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
    public void addCommand(Command command) {
        synchronized (commandsBuffer) {
            this.commandsBuffer.addLast(command);
        }
    }

    public void setInspectedTopic(Topic t) {
        synchronized (inspectedObjectsLock) {
            if (t != null) {
                this.inspectedTopic = t;
            } else {
                this.inspectedTopic = null;
                executeOperation();
            }
        }
    }

    public boolean inspectedTopicIsNull() {
        synchronized (inspectedObjectsLock) {
            return inspectedTopic == null;
        }
    }

    public boolean equalsInpectedTopic(Topic topic) {
        synchronized (inspectedObjectsLock) {
            if (inspectedTopic != null) {
                return inspectedTopic.equals(topic);
            } else {
                return false;
            }
        }
    }

    public void removeAllClients() {
        synchronized (clients) {
            Iterator<ClientHandler> iter = clients.iterator();
            while (iter.hasNext()) {
                ClientHandler ch = iter.next();
                ch.quit();
                ch.forward("quit");
                System.out.println("Interruzione client " + ch);
                iter.remove();
            }
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

    public void removeClient(ClientHandler clientHandler) {
        synchronized (clients) {
            this.clients.remove(clientHandler);
        }
    }
}
