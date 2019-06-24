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
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A component containing simple media player controls.
 */
public class ControlsPane extends VBox {

    private static final String COMPONENT_STYLE = "-fx-padding: 8; -fx-background-color: rgb(232, 232, 232);";

    private static final String BUTTON_STYLE = "-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: black;";

    private final MediaPlayer mediaPlayer;

    private final Slider timelineSlider;

    private final Button playButton;
    private final Button pauseButton;
    private final Button stopButton;

    private final AtomicBoolean tracking = new AtomicBoolean();

    public ControlsPane(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;

        timelineSlider = new Slider(0, 100, 0);
        timelineSlider.setPadding(new Insets(8));

        TilePane buttonsPane = new TilePane();
        buttonsPane.setPadding(new Insets(8));

        playButton = createButton("Play", "play");
        pauseButton = createButton("Pause", "pause");
        stopButton = createButton("Stop", "stop");

        setStyle(COMPONENT_STYLE);

        getChildren().addAll(timelineSlider, buttonsPane);

        buttonsPane.getChildren().addAll(playButton, pauseButton, stopButton);

        playButton.setOnAction(actionEvent -> mediaPlayer.controls().play());
        pauseButton.setOnAction(actionEvent -> mediaPlayer.controls().pause());
        stopButton.setOnAction(actionEvent -> mediaPlayer.controls().stop());

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
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
    }

    private synchronized void updateSliderPosition(float newValue) {
        if (!tracking.get()) {
            timelineSlider.setValue(newValue * 100);
        }
    }

}
