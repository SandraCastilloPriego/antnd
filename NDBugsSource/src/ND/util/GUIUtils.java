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
package ND.util;

import ND.data.Dataset;
import ND.main.NDCore;
import ND.util.Tables.DataTable;
import ND.util.Tables.DataTableModel;
import ND.util.Tables.implement.PushableTable;
import ND.util.components.FileUtils;
import ND.util.internalframe.DataInternalFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import org.sbml.jsbml.SBMLDocument;

/**
 * @author Taken from MZmine2 http://mzmine.sourceforge.net/
 *
 * GUI related utilities
 */
public class GUIUtils {

        /**
         * Registers a keyboard handler to a given component
         *
         * @param component Component to register the handler to
         * @param stroke Keystroke to activate the handler
         * @param listener ActionListener to handle the key press
         * @param actionCommand Action command string
         */
        public static void registerKeyHandler(JComponent component,
                KeyStroke stroke, final ActionListener listener,
                final String actionCommand) {
                component.getInputMap().put(stroke, actionCommand);
                component.getActionMap().put(actionCommand, new AbstractAction() {

                        @Override
                        public void actionPerformed(ActionEvent event) {
                                ActionEvent newEvent = new ActionEvent(event.getSource(),
                                        ActionEvent.ACTION_PERFORMED, actionCommand);
                                listener.actionPerformed(newEvent);
                        }
                });
        }

        /**
         * Add a new menu item to a given menu
         *
         * @param menu Menu to add the item to
         * @param text Menu item text
         * @param listener Menu item's ActionListener or null
         * @return Created menu item
         */
        public static JMenuItem addMenuItem(Container menu, String text,
                ActionListener listener) {
                return addMenuItem(menu, text, listener, null, 0, false);
        }

        /**
         * Add a new menu item to a given menu
         *
         * @param menu Menu to add the item to
         * @param text Menu item text
         * @param listener Menu item's ActionListener or null
         * @param actionCommand Menu item's action command or null
         * @return Created menu item
         */
        public static JMenuItem addMenuItem(Container menu, String text,
                ActionListener listener, String actionCommand) {
                return addMenuItem(menu, text, listener, actionCommand, 0, false);
        }

        /**
         * Add a new menu item to a given menu
         *
         * @param menu Menu to add the item to
         * @param text Menu item text
         * @param listener Menu item's ActionListener or null
         * @param mnemonic Menu item's mnemonic (virtual key code) or 0
         * @return Created menu item
         */
        public static JMenuItem addMenuItem(Container menu, String text,
                ActionListener listener, int mnemonic) {
                return addMenuItem(menu, text, listener, null, mnemonic, false);
        }

        /**
         * Add a new menu item to a given menu
         *
         * @param menu Menu to add the item to
         * @param text Menu item text
         * @param listener Menu item's ActionListener or null
         * @param mnemonic Menu item's mnemonic (virtual key code) or 0
         * @param setAccelerator Indicates whether to set key accelerator to
         * CTRL + the key specified by mnemonic parameter
         * @return Created menu item
         */
        public static JMenuItem addMenuItem(Container menu, String text,
                ActionListener listener, int mnemonic, boolean setAccelerator) {
                return addMenuItem(menu, text, listener, null, mnemonic, setAccelerator);
        }

        /**
         * Add a new menu item to a given menu
         *
         * @param menu Menu to add the item to
         * @param text Menu item text
         * @param listener Menu item's ActionListener or null
         * @param actionCommand Menu item's action command or null
         * @param mnemonic Menu item's mnemonic (virtual key code) or 0
         * @param setAccelerator Indicates whether to set key accelerator to
         * CTRL + the key specified by mnemonic parameter
         * @return Created menu item
         */
        public static JMenuItem addMenuItem(Container menu, String text,
                ActionListener listener, String actionCommand, int mnemonic,
                boolean setAccelerator) {
                JMenuItem item = new JMenuItem(text);
                if (listener != null) {
                        item.addActionListener(listener);
                }
                if (actionCommand != null) {
                        item.setActionCommand(actionCommand);
                }
                if (mnemonic > 0) {
                        item.setMnemonic(mnemonic);
                }
                if (setAccelerator) {
                        item.setAccelerator(KeyStroke.getKeyStroke(mnemonic,
                                ActionEvent.CTRL_MASK));
                }
                if (menu != null) {
                        menu.add(item);
                }
                return item;
        }

        /**
         * Add a new button to a given component
         *
         * @param component Component to add the button to
         * @param text Button's text or null
         * @param icon Button's icon or null
         * @param listener Button's ActionListener or null
         * @return Created button
         */
        public static JButton addButton(Container component, String text,
                Icon icon, ActionListener listener) {
                return addButton(component, text, icon, listener, null, 0, null);
        }

