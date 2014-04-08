/*
 * Copyright 2007-2012 
 * This file is part of AntND.
 *
 * AntND is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * AntND is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * AntND; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.desktop;

import ND.data.Dataset;
import ND.modules.NDModule;
import java.awt.Color;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import org.w3c.dom.Element;

/**
 * This interface represents the application GUI
 * 
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
public interface Desktop extends NDModule {

        /**
         * Returns a reference to main application window
         *
         * @return Main window frame
         */
        public JFrame getMainFrame();

        /**
         * Adds a new internal frame (JInternalFrame) to the desktop pane
         *
         * @param frame Internal frame to add
         */
        public void addInternalFrame(JInternalFrame frame);

        /**
         * Returns all internal frames in the desktop pane
         *
         * @return Array of all internal frames
         */
        public JInternalFrame[] getInternalFrames();

        /**
         * Returns the currently selected frame or null if no frame is selected
         *
         * @return Selected frame
         */
        public JInternalFrame getSelectedFrame();

        /**
         * Displays a given text on the application status bar in black color
         *
         * @param text Text to show
         */
        public void setStatusBarText(String text);

        /**
         * Displays a given text on the application status bar in a given color
         *
         * @param text Text to show
         * @param textColor Text color
         */
        public void setStatusBarText(String text, Color textColor);

        /**
         * Displays a message box with a given text
         *
         * @param msg Text to show
         */
        public void displayMessage(String msg);

        /**
         * Displays a message box with a given text
         *
         * @param title Message box title
         * @param msg Text to show
         */
        public void displayMessage(String title, String msg);

        /**
         * Displays an error message box with a given text
         *
         * @param msg Text to show
         */
        public void displayErrorMessage(String msg);

        /**
         * Displays an error message box with a given text
         *
         * @param title Message box title
         * @param msg Text to show
         */
        public void displayErrorMessage(String title, String msg);

        /**
         * Displays an error message
         *
         */
        public void displayException(Exception e);

        /**
         * Returns array of currently selected raw data files in GUI
         *
         * @return Array of selected raw data files
         */
        public Dataset[] getSelectedDataFiles();
        
        public Dataset[] getAllDataFiles();

        public void AddNewFile(Dataset dataset);

        public void removeData(Dataset file);

        public JDesktopPane getDesktopPane();

        public void loadParameterPathFromXML(Element xmlElement);

        public void saveParameterPathToXML(Element xmlElement);

        public void setParameteresPath(String path);

        public String getParameteresPath();
}
