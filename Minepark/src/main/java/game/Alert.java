package game;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Alert {

    private String message;
    private Stage stage;
    private VBox messageBox;
    private FadeTransition fadeIn, fadeOut;

    public Alert() {

        stage = new Stage();
        messageBox = new VBox();
        BorderPane pane = new BorderPane();
        pane.setMinSize(300, 200);
        pane.setMaxSize(300, 200);
        messageBox.setSpacing(10);
        messageBox.setAlignment(Pos.CENTER);

        // Set fade in effect
        fadeIn = new FadeTransition(Duration.seconds(0.5), pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);

        // Set fade out effect
        fadeOut = new FadeTransition(Duration.seconds(1), pane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);

        pane.setTop(null);
        pane.setCenter(messageBox);
        pane.setBottom(null);

        // SET SCENE AND OPTIONS
        Scene scene = new Scene(pane);
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image("minepark.png"));
        stage.setScene(scene);
        stage.setTitle("Minepark Alert");
        stage.setResizable(false);
        stage.setX(Screen.getScreens().get(0).getBounds().getMaxX() - 325);
        stage.setY(Screen.getScreens().get(0).getBounds().getMaxY() - 225);
        stage.requestFocus();
        stage.show();

    }

    public void showMessage(String message) {

        Text alert = new Text(message);
        alert.setTextAlignment(TextAlignment.CENTER);
        messageBox.getChildren().clear();
        messageBox.getChildren().add(alert);

        fadeIn.play();

        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.play();

        // After message fade out, close window
        fadeOut.setOnFinished((e) -> {
            stage.close();
        });
    }

}
