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

package uk.co.caprica.vlcj.javafx.demo.controls.timeline;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.tbee.javafx.scene.layout.MigPane;

import java.util.function.Consumer;

final public class TimelineComponent extends MigPane {

    private final MediaPlayerPositionControl slider;

    private final TimerLabel elapsedTimeLabel;

    private final TimerLabel durationLabel;

    private volatile long length;

    public TimelineComponent(Consumer<Float> onPlayerPositionChanged) {
        super("fill, ins 6", "[shrink]12[grow, fill]12[shrink]");

        slider = new MediaPlayerPositionControl(onPlayerPositionChanged);

        elapsedTimeLabel = new TimerLabel(TimerMode.ELAPSED);
        elapsedTimeLabel.setMinWidth(Region.USE_PREF_SIZE);
        elapsedTimeLabel.setTextFill(Color.WHITE);

        durationLabel = new TimerLabel(TimerMode.DURATION, TimerMode.REMAINING);
        durationLabel.setMinWidth(Region.USE_PREF_SIZE);
        durationLabel.setTextFill(Color.WHITE);

        add(elapsedTimeLabel);
        add(slider);
        add(durationLabel);

        durationLabel.setMode(TimerMode.REMAINING);
    }

    public void setSeekable(boolean newSeekable) {
        slider.setSeekable(newSeekable);
    }

    public void setLength(long newLength) {
        this.length = newLength;
    }

    public void setTime(long time) {
        elapsedTimeLabel.tick(time, length);
        durationLabel.tick(time, length);
    }

    public void setPosition(float position) {
        slider.setControlPosition(position);
    }
}
