public class MainServer {
    public static void main(String[] args) {
        final int portNumber = 9000;
        String localHost = "127.0.0.1";

        Server server = new Server(portNumber);
        Thread threadServer = new Thread(server);
        threadServer.start();

        try {
            threadServer.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}