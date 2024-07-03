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
        // valori iniziali temporanei
        server.insertMessage(new Topic("cibo"));
        server.insertMessage(new Topic("musica"));
        server.insertMessage(new Topic("sport"));
        // modificare per accettare input da linea di comando
        server.create(9000);
    }
    /**
     * Inizalizza il server
     * @param port porta sulla quale viene aperto il server
     */
    public void create(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server avviato");
            this.run();
            // loop principale per accettare client
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione da " + clientSocket.getInetAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Funzione eseguita quando viene avviato il thread
     *
     */
    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        while (running) {
            if (inspectedTopic==null) System.out.println("\n> Inserisci comando");
            else System.out.println("Inserisci comando (ispezionando "+ inspectedTopic.getTitle()+ ")" );
            // analizza il comando
            String command = input.nextLine();
            String[] parts = command.split(" ");
            // non si sta ispezionando un topic
            if (inspectedTopic==null) {
                if(parts.length==1) notInspecting(parts[0], null);
                if(parts.length==2) notInspecting(parts[0], parts[1]);
            }
            // si sta ispezionando un topic
            else {
                if(parts.length==1) inspecting(parts[0], null);
                if(parts.length==2) inspecting(parts[0], parts[1]);
            }
        }
    }
    /**
     * Comandi per quando non si sta ispezionando un topic
     * @param command comando, prima parte del input utente
     * @param parameter valore per i comandi che hanno parametri
     */
    public void notInspecting(String command, String parameter){
        switch (command) {
            case "quit" -> quit();
            case "show" -> show();
            case "inspect" -> inspect(parameter);
            default -> System.err.println("Comando non riconosciuto: " + command);
        }
    }
    /**
     * Comandi per quando si sta ispezionando un topic
     * @param command comando, prima parte del input utente
     * @param parameter valore per i comandi che hanno parametri
     */
    public void inspecting(String command, String parameter){
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
    public void quit(){
        for(Client c : clients){
            c.disconnect();
        }
        running = false;
    }

    /**
     * Mostra i topic, se ce ne sono
     */
    public void show(){
        if(topics.isEmpty()) System.err.println("Non sono presenti topic");
        else{
            System.out.println("Topics:");
            for(Topic t : topics){
                System.out.println("\t"+t.getTitle());
            }
        }
    }

    /**
     * imposta il topic ispezionato se è presente
     * @param parameter topic che si vuole ispezionare
     */
    public void inspect(String parameter){
        if(parameter==null){System.err.println("Inserisci topic da ispezionare");}
        else {
            for(Topic t: topics){
                if(Objects.equals(t.getTitle(), parameter)){
                    inspectedTopic = t;
                    break;
                }
            }
            if(inspectedTopic==null)System.err.println("Topic "+ parameter+ " non esiste");
        }
    }

    /**
     * Elenca i messaggi in un topic, se ce ne sono
     */
    public void listAll(){
        ArrayList<Message> messages = inspectedTopic.getMessages();
        if(messages.isEmpty()){
            System.err.println("Non ci sono messaggi");
        }
        else{
            System.out.println("Messaggi:");
            for(Message m : inspectedTopic.getMessages()){
                System.out.println("\t"+m.getText());
            }
        }
    }
    /**
     * Termina la sessione interattiva
     */
    public void end(){
        inspectedTopic = null;
    }

    /**
     * Cancella messaggi dal topic ispezionato
     * @param id ID del messaggio che si vuole cancellare
     */
    public void delete(int id){
        // da cambiare in base a come decidiamo di fare l'id
        ArrayList<Message> messages = inspectedTopic.getMessages();
        int initialSize = messages.size();
        // Costrutto simile all'Iterator per rimuovere con
        // sicurezza un elemento da una lista se soddisfa una condizione
        messages.removeIf(m -> m.getID() == id);
        // confronto le dimensioni della lista per capire se è stato cancellato un elemento
        if(initialSize==messages.size()){
            System.err.println("Messaggio con id "+id+" non esiste");
        }
        else System.out.println("Messaggio eliminato");

    }
    // funzione temporanea
    public void insertMessage(Topic topic){
        topics.add(topic);
    }
}
