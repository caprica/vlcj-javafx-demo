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

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
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

import java.io.File;
import java.nio.ByteBuffer;

import static uk.co.caprica.vlcj.javafx.test.MenuBuilder.createMenu;

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
 * Originally based on an example long ago contributed by John Hendrikx, now almost completely reimplemented with a
 * different approach to video scaling - the video is always rendered at its native size, but the graphics context
 * itself is scaled to resize the video, a similar implementation to how the equivalent Swing version now works.
 * <p>
 * This version requires a build of JavaFX with the proposed PixelBuffer class, currently this is available only as a
 * custom build of JavaFX from here https://mail.openjdk.java.net/pipermail/openjfx-dev/2019-June/023347.html.
 * <p>
 * Using the PixelBuffer means that LibVLC can now render directly into a native video buffer that is shared with the
 * JavaFX image used to render the video frame.
 * <p>
 * This approach now, along with JavaFX hardware acceleration, probably outperforms the corresponding implementation
 * that uses Swing/Java2D.
 */
public abstract class JavaFXDirectRenderingTest extends Application {

    private static final String BLACK_BACKGROUND_STYLE = "-fx-background-color: rgb(0, 0, 0);";

    private static final String STATUS_BACKGROUND_STYLE = "-fx-background-color: rgb(232, 232, 232); -fx-label-padding: 8 8 8 8;";

    private static final Color BLACK = new Color(0, 0, 0,1);
    private static final Color WHITE = new Color(1,1,1,1);
    private static final Font FONT = Font.font("Monospace", 40);

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

    private final FileChooser fileChooser;

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

    private Stage videoControlsStage;

    /**
     *
     */
    private Scene scene;

    private MenuBar menuBar;

    private ControlsPane controlsPane;

    private int bufferWidth;

    private int bufferHeight;

    /**
     *
     */
    private PixelBuffer pixelBuffer;

    private WritableImage img;

    private Rectangle2D updatedBuffer;

    private boolean showStats = true;

    private boolean showAnimation = true;

    private long start;
    private long frames;
    private long maxFrameTime;
    private long totalFrameTime;

    DoubleProperty x  = new SimpleDoubleProperty();
    DoubleProperty y  = new SimpleDoubleProperty();

    DoubleProperty opacity = new SimpleDoubleProperty();