        /**
         * Add a new button to a given component
         *
         * @param component Component to add the button to
         * @param text Button's text or null
         * @param icon Button's icon or null
         * @param listener Button's ActionListener or null
         * @param actionCommand Button's action command or null
         * @return Created button
         */
        public static JButton addButton(Container component, String text,
                Icon icon, ActionListener listener, String actionCommand) {
                return addButton(component, text, icon, listener, actionCommand, 0,
                        null);
        }

        /**
         * Add a new button to a given component
         *
         * @param component Component to add the button to
         * @param text Button's text or null
         * @param icon Button's icon or null
         * @param listener Button's ActionListener or null
         * @param actionCommand Button's action command or null
         * @param toolTip Button's tooltip text or null
         * @return Created button
         */
        public static JButton addButton(Container component, String text,
                Icon icon, ActionListener listener, String actionCommand,
                String toolTip) {
                return addButton(component, text, icon, listener, actionCommand, 0,
                        toolTip);
        }

        /**
         * Add a new button to a given component
         *
         * @param component Component to add the button to
         * @param text Button's text or null
         * @param icon Button's icon or null
         * @param listener Button's ActionListener or null
         * @param actionCommand Button's action command or null
         * @param mnemonic Button's mnemonic (virtual key code) or 0
         * @param toolTip Button's tooltip text or null
         * @return Created button
         */
        public static JButton addButton(Container component, String text,
                Icon icon, ActionListener listener, String actionCommand,
                int mnemonic, String toolTip) {
                JButton button = new JButton(text, icon);
                if (listener != null) {
                        button.addActionListener(listener);
                }
                if (actionCommand != null) {
                        button.setActionCommand(actionCommand);
                }
                if (mnemonic > 0) {
                        button.setMnemonic(mnemonic);
                }
                if (toolTip != null) {
                        button.setToolTipText(toolTip);
                }
                if (component != null) {
                        component.add(button);
                }
                return button;
        }

        /**
         * Add a new button to a JPanel and then add the panel to a given
         * component
         *
         * @param component Component to add the button to
         * @param text Button's text or null
         * @param icon Button's icon or null
         * @param listener Button's ActionListener or null
         * @return Created button
         */
        public static JButton addButtonInPanel(Container component, String text,
                ActionListener listener) {
                return addButtonInPanel(component, text, listener, null);
        }

        /**
         * Add a new button to a JPanel and then add the panel to a given
         * component
         *
         * @param component Component to add the button to
         * @param text Button's text or null
         * @param icon Button's icon or null
         * @param listener Button's ActionListener or null
         * @param actionCommand Button's action command or null
         * @return Created button
         */
        public static JButton addButtonInPanel(Container component, String text,
                ActionListener listener, String actionCommand) {
                JPanel panel = new JPanel();
                JButton button = new JButton(text);
                if (listener != null) {
                        button.addActionListener(listener);
                }
                if (actionCommand != null) {
                        button.setActionCommand(actionCommand);
                }
                panel.add(button);
                if (component != null) {
                        component.add(panel);
                }
                return button;
        }

        /**
         * Add a new label to a given component
         *
         * @param component Component to add the label to
         * @param text Label's text
         * @return Created label
         */
        public static JLabel addLabel(Container component, String text) {
                return addLabel(component, text, null, JLabel.LEFT, null);
        }

        /**
         * Add a new label to a given component
         *
         * @param component Component to add the label to
         * @param text Label's text
         * @param horizontalAlignment Label's horizontal alignment (e.g.
         * JLabel.LEFT)
         * @return Created label
         */
        public static JLabel addLabel(Container component, String text,
                int horizontalAlignment) {
                return addLabel(component, text, null, horizontalAlignment, null);
        }

        /**
         * Add a new label to a given component
         *
         * @param component Component to add the label to
         * @param text Label's text
         * @param horizontalAlignment Label's horizontal alignment (e.g.
         * JLabel.LEFT)
         * @param font Label's font
         * @return Created label
         */
        public static JLabel addLabel(Container component, String text,
                int horizontalAlignment, Font font) {
                return addLabel(component, text, null, horizontalAlignment, font);
        }

        /**
         * Add a new label to a given component
         *
         * @param component Component to add the label to
         * @param text Label's text
         * @param icon Label's icon
         * @param horizontalAlignment Label's horizontal alignment (e.g.
         * JLabel.LEFT)
         * @param font Label's font
         * @return Created label
         */
        public static JLabel addLabel(Container component, String text, Icon icon,
                int horizontalAlignment, Font font) {
                JLabel label = new JLabel(text, icon, horizontalAlignment);
                if (component != null) {
                        component.add(label);
                }
                if (font != null) {
                        label.setFont(font);
                }
                return label;
        }

