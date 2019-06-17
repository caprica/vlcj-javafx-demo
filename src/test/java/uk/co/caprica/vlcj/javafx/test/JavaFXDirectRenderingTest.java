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
 * Copyright 2009, 2010, 2011, 2012, 2013, 2014, 2015 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.javafx.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;

// README IMPORTANT
//
// This class is a hacked-up version of the original JavaFX vlcj demo class with changes to use the new experimental
// JavaFX PixelBuffer - the implementation may still not be optimal and may not use best practice for the pixel buffer,
// but this was knocked together quickly with minimal changes from the original pixel writer version
//
// Some things to be considered:
//
//  1. There is no synchronisation - nothing prevents the buffer from being rendered while it is being updated by the
//     native thread, although in practice this seems to cause no apparent problem
//  2. The timer should ideally be paused (and later resumed) when playback is paused, stopped, finished or in error,
//     and also when the application is minimised etc
//  3. It does not seem to make much difference whether the buffer is marked as updated in the native display callback,
//     or if you wait until the renderFrame() method (so this is native thread vs timer implementation) - both
//     approaches work with minimal performance differences

/**
 * Example showing how to render video to a JavaFX Canvas component.
 * <p>
 * The target is to render full HD video (1920x1080) at a reasonable frame rate (>25fps).
 * <p>
 * This test can render the video at a fixed size, or it can take the size from the video itself.
 * <p>
 * Originally based on an example contributed by John Hendrikx.
 * <p>
 * This version takes a different approach to video scaling - the video is always rendered at its native size, but the
 * graphics context itself is scaled to resize the video, a similar implementation to how the equivalent Swing version
 * now works.
 * <p>
 * This version works with JavaFX on JDK 1.8, without "wrong thread" errors.
 */
public abstract class JavaFXDirectRenderingTest extends Application {

    /**
     * Filename of the video to play.
     */
    private static final String VIDEO_FILE = "/home/mark/sekiro.mp4";

    /**
     * Lightweight JavaFX canvas, the video is rendered here.
     */
    private final Canvas canvas;

    /**
     * Pixel format.
     */
    private final WritablePixelFormat<ByteBuffer> pixelFormat;

    /**
     *
     */
    private final BorderPane borderPane;

    /**
     *
     */
    private final MediaPlayerFactory mediaPlayerFactory;

    /**
     * The vlcj direct rendering media player component.
     */
    private final EmbeddedMediaPlayer mediaPlayer;

    /**
     *
     */
    private Stage stage;

    /**
     *
     */
    private Scene scene;

    private int bufferWidth;

    private int bufferHeight;

    /**
     *
     */
    private PixelBuffer pixelBuffer;

    private WritableImage img;

    private Rectangle2D updatedBuffer;

    /**
     *
     */
    public JavaFXDirectRenderingTest() {
        canvas = new Canvas();

        pixelFormat = PixelFormat.getByteBgraPreInstance();

        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                startTimer();
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                stopTimer();
//                pauseTimer();
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                stopTimer();
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                stopTimer();
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                stopTimer();
            }
        });

        mediaPlayer.videoSurface().set(new JavaFxVideoSurface());

        borderPane = new BorderPane();
        borderPane.setCenter(canvas);
        borderPane.setStyle("-fx-background-color: rgb(0, 0, 0);");

        canvas.setOnMouseClicked(event -> {
            mediaPlayer.controls().pause();
        });

        canvas.widthProperty().bind(borderPane.widthProperty());
        canvas.heightProperty().bind(borderPane.heightProperty());
    }

    @Override
    public final void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        stage.setTitle("vlcj JavaFX PixelBuffer test");

        scene = new Scene(borderPane, Color.BLACK);

        primaryStage.setScene(scene);
        primaryStage.show();

        mediaPlayer.controls().setRepeat(true);

        mediaPlayer.media().play(VIDEO_FILE);
    }

    @Override
    public final void stop() throws Exception {
        stopTimer();

        mediaPlayer.controls().stop();
        mediaPlayer.release();
        mediaPlayerFactory.release();
    }

    private class JavaFxVideoSurface extends CallbackVideoSurface {

        JavaFxVideoSurface() {
            super(new JavaFxBufferFormatCallback(), new JavaFxRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
        }

    }

    private class JavaFxBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            bufferWidth = sourceWidth;
            bufferHeight = sourceHeight;

            Platform.runLater(() -> {
                // The stage size doesn't really matter, it's the buffer size that's important
//                stage.setWidth(sourceWidth);
//                stage.setHeight(sourceHeight);

                // This does not need to be done here, but you could set the video surface size to match the native
                // video size here
                stage.setWidth(960);
                stage.setHeight(540);
            });
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

        @Override
        public void allocatedBuffers(ByteBuffer[] buffers) {
            // This is the new magic sauce, the native video buffer is used directly for the image buffer - there is no
            // full-frame buffer copy here
            pixelBuffer = new PixelBuffer(bufferWidth, bufferHeight, buffers[0], pixelFormat);
            img = new WritableImage(pixelBuffer);
            // Since for every frame the entire buffer will be updated, we can optimise by caching the result here
            updatedBuffer = new Rectangle2D(0, 0, bufferWidth, bufferHeight);
        }

    }

    // This is correct as far as it goes, but we need to use one of the timers to get smooth rendering (the timer is
    // handled by the demo sub-classes)
    private class JavaFxRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            // We only need to tell the pixel buffer which pixels were updated (in this case all of them) - the
            // pre-cached value is used
            Platform.runLater(() -> pixelBuffer.updateBuffer(pixBuf -> updatedBuffer));
        }
    }

    /**
     * This method is called for each tick of whatever timer implementation has been chosen..
     * <p>
     * Needless to say, this method should run as quickly as possible.
     */
    protected final void renderFrame() {
        GraphicsContext g = canvas.getGraphicsContext2D();

        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // The frame must always be filled with background colour first since the rendered image may actually be smaller
        // than the full canvas - otherwise we will end up with garbage in the borders
        g.setFill(new Color(0, 0, 0, 1));
        g.fillRect(0, 0, width, height);

        if (img != null) {
            double imageWidth = img.getWidth();
            double imageHeight = img.getHeight();

            double sx = width / imageWidth;
            double sy = height / imageHeight;

            double sf = Math.min(sx, sy);

            double scaledW = imageWidth * sf;
            double scaledH = imageHeight * sf;

            Affine ax = g.getTransform();

            g.translate(
                (width - scaledW) / 2,
                (height - scaledH) / 2
            );

            if (sf != 1.0) {
                g.scale(sf, sf);
            }

            // You can do this here if you want, instead of the display() callback, doesn't seem to make much difference
//            pixelBuffer.updateBuffer(pixBuf -> updatedBuffer);

            g.drawImage(img, 0, 0);

            g.setTransform(ax);
        }
    }

    /**
     *
     */
    protected abstract void startTimer();

    /**
     *
     */
    protected abstract void stopTimer();
}
