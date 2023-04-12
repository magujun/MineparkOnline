package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.net.ssl.SSLSocket;
import static server.MineparkServer.*;

/**
 * The class for the helper threads in this multithreaded server application. A
 * Player is identified by a character mark which is either 'X' or 'O'. For
 * communication with the client the player has a socket with its input and
 * output streams. Since only text is being communicated we use a reader and a
 * writer.
 */
class Player extends Thread {

    private String nickname, message, grid, move;
    private Player opponent;
    private SSLSocket socket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected;
    private Match match;

    /**
     * Constructs a handler thread for a given socket and mark initializes the
     * stream fields, displays the first two welcoming messages.
     */
    public Player(SSLSocket socket) {

        try {
            this.socket = socket;
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
            if (input.readLine().equals("CONNECT")) {
                this.nickname = input.readLine();
                this.connected = true;
            }
        } catch (IOException e) {
            System.out.println("Connection error with player " + nickname + ":" + e);
        }
    }

    public void findMatch() {
        while (getOpponent() == null && isConnected()) {
            listen();
        }

        // The thread is only started after everyone connects.
        if (isConnected()) {
            setMatch(new Match(this, getOpponent()));
            getMatch().connect();
        }
    }

    public void listen() {

        if (input == null) {
            disconnectPlayer(this);
        } else {
            message = receive();
            if (message == null) {
                disconnectPlayer(this);
            } else if (message.equals("LIST")) {
                listPlayers(this);
            } else if (message.startsWith("OPPONENT|")) {
                String opponentNickname = getMessage().split("\\|")[1];
                setOpponent(getPlayer(opponentNickname));
            }
        }

    }

    /**
     * Accepts notification of who the opponent is.
     */
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    /**
     * Handles the otherPlayerMoved message.
     */
    public void otherPlayerMoved() {
        send("OPPONENT_MOVED");
    }

    /**
     * The run method of this thread.
     */
    @Override
    public void run() {

        findMatch();
        while (isConnected() && getOpponent() != null) {

            // Repeatedly get commands from the client and process them
            setGrid(null);
            send("TURN");
            getOpponent().send("WAIT");
            while (getOpponent() != null && isConnected()) {
                setMove(receive());
                if (getMove() == null) {
                    break;
                } else if (getMove().startsWith("WON")) {
                    setGrid(getMove().substring(4));
                    getOpponent().send("LOST" + getGrid());
                    break;
                } else if (getMove().startsWith("LOST")) {
                    setGrid(getMove().substring(5));
                    getOpponent().send("WON" + getGrid());
                    break;
                } else {
                    setGrid(getMove());
                    getOpponent().send(getGrid());
                }
                System.out.println(getGrid());
                send("WAIT");
                getOpponent().send("TURN");
            }
        }
    }

    /**
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return the opponent
     */
    public Player getOpponent() {
        return opponent;
    }

    /**
     * @return the socket
     */
    public SSLSocket getSocket() {
        return socket;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(SSLSocket socket) {
        this.socket = socket;
    }

    /**
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

    public void send(String message) {
        this.output.println(message);
    }

    public String receive() {
        try {
            return this.input.readLine();
        } catch (IOException ioe) {
            System.out.println("InputStream error: " + ioe.getLocalizedMessage());
            disconnectPlayer(this);
        }
        return null;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the grid
     */
    public String getGrid() {
        return grid;
    }

    /**
     * @param grid the grid to set
     */
    public void setGrid(String grid) {
        this.grid = grid;
    }

    /**
     * @return the move
     */
    public String getMove() {
        return move;
    }

    /**
     * @param move the move to set
     */
    public void setMove(String move) {
        this.move = move;
    }

    /**
     * @return the input
     */
    public BufferedReader getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(BufferedReader input) {
        this.input = input;
    }

    /**
     * @return the output
     */
    public PrintWriter getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(PrintWriter output) {
        this.output = output;
    }

    /**
     * @return the match
     */
    public Match getMatch() {
        return match;
    }

    /**
     * @param match the match to set
     */
    public void setMatch(Match match) {
        this.match = match;
    }
}
