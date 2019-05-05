package uk.co.caprica.vlcj.javafx.test.resize;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;

public class JavaFXExample extends Application {

    private static final String PATH_TO_VIDEO = "/media/linus/60C78DDD3E5786A3/Anime/The Disastrous Life of Saiki K./Staffel 2/Episoden/[Davinci] Saiki Kusuo no Psi-nan 2 - 01v2 [720p][69539A2C].mkv";

    private ImageView imageView;
    private EmbeddedMediaPlayer mediaPlayerComponent;
    private Pane playerHolder;
    private FloatProperty videoSourceRatioProperty;
    private WritableImage writableImage;
    private WritablePixelFormat<ByteBuffer> pixelFormat;

    @Override
    public void start(Stage primaryStage) {
        mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
        videoSourceRatioProperty = new SimpleFloatProperty(0.4f);
        pixelFormat = PixelFormat.getByteBgraPreInstance();
        playerHolder = new Pane();

        initializeImageView();

        CallbackVideoSurface callbackVideoSurface = new CallbackVideoSurface(new CanvasBufferFormatCallback(), new CanvasPlayerComponent(), true, null);

        mediaPlayerComponent.videoSurface().set(callbackVideoSurface);
        mediaPlayerComponent.media().prepare(PATH_TO_VIDEO);

        primaryStage.setOnCloseRequest(event -> {
            mediaPlayerComponent.release();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setScene(new Scene(new BorderPane(playerHolder)));
        primaryStage.show();

        // to make window active
        primaryStage.setIconified(true);
        primaryStage.setIconified(false);

        mediaPlayerComponent.controls().start();
    }

    private void initializeImageView() {
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        writableImage = new WritableImage((int) visualBounds.getWidth(), (int) visualBounds.getHeight());

        imageView = new ImageView(writableImage);
        playerHolder.getChildren().add(imageView);

        playerHolder.widthProperty().addListener((observable, oldValue, newValue) ->
            fitImageViewSize(newValue.floatValue(), (float) playerHolder.getHeight())
        );

        playerHolder.heightProperty().addListener((observable, oldValue, newValue) ->
            fitImageViewSize((float) playerHolder.getWidth(), newValue.floatValue())
        );

        videoSourceRatioProperty.addListener((observable, oldValue, newValue) ->
            fitImageViewSize((float) playerHolder.getWidth(), (float) playerHolder.getHeight())
        );
    }

    private void fitImageViewSize(float width, float height) {
        Platform.runLater(() -> {
            float fitHeight = videoSourceRatioProperty.get() * width;

            if (fitHeight > height) {
                imageView.setFitHeight(height);
                double fitWidth = height / videoSourceRatioProperty.get();
                imageView.setFitWidth(fitWidth);
                imageView.setX((width - fitWidth) / 2);
                imageView.setY(0);
            } else {
                imageView.setFitWidth(width);
                imageView.setFitHeight(fitHeight);
                imageView.setY((height - fitHeight) / 2);
                imageView.setX(0);
            }
        });
    }

    private class CanvasPlayerComponent implements RenderCallback {

        PixelWriter pixelWriter = null;

        private PixelWriter getPixelWriter() {
            if (pixelWriter == null) {
                pixelWriter = writableImage.getPixelWriter();
            }

            return pixelWriter;
        }

        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            if (writableImage == null) {
                return;
            }

            Platform.runLater(() -> {
                try {
                    ByteBuffer byteBuffer = nativeBuffers[0];
                    getPixelWriter().setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

    }

    private class CanvasBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            Platform.runLater(() -> videoSourceRatioProperty.set((float) sourceHeight / (float) sourceWidth));
            return new RV32BufferFormat((int) visualBounds.getWidth(), (int) visualBounds.getHeight());
        }
    }

    public static void main(String[] args) {
        Application.launch(JavaFXExample.class);
    }

}
