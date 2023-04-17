package game;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import multiplayer.Multiplayer;

public class Game {

    private int numCols, numRows, cleared, timer, mines, round;
    private boolean safe, dead, turn;
    private String difficulty, playerImg, alert, tileSet;
    private Stage stage;
    private BorderPane base;
    private Timeline timeline;
    private Grid grid;
    private ScoreBar scorebar;
    private MainMenu menu;
    private HighScores highscores;
    private Multiplayer multiplayer;
    private Music music;
    private Tile mine;

    // First run constructor
    public Game(Stage stage) {
        this.stage = stage;
        this.safe = false;
        this.dead = false;
        this.difficulty = "Beginner";
        this.round = 0;
        this.multiplayer = null;
    }

    public void play() {
        // Start game with default "Beginner" difficulty
        play(difficulty, null);
    }

    // Start new game
    public void play(String difficulty, Multiplayer multiplayer) {

        setDifficulty(difficulty);
        setMultiplayer(multiplayer);
        setTimer(multiplayer != null ? 20 : 0);
        setDead(false);
        setSafe(false);

        if (getMusic() != null && getMusic().isPlaying()) {
            getMusic().stop();
        }

        setMusic(new Music("background", getDifficulty()));
        getMusic().start();

        // Start nem game with current or newly selected difficulty
        switch (getDifficulty()) {
            case "Intermediate":
                setNumCols(16);
                setNumRows(16);
                setMines(40);
                setPlayerImg("Intermediate.png");
                break;
            case "Expert":
                setNumCols(32);
                setNumRows(16);
                setMines(99);
                setPlayerImg("Expert.png");
                break;
            default: // "Beginner"
                setNumCols(8);
                setNumRows(8);
                setMines(10);
                setPlayerImg("Beginner.png");
                break;
        }

        // Define number of tiles that must be cleared to win the game
        setCleared((getNumCols() * getNumRows()) - getMines());

        Canvas canvas = new Canvas(0, 0);
        canvas.autosize();

        setScorebar(new ScoreBar(this));
        setMenu(new MainMenu(this));
        setGrid(new Grid(this));

        // SET BORDER PANE LAYOUT
        // Top pane scorebar, contains flagged mines counter, game button and a timer
        // Center pane grid, contains the tiles
        // Bottom pane menu, contains game options menu
        setBase(new BorderPane(canvas));
        getBase().setStyle("-fx-border-width: 10px; -fx-border-color: #444; -fx-background-color: #444;");
        getBase().setTop(getScorebar());
        getBase().setCenter(getGrid());
        getBase().setBottom(getMenu());

        // Update the BorderPane top region with a new scoreBar, every second
        // Check the number of cleared non mined tiles to process a game win
        setTimeline(new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            getBase().setTop(new ScoreBar(this));
            if (getCleared() < 1) {
                gameWin();
            }
            if (isDead() == true) {
                gameOver(getMine());
            }
        })));

        getTimeline().setCycleCount(Animation.INDEFINITE);

        if (multiplayer != null) {
            Timeline multiline = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
                play(isTurn());
            }));

            multiline.setCycleCount(Animation.INDEFINITE);
            multiline.play();
        }

        // SET SCENE AND OPTIONS
        Scene scene = new Scene(getBase());
        getStage().getIcons().add(new Image("minepark.png"));
        getStage().setScene(scene);
        getStage().setTitle("Minepark");
        getStage().setResizable(false);
        getStage().show();
    }

    public void play(boolean turn) {
        while (true) {
            if (isTurn()) {
                myTurn();
            } else {
                opponentTurn();
            }
        }
    }

    public void myTurn() {
        getGrid().setDisable(false);
        getGrid().opacityProperty().set(100.0);
        setPlayerImg(getDifficulty() + ".png");
        setTimer(20);
        while (timer > 0 || !getGrid().isPressed()) {}
        if (round == 0) {
            tileSet = "TILESET|";
            for (Node node : getGrid().getChildren()) {
                Tile tile = (Tile) node;
                tileSet += (char) tile.getType() + '0';
                multiplayer.getClient().send(tileSet);
            }
        }
        tileSet = getGrid().getGridState();
        tileSet = (isSafe() ? "WON|" + tileSet : isDead() ? "LOST|" + tileSet : tileSet);
        multiplayer.getClient().send(tileSet);
        turn = false;
        round++;
    }

    public void opponentTurn() {
        getGrid().setDisable(true);
        getGrid().opacityProperty().set(90.0);
        setPlayerImg("opponent" + getDifficulty() + ".png");
        setTimer(20);
        if (round == 0) {
            tileSet = multiplayer.getClient().receive().split("\\|")[1];
            getGrid().setGridState(getGrid().getTiles(), getGrid().getNumRows(), getGrid().getNumCols(), tileSet);
        }
        while (timer > 0) {}
        tileSet = multiplayer.getClient().receive();
        getGrid().setGridState(getGrid().getTiles(), getGrid().getNumRows(), getGrid().getNumCols(), tileSet);
        turn = true;
        round++;
    }

    // Process game win for clearing all safe tiles
    public void gameWin() {
        setSafe(true);
        getBase().setTop(new ScoreBar(this));
        highscores = new HighScores(this);
        for (Node tile : getGrid().getChildren()) {
            tile.setDisable(true);
            tile.opacityProperty().set(90.0);
        }
        getHighscores().score();
        getTimeline().stop();
        getMusic().stop();
        setMusic(new Music("win", getDifficulty()));
        getMusic().start();
    }

    // End game for clicking on a mine tile
    public void gameOver(Tile mine) {
        setDead(true);
        getBase().setTop(new ScoreBar(this));
        for (int row = 0; row < getNumRows(); row++) {
            for (int col = 0; col < getNumCols(); col++) {
                if (row == mine.getRow() && col == mine.getCol()) {
                    continue;
                }
                int item = row * getNumCols() + col;
                Tile tile = (Tile) getGrid().getChildren().get(item);
                if (tile.isCovered()) {
                    tile.clear();
                }
                tile.setDisable(true);
                tile.opacityProperty().set(90.0);
            }
        }
        // Stop the timer and load new music
        getTimeline().stop();
        getMusic().stop();
        setMusic(new Music("gameover", getDifficulty()));
        getMusic().start();
    }

    // End game for losing a multiplayer match
    public void gameOver() {
        setDead(true);
        getBase().setTop(new ScoreBar(this));
        for (int row = 0; row < getNumRows(); row++) {
            for (int col = 0; col < getNumCols(); col++) {
                int item = row * getNumCols() + col;
                Tile tile = (Tile) getGrid().getChildren().get(item);
                if (tile.isCovered()) {
                    tile.clear();
                }
                tile.setDisable(true);
                tile.opacityProperty().set(90.0);
            }
        }
        // Stop the timer and load new music
        getTimeline().stop();
        getMusic().stop();
        setMusic(new Music("gameover", getDifficulty()));
        getMusic().start();
    }

    /**
     * @return the numCols
     */
    public int getNumCols() {
        return numCols;
    }

    /**
     * @param aNumCols the numCols to set
     */
    public void setNumCols(int aNumCols) {
        numCols = aNumCols;
    }

    /**
     * @return the numRows
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * @param aNumRows the numRows to set
     */
    public void setNumRows(int aNumRows) {
        numRows = aNumRows;
    }

    /**
     * @return the cleared
     */
    public int getCleared() {
        return cleared;
    }

    /**
     * @param aCleared the cleared to set
     */
    public void setCleared(int aCleared) {
        cleared = aCleared;
    }

    /**
     * @return the timer
     */
    public int getTimer() {
        return timer;
    }

    /**
     * @param aTimer the timer to set
     */
    public void setTimer(int aTimer) {
        timer = aTimer;
    }

    /**
     * @return the mines
     */
    public int getMines() {
        return mines;
    }

    /**
     * @param aMines the mines to set
     */
    public void setMines(int aMines) {
        mines = aMines;
    }

    /**
     * @return the Safe
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * @param aSafe the Safe to set
     */
    public void setSafe(boolean aSafe) {
        safe = aSafe;
    }

    /**
     * @return the Dead
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * @param aDead the Dead to set
     */
    public void setDead(boolean aDead) {
        dead = aDead;
    }

    /**
     * @return the difficulty
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * @param aDifficulty the difficulty to set
     */
    public void setDifficulty(String aDifficulty) {
        difficulty = aDifficulty;
    }

    /**
     * @return the playerImg
     */
    public String getPlayerImg() {
        return playerImg;
    }

    /**
     * @param aPlayerImg the playerImg to set
     */
    public void setPlayerImg(String aPlayerImg) {
        playerImg = aPlayerImg;
    }

    /**
     * @return the grid
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * @param aGrid the grid to set
     */
    public void setGrid(Grid aGrid) {
        grid = aGrid;
    }

    /**
     * @return the timeline
     */
    public Timeline getTimeline() {
        return timeline;
    }

    /**
     * @param aTimeline the timeline to set
     */
    public void setTimeline(Timeline aTimeline) {
        timeline = aTimeline;
    }

    /**
     * @return the menu
     */
    public MainMenu getMenu() {
        return menu;
    }

    /**
     * @param aMenu the menu to set
     */
    public void setMenu(MainMenu aMenu) {
        menu = aMenu;
    }

    /**
     * @return the highscores
     */
    public HighScores getHighscores() {
        return highscores;
    }

    /**
     * @param aHighscores the highscores to set
     */
    public void setHighscores(HighScores aHighscores) {
        highscores = aHighscores;
    }

    /**
     * @return the music
     */
    public Music getMusic() {
        return music;
    }

    /**
     * @param aMusic the music to set
     */
    public void setMusic(Music aMusic) {
        music = aMusic;
    }

    /**
     * @return the mine
     */
    public Tile getMine() {
        return mine;
    }

    /**
     * @param aMine the mine to set
     */
    public void setMine(Tile aMine) {
        mine = aMine;
    }

    /**
     * @return the stage
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * @param aStage the stage to set
     */
    public void setStage(Stage aStage) {
        stage = aStage;
    }

    /**
     * @return the base
     */
    public BorderPane getBase() {
        return base;
    }

    /**
     * @param aBase the base to set
     */
    public void setBase(BorderPane aBase) {
        base = aBase;
    }

    /**
     * @return the scorebar
     */
    public ScoreBar getScorebar() {
        return scorebar;
    }

    /**
     * @param aScorebar the scorebar to set
     */
    public void setScorebar(ScoreBar aScorebar) {
        scorebar = aScorebar;
    }

    /**
     * @return the alert
     */
    public String getAlert() {
        return alert;
    }

    /**
     * @param alert the alert to set
     */
    public void setAlert(String alert) {
        this.alert = alert;
    }

    /**
     * @return the round
     */
    public int getRound() {
        return round;
    }

    /**
     * @param round the round to set
     */
    public void setRound(int round) {
        this.round = round;
    }

    /**
     * @return the multiplayer
     */
    public Multiplayer getMultiplayer() {
        return multiplayer;
    }

    /**
     * @param multiplayer the multiplayer to set
     */
    public void setMultiplayer(Multiplayer multiplayer) {
        this.multiplayer = multiplayer;
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
