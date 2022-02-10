
import java.awt.BorderLayout;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import javax.swing.*;

/**
 * the server implementation for blackjack
 */
public class BlackjackServer extends JFrame{

    /**
     * the server side text display box
     */
    private JTextArea displayArea;

    /**
     * the output to be sent to each of the clients
     */
    private ObjectOutputStream output;

    /**
     * the input from the client
     */
    private ObjectInputStream input;

    /**
     * keeps the correct port and other server information
     */
    private ServerSocket server;

    /**
     * the connected client
     */
    private Socket connection;

    /**
     * keeps track of what number of clients have connected so far
     */
    private int count = 1;

    /**
     * the deck used to preform the blackjack options
     */
    private Deck deck;

    /**
     * an array list storing all of the cards in the players hand
     */
    private ArrayList<String> playerHand;

    /**
     * an array list storing all of the cards in the dealers hand, index 0 represents the card the player can see
     */
    private ArrayList<String> dealerHand;

    /**
     * keeps the players score as to not have to restore every time
     */
    private int playerScore;

    /**
     * keeps the dealers score as to not have to restore every time
     */
    private int dealerScore;

    /**
     * stores weather a blackjack game has been started
     * the hit and stay button will do nothing when this is false
     */
    private boolean gameStarted = false;

    /**
     * constructor for the server, initializes the variables and makes it visible to the user
     */
    public BlackjackServer(){

        super("Blackjack Sever");

        deck = new Deck();
        playerHand = new ArrayList<>();
        dealerHand = new ArrayList<>();

        displayArea = new JTextArea();
        add(new JScrollPane(displayArea), BorderLayout.CENTER);
        setSize(400, 300);
        setVisible(true);

    }

    /**
     * runner for the server, once this is called the program will continue to run until disconnection
     */
    public  void runServer(){
        try{
            server = new ServerSocket(23716, 100); // create ServerSocket
            while (true) { //runs while the server is open, repeats trying to get connection and after it's closed finds another one
                try {
                    waitForConnection();
                    getStreams();
                    processConnection();
                }
                catch (EOFException eofException) {
                    displayMessage("\nServer terminated connection");
                }
                finally {
                    closeConnection();
                    count++;
                }
            }
        }
      catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    /**
     * halts the server until a client is connected, and displays messages when a client is connected
     * @throws IOException otherwise throws an IO exception
     */
    private void waitForConnection() throws IOException{
        displayMessage("waiting for connection\n");
        connection = server.accept();
        displayMessage("Connection " + count + " received from: " + connection.getInetAddress().getHostName());


    }

    /**
     * gets the input and output streams from the client that just connected
     * @throws IOException otherwise throws an IO exception
     */
    private void getStreams() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();

        input = new ObjectInputStream(connection.getInputStream());
        displayMessage("\ngot IO streams\n");
    }

    /**
     * while the client is connected, this continues to read what the client writes and processes it to usable information
     * @throws IOException otherwise throws an IO exception
     */
    private void processConnection() throws IOException{
        String message = "Connection Successful";
        sendData(message);

        do{
            try{
                message = (String) input.readObject(); // reads the message sent from the client
                displayMessage("\n" + message);
                processInput(message); //sends to another method to run the blackjack game
            }
            catch (ClassNotFoundException classNotFoundException){
                displayMessage("\nUnknown object type received");
            }

        } while (!message.equals("Disconnect")); // as long as the client does not press the disconnect button
    }

