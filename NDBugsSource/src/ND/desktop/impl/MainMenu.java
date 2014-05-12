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

import ND.main.NDCore;
import ND.modules.NDModuleCategory;
import static ND.modules.NDModuleCategory.SIMULATION;
import ND.modules.NDProcessingModule;
import ND.parameters.ParameterSet;
import ND.util.dialogs.ExitCode;
import ca.guydavis.swing.desktop.CascadingWindowPositioner;
import ca.guydavis.swing.desktop.JWindowsMenu;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.*;

/**
 * @author Taken from MZmine2 http://mzmine.sourceforge.net/
 */
public final class MainMenu extends JMenuBar implements ActionListener {

        private final JMenu fileMenu, configurationMenu, simulationMenu, optimizationMenu, analysisMenu, reactionMenu, helpMenu;
        private final JWindowsMenu windowsMenu;
        private final JMenuItem showAbout;
        private final Map<JMenuItem, NDProcessingModule> moduleMenuItems = new HashMap<>();

        MainMenu() {
                this.setBackground(Color.WHITE);

                fileMenu = new JMenu("File");
                fileMenu.setMnemonic(KeyEvent.VK_F);
                fileMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
                fileMenu.setIcon(new ImageIcon("icons/file.jpg"));
                add(fileMenu);

                configurationMenu = new JMenu("Configuration");
                configurationMenu.setMnemonic(KeyEvent.VK_C);
                configurationMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
                configurationMenu.setIcon(new ImageIcon("icons/setting.png"));
                add(configurationMenu);

                simulationMenu = new JMenu("Simulation");
                simulationMenu.setMnemonic(KeyEvent.VK_S);
                simulationMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
                simulationMenu.setIcon(new ImageIcon("icons/simulationMenu.png"));
                add(simulationMenu);

                reactionMenu = new JMenu("Model");
                reactionMenu.setMnemonic(KeyEvent.VK_R);
                reactionMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
                reactionMenu.setIcon(new ImageIcon("icons/model.png"));
                add(reactionMenu);

                optimizationMenu = new JMenu("Optimization");
                optimizationMenu.setMnemonic(KeyEvent.VK_R);
                optimizationMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
                optimizationMenu.setIcon(new ImageIcon("icons/optimizer.png"));
                add(optimizationMenu);

                analysisMenu = new JMenu("Analysis");
                analysisMenu.setMnemonic(KeyEvent.VK_R);
                analysisMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
                analysisMenu.setIcon(new ImageIcon("icons/optimizer.png"));
                add(analysisMenu);

                JDesktopPane mainDesktopPane = ((MainWindow) NDCore.getDesktop()).getDesktopPane();
                windowsMenu = new JWindowsMenu(mainDesktopPane);
                CascadingWindowPositioner positioner = new CascadingWindowPositioner(
                        mainDesktopPane);
                windowsMenu.setWindowPositioner(positioner);
                windowsMenu.setMnemonic(KeyEvent.VK_W);
                windowsMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
                windowsMenu.setIcon(new ImageIcon("icons/window.png"));
                this.add(windowsMenu);


                /*
                 * Help menu
                 */
                helpMenu = new JMenu("Help");
                helpMenu.setMnemonic(KeyEvent.VK_H);
                helpMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
                helpMenu.setIcon(new ImageIcon("icons/helpMenu.png"));
                this.add(helpMenu);

                showAbout = new JMenuItem("About ND ...");
                showAbout.addActionListener(this);
                showAbout.setIcon(new ImageIcon("icons/help.png"));
                addMenuItem(NDModuleCategory.HELPSYSTEM, showAbout);

        }

        public synchronized void addMenuItem(NDModuleCategory parentMenu,
                JMenuItem newItem) {
                switch (parentMenu) {
                        case FILE:
                                fileMenu.add(newItem);
                                break;
                        case CONFIGURATION:
                                configurationMenu.add(newItem);
                                break;
                        case SIMULATION:
                                simulationMenu.add(newItem);
                                break;
                        case REACTION:
                                reactionMenu.add(newItem);
                                break;
                        case OPTIMIZATION:
                                optimizationMenu.add(newItem);
                                break;
                        case ANALYSIS:
                                analysisMenu.add(newItem);
                                break;
                        case HELPSYSTEM:
                                helpMenu.add(newItem);
                                break;
                }
        }

        public void addMenuSeparator(NDModuleCategory parentMenu) {
                switch (parentMenu) {
                        case FILE:
                                fileMenu.addSeparator();
                                break;
                        case CONFIGURATION:
                                configurationMenu.addSeparator();
                                break;
                        case SIMULATION:
                                simulationMenu.addSeparator();
                                break;
                        case REACTION:
                                reactionMenu.addSeparator();
                                break;
                        case OPTIMIZATION:
                                optimizationMenu.addSeparator();
                                break;
                        case ANALYSIS:
                                analysisMenu.addSeparator();
                                break;
                        case HELPSYSTEM:
                                helpMenu.addSeparator();
                                break;

                }
        }

        /**
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();

                NDProcessingModule module = moduleMenuItems.get(src);
                if (module != null) {
                        ParameterSet moduleParameters = module.getParameterSet();

                        if (moduleParameters == null) {
                                module.runModule(null);
                                return;
                        }

                        boolean allParametersOK = true;
                        LinkedList<String> errorMessages = new LinkedList<String>();

                        if (!allParametersOK) {
                                StringBuilder message = new StringBuilder();
                                for (String m : errorMessages) {
                                        message.append(m);
                                        message.append("\n");
                                }
                                NDCore.getDesktop().displayMessage(message.toString());
                                return;
                        }

                        ExitCode exitCode = moduleParameters.showSetupDialog();
                        if (exitCode == ExitCode.OK) {
                                ParameterSet parametersCopy = moduleParameters.clone();
                                module.runModule(parametersCopy);
                        }
                        return;
                }

                if (src == showAbout) {
                        MainWindow mainWindow = (MainWindow) NDCore.getDesktop();
                        mainWindow.showAboutDialog();
                }
        }

        public void addMenuItemForModule(NDProcessingModule module) {

                NDModuleCategory parentMenu = module.getModuleCategory();
                String menuItemText = module.toString();
                String menuItemIcon = module.getIcon();
                boolean separator = module.setSeparator();

                JMenuItem newItem = new JMenuItem(menuItemText);
                if (menuItemIcon != null) {
                        newItem.setIcon(new ImageIcon(menuItemIcon));
                }
                newItem.addActionListener(this);

                moduleMenuItems.put(newItem, module);

                addMenuItem(parentMenu, newItem);

                if (separator) {
                        this.addMenuSeparator(parentMenu);
                }

        }
}
