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
import javafx.scene.control.ComboBox;
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

    private Game game;
    private SoundEffect effect;
    private FadeTransition fadeInCenter, fadeInTop, fadeOutCenter;
    private GridPane centerPane, topPane;
    private TilePane topMenu;
    private Button connectButton;
    private Label messageLabel, nickLabel, serverLabel;
    private TextField nickField;
    private ComboBox serverDropDown;
    private ArrayList<String> remotePlayers;
    private ObservableList<String> playerList;
    private ListView<String> list;
    private MineparkClient client;
    private final int port = 7001;
    private volatile boolean exit;
    private boolean turn;
    private String serverAddress, server, nickname, message, input, opponent, difficulty;

    public Multiplayer(Game game) {
        this.game = game;
        this.exit = false;
        this.effect = new SoundEffect("fastclick", 1);
        this.difficulty = game.getDifficulty();
    }

    public void multiplayerMenu() {

        centerPane = new GridPane();
        centerPane.setPrefSize(game.getGrid().getWidth(), game.getGrid().getHeight());

        topPane = new GridPane();
        topPane.setAlignment(Pos.CENTER);
        topPane.setPrefSize(game.getScorebar().getWidth(), game.getScorebar().getHeight());

        topMenu = new TilePane();
        topMenu.setPrefSize(game.getScorebar().getWidth(), game.getScorebar().getHeight());
        topMenu.setPrefRows(3);
        topMenu.setPrefColumns(2);
        topMenu.setBackground(Background.fill(Color.WHITE));
        topMenu.setOrientation(Orientation.HORIZONTAL);
        topMenu.setAlignment(Pos.CENTER);
        topMenu.setHgap(2.0);
        topMenu.setVgap(5.0);

        nickField = new TextField();
        nickField.setAlignment(Pos.CENTER_LEFT);
        nickField.setPrefWidth(game.getScorebar().getWidth() / 3);
        nickLabel = new Label("Nickname");
        nickLabel.autosize();
        nickLabel.setLabelFor(nickField);
        nickLabel.setAlignment(Pos.CENTER_RIGHT);

        serverDropDown = new ComboBox();
        serverDropDown.setPrefWidth(game.getScorebar().getWidth() / 3);
        serverDropDown.setEditable(true);
        serverDropDown.getItems().add("Local");
        serverDropDown.getItems().add("Online");
        serverLabel = new Label("Server");
        serverLabel.autosize();
        serverLabel.setLabelFor(serverDropDown);
        serverLabel.setAlignment(Pos.CENTER_RIGHT);

        serverDropDown.setOnAction((event) -> {
            effect.start();
            exit = true;
            if (serverAddress != null && getClient() != null) {
                disconnectClient();
            }
            server = (String) serverDropDown.getValue();
            serverAddress = (server.equals("Online") ? "minepark.oncoto.app" : server.equals("Online") ? "localhost" : null);
            connectButton.setText("_Connect");
            connectButton.setDisable(false);
        });

        messageLabel = new Label();
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setTextFill(Color.RED);
        messageLabel.setText("Please select a server");

        connectButton = new Button();
        connectButton.setText("_Connect");
        connectButton.setMnemonicParsing(true);
        connectButton.setDisable(true);
        connectButton.setOnAction(e -> {
            effect.start();
            nickField.setBorder(Border.EMPTY);
            if (connectButton.getText().equals("_Accept")) {
                exit = true;
                opponentConnect();
                turn = false;
                startMultiplayerGame();
            } else if (connectButton.getText().equals("_Play")) {
                exit = true;
                turn = true;
                startMultiplayerGame();
            }
            if (nickField.getText().isBlank()) {
                nickField.setBorder(Border.stroke(Color.RED));
                nickField.setPromptText("Please enter your nickname");
            } else if (nickname == null) {
                setNickname(nickField.getText());
                nickField.setDisable(true);
                connectClient();
            }
            if (getClient() == null || !client.isConnected()) {
                connectClient();
            } else if (opponent == null) {
                getClient().send("LIST");
                messageLabel.setText("Please select an opponent");
            }
            if (getClient() != null && getClient().isConnected()) {
                messageLabel.setText("Click to get opponents");
                connectButton.setText("_Get Opponents");
            }
        });

        playerList = FXCollections.observableArrayList();
        list = new ListView<>();
        list.setPrefSize(centerPane.getPrefWidth(), centerPane.getPrefHeight());
        list.setItems(playerList);
        list.setOnMouseClicked(e -> {
            effect.start();
            String selectedOpponent = list.getSelectionModel().getSelectedItem();
            if (selectedOpponent != null && selectedOpponent.contains(" vs ")) {
                messageLabel.setText("Opponents playing");
            } else if (selectedOpponent != null) {
                messageLabel.setText("Inviting " + selectedOpponent + " to play");
                connectOpponent(selectedOpponent);
            }
        });

        topMenu.getChildren().add(nickLabel);
        topMenu.getChildren().add(nickField);
        topMenu.getChildren().add(serverLabel);
        topMenu.getChildren().add(serverDropDown);
        topMenu.getChildren().add(connectButton);
        topMenu.getChildren().add(messageLabel);

        topPane.add(topMenu, 0, 0);
        centerPane.getChildren().add(list);

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

    public void disconnectClient() {
        list.setItems(null);
        centerPane.getChildren().remove(list);
        centerPane.getChildren().add(list);
        messageLabel.setText("");
        exit = true;
        if (getClient() != null) {
            getClient().disconnect();
        }
        setClient(null);
        opponent = null;

    }

    public void connectClient() {
        try {
            setClient(new MineparkClient(serverAddress, port));
            getClient().send("CONNECT");
            if (getClient().receive().equals("NICKNAME")) {
                getClient().send(nickname);
                if (getClient() != null && getClient().receive().equals("CONNECTED")) {
                    exit = false;
                    start();
                    getClient().send("LIST");
                }
            }
        } catch (Exception ex) {
            setClient(null);
            System.out.println("Client connection error: " + ex);
            message = "No connection to " + server + " Minepark server";
            Alert alert = new Alert();
            alert.showMessage(message);
        }
    }

    @Override
    public void run() {
        while (!exit) {
            message = getClient().listen();
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
                    setOpponent(message.split("\\|")[1]);
                    setDifficulty(message.split("\\|")[2]);
                    System.out.println("Invitation received");
                    exit = true;
                    Platform.runLater(() -> {
                        connectButton.setText("_Accept");
                        messageLabel.setText(getOpponent() + ": " + getDifficulty() + " match");
                    });
                } else if (message.startsWith("ACCEPTED|")) {
                    setOpponent(message.split("\\|")[1]);
                    System.out.println("Invitation accepted");
                    exit = true;
                    Platform.runLater(() -> {
                        connectButton.setText("_Play");
                        messageLabel.setText(getOpponent() + ": " + getDifficulty() + " match");
                    });
                }
            }
        }
    }

    public void opponentConnect() {
        messageLabel.setText("Connecting to " + getOpponent());
        getClient().send("ACCEPT|" + getOpponent());
    }

    public void connectOpponent(String selectedOpponent) {
        System.out.println("Sending invitation to " + selectedOpponent);
        getClient().send("OPPONENT|" + selectedOpponent + "|" + getDifficulty());
    }

    public void startMultiplayerGame() {
        game.setTurn(turn);
        game.play(difficulty, this);
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
     * @return the client
     */
    public MineparkClient getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(MineparkClient client) {
        this.client = client;
    }

}
