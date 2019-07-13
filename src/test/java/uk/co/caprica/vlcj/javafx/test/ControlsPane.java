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
 * Copyright 2009, 2010, 2011, 2012, 2013, 2014, 2015 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.javafx.test;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A component containing simple media player controls.
 */
public class ControlsPane extends VBox {

    private static final String COMPONENT_STYLE = "-fx-padding: 8; -fx-background-color: rgb(232, 232, 232);";

    private static final String BUTTON_STYLE = "-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: black;";

    private final MediaPlayer mediaPlayer;

    private final Label currentTimeLabel;
    private final Slider timelineSlider;
    private final Label durationLabel;

    private final Button playButton;
    private final Button pauseButton;
    private final Button stopButton;

    private final AtomicBoolean tracking = new AtomicBoolean();

    private Timer clockTimer;

    public ControlsPane(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;

        currentTimeLabel = new Label(Time.formatTime(0L));

        timelineSlider = new Slider(0, 100, 0);
        timelineSlider.setPadding(new Insets(8));

        durationLabel = new Label(Time.formatTime(0L));

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(currentTimeLabel, Priority.NEVER);
        HBox.setHgrow(timelineSlider, Priority.ALWAYS);
        HBox.setHgrow(durationLabel, Priority.NEVER);

        box.getChildren().addAll(currentTimeLabel, timelineSlider, durationLabel);

        TilePane buttonsPane = new TilePane();
        buttonsPane.setPadding(new Insets(8));

        playButton = createButton("Play", "play");
        pauseButton = createButton("Pause", "pause");
        stopButton = createButton("Stop", "stop");

        setStyle(COMPONENT_STYLE);

        getChildren().addAll(box, buttonsPane);

        buttonsPane.getChildren().addAll(playButton, pauseButton, stopButton);

        playButton.setOnAction(actionEvent -> mediaPlayer.controls().play());
        pauseButton.setOnAction(actionEvent -> mediaPlayer.controls().pause());
        stopButton.setOnAction(actionEvent -> mediaPlayer.controls().stop());

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                startTimer();
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                stopTimer();
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

            @Override
            public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
                Platform.runLater(() -> updateDuration(newLength));
            }

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                Platform.runLater(() -> updateSliderPosition(newPosition));
            }
        });

        timelineSlider.setOnMousePressed(mouseEvent -> beginTracking());
        timelineSlider.setOnMouseReleased(mouseEvent -> endTracking());

        timelineSlider.valueProperty().addListener((obs, oldValue, newValue) -> updateMediaPlayerPosition(newValue.floatValue() / 100));
    }

    private Button createButton(String name, String icon) {
        Button button = new Button();
        String url = String.format("/icons/buttons/%s.png", icon);
        Image image = new Image(getClass().getResourceAsStream(url));
        button.setGraphic(new ImageView(image));
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setStyle(BUTTON_STYLE);
        return button;
    }

    private void startTimer() {
        clockTimer = new Timer();
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> currentTimeLabel.setText(Time.formatTime(mediaPlayer.status().time())));
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        clockTimer.cancel();
    }

    private void updateDuration(long newValue) {
        durationLabel.setText(Time.formatTime(newValue));
    }

    private synchronized void updateMediaPlayerPosition(float newValue) {
        if (tracking.get()) {
            mediaPlayer.controls().setPosition(newValue);
        }
    }

    private synchronized void beginTracking() {
        tracking.set(true);
    }

    private synchronized void endTracking() {
        tracking.set(false);
        // This deal with the case where there was an absolute click in the timeline rather than a drag
        mediaPlayer.controls().setPosition((float) timelineSlider.getValue() / 100);
    }

    private synchronized void updateSliderPosition(float newValue) {
        if (!tracking.get()) {
            timelineSlider.setValue(newValue * 100);
        }
    }

}
