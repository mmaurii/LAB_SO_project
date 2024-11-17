/**
 * La classe definisce un pattern per memorizzare i possibili
 * comandi che il server può ricevere da un client
 */
public class Command {
    //nome del comando
    private final String command;
    private final ClientHandler sender;
    //eventuale contenuto
    private Message message;

    public Command(String command, Message message, ClientHandler sender) {
        this.command = command;
        this.message = message;
        this.sender = sender;
    }

    public Command(String command, ClientHandler sender) {
        this.command = command;
        this.sender = sender;
    }

    /**
     * Esegue il comando memorizzato
     */
    public void execute() {
        switch (command) {
            case "list" -> sender.listExecute();
            case "listall" -> sender.listallExecute();
            case "send" -> {
                sender.sendExecute(message);

                sender.notifyClient("Il tuo messaggio \"" + message.getTesto() +  "\" è stato inviato");
            }
        }
    }

   
    
}
