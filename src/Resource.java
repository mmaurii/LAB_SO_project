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
     * Restituisce una stringa pronta per la stampa contenente tutti i topic presenti fin'ora sul server.
     * Se non ci sono topic restituisce una stringa vuota
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
     * @param title stringa contente il titolo del topic da restituire
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
     * o una stringa vuota se non ci sono messaggi.
     * Se il topic passato come parametro è null si prende come topic di riferimento quello in ispezione.
     * Se non si è in ispezione si solleverà un NullPointerException.
     *
     * @param topic di cui restituire tutti i messaggi
     * @return Stringa contenente tutti i messaggi del topic in ispezione se topic è null, altrimenti
     * tutti i messaggi sul topic preso come parametro
     * @throws NullPointerException se non si è in fase di ispezione e topic è null
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
     * Esegue in ordine di ricezione i comandi presenti sul commandBuffer
     * che sono stati ricevuti durante la fase di ispezione.
     * Una volta eseguiti tutti svuota il buffer
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
     * @return restituisce il nome del topic che sta venendo ispezionato
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
     * @return una stringa per l'output che specifica se il messaggio è stato eliminato o non è stato trovato
     * @throws NullPointerException se il metodo viene richiamato mentre non si è in fase di ispezione
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
     * Aggiunge un nuovo topic se non è già presente un topic con lo stesso nome
     *
     * @param topic che si vuole aggiungere
     * @return restituisce il topic che è stato aggiunto, o quello che era già presente
     * se ce n'era già uno con lo stesso nome
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
     * Aggiunge un Subscriber a un topic
     *
     * @param subscriber da iscrivere
     * @param topic      a cui iscrivere il subscriber
     * @return il topic a cui si è iscritto il subscriber, null se non è presente quel topic
     */
    public Topic addSubscriber(ClientHandler subscriber, Topic topic) {
        synchronized (topics) {
            for (Topic t : topics) {
                if (topic.equals(t)) {
                    t.addSubscriber(subscriber);
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

    /**
     * Imposta il topic su cui si svolge l'ispezione.
     * Se il topic preso come parametro è null si setta inspectedTopic a null (non più in ispezione),
     * e si eseguono i comandi presenti nel commandBuffer.
     *
     * @param t topic da ispezionare
     */
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

    /**
     * Verifica se inspectedTopic è null (non in fase di ispezione)
     * o è valorizzato con un topic (topic in fase di ispezione)
     *
     * @return true se inspectedTopic è null, altrimenti false
     */
    public boolean inspectedTopicIsNull() {
        synchronized (inspectedObjectsLock) {
            return inspectedTopic == null;
        }
    }

    /**
     * Verifica se un topic è uguale a quello in ispezione.
     *
     * @param topic su cui verificare l'uguaglianza con inspectedTopic.
     * @return Se inspectedTopic è null ritorna false.
     * Altrimenti ritorna il risultato dato dal metodo equals di default per la classe Topic
     */
    public boolean equalsInspectedTopic(Topic topic) {
        synchronized (inspectedObjectsLock) {
            if (inspectedTopic != null) {
                return inspectedTopic.equals(topic);
            } else {
                return false;
            }
        }
    }

    /**
     * Sconnette tutti i client connessi al server
     */
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
     * Aggiunge un ClientHandler alla lista dei client connessi al server.
     *
     * @param ch ClientHandler da aggiungere
     */
    public void addClient(ClientHandler ch) {
        synchronized (clients) {
            this.clients.add(ch);
        }
    }

    /**
     * Rimuove il client preso come parametro dalla lista dei client connessi al server.
     *
     * @param clientHandler da rimuovere
     */
    public void removeClient(ClientHandler clientHandler) {
        synchronized (clients) {
            this.clients.remove(clientHandler);
        }
    }
}
