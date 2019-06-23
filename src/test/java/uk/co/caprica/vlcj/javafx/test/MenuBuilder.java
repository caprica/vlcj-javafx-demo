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

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

/**
 * Helper class to create the application main menu.
 */
final class MenuBuilder {

    /**
     * Create the application menu.
     * <p>
     * The menu is non-functional, it is currently used for demo purposes only.
     *
     * @return menu
     */
    static MenuBar createMenu(JavaFXDirectRenderingTest application) {
        MediaPlayer mediaPlayer = application.mediaPlayer();

        MenuBar menuBar = new MenuBar();

        Menu mediaMenu = new Menu("_Media");
        mediaMenu.setMnemonicParsing(true);

        MenuItem mediaOpenFileMenuItem = new MenuItem("_Open File");
        mediaOpenFileMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        mediaOpenFileMenuItem.setMnemonicParsing(true);

        MenuItem mediaQuitMenuItem = new MenuItem("_Quit");
        mediaQuitMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        mediaQuitMenuItem.setMnemonicParsing(true);

        mediaMenu.getItems().add(mediaOpenFileMenuItem);
        mediaMenu.getItems().add(new SeparatorMenuItem());
        mediaMenu.getItems().add(mediaQuitMenuItem);

        menuBar.getMenus().add(mediaMenu);

        Menu playbackMenu = new Menu("P_layback");
        mediaMenu.setMnemonicParsing(true);

        MenuItem playbackJumpForwardMenuItem = new MenuItem("_Jump Forward");
        playbackJumpForwardMenuItem.setMnemonicParsing(true);
        playbackJumpForwardMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Right"));

        MenuItem playbackJumpBackwardMenuItem = new MenuItem("Jump Bac_kward");
        playbackJumpBackwardMenuItem.setMnemonicParsing(true);
        playbackJumpBackwardMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Left"));

        MenuItem playbackPlayMenuItem = new MenuItem("_Play");
        playbackPlayMenuItem.setMnemonicParsing(true);

        MenuItem playbackPauseMenuItem = new MenuItem("Pa_use");
        playbackPauseMenuItem.setMnemonicParsing(true);

        MenuItem playbackStopMenuItem = new MenuItem("_Stop");
        playbackStopMenuItem.setMnemonicParsing(true);

        playbackMenu.getItems().add(playbackJumpForwardMenuItem);
        playbackMenu.getItems().add(playbackJumpBackwardMenuItem);
        playbackMenu.getItems().add(new SeparatorMenuItem());
        playbackMenu.getItems().add(playbackPlayMenuItem);
        playbackMenu.getItems().add(playbackPauseMenuItem);
        playbackMenu.getItems().add(playbackStopMenuItem);

        menuBar.getMenus().add(playbackMenu);

        Menu audioMenu = new Menu("_Audio");
        audioMenu.setMnemonicParsing(true);

        CheckMenuItem audioMuteMenuItem = new CheckMenuItem("_Mute");
        audioMuteMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
        audioMuteMenuItem.setMnemonicParsing(true);
        // Mute is a bit tricksy, sadly

        audioMenu.getItems().add(audioMuteMenuItem);

//        menuBar.getMenus().add(audioMenu);

        Menu toolsMenu = new Menu("Tool_s");
        toolsMenu.setMnemonicParsing(true);

        CheckMenuItem toolsVideoAdjustmentMenuItem = new CheckMenuItem("Adjust _Video");
        toolsVideoAdjustmentMenuItem.setMnemonicParsing(true);

        toolsMenu.getItems().add(toolsVideoAdjustmentMenuItem);

        menuBar.getMenus().add(toolsMenu);

        Menu viewMenu = new Menu("V_iew");
        viewMenu.setMnemonicParsing(true);

        CheckMenuItem viewAlwaysOnTopMenuItem = new CheckMenuItem("Always on _top");
        viewAlwaysOnTopMenuItem.setMnemonicParsing(true);

        CheckMenuItem viewMinimalInterfaceMenuItem = new CheckMenuItem("Mi_nimal Interface");
        viewMinimalInterfaceMenuItem.setMnemonicParsing(true);
        viewMinimalInterfaceMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));

        MenuItem viewFullScreenMenuItem = new MenuItem("_Fullscreen Interface");
        viewFullScreenMenuItem.setMnemonicParsing(true);
        viewFullScreenMenuItem.setAccelerator(KeyCombination.keyCombination("F11"));

        CheckMenuItem viewStatsOverlayMenuItem = new CheckMenuItem("_Statistics Overlay");
        viewStatsOverlayMenuItem.setMnemonicParsing(true);
        viewStatsOverlayMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        viewStatsOverlayMenuItem.setSelected(true);

        CheckMenuItem viewAnimationOverlayMenuItem = new CheckMenuItem("_Animation Overlay");
        viewAnimationOverlayMenuItem.setMnemonicParsing(true);
        viewAnimationOverlayMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        viewAnimationOverlayMenuItem.setSelected(true);

        viewMenu.getItems().add(viewAlwaysOnTopMenuItem);
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(viewMinimalInterfaceMenuItem);
        viewMenu.getItems().add(viewFullScreenMenuItem);
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(viewStatsOverlayMenuItem);
        viewMenu.getItems().add(viewAnimationOverlayMenuItem);

        menuBar.getMenus().add(viewMenu);

        Menu helpMenu = new Menu("_Help");
        helpMenu.setMnemonicParsing(true);

        MenuItem helpAboutMenuItem = new MenuItem("_About");
        helpAboutMenuItem.setAccelerator(KeyCombination.keyCombination("Shift+F1"));
        helpAboutMenuItem.setMnemonicParsing(true);
        helpMenu.getItems().add(helpAboutMenuItem);

        menuBar.getMenus().add(helpMenu);

        mediaQuitMenuItem.setOnAction(actionEvent -> System.exit(0));
        mediaOpenFileMenuItem.setOnAction(actionEvent -> application.openFile());

        playbackJumpForwardMenuItem.setOnAction(actionEvent -> mediaPlayer.controls().skipTime(10000));
        playbackJumpBackwardMenuItem.setOnAction(actionEvent -> mediaPlayer.controls().skipTime(-10000));
        playbackPlayMenuItem.setOnAction(actionEvent -> mediaPlayer.controls().play());
        playbackPauseMenuItem.setOnAction(actionEvent -> mediaPlayer.controls().setPause(true));
        playbackStopMenuItem.setOnAction(actionEvent -> mediaPlayer.controls().stop());

        audioMuteMenuItem.setOnAction(actionEvent -> mediaPlayer.audio().setMute(audioMuteMenuItem.isSelected()));

        toolsVideoAdjustmentMenuItem.setOnAction(actionEvent -> application.adjustVideo(toolsVideoAdjustmentMenuItem.isSelected()));

        viewAlwaysOnTopMenuItem.setOnAction(actionEvent -> application.toggleAlwaysOnTop());
        viewMinimalInterfaceMenuItem.setOnAction(actionEvent -> application.toggleMinimalInterface(!viewMinimalInterfaceMenuItem.isSelected()));
        viewFullScreenMenuItem.setOnAction(actionEvent -> application.toggleFullScreen());
        viewStatsOverlayMenuItem.setOnAction(actionEvent -> application.toggleStatsOverlay(viewStatsOverlayMenuItem.isSelected()));
        viewAnimationOverlayMenuItem.setOnAction(actionEvent -> application.toggleAnimationOverlay(viewAnimationOverlayMenuItem.isSelected()));

        helpAboutMenuItem.setOnAction(actionEvent -> application.showAbout());

        return menuBar;
    }


}