    /**
     * after the client has disconnected closes all of the IO streams and the connection
     */
    private void closeConnection(){
        displayMessage("\nconnection ended\n");
        try {
            output.close(); // close output stream
            input.close(); // close input stream
            connection.close(); // close socket
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * sends a message to the client
     * @param message the message to be sent to the client
     */
    private void sendData(String message){
        try{
            output.writeObject(message);
            output.flush(); // flush output to client
            displayMessage("\nSERVER>>> " + message);
        }
        catch (IOException ioException) {
            displayArea.append("\nError writing object");
        }
    }

    /**
     * displays the message to the display area
     * @param message the message to be displayed
     */
    private void displayMessage(final String message){
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        displayArea.append(message);
                    }
                }
        );
    }

    /**
     * starts the blackjack game
     */
    private void startGame(){
        gameStarted = true;
        playerHand.add(deck.dealACard());
        playerHand.add(deck.dealACard());
        dealerHand.add(deck.dealACard());
        dealerHand.add(deck.dealACard()); // deals 2 cards to the players hand and dealers hand
        playerScore = deck.score(playerHand);
        dealerScore = deck.score(dealerHand); //updates the score variable

        if(playerScore == 21){ //if the player has a blackjack goes to end the game and exit the method before the message gets sent to the client
            endGame();
            return;
        }

        //tells the client what their opening hand is and what card the dealer has up
        sendData("You have " + playerHand.get(0) + " " + playerHand.get(1) + " making a score of " + playerScore);
        sendData("The dealer has a " + dealerHand.get(0) + " showing");

    }

    /**
     * sends the input from the client and makes the buttons do the correct things
     * @param input the input from the client
     */
    private void processInput(String input){
        if(input.equals("Hit")){ // if the hit button is pressed
            if(gameStarted){ // cant do anything with hit if game start has not been called
                playerHand.add(deck.dealACard());
                playerScore = deck.score(playerHand);
                if(playerScore >= 21){ // if the score is >= 21 the game is over and the player has either busted or got a maximum score
                    endGame();
                }else { // if the game is not over gives the client information about what card was drawn and what information they have
                    String message = "You now have "; // creates a message to add all of the cards in the players hand to
                    for (String card : playerHand) {
                        message = message + card + " "; // adds each card one by one
                    }
                    // adds information about score and dealers showing card
                    message = message + "making a score of " + playerScore + " and the dealer has a " + dealerHand.get(0) + " showing";
                    sendData(message); //sends the message to the client
                }
            }
        }else if(input.equals("Stay")){ // the stay button is pressed
            if(gameStarted) { //makes sure the game has started and ends the game
                endGame();
            }
        }else if(input.equals("New Hand")){ // the new hand button was pressed
            playerHand = new ArrayList<>(); // resets the player and dealer hands
            dealerHand = new ArrayList<>();
            deck.resetDeck(); // resents the deck to a normal shuffled 52 card deck
            startGame(); // starts the game again
        }
    }

    /**
     * ends the blackjack game informing the client of who won
     */
    private void endGame(){
        gameStarted= false;
        playDealer(); // plays the dealer as per the rules of blackjack
        String message =  ""; // creates a message to be appended with specific information
        //goes through every possible game outcome and adds a specific message for each case
        if(playerScore > 21 && dealerScore <= 21){
            message  = "You busted and the dealer did not, you lost with ";
        }else if(playerScore > 21 && dealerScore > 21){
            message = "You busted and so did the dealer, you lost with ";
        }else if(playerScore <= 21 && dealerScore >21){
            message = "You did not bust and the dealer did, you win with ";
        }else if(playerScore > dealerScore){
            message = "You had a higher score than the dealer without busting, you win with ";
        }else if(playerScore < dealerScore){
            message = "You had a lower score than the dealer without busting, you lost with ";
        }else if(playerScore == dealerScore){
            message = "You got the same score as the dealer, you tied with ";
        }
        message = message + playerScore + " points \nwith these cards: ";
        for(String card:playerHand){
           message = message + card + " "; // shows all of the cards the player hand
        }
        message = message + "\n and the dealer had " + dealerScore + " points with these cards: ";
        for(String card:dealerHand){
            message = message + card + " "; // shows all the cards the dealer ended up with
        }
        sendData(message);

    }

    /**
     * plays the dealers hand as per the rule that the dealer must stay after their score gets to 17 and must hit before that
     */
    private void playDealer(){
        while (dealerScore < 17){
            dealerHand.add(deck.dealACard());
            dealerScore = deck.score(dealerHand);
        }
    }


}
