package game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

public class SoundEffect {

    private MediaPlayer mediaPlayer;
    private String effect;
    int count;

    public SoundEffect(String effect, int count) {
        this.effect = effect;
        this.count = count;
    }

    public void start() {
        try {
            String file = getClass().getResource("/" + effect + ".mp3").toExternalForm();
            Media music = new Media(file);
            mediaPlayer = new MediaPlayer(music);
        } catch (MediaException e) {
            System.out.println("Missing media files!" + e.getMessage());
        }
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setCycleCount(count);
    }

    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.dispose();
    }

}
