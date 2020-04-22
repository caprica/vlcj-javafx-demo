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

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import uk.co.caprica.vlcj.javafx.demo.domain.MediaItem;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A grid of media thumbnails.
 */
public class MediaGrid extends ScrollPane {

    private static final String STYLESHEET = "/css/media-grid.css";

    private static final String STYLE_CLASS = "grid";

    private final ObservableList<MediaItem> mediaList;

    private final TilePane tilePane;

    public MediaGrid(ObservableList<MediaItem> mediaList, MediaGridListener listener) {
        this.mediaList = mediaList;

        this.tilePane = new TilePane();
        this.tilePane.setPrefWidth(800);

        this.tilePane.getStyleClass().add(STYLE_CLASS);
        getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        setFitToWidth(true);
        setFitToHeight(true);

        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        tilePane.setOnScroll(event -> {
            if (event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                double deltaY = event.getDeltaY() * 0.001;
                setVvalue(getVvalue() - deltaY);
            }
        });

        this.mediaList.addListener((ListChangeListener<? super MediaItem>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    List<MediaItemView> newGridItems = change.getAddedSubList().stream()
                        .map(mediaItem -> {
                            MediaItemView gridItem = new MediaItemView(mediaItem);
                            gridItem.setOnMouseClicked(mouseEvent -> listener.itemClicked(mediaItem.getFile()));
                            return gridItem;
                        })
                        .collect(toList());
                    tilePane.getChildren().addAll(newGridItems);
                }
            }
        });

        setContent(tilePane);
    }
}
