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

package uk.co.caprica.vlcj.javafx.demo.controls.media;

import javafx.scene.image.Image;
import uk.co.caprica.vlcj.javafx.demo.controls.MediaPlayerButton;

final public class PlayPauseButton extends MediaPlayerButton {

    private static final Image PLAY_IMAGE = getImage("/icons/buttons/play_24dp.png");

    private static final Image PAUSE_IMAGE = getImage("/icons/buttons/pause_24dp.png");

    private static final int FIT_SIZE = 48;

    public PlayPauseButton(Runnable action) {
        super(action, PLAY_IMAGE, FIT_SIZE, FIT_SIZE);
    }

    public void setPaused(boolean paused) {
        setImage(paused ? PAUSE_IMAGE : PLAY_IMAGE);
    }
}
