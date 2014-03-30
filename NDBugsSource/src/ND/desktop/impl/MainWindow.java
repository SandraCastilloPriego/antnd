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
package ND.desktop.impl;

import ND.data.Dataset;
import ND.desktop.Desktop;
import ND.desktop.impl.helpsystem.NDHelpSet;
import ND.main.NDCore;
import ND.modules.NDModule;
import ND.modules.NDModuleCategory;
import ND.parameters.ParameterSet;
import ND.taskcontrol.impl.TaskProgressWindow;
import ND.util.ExceptionUtils;
import ND.util.TextUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.help.HelpBroker;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is the main window of application
 *
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 * 
 */
public class MainWindow extends JFrame implements NDModule, Desktop,
        WindowListener {

        static final String aboutHelpID = "MM/desktop/help/AboutND.html";
        private JDesktopPane desktopPane;
        private JSplitPane split;
        private ItemSelector itemSelector;
        private TaskProgressWindow taskList;
        private String parametersFilePath;

        public TaskProgressWindow getTaskList() {
                return taskList;
        }
        private MainMenu menuBar;
        private Statusbar statusBar;

        public MainMenu getMainMenu() {
                return menuBar;
        }

        @Override
        public void addInternalFrame(JInternalFrame frame) {
                try {
                        desktopPane.add(frame);
                        frame.setVisible(true);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        public void setParameteresPath(String path) {
                this.parametersFilePath = path;
        }

        @Override
        public String getParameteresPath() {
                return this.parametersFilePath;
        }

        @Override
        public void loadParameterPathFromXML(Element xmlElement) {
                NodeList list = xmlElement.getElementsByTagName("parameter");
                for (int i = 0; i < list.getLength(); i++) {
                        Element nextElement = (Element) list.item(i);
                        String paramName = nextElement.getAttribute("name");
                        if ("Path".equals(paramName)) {
                                try {
                                        String fileString = xmlElement.getTextContent();
                                        if (fileString.length() == 0) {
                                                return;
                                        }
                                        this.parametersFilePath = new File(fileString).getPath();
                                } catch (Exception e) {
                                }
                        }
                }
        }

        @Override
        public void saveParameterPathToXML(Element xmlElement) {
                Document parentDocument = xmlElement.getOwnerDocument();
                Element paramElement = parentDocument.createElement("parameter");
                paramElement.setAttribute("name", "Path");               
                if (this.parametersFilePath == null) {
                        return;
                }
                paramElement.setTextContent(this.parametersFilePath);
                xmlElement.appendChild(paramElement);
        }

        /**
         * This method returns the desktop
         */
        @Override
        public JDesktopPane getDesktopPane() {
                return desktopPane;
        }

        /**
         * WindowListener interface implementation
         */
        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
                NDCore.exitND();
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        @Override
        public void setStatusBarText(String text) {
                setStatusBarText(text, Color.black);
        }

        /**
         */
        @Override
        public void displayMessage(String msg) {
                displayMessage("Message", msg, JOptionPane.INFORMATION_MESSAGE);
        }

        /**
         */
        @Override
        public void displayMessage(String title, String msg) {
                displayMessage(title, msg, JOptionPane.INFORMATION_MESSAGE);
        }

        @Override
        public void displayErrorMessage(String msg) {
                displayMessage("Error", msg);
        }

        @Override
        public void displayErrorMessage(String title, String msg) {
                displayMessage(title, msg, JOptionPane.ERROR_MESSAGE);
        }

        public void displayMessage(String title, String msg, int type) {
                String wrappedMsg = TextUtils.wrapText(msg, 80);
                JOptionPane.showMessageDialog(this, wrappedMsg, title, type);
        }

        public void addMenuItem(NDModuleCategory parentMenu, JMenuItem newItem) {
                menuBar.addMenuItem(parentMenu, newItem);
        }

        /**
         */
        public void initModule() {

                SwingParameters.initSwingParameters();

                try {
                        BufferedImage NDIcon = ImageIO.read(new File(
                                "icons/NDIcon.png"));
                        setIconImage(NDIcon);
                } catch (IOException e) {
                       // e.printStackTrace();
                }

                // Initialize item selector
                itemSelector = new ItemSelector(this);
                

                // Place objects on main window
                desktopPane = new JDesktopPane();

                split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, itemSelector,
                        desktopPane);

                desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

                desktopPane.setBorder(new EtchedBorder(EtchedBorder.RAISED));
                desktopPane.setBackground(Color.WHITE);
                Container c = getContentPane();
                c.setLayout(new BorderLayout());
                c.add(split, BorderLayout.CENTER);

                statusBar = new Statusbar();
                c.add(statusBar, BorderLayout.SOUTH);

                // Construct menu
                menuBar = new MainMenu();
                setJMenuBar(menuBar);

                // Initialize window listener for responding to user events
                addWindowListener(this);

                pack();

                // TODO: check screen size?
                setBounds(0, 0, 1000, 700);
                setLocationRelativeTo(null);

                // Application wants to control closing by itself
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                updateTitle();

                setTitle("AntND");
        }

        void updateTitle() {
                setTitle("AntND " + NDCore.getNDVersion());
        }

        @Override
        public JFrame getMainFrame() {
                return this;
        }

        @Override
        public JInternalFrame getSelectedFrame() {
                return desktopPane.getSelectedFrame();
        }

        @Override
        public JInternalFrame[] getInternalFrames() {
                return desktopPane.getAllFrames();
        }

        @Override
        public void setStatusBarText(String text, Color textColor) {
                statusBar.setStatusText(text, textColor);
        }

        @Override
        public ParameterSet getParameterSet() {
                // return parameters;
                return null;
        }

        public void setParameters(ParameterSet parameterValues) {
                // this.parameters = (DesktopParameters) parameterValues;
        }

        public ItemSelector getItemSelector() {
                return itemSelector;
        }

        @Override
        public Dataset[] getSelectedDataFiles() {
                return this.itemSelector.getSelectedDatasets();
        }

        /*public Vector[] getSelectedExperiments() {
        return this.itemSelector.getSelectedExperiments();
        }*/
        /**
         *
         * @param dataset
         */
        @Override
        public void AddNewFile(Dataset dataset) {
                this.itemSelector.addNewFile(dataset);
        }

        @Override
        public void removeData(Dataset file) {
                this.itemSelector.removeData(file);
        }

        @Override
        public void displayException(Exception e) {
                displayErrorMessage(ExceptionUtils.exceptionToString(e));
        }

        public void showAboutDialog() {
                NDHelpSet hs = NDCore.getHelpImpl().getHelpSet();
                if (hs == null) {
                        return;
                }

                HelpBroker hb = hs.createHelpBroker();
                hs.setHomeID(aboutHelpID);

                hb.setDisplayed(true);
        }
}
