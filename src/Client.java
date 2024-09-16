import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    protected final String quit = "quit";
    private final String show = "show";
    private final String publisher = "publisher";
    private final String subscriber = "subscriber";
    private boolean running = true;
    protected Socket socket;


    public Client(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }

    public Client(Socket socket) {
        this.socket = socket;
    }

    protected void send(String command) {
        try {
            /*
             * Delega la gestione di input/output a due thread separati, uno per inviare
             * messaggi e uno per leggerli
             *
             */
            PrintWriter to = new PrintWriter(this.socket.getOutputStream(), true);
            to.println(command);
            to.close();
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        }
    }

    protected void recive() {
        Scanner from = null;
        try {
            from = new Scanner(this.socket.getInputStream());

            while (from.hasNextLine()) {
                System.out.println(from.nextLine());
            }
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Client waitingForCommands() {
        while (running) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

            String workingCommand = command.substring(0, command.indexOf(' '));
            switch (workingCommand) {
                case quit:
                    running = false;
                    break;
                case show:
                    show();
                    break;
                case publisher:
                    running = false;
                    String parameter;
                    try {
                        parameter = command.substring(command.indexOf(' ') + 1);
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("No such parameter for: " + command + "\nRequired: publisher topic");
                        break;
                    }
                    return publisher(parameter);
                case subscriber:
                    running = false;
                    return subscriber();
            }
        }
        return null;
    }

    private Subscriber subscriber() {
        send(subscriber);
        recive();
        Subscriber subscriber = null;

        try {
            subscriber = new Subscriber(socket);
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        }
        return subscriber;
    }

    private Publisher publisher(String topicTitle) {

        send(publisher);
        recive();
        Publisher publisher = null;

            Topic topic = new Topic(topicTitle);
            publisher = new Publisher(socket, topic);

        return publisher;
    }

    private void show() {
        send(show);
        recive();
    }
}
