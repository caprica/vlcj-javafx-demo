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

package uk.co.caprica.vlcj.javafx.demo.thumbnailer;

import javafx.scene.image.Image;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.Picture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Component providing media thumbnails.
 * <p>
 * Thumbnails are stored in a disk cache, if a thumbnail is not already present in the cache it will be generated.
 */
public class ThumbnailManager {

    private final MediaPlayerFactory mediaPlayerFactory;

    private final Path cacheDirectory;

    private final float position;

    private final int width;

    private final int height;

    private final long timeout;

    public ThumbnailManager(MediaPlayerFactory mediaPlayerFactory, Path cacheDirectory, float position, int width, int height, long timeout) {
        this.mediaPlayerFactory = mediaPlayerFactory;
        this.cacheDirectory = cacheDirectory;
        this.position = position;
        this.width = width;
        this.height = height;
        this.timeout = timeout;
    }

    public Optional<Image> thumbnailFor(Path filePath) {
        Path thumbnailFile = getCacheFilePath(filePath);
        if (!Files.exists(thumbnailFile)) {
            generateThumbnailFrom(filePath).map(imageData -> storeThumbnail(thumbnailFile, imageData));
        }
        try {
            return Optional.of(new Image(new FileInputStream(thumbnailFile.toFile())));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    private Path getCacheFilePath(Path filePath) {
        return cacheDirectory.resolve(Integer.toHexString(filePath.hashCode()) + ".png");
    }

    private Optional<byte[]> generateThumbnailFrom(Path filePath) {
        System.out.printf("Generating thumbnail for %s%n", filePath);
        Media media = null;
        try {
            media = mediaPlayerFactory.media().newMedia(filePath.toString());
            return new ThumbnailHandler(media, position, width, height, timeout).getThumbnail().map(Picture::buffer);
        } finally {
            if (media != null) {
                media.release();
            }
        }
    }

    private Optional<Path> storeThumbnail(Path thumbnailFile, byte[] imageData) {
        System.out.printf("Storing thumbnail %s%n", thumbnailFile);
        try {
            Files.createDirectories(thumbnailFile.getParent());
            return Optional.of(Files.write(thumbnailFile, imageData));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
