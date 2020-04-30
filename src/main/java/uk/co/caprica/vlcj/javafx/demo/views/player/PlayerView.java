/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2020 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.javafx.demo.views.player;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.demo.controls.PlayerControls;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

/**
 * Media player view component.
 */
final public class PlayerView extends Pane {

    private static final int CONTROLS_PANE_BOTTOM_OFFSET = 32;

    private static final int CONTROLS_PANE_WIDTH_ADJUSTMENT = 56 * 2;

    private static final double CONTROLS_PANE_OPACITY = 0.9f;

    private static final int MIN_VOLUME = 0;

    private static final int MAX_VOLUME = 100;

    private final ImageView videoImageView;

    private final PlayerControls playerControls;

    private final FadeTransition playerControlsFadeIn;

    private final FadeTransition playerControlsFadeOut;

    private final MediaPlayerFactory mediaPlayerFactory;

    private final EmbeddedMediaPlayer mediaPlayer;

    private double previousWidth = -1;
    private double previousHeight = -1;

    public PlayerView() {
        // Not sure if this helps or not
        setCache(true);
        setCacheHint(CacheHint.SPEED);

        videoImageView = new ImageView();
        videoImageView.setPreserveRatio(true);

        playerControls = new PlayerControls(
            this::handleSetMinVolume,
            this::handleSetMaxVolumne,
            this::handleSetVolume,
            this::handlePlayPause,
            this::handleSetPlaybackPosition
        );
        playerControls.setVisible(false);

        playerControlsFadeIn = new FadeTransition(Duration.millis(400), this.playerControls);
        playerControlsFadeIn.setFromValue(0);
        playerControlsFadeIn.setToValue(CONTROLS_PANE_OPACITY);
        playerControlsFadeIn.setAutoReverse(false);

        playerControlsFadeOut = new FadeTransition(Duration.millis(200), this.playerControls);
        playerControlsFadeOut.setFromValue(CONTROLS_PANE_OPACITY);
        playerControlsFadeOut.setToValue(0);
        playerControlsFadeOut.setAutoReverse(false);
        playerControlsFadeOut.setOnFinished(actionEvent -> playerControls.setVisible(false));

        mediaPlayerFactory = new MediaPlayerFactory();

        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.videoSurface().set(videoSurfaceForImageView(this.videoImageView));
        mediaPlayer.controls().setRepeat(true);

        setOnMouseEntered(this::handleMouseEntered);
        setOnMouseMoved(this::handleMouseMoved);
        setOnMouseExited(this::handleMouseExited);

        videoImageView.imageProperty().addListener(this::handleImageChanged);

        getChildren().addAll(this.videoImageView, playerControls);

        registerMediaPlayerEvents();
    }

    private void registerMediaPlayerEvents() {
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> playerControls.setPaused(false));
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> playerControls.setPaused(true));
            }

            @Override
            public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {
                Platform.runLater(() -> playerControls.setSeekable(newSeekable != 0));
            }

            @Override
            public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
                Platform.runLater(() -> playerControls.setLength(newLength));
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                // Time events are somewhat erratic, ideally we'd smooth them out somehow
                Platform.runLater(() -> playerControls.setTime(newTime));
            }

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                Platform.runLater(() -> playerControls.setPosition(newPosition));
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
                Platform.runLater(() -> playerControls.setVolume(volume));
            }

            @Override
            public void muted(MediaPlayer mediaPlayer, boolean muted) {
                if (muted) {
                    Platform.runLater(() -> playerControls.setVolume(0));
                } else {
                    Platform.runLater(() -> playerControls.setVolume(mediaPlayer.audio().volume()));
                }
            }
        });
    }

    public void play(String mrl) {
        mediaPlayer.media().play(mrl);
    }

    public void release() {
        mediaPlayer.controls().stop();
        mediaPlayer.release();
        mediaPlayerFactory.release();
    }

    /**
     * Custom layout, required to:
     * <ol>
     *     <li>Resize the video surface ImageView to fit the whole view (preserving aspect ratio);</li>
     *     <li>Resize and position the overlaid media player controls on top of the video surface.</li>
     * </ol>
     */
    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        if (width == previousWidth && height == previousHeight) {
            return;
        }

        previousWidth = width;
        previousHeight = height;

        videoImageView.setFitWidth(width);
        videoImageView.setFitHeight(height);

        layoutInArea(videoImageView, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);

        // Do the essential layout for the media player controls - without this the pane would seemingly have a width
        // and height of zero, regardless of any child nodes (in an ordinary layout this would be done in the superclass
        // layoutChildren method)
        playerControls.autosize();

        // We want to resize and reposition the media player controls to be within the video surface - this may be a
        // smaller bounding rectangle than that of this component (due to aspect ratio being preserved and the resultant
        // black bars)
        Bounds videoBounds = videoImageView.getBoundsInParent();

        double videoWidth = videoBounds.getWidth();
        double videoLeftEdge = videoBounds.getMinX();
        double videoBottomEdge = videoBounds.getMaxY();

        playerControls.resize(videoWidth - CONTROLS_PANE_WIDTH_ADJUSTMENT, playerControls.getHeight());
        playerControls.relocate(
            videoLeftEdge + (videoWidth - playerControls.getWidth()) / 2,
            videoBottomEdge - playerControls.getHeight() - CONTROLS_PANE_BOTTOM_OFFSET
        );
    }

    private void handleMouseEntered(MouseEvent mouseEvent) {
        playerControlsFadeOut.stop();
        playerControls.setVisible(true);
        playerControlsFadeIn.playFromStart();
    }

    private void handleMouseMoved(MouseEvent mouseEvent) {
        // FIXME ideally some sort of idle timer for mouse moves to show/hide the player controls (even if the mouse does not exit the component)
    }

    private void handleMouseExited(MouseEvent mouseEvent) {
        if (playerControls.isVisible()) {
            playerControlsFadeIn.stop();
            playerControlsFadeOut.playFromStart();
        }
    }

    private void handleImageChanged(ObservableValue<? extends Image> image, Image oldValue, Image newValue) {
        // Reset the layout optimisation if the image in the video image view changes
        previousWidth = previousHeight = -1;
    }

    private void handleSetMinVolume() {
        mediaPlayer.audio().setVolume(MIN_VOLUME);
    }

    private void handleSetMaxVolumne() {
        mediaPlayer.audio().setVolume(MAX_VOLUME);
    }

    private void handleSetVolume(Integer volume) {
        mediaPlayer.audio().setVolume(volume);
    }

    private void handlePlayPause() {
        mediaPlayer.controls().pause();
    }

    private void handleSetPlaybackPosition(float position) {
        mediaPlayer.controls().setPosition(position, true);
    }
}
