package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import multiplayer.MineparkClient;

public class Multiplayer extends Thread {

    private String nickname, opponent, serverAddress, opponentImg;
    private boolean turn;
    private final Game game;
    private GridPane centerPane, topPane;
    private MineparkClient client;
    private FadeTransition fadeInCenter, fadeInTop, fadeOutCenter, fadeOutTop;
    private ArrayList<String> remotePlayers;
    private ObservableList<String> playerList;
    private ListView<String> list;
    private ScheduledExecutorService executor;

    public Multiplayer(Game game) {
        this.game = game;
    }

    public void multiplayerMenu() {

        centerPane = new GridPane();
        centerPane.setPrefSize(game.getGrid().getPrefWidth(), game.getGrid().getPrefHeight());

        list = new ListView<>();
        list.setPrefSize(centerPane.getPrefWidth(), centerPane.getPrefHeight());
        playerList = FXCollections.observableArrayList();
        list.setItems(playerList);
        centerPane.getChildren().removeAll();
        centerPane.getChildren().add(list);

        topPane = new GridPane();
        topPane.setPrefSize(game.getScorebar().getWidth(), game.getScorebar().getHeight());

        TilePane topMenu = new TilePane();
        topMenu.setPrefSize(topPane.getPrefWidth(), topPane.getPrefHeight());
        topMenu.setBackground(Background.fill(Color.WHITE));
        topMenu.setOrientation(Orientation.HORIZONTAL);
        topMenu.setAlignment(Pos.CENTER);
        topMenu.setHgap(5.0);
        topMenu.setVgap(5.0);

        TextField nickField = new TextField();
        nickField.setAlignment(Pos.CENTER);
        nickField.setPrefWidth(topPane.getPrefWidth() / 2);

        Label nickLabel = new Label("Nickname");
        nickLabel.setLabelFor(nickField);

        Button connectButton = new Button();
        connectButton.setText("_Connect");
        connectButton.setMnemonicParsing(true);
        connectButton.setOnAction(e -> {
            nickField.setBorder(Border.EMPTY);
            if (nickField.getText().isBlank()) {
                nickField.setBorder(Border.stroke(Color.RED));
                nickField.setPromptText("Please enter your nickname");
            } else if (nickname == null) {
                setNickname(nickField.getText());
                nickField.setDisable(true);
                connectServer();
            } else if (opponent == null) {
                String selectedOpponent = list.getSelectionModel().getSelectedItem();
                if (selectedOpponent != null) {
                    setOpponent(selectedOpponent);
                }
            } else if (opponent != null) {
                connectOpponent();
            }

        });

        topMenu.getChildren().add(nickLabel);
        topMenu.getChildren().add(nickField);
        topMenu.getChildren().add(connectButton);
        topPane.add(topMenu, 0, 0);

        game.getBase().setTop(topPane);
        game.getBase().setCenter(centerPane);

        fadeInTop = new FadeTransition(Duration.seconds(1), topPane);
        fadeInTop.setFromValue(0);
        fadeInTop.setToValue(1);
        fadeInTop.setCycleCount(1);

        fadeInCenter = new FadeTransition(Duration.seconds(1), centerPane);
        fadeInCenter.setFromValue(0);
        fadeInCenter.setToValue(1);
        fadeInCenter.setCycleCount(1);

        // Set fade out effect
        fadeOutCenter = new FadeTransition(Duration.seconds(1), centerPane);
        fadeOutCenter.setFromValue(1);
        fadeOutCenter.setToValue(0);
        fadeOutCenter.setCycleCount(1);

        fadeInCenter.play();
        fadeInTop.play();
        fadeInTop.setOnFinished(e -> {
            nickField.requestFocus();
        });
    }

    public void disconnectServer() {
        client.disconnect();
    }

    public void connectServer() {
        if (client == null) {
            serverAddress = "minepark.oncoto.app";
            serverAddress = ""; // defaults to localhost
            try {
                client = new MineparkClient(serverAddress);
                client.send(getNickname());
            } catch (Exception ex) {
                System.out.println("Error: " + ex);
            }
        }
        if (client.isConnected()) {
            Runnable listPlayers = () -> {
                listPlayers();
                opponentConnect();
            };
            executor = Executors.newScheduledThreadPool(10);
            executor.scheduleAtFixedRate(listPlayers, 0, 5, TimeUnit.SECONDS);
        } else {
            String message = "No connection to MineparkOnline server.";
            Alert alert = new Alert();
            alert.showMessage(message);
        }
    }

    public void listPlayers() {
        client.send("LIST");
        String response = client.listen();
        if (response != null) {
            System.out.println(response);
            if (response.startsWith("PL|")) {
                remotePlayers = new ArrayList<>();
                String input = response.split("PL\\|")[1];
                remotePlayers.addAll(Arrays.asList(input.split("\\|")));
                playerList = FXCollections.observableArrayList(remotePlayers);
                list.setItems(playerList);
                centerPane.getChildren().removeAll();
                centerPane.getChildren().add(list);
            }
        }
    }

    public void opponentConnect() {
        String response = client.listen();
        System.out.println(response);
        if (response != null) {
            if (response.startsWith("OPPONENT|")) {
                list.setDisable(true);
                executor.shutdown();
                setTurn(false);
                startMultiplayerGame();
            }
        }
    }

    public void connectOpponent() {
        client.send("OPPONENT|" + getOpponent());
        String response = client.listen();
        System.out.println(response);
        if (response.equals("ACCEPT")) {
            list.setDisable(true);
            executor.shutdown();
            setTurn(true);
            startMultiplayerGame();
        }
    }

    public void startMultiplayerGame() {
        game.play(game.getDifficulty(), this, client);
        while (true) {
            if (game.isTurn()) {
                game.myTurn();
            } else {
                game.opponentTurn();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                client.send(game.getGrid().getGridState());
                String response = client.receive();
                if (response.startsWith("LOST")) {
                    game.gameWin();
                    break;
                } else if (response.startsWith("WON")) {
                    game.gameOver();
                } else if (response.startsWith("MESSAGE")) {
                    String message = client.receive();
                }
                game.getGrid().setGridState(client.receive());
            }
        } finally {
            client.disconnect();
        }
    }

    private boolean wantsToPlayAgain() {
        return false;
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
     * @return the opponentImg
     */
    public String getOpponentImg() {
        return opponentImg;
    }

    /**
     * @param opponentImg the opponentImg to set
     */
    public void setOpponentImg(String opponentImg) {
        this.opponentImg = opponentImg;
    }

    /**
     * @return the opponent
     */
    public String getOpponent() {
        return opponent;
    }

    /**
     * @param opponent the opponent to set
     */
    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    /**
     * @return the turn
     */
    public boolean isTurn() {
        return turn;
    }

    /**
     * @param turn the turn to set
     */
    public void setTurn(boolean turn) {
        this.turn = turn;
    }
}
