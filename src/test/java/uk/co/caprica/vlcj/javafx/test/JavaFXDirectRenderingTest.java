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

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

// README IMPORTANT
//
// This class is a hacked-up version of the original JavaFX vlcj demo class with changes to use the new experimental
// pixel buffer - the implementation may still not be optimal and may not use best practice for the pixel buffer, but
// this was knocked together quickly with minimal changes from the original pixel writer version

/**
 * Example showing how to render video to a JavaFX Canvas component.
 * <p>
 * The target is to render full HD video (1920x1080) at a reasonable frame rate (>25fps).
 * <p>
 * This test can render the video at a fixed size, or it can take the size from the video itself.
 * <p>
 * Originally based on an example contributed by John Hendrikx.
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
     *
     */
    private PixelBuffer pixelBuffer;

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
    private WritableImage img;

    private JavaFxVideoSurface videoSurface;

    /**
     *
     */
    public JavaFXDirectRenderingTest() {
        canvas = new Canvas();

        pixelFormat = PixelFormat.getByteBgraPreInstance();

        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        videoSurface = new JavaFxVideoSurface();
        mediaPlayer.videoSurface().set(videoSurface);

        borderPane = new BorderPane();
        borderPane.setCenter(canvas);
        borderPane.setStyle("-fx-background-color: rgb(0, 0, 0);");

        Button button = new Button("Show FileChooser");
        button.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.showOpenDialog(stage);
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Information Dialog");
//                alert.setHeaderText("Look, an Information Dialog");
//                alert.setContentText("I have a great message for you!");
//
//                alert.showAndWait();
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

//        mediaPlayer.controls().setPosition(0.9f);

        startTimer();
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
                stage.setWidth(sourceWidth);
                stage.setHeight(sourceHeight);
            });
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }
    }

    // FIXME not sure if we need to protect a repaint while a buffer update is in progress
    // FIXME might be a good to idea in the video surface to invoke some function when setup is called, so we know the buffers are ready

    // This is correct as far as it goes, but we need to use one of the timers to get smooth rendering (the timer is
    // handled by the demo sub-classes)
    private class JavaFxRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            // FIXME ideally this would not be done in each display, see above FIXME about setup method from video surface
            if (pixelBuffer == null) {
                // This is the new magic sauce, the native video buffer is used directly for the image buffer - there is
                // no full-frame buffer copy here
                pixelBuffer = new PixelBuffer(bufferWidth, bufferHeight, videoSurface.getNativeBuffers()[0], pixelFormat);
                img = new WritableImage(pixelBuffer);
            }

            // We only need to tell the pixel buffer which pixels were updated (in this case all of them)
            Platform.runLater(() -> pixelBuffer.updateBuffer(pixBuf -> new Rectangle2D(0, 0, img.getWidth(), img.getHeight())));
        }
    }

    protected final void renderFrame() {
        GraphicsContext g = canvas.getGraphicsContext2D();

        double width = canvas.getWidth();
        double height = canvas.getHeight();

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
