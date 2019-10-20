package uk.co.caprica.vlcj.javafx.test;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import uk.co.caprica.vlcj.player.base.LibVlcConst;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

class VideoControlsPane extends BorderPane {

    private final Label hueLabel;
    private final Label saturationLabel;
    private final Label brightnessLabel;
    private final Label contrastLabel;
    
    private final Slider hueSlider;
    private final Slider saturationSlider;
    private final Slider brightnessSlider;
    private final Slider contrastSlider;

    VideoControlsPane(MediaPlayer mediaPlayer) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(16));

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setHgrow(Priority.NEVER);

        ColumnConstraints sliderColumn = new ColumnConstraints();
        sliderColumn.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(labelColumn, sliderColumn);

        hueLabel = new VideoControlLabel("Hue");
        saturationLabel = new VideoControlLabel("Saturation");
        brightnessLabel = new VideoControlLabel("Brightness");
        contrastLabel = new VideoControlLabel("Contrast");
        
        hueSlider = new VideoControlSlider(LibVlcConst.MIN_HUE, LibVlcConst.MAX_HUE, mediaPlayer.video().hue());
        saturationSlider = new VideoControlSlider(LibVlcConst.MIN_SATURATION, LibVlcConst.MAX_SATURATION, mediaPlayer.video().saturation());
        brightnessSlider = new VideoControlSlider(LibVlcConst.MIN_BRIGHTNESS, LibVlcConst.MAX_BRIGHTNESS, mediaPlayer.video().brightness());
        contrastSlider = new VideoControlSlider(LibVlcConst.MIN_CONTRAST, LibVlcConst.MAX_CONTRAST, mediaPlayer.video().contrast());

        GridPane.setConstraints(hueLabel, 0, 0);
        GridPane.setConstraints(saturationLabel, 0, 1);
        GridPane.setConstraints(brightnessLabel, 0, 2);
        GridPane.setConstraints(contrastLabel, 0, 3);

        GridPane.setConstraints(hueSlider, 1, 0);
        GridPane.setConstraints(saturationSlider, 1, 1);
        GridPane.setConstraints(brightnessSlider, 1, 2);
        GridPane.setConstraints(contrastSlider, 1, 3);

        grid.getChildren().addAll(hueLabel, hueSlider);
        grid.getChildren().addAll(saturationLabel, saturationSlider);
        grid.getChildren().addAll(brightnessLabel, brightnessSlider);
        grid.getChildren().addAll(contrastLabel, contrastSlider);

        setCenter(grid);

        hueSlider.valueProperty().addListener((obs, oldValue, newValue) -> mediaPlayer.video().setHue(newValue.floatValue()));
        saturationSlider.valueProperty().addListener((obs, oldValue, newValue) -> mediaPlayer.video().setSaturation(newValue.floatValue()));
        brightnessSlider.valueProperty().addListener((obs, oldValue, newValue) -> mediaPlayer.video().setBrightness(newValue.floatValue()));
        contrastSlider.valueProperty().addListener((obs, oldValue, newValue) -> mediaPlayer.video().setContrast(newValue.floatValue()));
    }

    private class VideoControlLabel extends Label {

        private VideoControlLabel(String caption) {
            super(caption);
            setPadding(new Insets(8));
        }

    }

    private class VideoControlSlider extends Slider {

        private VideoControlSlider(double min, double max, double value) {
            super(min, max, value);
            setPadding(new Insets(8));
        }

    }

}
