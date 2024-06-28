import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Receiver implements Runnable {

    Socket s;

    public Receiver(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            Scanner from = new Scanner(this.s.getInputStream());
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
