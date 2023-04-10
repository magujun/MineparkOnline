package game;

/**
 * ***********************************************
 * Marcelo Guimaraes Junior Minepark
 * https://linkedin.com/in/marcelo-guimaraes-junior https://github.com/magujun
 * Please don't copy my code =) **********************************************
 */
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaException;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Minepark extends Application {

    public static void main(String[] args) {
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {

        // Set title bar icon & load external font
        try {
            primaryStage.getIcons().add(new Image("minepark.png"));
            var file = getClass().getResource("/fonts/digital-7.ttf").toExternalForm();
            Font.loadFont(file, 0);
        } catch (MediaException e) {
            System.out.println("Missing media files!");
        }

        // Start game
        loadSplashScreen(primaryStage);
    }

    // Splash screen =)
    void loadSplashScreen(Stage primaryStage) {

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        BorderPane base = new BorderPane();
        base.setStyle("-fx-background-color: #BBB;");
        base.setMinSize(720, 345);
        String splashImg = ("splash.png");
        ImageView splash = new ImageView(new Image(splashImg));
        splash.minHeight(base.getHeight());
        splash.minWidth(base.getWidth());
        splash.maxHeight(base.getHeight());
        splash.maxWidth(base.getWidth());
        base.getChildren().setAll(splash);

        // SET SCENE AND OPTIONS
        Scene scene = new Scene(base);
        stage.getIcons().add(new Image("minepark.png"));
        stage.setScene(scene);
        stage.setTitle("Minepark");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        // Set fade in effect
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), base);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);
        fadeIn.play();

        // Set fade out effect
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), base);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        fadeOut.setDelay(Duration.seconds(0.5));

        //After fade in, start fade out
        fadeIn.setOnFinished(e -> {
            fadeOut.play();
        });

        //After fade out, start game
        fadeOut.setOnFinished(e -> {
            stage.close();
            Game game = new Game(primaryStage);
            game.play();
            primaryStage.setOnCloseRequest(ex -> {
                Platform.exit();
                System.exit(0);
            });
        });
    }
}
