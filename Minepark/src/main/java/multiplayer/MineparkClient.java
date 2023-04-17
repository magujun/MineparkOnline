package multiplayer;

import game.Alert;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * MpGP (Minepark Game Protocol) which is entirely text based. These are the
 * <String> messages that are transmitted: Client Server Client GRID GRID GRID
 * WON WON-LOST LOST LOST LOST-WON WON LIST PLS // players list start PLAYER vs
 * OPPONENT // players loop PLE // players list end TIE TIE TIE MESSAGE
 *
 */
/**
 * The main thread of the client will listen for messages from the server.The
 * first message will be a "CONNECTED" message in which we receive our mark.Then
 * we go into a loop listening for "VALID_MOVE", "OPPONENT_MOVED", "VICTORY",
 * "DEFEAT", "TIE", "OPPONENT_QUIT or "MESSAGE" messages, and handling each
 * message appropriately. The "VICTORY", "DEFEAT" and "TIE" ask the user whether
 * or not to play another game. If the answer is no, the loop is exited and the
 * server is sent a "QUIT" message. If an OPPONENT_QUIT message is received then
 * the loop will exit and the server will be sent a "QUIT" message also.
 *
 */
public class MineparkClient {

    private boolean connected;
    private String message, playerList;
    private SSLSocket socket;
    private BufferedReader input;
    private PrintWriter output;

    /**
     * Constructs the client by connecting to a server, laying out the GUI and
     * registering GUI listeners.
     *
     * @param serverAddress
     * @param port
     */
    public MineparkClient(String serverAddress, int port) {

        // Setup networking
        try {
            System.setProperty("javax.net.ssl.trustStore", "src/main/resources/trustedcerts");
            System.setProperty("javax.net.ssl.trustStorePassword", "letmein");
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            this.socket = (SSLSocket) factory.createSocket(serverAddress, port);
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
            this.connected = true;
        } catch (IOException ioe) {
            this.connected = false;
            System.out.println("Socket error: " + ioe.getLocalizedMessage());
            message = "No connection to server";
            Alert alert = new Alert();
            alert.showMessage(message);
        }
    }

    public void send(String message) {
        output.println(message);
    }

    public String receive() {
        try {
            return input.readLine();
        } catch (IOException ioe) {
            System.out.println("InputStream error: " + ioe.getLocalizedMessage());
            disconnect();
        }
        return null;
    }

    public void disconnect() {
        try {
            send("DISCONNECT");
            input.close();
            output.close();
            socket.close();
            connected = false;
        } catch (IOException ioe) {
            System.out.println("Error closing socket: " + ioe.getLocalizedMessage());
        }
    }

    public String listen() {

        if (input != null) {
            message = receive();
        }
        if (message != null) {
            if (message.equals("CONNECTED")) {
                setConnected(true);
                return message;
            } else if (message.startsWith("OPPONENT|")) {
                return message;
            } else if (message.startsWith("ACCEPTED|")) {
                return message;
            } else if (message.equals("PLS")) {
                playerList = "PL|";
                message = receive();
                while (!message.equals("PLE")) {
                    playerList += message;
                    message = receive();
                    playerList += (!message.equals("PLE") ? "|" : "");
                }
                return (playerList.split("\\|").length > 1 ? playerList : null);
            }
        }
        return null;
    }

    /**
     *
     *
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Runs the client as an application.
     *
     * @param args
     */
    public static void main(String[] args) {
        String serverAddress = (args.length == 0 ? "localhost" : args[0]);
        int port = (args.length < 2 ? 7001 : Integer.parseInt(args[1]));
        MineparkClient client = new MineparkClient(serverAddress, port);
        Scanner input = new Scanner(System.in);
        while (client.isConnected()) {
            System.out.print("Type a message to send to server: ");
            client.send(input.nextLine());
            System.out.println(client.listen());
        }
    }

}
