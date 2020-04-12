package uk.co.caprica.vlcj.javafx.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.util.List;

import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

/**
 *
 */
public class VlcjJavaFxApplication extends Application {

    private final MediaPlayerFactory mediaPlayerFactory;

    private final EmbeddedMediaPlayer embeddedMediaPlayer;

    private ImageView videoImageView;

    public VlcjJavaFxApplication() {
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
            }
        });
    }

    @Override
    public void init() {
        this.videoImageView = new ImageView();
        this.videoImageView.setPreserveRatio(true);

        embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(this.videoImageView));
    }

    @Override
    public final void start(Stage primaryStage) throws Exception {
        List<String> params = getParameters().getRaw();
        if (params.size() != 1) {
            System.out.println("Specify a single MRL");
            System.exit(-1);
        }

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        videoImageView.fitWidthProperty().bind(root.widthProperty());
        videoImageView.fitHeightProperty().bind(root.heightProperty());

        root.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            // If you need to know about resizes
        });

        root.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            // If you need to know about resizes
        });

        root.setCenter(videoImageView);

        Scene scene = new Scene(root, 1200, 675, Color.BLACK);
        primaryStage.setTitle("vlcj JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        embeddedMediaPlayer.media().play(params.get(0));

        embeddedMediaPlayer.controls().setPosition(0.4f);
    }

    @Override
    public final void stop() {
        embeddedMediaPlayer.controls().stop();
        embeddedMediaPlayer.release();
        mediaPlayerFactory.release();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

