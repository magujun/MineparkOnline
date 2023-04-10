package game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

class SoundEffect {

    private MediaPlayer mediaPlayer;
    private String effect;

    public SoundEffect(String effect) {
        this.effect = effect;
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
    }

    void stop() {
        mediaPlayer.stop();
        mediaPlayer.dispose();
    }
}
