package uk.co.caprica.vlcj.javafx.test;

import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

/**
 * A media player event listener dedicated to managing the repaint timer.
 * <p>
 * No need to consume CPU if paused/stopped.
 */
final class TimerHandler extends MediaPlayerEventAdapter {

    private final JavaFXDirectRenderingTest application;

    TimerHandler(JavaFXDirectRenderingTest application) {
        this.application = application;
    }

    private void startTimer() {
        application.startTimer();
    }

    private void pauseTimer() {
        application.pauseTimer();
    }

    private void stopTimer() {
        application.stopTimer();
    }

    @Override
    public void playing(MediaPlayer mediaPlayer) {
        startTimer();
    }

    @Override
    public void paused(MediaPlayer mediaPlayer) {
        pauseTimer();
    }

    @Override
    public void stopped(MediaPlayer mediaPlayer) {
        stopTimer();
    }

    @Override
    public void finished(MediaPlayer mediaPlayer) {
        stopTimer();
    }

    @Override
    public void error(MediaPlayer mediaPlayer) {
        stopTimer();
    }
}