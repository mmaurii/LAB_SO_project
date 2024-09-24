import java.io.IOException;

public class TestConcurrency {
    public static void main(String[] Args) {
        final int portNumber = 9000;
        String localHost = "127.0.0.1";
        Thread server = new Thread(new Server(portNumber));
//        Client client = null;
//        Client client1 = null;
//        Client client2 = null;
//        Client client3 = null;
//        Client client4 = null;
//        Client client5 = null;
//        Client client6 = null;
//        Client client7 = null;
//        Client client8 = null;
//        Client client9 = null;
//        try {
//            client = new Client();
//            client1 = new Client();
//            client2 = new Client();
//            client3 = new Client();
//            client4 = new Client();
//            client5 = new Client();
//            client6 = new Client();
//            client7 = new Client();
//            client8 = new Client();
//            client9 = new Client();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        server.start();
//        client.startClient();
//        client1.startClient();
//        client2.startClient();
//        client3.startClient();
//        client4.startClient();
//        client5.startClient();
//        client6.startClient();
//        client7.startClient();
//        client8.startClient();
//        client9.startClient();
//
//        client.send("publish calcio");
//        client1.send("publish calcio");
//        client2.send("publish calcio");
//        client3.send("publish calcio");
//        client4.send("publish calcio");
//        client5.send("publish calcio");
//        client6.send("publish calcio");
//        client7.send("publish calcio");
//        client8.send("publish calcio");
//        client9.send("subscribe calcio");
//        client1.send("1");
//        client2.send("2");
////        server.inspect("calcio");
//        client3.send("3");
//        client4.send("4");
//        client5.send("5");
//        client6.send("6");
//        client7.send("7");
//        client8.send("8");
////        server.end();
//
    }
}
