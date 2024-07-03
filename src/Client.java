import java.io.IOException;
import java.net.Socket;

public class Client {
    protected final String quit = "quit";
    protected Socket socket;

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
