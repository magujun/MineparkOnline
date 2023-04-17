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

    private String nickname, message, difficulty, move;
    private Player opponent;
    private SSLSocket socket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected, invited;
    private volatile boolean exit;
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
                this.output.println("NICKNAME");
                this.nickname = input.readLine();
                this.connected = true;
                this.exit = false;
            }
        } catch (IOException e) {
            System.out.println("Connection error with player " + nickname + ":" + e);
        }
    }

    /**
     * The run method of this thread.
     */
    @Override
    public void run() {
        while (!exit) {
            match = null;
            findMatch();
        }
        if (opponent != null) {
            playMatch();
        }
    }

    public void findMatch() {
        while (getOpponent() == null) {
            listen();
        }
    }

    public void playMatch() {
        exit = false;

        // The match thread is only started after opponent accepts connection.
        if (!isInvited()) {
            System.out.println(nickname + " is inviting " + getOpponent().getNickname() + " to a match");
            String response = receive();
            while (response == null || !response.startsWith("ACCEPTED|")) {
                response = receive();
            }
        } else { // Accepted the connection as opponent and the match thread starts.
            System.out.println(nickname + " is joining " + getOpponent().getNickname() + " in a match");
        }
        match = new Match(this, getOpponent());
        match.play();
    }

    public void listen() {
        message = receive();
        if (message != null) {
            if (message.startsWith("DISCONNECT")) {
                disconnectPlayer(this);
            } else if (message.equals("LIST")) {
                listPlayers(this);
            } else if (message.startsWith("OPPONENT|")) {
                String opponentNickname = getMessage().split("\\|")[1];
                setDifficulty(message.split("\\|")[2]);
                setOpponent(getPlayer(opponentNickname));
                String handshake = "OPPONENT|" + nickname + "|" + difficulty;
                opponent.send(handshake);
                setExit(true);
            } else if (message.startsWith("ACCEPT|")) {
                String opponentNickname = message.split("\\|")[1];
                setOpponent(getPlayer(opponentNickname));
                opponent.send("ACCEPTED|" + getNickname());
                setInvited(true);
                setExit(true);

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
     * Accepts notification of who the opponent is.
     */
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
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
            System.out.println("Input" + ioe.getLocalizedMessage());
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

    /**
     * @return the difficulty
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * @param difficulty the difficulty to set
     */
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * @return the invited
     */
    public boolean isInvited() {
        return invited;
    }

    /**
     * @param invited the invited to set
     */
    public void setInvited(boolean invited) {
        this.invited = invited;
    }

    /**
     * @return the exit
     */
    public boolean isExit() {
        return exit;
    }

    /**
     * @param exit the exit to set
     */
    public void setExit(boolean exit) {
        this.exit = exit;
    }
}
