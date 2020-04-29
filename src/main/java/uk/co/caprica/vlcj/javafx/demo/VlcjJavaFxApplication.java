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

package uk.co.caprica.vlcj.javafx.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import uk.co.caprica.mediascanner.MediaScanner;
import uk.co.caprica.mediascanner.domain.MediaSet;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.demo.domain.MediaItem;
import uk.co.caprica.vlcj.javafx.demo.thumbnailer.ThumbnailManager;
import uk.co.caprica.vlcj.javafx.demo.views.grid.MediaGrid;
import uk.co.caprica.vlcj.javafx.demo.views.player.PlayerView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JavaFX demonstration application for vlcj.
 * <p>
 * This version requires at least Java 11, JavaFX 13 (for PixelBuffer), vlcj 5.0.0 and LibVLC 4.0.0.
 * <p>
 * Thumbnails are generated with the new native thumbnailer that comes with LibVLC 4.0.0.
 * <p>
 * You can specify multiple concurrent media players by changing the ROWS and COLS constants. The maximum number of
 * concurrent media players is obviously limited by your available CPU/GPU resources. Adding more players will reduce
 * overall performance and will eventually lead to stuttering playback and over-utilised CPUs.
 * <p>
 * With LibVLC 4.0.0 (unlike earlier versions) the media player volume controls <em>are</em> independent.
 * <p>
 * Your home directory is scanned for various video files and the resulting list is used to generate thumbnails. On
 * clicking a thumbnail, the next media player in sequence is used to play that media.
 * <p>
 * On Linux, the repaints can be glitchy if your scene graph is "busy", e.g. if you add multiple video views or numerous
 * other nodes. This manifests itself as flickering painting on the later components (the ones added latest to the
 * scene), and/or the background fill of the Scene leaking through to some other component.
 * <p>
 * If this is a problem, it is recommended to pass "-Dprism.dirtyopts=false" as a system property when starting the JVM.
 * <p>
 * This will incur a performance penalty.
 * <p>
 * Similarly, if you add more components (especially something like a menu bar) and the repaint of the controls starts
 * lagging on a dynamic re-size, passing "-Dprism.forceUploadingPainter=true" may help.
 * <p>
 * See https://github.com/caprica/vlcj-javafx-demo/issues/31.
 * <p>
 * See http://werner.yellowcouch.org/log/javafx-8-command-line-options for a list of the various JavaFX system
 * properties available.
 * <p>
 * If on Linux your application would cause LibX to be used, for example by opening a file chooser dialog box, this can
 * cause a fatal JVM crash unless you pass "-DVLCJ_INITX=no" when starting the JVM.
 * <p>
 * See https://github.com/caprica/vlcj/issues/929 and related issues.
 * <p>
 * This is the command-line I use on Linux:
 * <p>
 * -DVLCJ_INITX=no -Dprism.dirtyopts=false -Dprism.forceUploadingPainter=true -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC
 */
public class VlcjJavaFxApplication extends Application {

    /**
     * Thumbnail images will be cached in this sub-directory of the user home directory.
     */
    private static final String THUMBNAIL_CACHE_DIRECTORY = ".vlcj-javafx-cache";

    private static final String MEDIA_DIRECTORY = System.getProperty("user.home") + "/Downloads";

    private static final float THUMBNAIL_POSITION = 0.1f;

    private static final int THUMBNAIL_WIDTH = 240;

    private static final int THUMBNAIL_HEIGHT = 135;

    private static final long THUMBNAIL_TIMEOUT = 3000L;

    private static final int ROWS = 2;

    private static final int COLS = 2;

    private static final int PLAYERS = ROWS * COLS;

    private static final String STYLESHEET = "/css/main.css";

    private PlayerView[] playerViews;

    private final ThumbnailManager thumbnailManager;

    private final ExecutorService mediaLoader = Executors.newSingleThreadExecutor();

    private final ObservableList<MediaItem> mediaList;

    private int currentPlayerIndex = 0;

    public VlcjJavaFxApplication() {
        this.thumbnailManager = new ThumbnailManager(
            new MediaPlayerFactory(),
            Path.of(System.getProperty("user.home"), THUMBNAIL_CACHE_DIRECTORY),
            THUMBNAIL_POSITION,
            THUMBNAIL_WIDTH,
            THUMBNAIL_HEIGHT,
            THUMBNAIL_TIMEOUT
        );
        this.mediaList = FXCollections.observableArrayList();
    }

    @Override
    public void init() {
    }

    @Override
    public final void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        this.playerViews = new PlayerView[PLAYERS];
        for (int i = 0; i < PLAYERS; i++) {
            this.playerViews[i] = new PlayerView();
        }

        MediaGrid grid = new MediaGrid(mediaList, file -> {
            PlayerView playerView = playerViews[currentPlayerIndex++ % PLAYERS];
            playerView.play(file.getAbsolutePath());
        });

        GridPane playerGrid = new GridPane();
        playerGrid.getStyleClass().add("player-grid");

        ColumnConstraints fillColumn = new ColumnConstraints();
        fillColumn.setHgrow(Priority.ALWAYS);
        fillColumn.setFillWidth(true);

        RowConstraints fillRow = new RowConstraints();
        fillRow.setVgrow(Priority.ALWAYS);
        fillRow.setFillHeight(true);

        for (int row = 0; row < ROWS; row++) {
            playerGrid.getRowConstraints().add(fillRow);
        }

        for (int col = 0; col < COLS; col++) {
            playerGrid.getColumnConstraints().add(fillColumn);
        }

        int i = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                GridPane.setConstraints(playerViews[i++], col, row);
            }
        }

        playerGrid.getChildren().addAll(playerViews);

        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: black;");
        splitPane.setOrientation(Orientation.HORIZONTAL);

        splitPane.getItems().addAll(grid, playerGrid);
        splitPane.setDividerPosition(0, 0.395f);

        Scene scene = new Scene(splitPane, 1900, 650, Color.BLACK);
        primaryStage.setTitle("vlcj JavaFX");
        primaryStage.setScene(scene);

        scene.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        scene.getAccelerators().put(KeyCombination.keyCombination("f11"), () -> primaryStage.setFullScreen(!primaryStage.isFullScreen()));

        primaryStage.show();

        prepareMedia();
    }

    private void prepareMedia() {
        System.out.printf("Scanning '%s'...%n", MEDIA_DIRECTORY);

        mediaLoader.submit(() -> {
            try {
                MediaSet mediaSet = MediaScanner.create()
                    .followLinks()
                    .directory(MEDIA_DIRECTORY)
                    .matching("glob:**/*.{mp4,avi,flv}")    // Adjust to suit
                    .findMedia()
                    .mediaSet();

                mediaSet.entries().forEach(mediaEntry -> {
                    File file = mediaEntry.file().toFile();
                    Optional<Image> thumbnail = thumbnailManager.thumbnailFor(file.toPath());
                    thumbnail.ifPresent(image -> Platform.runLater(() -> mediaList.add(MediaItem.of(file, image))));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        mediaLoader.shutdown();

        System.out.println("Finished scanning media directory.");
    }

    @Override
    public final void stop() {
        Arrays.stream(playerViews).forEach(PlayerView::release);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Separate launcher to avoid the awesome Java Module System
class VlcjJavaFxApplicationLauncher {
    public static void main(String[] args) {
        VlcjJavaFxApplication.main(args);
    }
}
