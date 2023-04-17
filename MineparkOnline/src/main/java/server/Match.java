package server;

/**
 *
 * @author marcelog
 */
class Match {

    /**
     * The current player1.
     */
    private final Player player1;
    private final Player player2;
    private String grid;

    public Match(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void play() {
        while (player2 != null) {
            if (player1.isExit()) {
                break;
            }
            // Repeatedly get commands from the client and process them
            player1.setMove(player1.receive());
            if (player1.getMove() == null) {
                break;
            }
            if (player1.getMove().equals("DISCONNECT")) {
                break;
            }
            if (player1.getMove().startsWith("WON|")) {
                grid = player1.getMove().split("\\|")[1];
                player2.send("LOST|" + grid);
                break;
            }
            if (player1.getMove().startsWith("LOST|")) {
                grid = player1.getMove().split("\\|")[1];
                player2.send("WON|" + grid);
                break;
            }
            if (player1.getMove().startsWith("TILESET|")) {
                grid = player1.getMove().split("\\|")[1];
                player2.send(grid);
                player1.setMove(player1.receive());
            }
            grid = player1.getMove();
            player2.send(grid);
            System.out.println(grid);
        }
        disconnect();
    }

    public void disconnect() {
        player1.setOpponent(null);
        player1.setInvited(false);
        player1.setExit(false);
        player1.setMatch(null);

        player2.setOpponent(null);
        player2.setInvited(false);
        player2.setExit(false);
        player2.setMatch(null);
    }
}
