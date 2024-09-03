import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Subscriber extends Client implements Runnable {
    private Topic topic;
    private final String listall = "listall";

    /**
     * Inizializza un Subscriber, prendendo in input i parametri per iniziare
     * il client.
     *
     * @param host
     * @param port
     */
    public Subscriber(String host, int port) throws IOException {
        super(host, port);
    }

    public Subscriber(Socket socket) throws IOException {
        super(socket);
    }

    /**
     * Iscrive il subsriber al topic specificato nel momento dell'inizializzazione
     *
     * @param topic a cui si verrà iscritti
     */
    public void subscribe(Topic topic) {
        this.topic = topic;

    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    /**
     * Elenca tutti i messaggi inviati sul proprio topic.
     *
     * @return String[] contenente tutti i messaggi ordinati dal primo all'ultimo
     */
//    public String[] listall(){
//        String[] allMessages;
//
//        return allMessages;
//    }
    @Override
    public void run() {
        //deve continuamente ad ascoltare che non ci siano nuovi comandi impartiti e restituire i messaggi
        //che vengono inviati al suo topic, lo dovrebbe fare tramite due thread paralleli e indipendenti

        /*
         * Delega la gestione di input/output a due thread separati, uno per inviare
         * messaggi e uno per leggerli
         *
         */
//        Thread sender = new Thread(new Sender(this.socket));
        Thread receiver = new Thread(new Receiver(this.socket));//sender, il server va definito
        receiver.start();

        Scanner userInput = new Scanner(System.in);

        try {
            PrintWriter to = new PrintWriter(this.socket.getOutputStream(), true);
            Boolean exit = false;
            while (!exit) {
                String request = userInput.nextLine();
                /*
                 * se il thread è stato interrotto mentre leggevamo l'input da tastiera, inviamo
                 * "quit" al server e usciamo
                 */
                if (Thread.interrupted()) {
                    to.println("quit");
                    break;
                }
                /* in caso contrario proseguiamo e analizziamo l'input inserito
                 *  verificando il comando impartito
                 */
                switch (request) {
                    case listall:
                        to.println(request);
//                        String[] allMessages = listAll();
//                        printAllMessages(allMessages);
                        break;
                    case quit:
                        //chiudi il client
                        exit = true;
                        to.println(request);
                        break;
                }
            }
            System.out.println("Sender closed.");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            userInput.close();
        }
    }

    private void printAllMessages(String[] allMessages) {
        for (String message : allMessages) {
            System.out.println(message);
        }
    }
}
