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

package uk.co.caprica.vlcj.javafx.demo.controls.volume;

import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import org.tbee.javafx.scene.layout.MigPane;

import java.util.function.Consumer;

final public class MediaPlayerVolumeControls extends MigPane {

    private final Consumer<Integer> volumeSetAction;

    private final Slider slider;

    public MediaPlayerVolumeControls(Runnable volumeMinAction, Runnable volumeMaxAction, Consumer<Integer> volumeSetAction) {
        super("ins 6", "[][][]");

        this.volumeSetAction = volumeSetAction;

        slider = new Slider();
        slider.setMin(0.0);
        slider.setMax(1.0);
        slider.setValue(0.0);

        slider.getStyleClass().add("volume");

        Button minButton = new VolumeMinButton(volumeMinAction);
        Button maxButton = new VolumeMaxButton(volumeMaxAction);

        add(minButton);
        add(slider);
        add(maxButton);

        slider.valueChangingProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue) {
                notifySetVolume(slider.getValue());
            }
        });
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (slider.isValueChanging()) {
                notifySetVolume(newValue.doubleValue());
            }
        });
    }

    private void notifySetVolume(double newVolume) {
        volumeSetAction.accept((int) (newVolume * 100));
    }

    public void setVolume(float newVolume) {
        if (!slider.isValueChanging()) {
            slider.setValue(newVolume);
        }
    }
}
