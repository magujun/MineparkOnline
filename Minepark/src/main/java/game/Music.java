package game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

class Music {

    private MediaPlayer mediaPlayer;
    private String theme, level;
    private boolean playing;

    public Music(String theme, String level) {
        this.theme = theme;
        this.level = level;
    }

    public void start() {
        try {
            String file = getClass().getResource("/" + getTheme() + getLevel() + ".mp3").toExternalForm();
            Media music = new Media(file);
            setMediaPlayer(new MediaPlayer(music));
            setPlaying(true);
        } catch (MediaException e) {
            System.out.println("Missing media files!" + e.getMessage());
        }
        getMediaPlayer().setAutoPlay(true);
        getMediaPlayer().setCycleCount(MediaPlayer.INDEFINITE);
    }

    public void stop() {
        getMediaPlayer().stop();
        getMediaPlayer().dispose();
    }

    /**
     * @return the mediaPlayer
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * @param mediaPlayer the mediaPlayer to set
     */
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * @param theme the theme to set
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * @return the playing
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * @param playing the playing to set
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
}
