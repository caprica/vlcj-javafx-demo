package uk.co.caprica.vlcj.javafx.test;

import java.nio.ByteBuffer;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

/**
 * This example was contributed by Michael Paus, incorporated here with some minor
 * modifications, see https://github.com/caprica/vlcj/issues/883.
 * <p>
 * This is a very simple example of a video player which uses the new WritableImage
 * of JavaFX with support for Buffers. The idea is to let VLC directly render into
 * this buffer and use the image directly in an ImageView without any explicit rendering
 * into a canvas or such thing. Only this brings the desired performance boost.
 * <p>
 * What I have not yet considered yet is any kind of synchronization.
 * I think an extension of the PixelBuffer to support some kine of double-buffering
 * would be the right thing to do.
 * <p>
 * The following dependencies should be enough to compile this example with OpenJDK 13.
 * <pre>
 *  <dependencies>
 *    <dependency>
 *      <groupId>uk.co.caprica</groupId>
 *      <artifactId>vlcj</artifactId>
 *      <version>4.2.0</version>
 *    </dependency>
 *    <dependency>
 *      <groupId>org.openjfx</groupId>
 *      <artifactId>javafx-controls</artifactId>
 *      <version>13</version>
 *    </dependency>
 *  </dependencies>
 * </pre>
 * <p>
 * Specify the MRL of the video to play as a command-line argument.
 *
 * @author Michael Paus
 */
public class WritableImageDemo extends Application {

    private MediaPlayerFactory mediaPlayerFactory;

    private EmbeddedMediaPlayer embeddedMediaPlayer;

    private WritableImage videoImage;

    private PixelBuffer<ByteBuffer> videoPixelBuffer;

    private ImageView videoImageView;

    @Override
	public void init() {
        mediaPlayerFactory = new MediaPlayerFactory();
        embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        embeddedMediaPlayer.videoSurface().set(new FXCallbackVideoSurface());
    }

    @Override
    public final void start(Stage primaryStage) throws Exception {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        videoImageView = new ImageView();
        videoImageView.setPreserveRatio(true);
        videoImageView.fitWidthProperty().bind(root.widthProperty());
        videoImageView.fitHeightProperty().bind(root.heightProperty());
        root.getChildren().add(videoImageView);

        // =========== Just for fun ===================
        Label label = new Label("This is JavaFX");
        label.setStyle("-fx-font-size: 60; -fx-text-fill: red;");
        Group group = new Group(label);
        root.getChildren().add(group);

        RotateTransition rt = new RotateTransition(Duration.millis(3000), label);
        rt.setByAngle(360);
        rt.setCycleCount(RotateTransition.INDEFINITE);
        rt.play();
        // ============================================

        Scene scene = new Scene(root, 1200, 675);
        primaryStage.setScene(scene);
        primaryStage.show();

        String mrl = getParameters().getRaw().get(0);

        embeddedMediaPlayer.media().play(mrl);
    }

    @Override
    public final void stop() throws Exception {
        embeddedMediaPlayer.controls().stop();
        embeddedMediaPlayer.release();
        mediaPlayerFactory.release();
    }

    private class FXCallbackVideoSurface extends CallbackVideoSurface {
        FXCallbackVideoSurface() {
            super(new FXBufferFormatCallback(), new FXRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
        }
    }

    private class FXBufferFormatCallback implements BufferFormatCallback {
        private int sourceWidth;
        private int sourceHeight;

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

        @Override
        public void allocatedBuffers(ByteBuffer[] buffers) {
            assert buffers[0].capacity() == sourceWidth * sourceHeight * 4;
            PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraPreInstance();
            videoPixelBuffer = new PixelBuffer<>(sourceWidth, sourceHeight, buffers[0], pixelFormat);
            videoImage = new WritableImage(videoPixelBuffer);
            videoImageView.setImage(videoImage);
        }
    }

    private class FXRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            Platform.runLater(() -> {
                videoPixelBuffer.updateBuffer(pb -> null);
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Launch via this class to avoid module system headaches.
class WritableImageDemoLauncher {public static void main(String[] args) {WritableImageDemo.main(args);}}

