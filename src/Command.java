public class Command {
    private final String command;
    private final ClientHandler sender;
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
            case "send" -> sender.sendExecute(message);
        }
    }
}
