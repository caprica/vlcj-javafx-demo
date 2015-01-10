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
 * Copyright 2009, 2010, 2011, 2012, 2013, 2014 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.javafx.test;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * Implementation of a JavaFX direct rendering media player that uses a JavaFX animation Timeline.
 */
public class TimelineJavaFXDirectRenderingTest extends JavaFXDirectRenderingTest {

    /**
     *
     */
    private static final double FPS = 60.0;

    /**
     *
     */
    private final Timeline timeline;

    /**
     *
     */
    private final EventHandler<ActionEvent> nextFrameHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent t) {
            renderFrame();
        }
    };

    public TimelineJavaFXDirectRenderingTest() {
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        double duration = 1000.0 / FPS;
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration), nextFrameHandler));
    }

    @Override
    protected void startTimer() {
        timeline.playFromStart();
    }

    @Override
    protected void stopTimer() {
        timeline.stop();
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}
