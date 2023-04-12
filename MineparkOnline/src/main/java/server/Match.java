package server;

/**
 *
 * @author marcelog
 */
class Match {

    /**
     * The current player.
     */
    Player player1;
    Player player2;

    public Match(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void connect() {
        player2.send("OPPONENT|" + player1.getNickname());
        String response = player2.receive();
        if (response != null && response.startsWith("ACCEPT|")) {
            player1.send(response);
            player1.setOpponent(player2);
            player2.setOpponent(player1);
        } else {
            disconnect();
        }
    }

    public void disconnect() {
        player1.setOpponent(null);
        player1.findMatch();
        player2.setOpponent(null);
        player2.findMatch();
    }

}
