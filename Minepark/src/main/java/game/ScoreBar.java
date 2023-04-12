package game;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ScoreBar extends ToolBar {

    private int timer, mines;
    private SoundEffect effect;

    public ScoreBar(Game game) {

        this.timer = game.getTimer();
        this.mines = game.getMines();
        this.effect = new SoundEffect("fastclick",1);
        game.setTimer(game.getMultiplayer() != null ? timer - 1 : timer + 1);

        setStyle("-fx-background-radius: 10 10 0 0;");
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setOffsetX(3.0f);
        ds.setColor(Color.color(0.3f, 0.3f, 0.3f));
        ds.setRadius(5.0f);
        Pane leftSpacer = new Pane();
        HBox.setHgrow(
                leftSpacer,
                Priority.SOMETIMES
        );

        Pane rightSpacer = new Pane();
        HBox.setHgrow(
                rightSpacer,
                Priority.SOMETIMES
        );

        Pane spacerLeft = new Pane();
        Pane spacerRight = new Pane();
        HBox.setMargin(spacerLeft, new Insets(0.0, 20.0, 0.0, 20.0));
        HBox.setMargin(spacerRight, new Insets(0.0, 20.0, 0.0, 20.0));

        getItems().add(leftSpacer);

        for (int i = 1; i < 4; i++) {
            int minesDigit = (int) (i == 1 ? Math.floor(mines / 100) : i == 2 ? Math.floor(mines / 10) : mines % 10);
            Text counter = new Text(Integer.toString(minesDigit));
            counter.setStyle("-fx-font-family: 'Digital-7 Mono';  -fx-fill:#222; -fx-font-size: 60px;");
            counter.setEffect(ds);
            counter.setCache(true);
            getItems().add(counter);
        }

        String playerImg = (game.isSafe() ? "win" + game.getDifficulty() + ".png" : game.isDead() ? "gameover" + game.getDifficulty() + ".png" : game.getPlayerImg());
        ImageView playerImage = new ImageView(new Image(playerImg));
        playerImage.setFitWidth(100);
        playerImage.setFitHeight(100);
        Button player = new Button();
        player.setGraphic(playerImage);
        player.autosize();

        getItems().add(spacerLeft);
        getItems().add(player);
        getItems().add(spacerRight);

        player.setOnMouseClicked(e -> {
            effect.start();
            game.getTimeline().stop();
            game.play(game.getDifficulty(), null, null);
        });

        for (int i = 1; i < 4; i++) {
            int timeDigit = (int) (i == 1 ? Math.floor(timer / 100 % 100) : i == 2 ? Math.floor(timer / 10 % 10) : timer % 10);
            Text timer = new Text(Integer.toString(timeDigit));
            timer.setStyle("-fx-font-family: 'Digital-7 Mono'; -fx-fill:#222; -fx-font-size: 60px;");
            timer.setEffect(ds);
            timer.setCache(true);
            getItems().add(timer);
        }
        getItems().add(rightSpacer);
    }
}
