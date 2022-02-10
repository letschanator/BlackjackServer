
import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * implementation of the client
 */
public class BlackjackClient extends JFrame{

    /**
     * hit button to be pressed when the user wants a new card
     */
    private JButton hitButton;

    /**
     * stay button to be pressed when user wants to keep their current hand
     */
    private JButton stayButton;

    /**
     * restart button to be pressed when the user wants to start a new hand
     */
    private JButton restartButton;

    /**
     * disconnect button to be pressed to disconnect from the server
     */
    private JButton disconnect;

    /**
     * the display area to display information about the game
     */
    private JTextArea displayArea;

    /**
     * the output stream to the server
     */
    private ObjectOutputStream output;

    /**
     * the input stream from the server
     */
    private ObjectInputStream input;

    /**
     * the message sent from the server
     */
    private String message = "";

    /**
     * the ip address of the server
     */
    private String server;

    /**
     * this clients socket
     */
    private Socket client;


    /**
     * constructor for the blackjack client
     * @param host ip address of the server
     */
    public BlackjackClient(String host){
        super("Blackjack Client");
        server = host;

        JPanel topButtons = new JPanel();
        topButtons.setLayout(new GridLayout(1,4));
        hitButton = new JButton("Hit");
        topButtons.add(hitButton);
        stayButton = new JButton("Stay");
        topButtons.add(stayButton);
        restartButton = new JButton("New Hand");
        topButtons.add(restartButton);
        disconnect = new JButton("Disconnect");
        topButtons.add(disconnect);
        add(topButtons,BorderLayout.NORTH); //puts a row of buttons on the top of the frame

        displayArea = new JTextArea();
        add(new JScrollPane(displayArea),BorderLayout.CENTER); //and the display area on the rest of the frame

        hitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendData("Hit"); // sends to the server the user wants to hit
            }
        });

        stayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendData("Stay"); //sends to the server the user wants to stay
            }
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendData("New Hand"); //sends to the server the user wants a new hand
            }
        });

        disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sendData("Disconnect"); //sends to the server the client is disconnecting
            }
        });

        setSize(500,300);
        setVisible(true); //shows the frame to the user

    }

    /**
     * runs the client until disconnection
     */
    public void runClient(){
        try{
            connectToServer();
            getStreams();
            processConnection();
        }catch (EOFException e){
            displayMessage("\nClient terminated connection");
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            closeConnection();
        }

    }

    /**
     * attempts to connects to the server
     * @throws IOException otherwise throws IO exception
     */
    private void connectToServer() throws IOException{
        displayMessage("attempting connection\n");
        client = new Socket(InetAddress.getByName(server), 23716);
        displayMessage("Connected to: " + client.getInetAddress().getHostName());
    }

    /**
     * attempts to get the IO streams
     * @throws IOException otherwise throws IO exception
     */
    private void getStreams() throws  IOException{
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush();
        input = new ObjectInputStream(client.getInputStream());
        displayMessage("\ngot IO streams\n");
    }

    /**
     * reads data sent from the server and relays it to the user
     * @throws IOException otherwise throws an IO exception
     */
    private void processConnection() throws IOException{
        do{
            try{
                message = (String) input.readObject(); // read new message
                displayMessage("\n" + message); // display message
            }
            catch (ClassNotFoundException classNotFoundException){
                displayMessage("\nUnknown object type received");
            }

        } while (!message.equals("Disconnect")); // while still connected to the server
    }

    /**
     * closes the connection to the server
     */
    private void closeConnection(){

        displayMessage("\nClosing Connection");

        try{
            output.close(); //closes all the input and output connections
            input.close();
            client.close();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    /**
     * sends data to the server
     * @param message message to be sent to the server
     */
    private void sendData(String message){

        try{
            output.writeObject(message);
            output.flush();
        }catch (IOException e){
            displayArea.append("\n error writing object");
        }
    }

    /**
     * displays a message to the server
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

}
