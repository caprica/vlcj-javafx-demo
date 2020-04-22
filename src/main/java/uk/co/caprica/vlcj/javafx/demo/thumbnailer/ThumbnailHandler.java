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

import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.media.Picture;
import uk.co.caprica.vlcj.media.PictureType;
import uk.co.caprica.vlcj.media.ThumbnailRequest;
import uk.co.caprica.vlcj.media.ThumbnailerSeekSpeed;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Component used to generate a single thumbnail.
 * <p>
 * This implementation uses the new native thumbnailer, only available with VLC 4.x onwards.
 */
final class ThumbnailHandler extends MediaEventAdapter {

    private final Media media;

    private final float position;

    private final int width;

    private final int height;

    private final long timeout;

    private final CountDownLatch latch = new CountDownLatch(1);

    private ThumbnailRequest thumbnailRequest;

    private volatile Picture thumbnail;

    ThumbnailHandler(Media media, float position, int width, int height, long timeout) {
        this.media = media;
        this.position = position;
        this.width = width;
        this.height = height;
        this.timeout = timeout;
    }

    Optional<Picture> getThumbnail() {
        // Add this component as a listener
        media.events().addMediaEventListener(this);

        // Submit the request
        thumbnailRequest = media.thumbnails().requestByPosition(
            position,
            ThumbnailerSeekSpeed.FAST,
            width,
            height,
            false,
            PictureType.PNG,
            timeout);

        // Wait for the listener to signal that the thumbnail was generated
        try {
            latch.await();
            return Optional.of(thumbnail);
        } catch (Exception e ) {
            return Optional.empty();
        } finally {
            // Stop this component from listening for further events
            media.events().removeMediaEventListener(this);

            if (thumbnailRequest != null) {
                thumbnailRequest.release();
            }
        }
    }

    @Override
    public void mediaThumbnailGenerated(Media media, Picture picture) {
        thumbnail = picture;
        latch.countDown();
    }
}
