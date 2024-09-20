import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Receiver extends Thread {
    Socket s;
    Sender sender;

    public Receiver(Socket s, Sender sender) {
        this.s = s;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            Scanner from = new Scanner(this.s.getInputStream());
            while (true) {
                if (from.hasNextLine()) {
                    String response = from.nextLine();
                    System.out.println("Received: " + response);
                    if (response.equals("quit")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            sender.interrupt();
            System.out.println("Receiver closed.");
        }
    }
}
