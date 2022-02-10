
import javax.swing.JFrame;

/**
 * creates a blackjack client
 */
public class BlackjackClientRunner {

    /**
     * main mated to create a blackjack client
     * @param args the arguments for the main method
     */
    public static void main( String[] args){

        BlackjackClient client;
        if (args.length == 0) {
            client = new BlackjackClient("127.0.0.1"); // connect to localhost
        }else {
            client = new BlackjackClient(args[0]); // use args to connect
        }
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.runClient();

    }
}
