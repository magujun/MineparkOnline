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

    public synchronized void connect() {
        player2.send("OPPONENT|" + player1.getNickname());
        if (player1.receive().equals("ACCEPT")) {
            player1.setOpponent(player2);
            player2.setOpponent(player1);
        } else {
            disconnect();
        }
    }

    public void disconnect() {
        player1.setOpponent(null);
    }

}
