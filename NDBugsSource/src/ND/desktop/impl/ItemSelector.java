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
import ND.main.NDCore;
import ND.util.GUIUtils;
import ND.util.components.DragOrderedJList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;

/**
 * This class implements a selector of data sets
 *
 * @author Taken from MZmine2 http://mzmine.sourceforge.net/
 */
public class ItemSelector extends JPanel implements ActionListener,
        MouseListener, ListSelectionListener {

        public static final String DATA_FILES_LABEL = "Data set Files";
        private DragOrderedJList DatasetFiles;
        private List<Dataset> DatasetFilesModel = new ArrayList<>();
        private DefaultListModel DatasetNamesModel = new DefaultListModel();
        private JPopupMenu dataFilePopupMenu;
        private int copies = 0;

        /**
         * Constructor
         */
        public ItemSelector(Desktop desktop) {


                // Create panel for raw data objects
                JPanel rawDataPanel = new JPanel();
                rawDataPanel.setBackground(new Color(251, 161, 82));
                JLabel rawDataTitle = new JLabel(DATA_FILES_LABEL);

                DatasetFiles = new DragOrderedJList(DatasetNamesModel);
                DatasetFiles.setCellRenderer(new ItemSelectorListRenderer());
                DatasetFiles.addMouseListener(this);
                DatasetFiles.addListSelectionListener(this);
                JScrollPane rawDataScroll = new JScrollPane(DatasetFiles);
                rawDataPanel.setLayout(new BorderLayout());
                rawDataPanel.add(rawDataTitle, BorderLayout.NORTH);
                rawDataPanel.add(rawDataScroll, BorderLayout.CENTER);
                rawDataPanel.setMinimumSize(new Dimension(150, 10));



                // Add panels to a split and put split on the main panel
                setPreferredSize(new Dimension(200, 10));
                setLayout(new BorderLayout());
                add(rawDataPanel, BorderLayout.CENTER);

                dataFilePopupMenu = new JPopupMenu();
                GUIUtils.addMenuItem(dataFilePopupMenu, "Show Tree Model", this, "SHOW_DATASET");
                GUIUtils.addMenuItem(dataFilePopupMenu, "Show Changes", this, "SHOW_INFO");
                GUIUtils.addMenuItem(dataFilePopupMenu, "Visualize", this, "VISUALIZE");
                GUIUtils.addMenuItem(dataFilePopupMenu, "Save Model in a File", this, "SAVE_DATASET");
                GUIUtils.addMenuItem(dataFilePopupMenu, "Remove", this, "REMOVE_FILE");



        }

        void addSelectionListener(ListSelectionListener listener) {
                DatasetFiles.addListSelectionListener(listener);
        }

        // Implementation of action listener interface
        @Override
        public void actionPerformed(ActionEvent e) {
                Runtime.getRuntime().freeMemory();
                String command = e.getActionCommand();
                if (command.equals("REMOVE_FILE")) {
                        removeData();
                }

                if (command.equals("SHOW_DATASET")) {
                        showData();
                }

                if (command.equals("SAVE_DATASET")) {
                        saveData();
                }
                if (command.equals("SHOW_INFO")) {
                        showInfo();
                }
                if (command.equals("VISUALIZE")) {
                        visualize();
                }
        }

        private void showData() {
                Dataset[] selectedFiles = getSelectedDatasets();
                for (Dataset file : selectedFiles) {
                        if (file != null) {
                                GUIUtils.showNewTable(file, false);
                        }
                }
        }

        private void showInfo() {
                Dataset[] selectedFiles = getSelectedDatasets();
                for (Dataset file : selectedFiles) {
                        if (file != null) {
                                JInternalFrame frame = new JInternalFrame("Changes", true, true, true, true);
                                JScrollPane panel = new JScrollPane(file.getInfo());
                                frame.setSize(new Dimension(700, 500));
                                frame.add(panel);
                                NDCore.getDesktop().addInternalFrame(frame);
                        }
                }
        }

        private void removeData() {
                Dataset[] selectedFiles = getSelectedDatasets();

                for (Dataset file : selectedFiles) {
                        if (file != null) {
                                DatasetFilesModel.remove(file);
                                DatasetNamesModel.removeElement(file.getDatasetName());
                        }
                }
        }

        public void removeData(Dataset file) {
                if (file != null) {
                        DatasetFilesModel.remove(file);
                        DatasetNamesModel.removeElement(file.getDatasetName());
                }

        }

        /**
         * Returns selected raw data objects in an array
         */
        public Dataset[] getSelectedDatasets() {

                Object o[] = DatasetFiles.getSelectedValues();

                Dataset res[] = new Dataset[o.length];

                for (int i = 0; i < o.length; i++) {
                        for (Dataset dataset : DatasetFilesModel) {
                                if (dataset.getDatasetName().compareTo((String) o[i]) == 0) {
                                        res[i] = dataset;
                                }
                        }
                }

                return res;

        }

        @Override
        public void mouseClicked(MouseEvent e) {

                if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1)) {
                        showData();
                }

        }

        @Override
        public void mouseEntered(MouseEvent e) {
                // ignore
        }

        @Override
        public void mouseExited(MouseEvent e) {
                // ignore
        }

        @Override
        public void mousePressed(MouseEvent e) {

                if (e.isPopupTrigger()) {
                        if (e.getSource() == DatasetFiles) {
                                dataFilePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                }

        }

        @Override
        public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                        if (e.getSource() == DatasetFiles) {
                                dataFilePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                }
        }

        @Override
        public void valueChanged(ListSelectionEvent event) {

                Object src = event.getSource();

                // Update the highlighting of peak list list in case raw data list
                // selection has changed and vice versa.
                if (src == DatasetFiles) {
                        DatasetFiles.revalidate();
                }

        }

        public void addNewFile(Dataset dataset) {
                for (int i = 0; i < DatasetNamesModel.getSize(); i++) {
                        if (dataset.getDatasetName().matches(DatasetNamesModel.getElementAt(i).toString())) {
                                dataset.setDatasetName(dataset.getDatasetName() + "_" + ++copies);
                        }
                }
                this.DatasetFilesModel.add(dataset);
                DatasetNamesModel.addElement(dataset.getDatasetName());
                this.DatasetFiles.revalidate();

        }

        private void saveData() {
                Dataset[] selectedFiles = getSelectedDatasets();

                for (Dataset file : selectedFiles) {
                        if (file != null) {
                                try {
                                        System.out.println(file.getPath());
                                        SBMLWriter writer = new SBMLWriter("AntND", "1.0");
                                        writer.write(file.getDocument(), file.getPath().replace(".sbml", "") + "(copy).sbml");
                                } catch (XMLStreamException | FileNotFoundException | SBMLException ex) {
                                        Logger.getLogger(ItemSelector.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                }

        }

        private void visualize() {
                Dataset[] selectedFiles = getSelectedDatasets();

                for (Dataset file : selectedFiles) {
                        JInternalFrame frame = new JInternalFrame(file.getDatasetName(), true, true, true, true);
                        JPanel pn = new JPanel();
                        JScrollPane panel = new JScrollPane(pn);

                        frame.setSize(new Dimension(700, 500));
                        frame.add(panel);
                        NDCore.getDesktop().addInternalFrame(frame);

                        PrintPaths print = new PrintPaths(file.getSources(), file.getBiomassId(), file.getDocument().getModel());
                        try {
                                 pn.add(print.printPathwayInFrame(file.getGraph()));
                        } catch (NullPointerException ex) {
                                System.out.println(ex.toString());
                        }
                }
        }
}
