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

        Client c = client.waitingForCommands();


        if (c instanceof Subscriber) {
            Subscriber subscriber = (Subscriber) c;
//            try {
//                subscriber = new Subscriber(localHost,portNumber);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            subscriber.run();
        } else if (c instanceof Publisher) {
            Publisher publisher = (Publisher) c;
//            try {
//                publisher = new Publisher(localHost,portNumber);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            publisher.run();
        }
    }
}