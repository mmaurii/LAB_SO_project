import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;

public class Resource {
    //set di tutti i topic che sono stati creati sul server
    private final HashSet<Topic> topics = new HashSet<>();
    //elenco di tutti i client connessi a l server
    private final HashSet<ClientHandler> clients = new HashSet<>();
    private Topic inspectedTopic = null;
    Objects inspectedObjectsLock = null;
    //Buffer per i comandi in attesa durante la fase di ispezione
    LinkedList<Command> commandsBuffer = new LinkedList<>();

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
     * Elenca tutti i messaggi nel topic selezionato col comando
     * inspect durante la fase di ispezione
     */
    public synchronized String listAll(Topic topic) {
        ArrayList<Message> messages;

        if(topic==null){
            if(inspectedTopic!=null){
                messages = inspectedTopic.getMessages();
            }else{
                throw new NullPointerException("the parameter topic is null can only be used during the server inspection");
            }
        }else {
            messages = topic.getMessages();
        }

        if (messages.isEmpty()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();

            sb.append("Sono stati inviati "+messages.size()+" messaggi in questo topic.\n");
            sb.append("MESSAGGI:");
            for (Message m : messages) {
                sb.append(m.replyString());
            }

            return sb.toString();
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
    public synchronized Topic getInspectedTopic() {
        return inspectedTopic;
    }

    /**
     * Cancella il messaggio con l'id specificato dal topic ispezionato
     *
     * @param id del messaggio che si vuole cancellare
     */
    public String delete(int id) {
        ArrayList<Message> messages;
        if(inspectedTopic!=null){
            messages = inspectedTopic.getMessages();
        }else{
            throw new NullPointerException("inspectedTopic is null this method can only be called during the server inspection");
        }

        synchronized (clients) {
            for (ClientHandler ch : clients) {
                ch.delMessage(inspectedTopic, id);
            }
        }

        boolean removed;
        synchronized (messages) {
            removed = messages.removeIf(m -> m.getID() == id);
        }

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
                    synchronized (t.getSubscribers()) {
                        t.getSubscribers().add(client);
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
    public void addCommand(Command command) {
        synchronized (commandsBuffer) {
            this.commandsBuffer.addLast(command);
        }
    }

    public synchronized void setInspectedTopic(Topic t) {
        if (t != null){
            this.inspectedTopic = t;
        } else {
            this.inspectedTopic = null;
            executeOperation();
        }
    }

    public synchronized boolean inspectedTopicIsNull() {
        return inspectedTopic == null;
    }

    public synchronized boolean equalsInpectedTopic(Topic topic) {
        return inspectedTopic.equals(topic);
    }

    public String clientInterrupt() {
        StringBuilder sb = new StringBuilder();
        synchronized (clients) {
            for (ClientHandler client : this.clients) {
                sb.append("Interruzione client " + client + "\n");
                client.quit();
            }
        }
        return sb.toString();
    }
}
