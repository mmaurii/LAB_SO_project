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

    private void sender(String command) {
        try {
            /*
             * Delega la gestione di input/output a due thread separati, uno per inviare
             * messaggi e uno per leggerli
             *
             */
            PrintWriter to = new PrintWriter(this.socket.getOutputStream(), true);
            to.println(command);
            to.close();

            Scanner from = new Scanner(this.socket.getInputStream());

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

    public void waitingForCommands() {
        while (running) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

            switch (command) {
                case quit:
                    running = false;
                    break;
                case show:
                    show();
                    break;
                case publisher:
                    publisher();
                    break;
                case subscriber:
                    subscriber();
                    break;
            }
        }
    }

    private void subscriber() {
        sender(subscriber);
    }

    private void publisher() {
        sender(publisher);
    }

    private void show() {
        sender(show);
    }
}
