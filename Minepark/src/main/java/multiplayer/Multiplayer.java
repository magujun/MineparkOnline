package multiplayer;

import game.Alert;
import game.Game;
import game.SoundEffect;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.application.Platform;
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

public class Multiplayer extends Thread {

    private final Game game;
    private final SoundEffect effect;
    private String nickname, message, input, opponent, opponentImg;
    private GridPane centerPane, topPane;
    private TilePane topMenu;
    private MineparkClient client;
    private FadeTransition fadeInCenter, fadeInTop, fadeOutCenter;
    private Button connectButton;
    private Label messageLabel, nickLabel;
    private TextField nickField;
    private ArrayList<String> remotePlayers;
    private ObservableList<String> playerList;
    private ListView<String> list;

    public Multiplayer(Game game) {
        this.game = game;
        this.effect = new SoundEffect("fastclick", 1);
    }

    public void multiplayerMenu() {

        centerPane = new GridPane();
        centerPane.setPrefSize(game.getGrid().getPrefWidth(), game.getGrid().getPrefHeight());

        list = new ListView<>();
        list.setPrefSize(centerPane.getPrefWidth(), centerPane.getPrefHeight());
        playerList = FXCollections.observableArrayList();
        list.setItems(playerList);
        centerPane.getChildren().add(list);

        topPane = new GridPane();
        topPane.setPrefSize(game.getScorebar().getWidth(), game.getScorebar().getHeight());

        topMenu = new TilePane();
        topMenu.setPrefSize(topPane.getPrefWidth(), topPane.getPrefHeight());
        topMenu.setBackground(Background.fill(Color.WHITE));
        topMenu.setOrientation(Orientation.HORIZONTAL);
        topMenu.setAlignment(Pos.CENTER);
        topMenu.setHgap(5.0);
        topMenu.setVgap(5.0);

        nickField = new TextField();
        nickField.setAlignment(Pos.CENTER);
        nickField.setPrefWidth(topPane.getPrefWidth() / 2);

        nickLabel = new Label("Nickname");
        nickLabel.setLabelFor(nickField);

        messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        connectButton = new Button();
        connectButton.setText("_Connect");
        connectButton.setMnemonicParsing(true);
        connectButton.setOnAction(e -> {
            effect.start();
            nickField.setBorder(Border.EMPTY);
            if (nickField.getText().isBlank()) {
                nickField.setBorder(Border.stroke(Color.RED));
                nickField.setPromptText("Please enter your nickname");
            } else if (nickname == null) {
                setNickname(nickField.getText());
                nickField.setDisable(true);
                connectButton.setText("_Get Opponents");
                connectServer();
            } else if (opponent == null) {
                client.send("LIST");
                messageLabel.setText("Please select an opponent");
            }
        });

        list.setOnMouseClicked(e -> {
            String selectedOpponent = list.getSelectionModel().getSelectedItem();
            if (!selectedOpponent.contains(" vs ")) {
                connectButton.setDisable(true);
                messageLabel.setText("Inviting " + selectedOpponent + " to play a match");
                connectOpponent(selectedOpponent);
            } else {
                messageLabel.setText("These folks are playing a match.\r\nPlease select another opponent");
            }
        });

        topMenu.getChildren().add(nickLabel);
        topMenu.getChildren().add(nickField);
        topMenu.getChildren().add(connectButton);
        topMenu.getChildren().add(messageLabel);
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
        if (client == null || !client.isConnected()) {
            try {
                String serverAddress = "minepark.oncoto.app";
                int port = 7001;
                //String serverAddress = ""; // defaults to localhost
                client = new MineparkClient(serverAddress, port);
                client.send("CONNECT");
                client.send(nickname);
                start();
            } catch (Exception ex) {
                System.out.println("Client connection error: " + ex);
                disconnectServer();
                message = "No connection to MineparkOnline server.";
                Alert alert = new Alert();
                alert.showMessage(message);
            }
        }
    }

    @Override
    public void run() {
        while (getOpponent() == null) {
            message = client.listen();
            if (message != null) {
                System.out.println(message);
                if (message.startsWith("PL|")) {
                    remotePlayers = new ArrayList<>();
                    input = message.split("PL\\|")[1];
                    remotePlayers.addAll(Arrays.asList(input.split("\\|")));
                    Platform.runLater(() -> {
                        playerList = FXCollections.observableArrayList(remotePlayers);
                        list.setItems(playerList);
                        centerPane.getChildren().remove(list);
                        centerPane.getChildren().add(list);
                    });
                } else if (message.startsWith("OPPONENT|")) {
                    opponentConnect(message);
                }
            }
        }
    }

    public void opponentConnect(String message) {
        list.setDisable(true);
        connectButton.setDisable(true);
        setOpponent(message.split("\\|")[1]);
        messageLabel.setText("Connecting to " + getOpponent());
        client.send("ACCEPT|" + getOpponent());
        System.out.println(message);
        startMultiplayerGame(false);
    }

    public void connectOpponent(String selectedOpponent) {
        list.setDisable(true);
        connectButton.setDisable(true);
        client.send("OPPONENT|" + selectedOpponent);
        String response = client.listen();
        System.out.println(response);
        if (response.startsWith("ACCEPT|")) {
            setOpponent(selectedOpponent);
            startMultiplayerGame(false);
        } else {
            list.setDisable(false);
            connectButton.setDisable(false);
        }
    }

    public void startMultiplayerGame(boolean turn) {
        game.setTurn(turn);
        game.play(game.getDifficulty(), this, client);
        client.disconnect();
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

}
