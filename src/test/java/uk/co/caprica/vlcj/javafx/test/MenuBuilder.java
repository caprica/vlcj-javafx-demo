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

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;

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
    static MenuBar createMenu() {
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

        MenuItem playbackPlayMenuItem = new MenuItem("_Play");
        playbackPlayMenuItem.setMnemonicParsing(true);
        playbackMenu.getItems().add(playbackPlayMenuItem);

        MenuItem playbackStopMenuItem = new MenuItem("_Stop");
        playbackStopMenuItem.setMnemonicParsing(true);
        playbackMenu.getItems().add(playbackStopMenuItem);

        menuBar.getMenus().add(playbackMenu);

        Menu viewMenu = new Menu("V_iew");
        viewMenu.setMnemonicParsing(true);

        MenuItem viewFullScreenMenuItem = new MenuItem("_Fullscreen Interface");
        viewFullScreenMenuItem.setMnemonicParsing(true);
        viewFullScreenMenuItem.setAccelerator(KeyCombination.keyCombination("F11"));
        viewMenu.getItems().add(viewFullScreenMenuItem);

        menuBar.getMenus().add(viewMenu);

        Menu helpMenu = new Menu("_Help");
        helpMenu.setMnemonicParsing(true);

        MenuItem helpAboutMenuItem = new MenuItem("_About");
        helpAboutMenuItem.setAccelerator(KeyCombination.keyCombination("Shift+F1"));
        helpAboutMenuItem.setMnemonicParsing(true);
        helpMenu.getItems().add(helpAboutMenuItem);

        menuBar.getMenus().add(helpMenu);

        return menuBar;
    }


}
