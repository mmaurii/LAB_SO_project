public class Command {
    private String command;
    private ClientHandler sender;
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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public ClientHandler getSender() {
        return sender;
    }

    public void setSender(ClientHandler sender) {
        this.sender = sender;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
