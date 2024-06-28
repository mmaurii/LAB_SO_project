import java.util.Scanner;

public class Subscriber extends Client implements Runnable {
    private Topic topic;
    private final String listall = "listall";

    /**
     * Inizializza un Subscriber, prendendo in input i parametri per iniziare
     * il client.
     * @param host
     * @param port
     */
    public Subscriber(String host, int port) {
        //super(host, port);
    }

    /**
     * Iscrive il subsriber al topic specificato nel momento dell'inizializzazione
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
     * @return String[] contenente tutti i messaggi ordinati dal primo all'ultimo
     */
    public String[] listall(){
        String[] allMessages;

        return allMessages;
    }

    @Override
    public void run() {
        //deve continuamente ad ascoltare che non ci siano nuovi comandi impartiti e restituire i messaggi
        //che vengono inviati al suo topic, lo dovrebbe fare tramite due thread paralleli e indipendenti

        /*
         * Delega la gestione di input/output a due thread separati, uno per inviare
         * messaggi e uno per leggerli
         *
         */
        Thread sender = new Thread(new Sender(socket));
        Thread receiver = new Thread(new Receiver(socket, sender));
//        Scanner scan = new Scanner(System.in);
//        String command = scan.next(); //bloccante? si credo
//
//        switch (command){
//            case listall:
//                String[] allMessages = listall();
//                printAllMessages(allMessages);
//                break;
//            case quit:
//                //chiudi il client
//                break;
//        }

    }

    private void printAllMessages(String[] allMessages) {
        for (String message : allMessages) {
            System.out.println(message);
        }
    }
}
