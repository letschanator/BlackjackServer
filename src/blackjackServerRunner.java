
import javax.swing.JFrame;

/**
 * creates and runs a blackjack server
 */
public class blackjackServerRunner {

    /**
     * main method to run the server
     * @param args the arguments for the main method
     */
    public static void main( String[] args){
        BlackjackServer application = new BlackjackServer();
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.runServer();
    }
}
