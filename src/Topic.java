import java.util.ArrayList;

public class Topic {
    private String title;
    private ArrayList<Message> messages = new ArrayList<>();

    public ArrayList<ClientHandler> getClients() {
        return clients;
    }

    public void setClients(ArrayList<ClientHandler> clients) {
        this.clients = clients;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public int getMaxMessageID() {
        return maxMessageID;
    }

    public void setMaxMessageID(int maxMessageID) {
        this.maxMessageID = maxMessageID;
    }

    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private int maxMessageID = 0;

    public Topic(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        maxMessageID += 1;
        messages.add(new Message(message));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Topic) {
            return ((Topic) obj).title.equals(title);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString(){
        return String.format("%s: %s",title,messages);
    }
}
