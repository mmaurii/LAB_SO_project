import java.util.ArrayList;

public class Topic {
    private String title;
    private ArrayList<Message> messages = new ArrayList<>();

    public Topic(String title) {
        this.title = title;
    }

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public ArrayList<Message> getMessages(){return messages;}
}
