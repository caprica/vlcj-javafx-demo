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

import javafx.animation.AnimationTimer;
import javafx.application.Application;

/**
 * Implementation of a JavaFX direct rendering media player that uses a JavaFX AnimationTimer.
 */
public class AnimationTimerJavaFXDirectRenderingTest extends JavaFXDirectRenderingTest {

    /**
     *
     */
    private final AnimationTimer timer;

    public AnimationTimerJavaFXDirectRenderingTest() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderFrame();
            }
        };
    }

    @Override
    protected void startTimer() {
        timer.start();
    }

    @Override
    protected void stopTimer() {
        timer.stop();
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
