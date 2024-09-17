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
//            to.close();
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

    public void waitingForCommands() {
        while (running) {
            Scanner scanner = new Scanner(System.in);
            String message = scanner.nextLine();

            if (message.equals(quit)) {
                send(message);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                running = false;
            } else {
                send(message);
                recive();
            }
        }
    }
}
