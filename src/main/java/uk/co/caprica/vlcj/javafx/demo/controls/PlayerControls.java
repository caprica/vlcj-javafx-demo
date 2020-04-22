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

package uk.co.caprica.vlcj.javafx.demo.controls;

import org.tbee.javafx.scene.layout.MigPane;
import uk.co.caprica.vlcj.javafx.demo.controls.media.PlayPauseButton;
import uk.co.caprica.vlcj.javafx.demo.controls.timeline.TimelineComponent;
import uk.co.caprica.vlcj.javafx.demo.controls.volume.MediaPlayerVolumeControls;

import java.util.function.Consumer;

final public class PlayerControls extends MigPane {

    private static final String STYLESHEET = "/css/player-controls.css";

    private static final String STYLE_CLASS = "player-controls";

    private final MediaPlayerVolumeControls volumeControls;
    private final PlayPauseButton playPauseButton;
    private final TimelineComponent timelineComponent;

    public PlayerControls(Runnable onMinVolume, Runnable onMaxVolume, Consumer<Integer> onSetVolume, Runnable onPlayPause, Consumer<Float> onPlayerPositionChanged) {
        super("fill, ins 6", "[sg, left][center][sg, right]", "[]0[]");

        getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        getStyleClass().add(STYLE_CLASS);

        volumeControls = new MediaPlayerVolumeControls(onMinVolume, onMaxVolume, onSetVolume);
        playPauseButton = new PlayPauseButton(onPlayPause);
        timelineComponent = new TimelineComponent(onPlayerPositionChanged);

        add(volumeControls);
        add(playPauseButton, "wrap");
        add(timelineComponent, "span 3, grow");
    }

    public void setPaused(boolean paused) {
        playPauseButton.setPaused(paused);
    }

    public void setSeekable(boolean newSeekable) {
        timelineComponent.setSeekable(newSeekable);
    }

    public void setLength(long newLength) {
        timelineComponent.setLength(newLength);
    }

    public void setTime(long newTime) {
        timelineComponent.setTime(newTime);
    }

    public void setPosition(float newPosition) {
        timelineComponent.setPosition(newPosition);
    }

    public void setVolume(float volume) {
        volumeControls.setVolume(volume);
    }
}
