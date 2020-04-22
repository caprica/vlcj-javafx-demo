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

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;

import java.util.function.Consumer;

final class MediaPlayerPositionControl extends Slider {

    private static final String STYLE_CLASS_NAME = "position";

    private final Consumer<Float> positionSetAction;

    private volatile boolean ignoreExternalEvents = false;

    MediaPlayerPositionControl(Consumer<Float> positionSetAction) {
        this.positionSetAction = positionSetAction;

        getStyleClass().add(STYLE_CLASS_NAME);

        setOrientation(Orientation.HORIZONTAL);
        setValue(0);
        setMin(0);
        setMax(1);
        setSeekable(false);

        valueChangingProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue) {
                notifySetPosition((float) getValue());
            }
        });

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isValueChanging()) {
                notifySetPosition((float) getValue());
            }
        });

        setOnMousePressed(mouseEvent -> {
            setValueChanging(true);
            ignoreExternalEvents = true;
            // Seek immediately on a mouse press
            notifySetPosition((float) getValue());
        });

        setOnMouseReleased(mouseEvent -> {
            ignoreExternalEvents = false;
            setValueChanging(false);
        });
    }

    private void notifySetPosition(float newPosition) {
        positionSetAction.accept(newPosition);
    }

    void setSeekable(boolean newSeekable) {
        setDisable(!newSeekable);
    }

    void setControlPosition(float newPosition) {
        if (!ignoreExternalEvents) {
            Platform.runLater(() -> {
                if (!isValueChanging()) {
                    setValue(newPosition);
                }
            });
        }
    }
}
