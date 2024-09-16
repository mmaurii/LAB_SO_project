import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Receiver extends Client implements Runnable {
    public Receiver(Socket s) {
        super(s);
    }

    @Override
    synchronized public void run() {
        try {
            Scanner from = new Scanner(this.socket.getInputStream());
            System.out.println("Messaggi:");
            while (true) {
                String response = from.nextLine();
                System.out.println(response);
                if (response.equals("quit")) {
                    break;
                }

            }
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            System.out.println("Receiver closed.");
        }
    }
}