    /**
     *
     */
    public JavaFXDirectRenderingTest() {
        pixelFormat = PixelFormat.getByteBgraPreInstance();

        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        mediaPlayer.events().addMediaPlayerEventListener(new TimerHandler(this));

        mediaPlayer.videoSurface().set(new JavaFxVideoSurface());

        borderPane = new BorderPane();
        borderPane.setStyle(BLACK_BACKGROUND_STYLE);

        canvas = new Canvas();
        canvas.setStyle(BLACK_BACKGROUND_STYLE);

        Pane canvasPane = new Pane();
        canvasPane.setStyle(BLACK_BACKGROUND_STYLE);
        canvasPane.getChildren().add(canvas);

        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        // Listen to width/height changes to force the video surface to re-render if the media player is not currently
        // playing - this is necessary to repaint damaged regions because the repaint timer is stopped/paused while the
        // media player is not playing
        canvas.widthProperty().addListener(event -> {if (!mediaPlayer.status().isPlaying()) renderFrame();});
        canvas.heightProperty().addListener(event -> {if (!mediaPlayer.status().isPlaying()) renderFrame();});

        borderPane.setCenter(canvasPane);

        Pane statusPane = new Pane();
        statusPane.setStyle(STATUS_BACKGROUND_STYLE);

        Label statusLabel = new Label("vlcj-javafx with PixelBuffer is ready for awesome");
        statusLabel.setStyle(STATUS_BACKGROUND_STYLE);
        statusPane.getChildren().add(statusLabel);

        borderPane.setBottom(statusPane);

        controlsPane = new ControlsPane(mediaPlayer);
        borderPane.setBottom(controlsPane);

        menuBar = createMenu(this);
        borderPane.setTop(menuBar);

        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Media File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Media Files", "*.avi", "*.flv", "*.mp4", "*.mpeg", "*.mpg", ".wmv"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                // Reset the frame stats each time the media is started (otherwise e.g. a pause would mess with the
                // stats (like FPS)
                resetStats();
            }
        });

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(0),
                new KeyValue(x, 10, Interpolator.EASE_BOTH),
                new KeyValue(y, 10)
            ),
            new KeyFrame(Duration.seconds(0.5),
                new KeyValue(x, 70, Interpolator.EASE_BOTH),
                new KeyValue(y, 10)
            )
        );
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);

        Timeline timeline2 = new Timeline(
            new KeyFrame(Duration.seconds(0),
                new KeyValue(opacity, 0, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.seconds(0.5),
                new KeyValue(opacity, 1, Interpolator.EASE_BOTH)
            )
        );
        timeline2.setAutoReverse(true);
        timeline2.setCycleCount(Timeline.INDEFINITE);

        timeline.play();
        timeline2.play();
    }

    @Override
    public final void start(Stage primaryStage) {
        this.stage = primaryStage;

        stage.setTitle("vlcj JavaFX PixelBuffer test");
        stage.setX(400);
        stage.setY(200);
        stage.setWidth(900);
        stage.setHeight(600);

        scene = new Scene(borderPane, Color.BLACK);

        stage.setOnCloseRequest(windowEvent -> System.exit(0));

        videoControlsStage = new Stage(StageStyle.UNDECORATED);
        videoControlsStage.setTitle("Video Adjustments");
        videoControlsStage.setScene(new Scene(new VideoControlsPane(mediaPlayer), Color.BLACK));
        videoControlsStage.setOnShowing(windowEvent -> {
            videoControlsStage.setX(stage.getX() + stage.getWidth() + 4);
            videoControlsStage.setY(stage.getY());
        });
        videoControlsStage.sizeToScene();

        primaryStage.setScene(scene);
        primaryStage.show();

        mediaPlayer.controls().setRepeat(true);

        startTimer();
    }

    @Override
    public final void stop() {
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

            // This does not need to be done here, but you could set the video surface size to match the native video
            // size

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
        frames++;

        long renderStart = System.currentTimeMillis();

        GraphicsContext g = canvas.getGraphicsContext2D();

        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // The canvas must always be filled with background colour first since the rendered image may actually be
        // smaller than the full canvas - otherwise we will end up with garbage in the borders on resize
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

            double fps = (double) 1000 * frames / (renderStart - start);
            double meanFrameTime = totalFrameTime / (double) frames;

            if (showStats) {
                String val = String.format(
                    " Frames: %d\n" +
                    "Seconds: %d\n" +
                    "    FPS: %01.1f\n" +
                    "Maximum: %d ms\n" +
                    "   Mean: %01.3f ms",
                    frames, (renderStart - start) / 1000, fps, maxFrameTime, meanFrameTime
                );

                renderText(g, val, 100, 200);
            }

            if (showAnimation) {
                g.setFill(Color.CORNSILK);
                g.fillOval(
                    x.doubleValue(),
                    y.doubleValue(),
                    40,
                    40);

                g.save();
                g.setGlobalAlpha(opacity.doubleValue());
                g.setTextAlign(TextAlignment.CENTER);
                renderText(g, "vlcj JavaFX PixelBuffer Win!", img.getWidth() / 2, img.getHeight() - 120);
                g.restore();
            }

            g.setTransform(ax);
        }

        if (renderStart - start > 1000) {
            long renderTime = System.currentTimeMillis() - renderStart;
            maxFrameTime = Math.max(maxFrameTime, renderTime);
            totalFrameTime += renderTime;
        }
    }

    /**
     * A crude, but fast, renderer to draw outlined text.
     * <p>
     * Generally the approach here is faster than getting the text outline and stroking it.
     *
     * @param g
     * @param text
     * @param x
     * @param y
     */
    private void renderText(GraphicsContext g, String text, double x, double y) {
        g.setFont(FONT);
        g.setFill(BLACK);
        g.fillText(text, x - 1, y - 1);
        g.fillText(text, x + 1, y - 1);
        g.fillText(text, x - 1, y + 1);
        g.fillText(text, x + 1, y + 1);
        g.setFill(WHITE);
        g.fillText(text, x, y);
    }

    private void resetStats() {
        start = System.currentTimeMillis();
        frames = 0;
        maxFrameTime = 0;
        totalFrameTime = 0;
    }

    void openFile() {
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            mediaPlayer.media().play(selectedFile.getAbsolutePath());
        }
    }

    void adjustVideo(boolean selected) {
        if (selected) {
            videoControlsStage.show();
            mediaPlayer.video().setAdjustVideo(true);
        } else {
            videoControlsStage.hide();
            mediaPlayer.video().setAdjustVideo(false);
        }
    }

    void toggleAlwaysOnTop() {
        stage.setAlwaysOnTop(!stage.isAlwaysOnTop());
    }

    void toggleMinimalInterface(boolean on) {
        menuBar.setVisible(on);
        controlsPane.setVisible(on);

        // Also need to set the managed property to prevent hidden components taking layout space
        menuBar.setManaged(on);
        controlsPane.setManaged(on);
    }

    void toggleFullScreen() {
        stage.setFullScreen(!stage.isFullScreen());
    }

    void toggleStatsOverlay(boolean show) {
        showStats = show;
    }

    void toggleAnimationOverlay(boolean show) {
        showAnimation = show;
    }

    void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About vlcj JavaFX");
        alert.setHeaderText(null);
        alert.setContentText(
            "This demo shows how to use vlcj with JavaFX and the new proposed JavaFX PixelBuffer class.\n\n" +
            "PixelBuffer allows us to share the native video buffer directly which means we can reduce the number of full-frame copies for nice performance!\n\n" +
            "This means LibVLC renders *directly* into the buffer used to render the frame image in a JavaFX canvas.\n\n" +
            "If you're interested in vlcj and JavaFX you should lobby hard for next version of JavaFX to include PixelBuffer!"
        );
        alert.initOwner(stage);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    /**
     *
     */
    protected abstract void startTimer();

    /**
     *
     */
    protected abstract void pauseTimer();

    /**
     *
     */
    protected abstract void stopTimer();
}
