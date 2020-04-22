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

package uk.co.caprica.vlcj.javafx.demo.views.grid;

import javafx.scene.image.ImageView;
import uk.co.caprica.vlcj.javafx.demo.domain.MediaItem;

/**
 * Visual representation of a media item.
 */
public class MediaItemView extends ImageView {

    private static final String STYLE_CLASS = "media-item";

    private final MediaItem mediaItem;

    public MediaItemView(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
        getStyleClass().add(STYLE_CLASS);
        setImage(mediaItem.getThumbnail());
        setFitWidth(224);
        setFitHeight(126);
        setPreserveRatio(true);
    }

    public final MediaItem mediaItem() {
        return mediaItem;
    }
}
