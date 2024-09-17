import java.io.IOException;
import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {
        final int portNumber = 9000;
        String localHost = "127.0.0.1";

        Client client = null;
        try {
            client = new Client(localHost, portNumber);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        client.waitingForCommands();
    }
}