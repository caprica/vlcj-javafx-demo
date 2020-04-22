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

import javafx.scene.control.Label;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

final class TimerLabel extends Label {

    private static final String STYLE_CLASS_NAME = "timer";

    private static final String DEFAULT_TIME_TEXT = "00:00";

    private static final long ONE_HOUR = 1000 * 60 * 60;

    private final List<TimerMode> timerModes;

    private int currentMode = 0;

    private long previousTime;

    private long previousLength;

    TimerLabel(TimerMode... timerModes) {
        this.timerModes = Arrays.asList(timerModes);
        getStyleClass().add(STYLE_CLASS_NAME);
        setText(DEFAULT_TIME_TEXT);

        if (timerModes.length > 1) {
            setOnMouseClicked(mouseEvent -> cycleMode());
        }
    }

    void setMode(TimerMode timerMode) {
        int requested = timerModes.indexOf(timerMode);
        if (requested != -1) {
            apply(requested);
        } else {
            throw new IllegalArgumentException();
        }
    }

    void cycleMode() {
        apply((currentMode + 1) % timerModes.size());
    }

    void tick(long time, long length) {
        this.previousTime = time;
        this.previousLength = length;

        Function<Long, String> timeFormat = length > ONE_HOUR ?
            Time::formatHoursMinutesSeconds :
            Time::formatHoursMinutes;

        switch (timerModes.get(currentMode)) {
            case ELAPSED:
                setText(timeFormat.apply(time));
                break;
            case DURATION:
                setText(timeFormat.apply(length));
                break;
            case REMAINING:
                setText(String.format("-%s", timeFormat.apply(length - time)));
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void apply(int newMode) {
        currentMode = newMode;
        tick(previousTime, previousLength);
    }
}
