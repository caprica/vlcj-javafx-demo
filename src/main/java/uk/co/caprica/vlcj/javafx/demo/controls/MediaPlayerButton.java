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

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

abstract public class MediaPlayerButton extends Button {

    private final Runnable action;

    protected MediaPlayerButton(Runnable action, Image image, int fitWidth, int fitHeight) {
        this.action = action;

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        setGraphic(imageView);

        setOnAction(actionEvent -> action.run());
    }

    protected final void setImage(Image image) {
        ((ImageView) getGraphic()).setImage(image);
    }

    protected static Image getImage(String resourcePath) {
        return new Image(MediaPlayerButton.class.getResourceAsStream(resourcePath));
    }
}
