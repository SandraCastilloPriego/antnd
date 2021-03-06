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
import ND.modules.analysis.Report.ReportFBATask;
import ND.modules.analysis.VisualizeCofactors.VisualizeCofactorsTask;
import ND.modules.reactionOP.CombineModels.CombineModelsModule;
import ND.util.GUIUtils;
import ND.util.components.DragOrderedJList;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.stream.XMLStreamException;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;

/**
 * This class implements a selector of data sets
 *
 * @author Taken from MZmine2 http://mzmine.sourceforge.net/
 */
public class ItemSelector extends JPanel implements ActionListener,
    MouseListener, ListSelectionListener {

    public static final String DATA_FILES_LABEL = "SBML Files";
    private final DragOrderedJList DatasetFiles;
    private final List<Dataset> DatasetFilesModel = new ArrayList<>();
    private final DefaultListModel DatasetNamesModel = new DefaultListModel();
    private final JPopupMenu dataFilePopupMenu;
    private int copies = 0;

    /**
     * Constructor
     *
     * @param desktop
     */
    public ItemSelector(Desktop desktop) {

        // Create panel for raw data objects
        JPanel rawDataPanel = new JPanel();
        rawDataPanel.setBackground(new Color(119, 186, 155));
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

        GUIUtils.addMenuItem(dataFilePopupMenu, "Change Name", this, "CHANGE_NAME");
        GUIUtils.addMenuItem(dataFilePopupMenu, "Set Parent", this, "CHANGE_PARENT");
        GUIUtils.addSeparator(dataFilePopupMenu);
        GUIUtils.addMenuItem(dataFilePopupMenu, "Show Reactions/Metabolites tables", this, "SHOW_DATASET");
        GUIUtils.addMenuItem(dataFilePopupMenu, "Show model information", this, "SHOW_INFO");
        GUIUtils.addMenuItem(dataFilePopupMenu, "Show Report", this, "REPORT");
        // GUIUtils.addMenuItem(dataFilePopupMenu, "Visualize Transport", this, "VISUALIZETRANSPORT");
        GUIUtils.addMenuItem(dataFilePopupMenu, "Visualize", this, "VISUALIZE");
        GUIUtils.addSeparator(dataFilePopupMenu);
        //       GUIUtils.addMenuItem(dataFilePopupMenu, "Save graph", this, "SAVEGRAPH");
        GUIUtils.addMenuItem(dataFilePopupMenu, "Combine Models", this, "COMBINE");
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
        if (command.equals("REPORT")) {
            showReport();
        }

        if (command.equals("CHANGE_NAME")) {
            changeName();
        }

        if (command.equals("CHANGE_PARENT")) {
            setParent();
        }
//        if (command.equals("SAVEGRAPH")) {
//            saveGraph();
//        }

        if (command.equals("SAVE_DATASET")) {
            saveData();
        }
        if (command.equals("SHOW_INFO")) {
            writeInfo();
        }
        if (command.equals("VISUALIZE")) {
            visualize();
        }
        if (command.equals("VISUALIZETRANSPORT")) {
            visualizeTransport();
        }
        if (command.equals("COMBINE")) {
            combine();
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

        List o = DatasetFiles.getSelectedValuesList();

        Dataset res[] = new Dataset[o.size()];

        for (int i = 0; i < o.size(); i++) {
            for (Dataset dataset : DatasetFilesModel) {
                if (dataset.getDatasetName().compareTo((String) o.get(i)) == 0) {
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
                String name = dataset.getDatasetName();
                name = name.split("\\.")[0];
                dataset.setDatasetName(name + "_" + ++copies + ".sbml");
            }
        }
        this.DatasetFilesModel.add(dataset);
        DatasetNamesModel.addElement(dataset.getDatasetName());
        this.DatasetFiles.revalidate();

    }

    private void saveData() {
        Dataset[] selectedFiles = getSelectedDatasets();

        for (final Dataset file : selectedFiles) {
            if (file != null) {

                System.out.println(file.getPath());
                final JInternalFrame frame = new JInternalFrame("Result", true, true, true, true);
                JPanel pn = new JPanel();
                JLabel label = new JLabel("File path: ");
                final JTextField field = new JTextField(file.getPath());
                JButton accept = new JButton("Accept");
                JButton cancel = new JButton("Cancel");
                pn.add(label);
                pn.add(field);
                pn.add(accept);
                pn.add(cancel);
                accept.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SBMLWriter writer = new SBMLWriter("AntND", "1.0");
                        try {
                            writer.write(file.getDocument(), field.getText());
                        } catch (XMLStreamException | FileNotFoundException | SBMLException ex) {
                            Logger.getLogger(ItemSelector.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        frame.doDefaultCloseAction();
                    }
                });

                cancel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frame.doDefaultCloseAction();
                    }
                });
                frame.add(pn);
                frame.setSize(new Dimension(600, 100));
                NDCore.getDesktop().addInternalFrame(frame);
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

            PrintPaths print = new PrintPaths(file.getDocument().getModel());
            try {

                System.out.println("Visualize");
                pn.add(print.printPathwayInFrame(file.getGraph()));

            } catch (NullPointerException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    private void saveGraph() {
        Dataset[] selectedFiles = getSelectedDatasets();

        for (Dataset file : selectedFiles) {
            try {
                PrintPaths print = new PrintPaths(file.getDocument().getModel());
                System.out.println("Save");
                VisualizationViewer<String, String> vv = print.printPathwayInFrame(file.getGraph());
                Properties p = new Properties();
                p.setProperty("PageSize", "A5");
                VectorGraphics g;
                try {
                    // g = new SVGGraphics2D(new File("Output.svg"), new Dimension(2400,1900));
                    g = new PDFGraphics2D(new File("Output.pdf"), new Dimension(2400, 1900));
                    g.setProperties(p);
                    g.startExport();
                    vv.print(g);
                    g.endExport();
                } catch (IOException ex) {

                }

            } catch (NullPointerException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    private void combine() {
        CombineModelsModule combine = new CombineModelsModule();
        combine.runModule(null);
    }

    public Dataset[] getAllDatasets() {
        return DatasetFilesModel.toArray(new Dataset[0]);
    }

    private void writeInfo() {
        final Dataset[] selectedFile = getSelectedDatasets();
        if (selectedFile != null) {
            final JInternalFrame frame = new JInternalFrame("Changes", true, true, true, true);
            JPanel pn = new JPanel();
            final JTextArea area = selectedFile[0].getInfo();

            JButton accept = new JButton("Accept");
            JButton cancel = new JButton("Cancel");
            JPanel buttonPanel = new JPanel();

            buttonPanel.add(accept);
            buttonPanel.add(cancel);
            buttonPanel.setBackground(Color.white);
            buttonPanel.setPreferredSize(new Dimension(700, 50));

            JScrollPane panel = new JScrollPane(area);
            panel.setPreferredSize(new Dimension(700, 400));

            pn.add(panel, BorderLayout.NORTH);
            pn.add(buttonPanel, BorderLayout.SOUTH);
            pn.setBackground(Color.white);
            frame.setSize(new Dimension(700, 500));
            frame.add(pn);
            NDCore.getDesktop().addInternalFrame(frame);

            /*  frame.addComponentListener(new ComponentAdapter() {               
             @Override
             public void componentResized(ComponentEvent ce) {
             String size = ce.paramString();
             size = size.substring(size.indexOf("("), size.indexOf(")"));
             size = size.split(" ")[1];
             String[] values = size.split("x");
                  
             frame.setPreferredSize(new Dimension(Integer.valueOf(values[0]), Integer.valueOf(values[1])));
             panel.setPreferredSize(new Dimension(Integer.valueOf(values[0]), Integer.valueOf(values[1])-100));
             System.out.println(ce.paramString() + " - "+ Integer.valueOf(values[0]) + " - " + Integer.valueOf(values[1]));
             frame.pack();
             }
               
             });
            
             */
            accept.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedFile[0].setInfo(area.getText());
                    frame.doDefaultCloseAction();
                }
            });

            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.doDefaultCloseAction();
                }
            });
        }

    }

    private void showReport() {
        Dataset[] selectedFiles = getSelectedDatasets();
        for (Dataset file : selectedFiles) {
            if (file != null && !file.isParent()) {
                /* ReportFBAParameters parameters = new ReportFBAParameters();
                 if (NDCore.getDesktop().getParameteresReport() != null) {
                 parameters.getParameter(ReportFBAParameters.fileName).setValue(NDCore.getDesktop().getParameteresReport());
                 }
                 ExitCode exit = parameters.showSetupDialog();*/
                // if (exit == ExitCode.OK) {
                ReportFBATask task = new ReportFBATask(file, null);
                task.run();
                //   NDCore.getDesktop().setParameteresReport(parameters.getParameter(ReportFBAParameters.fileName).getValue());
                //}
            }
        }
    }

    private void visualizeTransport() {
        Dataset[] selectedFiles = getSelectedDatasets();

        for (Dataset file : selectedFiles) {
            VisualizeCofactorsTask VC = new VisualizeCofactorsTask(file, null);
            VC.run();
        }
    }

    private void changeName() {
        final Dataset selectedFile = getSelectedDatasets()[0];
        final JInternalFrame frame = new JInternalFrame("Change Name of the Model");
        frame.setSize(new Dimension(500, 150));
        JPanel pn = new JPanel();
        final JLabel label = new JLabel("Introduce the new name:");

        final JTextField area = new JTextField();
        area.setText(selectedFile.getDatasetName());
        area.setPreferredSize(new Dimension(400, 30));
        JButton accept = new JButton("Accept");
        JButton cancel = new JButton("Cancel");
        JPanel buttonPanel = new JPanel();

        buttonPanel.add(accept);
        buttonPanel.add(cancel);
        buttonPanel.setBackground(Color.white);
        buttonPanel.setPreferredSize(new Dimension(700, 50));

        pn.add(label);
        pn.add(area);
        pn.add(buttonPanel);
        pn.setBackground(Color.white);
        frame.add(pn);
        NDCore.getDesktop().addInternalFrame(frame);

        accept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String oldName = selectedFile.getDatasetName();
                String newName = area.getText();

                boolean correct = true;
                for (Dataset data : DatasetFilesModel) {
                    if (data != selectedFile && data.getDatasetName().equals(newName)) {
                        correct = false;
                    }
                }
                System.out.println(newName);
                
                if(!newName.contains(".sbml") && !newName.contains(".xml")){
                    //correct = false;
                     //NDCore.getDesktop().displayErrorMessage("The name must end with \".sbml\" or \".xml\"");                    
                    newName = newName+".sbml";
                }

                if (correct) {
                    for(Dataset data : DatasetFilesModel){
                        if(data.getParent()!= null && data.getParent().equals(oldName)){
                            data.setParent(newName);
                        }
                    }
                   // int index = DatasetNamesModel.indexOf(newName);
                   // DatasetNamesModel.setElementAt(newName, index);
                    DatasetNamesModel.addElement(newName);
                    selectedFile.setDatasetName(newName);
                    DatasetFiles.updateUI();
                    DatasetNamesModel.removeElement(oldName);
                    selectedFile.addInfo("The name of the file has been changed from: " + oldName + " to: " + newName);
                     frame.doDefaultCloseAction();
                } else {
                   label.setText("This name is already used. Choose another one:");
                }

                //
               
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.doDefaultCloseAction();
            }
        });
    }

    private void setParent() {
        final Dataset selectedFile = getSelectedDatasets()[0];
        final JInternalFrame frame = new JInternalFrame("Choose the new Parent");
        frame.setSize(new Dimension(500, 150));
        JPanel pn = new JPanel();
        final JLabel label = new JLabel("Choose the new Parent:");

        final JComboBox area = new JComboBox();
        for (Dataset data : DatasetFilesModel) {
            if (data.getParent() == null) {
                area.addItem(data.getDatasetName());
            } else {
                System.out.println(data.getDatasetName() + " - " + data.getParent());
            }

        }

        area.setPreferredSize(new Dimension(400, 30));
        JButton accept = new JButton("Accept");
        JButton cancel = new JButton("Cancel");
        JPanel buttonPanel = new JPanel();

        buttonPanel.add(accept);
        buttonPanel.add(cancel);
        buttonPanel.setBackground(Color.white);
        buttonPanel.setPreferredSize(new Dimension(700, 50));

        pn.add(label);
        pn.add(area);
        pn.add(buttonPanel);
        pn.setBackground(Color.white);
        frame.add(pn);
        NDCore.getDesktop().addInternalFrame(frame);

        accept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String parent = (String) area.getSelectedItem();
                selectedFile.setParent(parent);

                frame.doDefaultCloseAction();
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.doDefaultCloseAction();
            }
        });
    }

}
