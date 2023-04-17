package game;

import multiplayer.Multiplayer;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class MainMenu extends MenuBar {

    private MenuItem newgameItem, scoresItem, quitItem, difficultyItem, modeSingle, modeMulti;
    private Menu menu, difficultyMenu, modeMenu;
    private SoundEffect effect;

    public MainMenu(Game game) {

        this.menu = new Menu("Menu");
        this.effect = new SoundEffect("fastclick", 1);
        setStyle("-fx-background-radius: 0 0 10 10;");
        getMenus().add(menu);

        newgameItem = new MenuItem("New game");
        menu.getItems().add(newgameItem);
        newgameItem.setOnAction(e -> {
            effect.start();
            modeMenu.setDisable(false);
            modeSingle.setDisable(false);
            modeMulti.setDisable(false);
            game.getTimeline().stop();
            game.play(game.getDifficulty(), null);
        });

        scoresItem = new MenuItem("High Scores");
        menu.getItems().add(scoresItem);
        scoresItem.setOnAction(e -> {
            effect.start();
            HighScores highscores = new HighScores(game);
            highscores.leaderboard();
        });

        quitItem = new MenuItem("Quit");
        menu.getItems().add(quitItem);
        quitItem.setOnAction(e -> System.exit(0));

        difficultyMenu = new Menu("Difficulty");
        if (game.getMultiplayer() != null) {
            difficultyMenu.setDisable(true);
        }
        getMenus().add(difficultyMenu);

        difficultyItem = new MenuItem("Beginner");
        difficultyMenu.getItems().add(difficultyItem);
        difficultyItem.setOnAction(e -> {
            effect.start();
            game.getTimeline().stop();
            game.getStage().close();
            game.play("Beginner", null);
        });

        difficultyItem = new MenuItem("Intermediate");
        difficultyMenu.getItems().add(difficultyItem);
        difficultyItem.setOnAction(e -> {
            effect.start();
            game.getTimeline().stop();
            game.getStage().close();
            game.play("Intermediate", null);
        });

        difficultyItem = new MenuItem("Expert");
        difficultyMenu.getItems().add(difficultyItem);
        difficultyItem.setOnAction(e -> {
            effect.start();
            game.getTimeline().stop();
            game.getStage().close();
            game.play("Expert", null);
        });

        modeMenu = new Menu("Mode");
        getMenus().add(modeMenu);

        modeMulti = new MenuItem("Multiplayer");
        modeMenu.getItems().add(modeMulti);
        modeMulti.setOnAction(e -> {
            effect.start();
            modeSingle.setDisable(false);
            modeMulti.setDisable(true);
            game.getTimeline().stop();
            Multiplayer multiplayer = new Multiplayer(game);
            multiplayer.multiplayerMenu();
        });

        modeSingle = new MenuItem("Singleplayer");
        modeMenu.getItems().add(modeSingle);
        modeSingle.setDisable(true);
        modeSingle.setOnAction(e -> {
            effect.start();
            modeSingle.setDisable(true);
            modeMulti.setDisable(false);
            game.getTimeline().stop();
            game.play(game.getDifficulty(), null);
        });
    }
}