        /**
         * Add a new label to a JPanel and then add the panel to a given
         * component
         *
         * @param component Component to add the label to
         * @param text Label's text
         * @return Created label
         */
        public static JLabel addLabelInPanel(Container component, String text) {
                JPanel panel = new JPanel();
                component.add(panel);
                return addLabel(panel, text);
        }

        /**
         * Add a separator to a given component
         *
         * @param component Component to add the separator to
         * @return Created separator
         */
        public static JSeparator addSeparator(Container component) {
                return addSeparator(component, 0);
        }

        /**
         * Add a separator to a given component
         *
         * @param component Component to add the separator to
         * @param margin Margin around the separator
         * @return Created separator
         */
        public static JSeparator addSeparator(Container component, int margin) {
                JSeparator separator = new JSeparator();
                if (margin > 0) {
                        addMargin(separator, margin);
                }
                if (component != null) {
                        component.add(separator);
                }
                return separator;
        }

        /**
         * Add a margin to a given component
         *
         * @param component Component to add the margin to
         * @param margin Margin size
         * @return Created border
         */
        public static Border addMargin(JComponent component, int margin) {
                Border marginBorder = BorderFactory.createEmptyBorder(margin, margin,
                        margin, margin);
                component.setBorder(marginBorder);
                return marginBorder;
        }

        /**
         * Add a margin and border to a given component
         *
         * @param component Component to add the margin to
         * @param margin Margin size
         * @return Created border
         */
        public static Border addMarginAndBorder(JComponent component, int margin) {
                Border marginBorder = BorderFactory.createEmptyBorder(margin, margin,
                        margin, margin);
                Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
                Border compoundBorder = BorderFactory.createCompoundBorder(
                        etchedBorder, marginBorder);
                component.setBorder(compoundBorder);
                return compoundBorder;
        }

        /**
         * This method creates a JPanel which layouts given components in a
         * table of given rows/columns. Last column is considered main, so its
         * components will be expanded to fill extra space.
         */
        public static JPanel makeTablePanel(int rows, int cols,
                JComponent components[]) {
                return makeTablePanel(rows, cols, cols - 1, components);
        }

        /**
         * This method creates a JPanel which layouts given components in a
         * table of given rows/columns. Specified column is considered main, so
         * its components will be expanded to fill extra space.
         */
        public static JPanel makeTablePanel(int rows, int cols, int mainColumn,
                JComponent components[]) {

                GridBagLayout layout = new GridBagLayout();
                JPanel panel = new JPanel(layout);
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.insets = new Insets(0, 0, 5, 5);

                for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < cols; col++) {

                                constraints.gridx = col;
                                constraints.gridy = row;
                                constraints.weightx = (col == mainColumn) ? 1 : 0;
                                JComponent component = components[row * cols + col];
                                panel.add(component);
                                layout.setConstraints(component, constraints);
                        }
                }

                /*
                 * Create one extra invisible component, which is vertically
                 * expandable. This will align the whole table to the top.
                 */
                JComponent space = (JComponent) Box.createGlue();
                constraints.gridx = 0;
                constraints.gridy = rows;
                constraints.weightx = 0;
                constraints.weighty = 1;
                panel.add(space);
                layout.setConstraints(space, constraints);

                return panel;
        }

        /**
         * Calls the function generateHelpID(Class<?> cl) to generate the path
         * of the help file depending on the name of the class which calls it.
         *
         * @param obj Class which calls the function
         * @return Path of the help file corresponding to the class
         */
        public static String generateHelpID(Object obj) {
                return generateHelpID(obj.getClass());
        }

        /**
         * Returns the path of the help file depending on the module class.
         *
         * @param cl class
         * @return Path of the help file
         */
        public static String generateHelpID(Class<?> cl) {
                String helpID = cl.getPackage().getName().replace('.', '/') + "/help/" + cl.getSimpleName() + ".html";
                return helpID;
        }

        /**
         * Creates the table of the data set and puts it into an internal frame
         * to show it in desktop.
         *
         * @param dataset data set which will be shown
         */
        public static void showNewTable(Dataset dataset, boolean addDataset) {
                DataTableModel model = FileUtils.getTableModel(dataset);
                DataTableModel modelMet = FileUtils.getTableModelMet(dataset);
                DataTable table = new PushableTable(model);
                DataTable metTable = new PushableTable(modelMet);
                table.formatNumbers(dataset.getType());
                
              
                DataInternalFrame frame = new DataInternalFrame(dataset.getDatasetName(), table.getTable(), metTable.getTable(),new Dimension(450, 450));
               
                NDCore.getDesktop().addInternalFrame(frame);
                if (addDataset) {
                        NDCore.getDesktop().AddNewFile(dataset);
                }
                frame.setVisible(true);
        }
}
