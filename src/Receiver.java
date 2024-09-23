import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            BufferedReader from = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = from.readLine()) != null) {
                responseBuilder.append(line).append("\n"); // aggiungi la prossima linea
                System.out.println("Received: " + line);
                if (line.equals("quit")) {
                    break;
                }
            }
            String response = responseBuilder.toString();
            System.out.println("Complete response: " + response);

        }catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            sender.interrupt();
            System.out.println("Receiver closed.");
        }
    }
}
