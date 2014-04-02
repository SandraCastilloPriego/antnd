/*
 * Copyright 2013-2014 VTT Biotechnology
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
package ND.modules.reactionOP.showAllCompoundList;

import ND.modules.reactionOP.showCompound.*;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;

/**
 *
 * @author scsandra
 */
public class ShowAllCompoundsTask extends AbstractTask {

        private SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JTextArea tf;
        private StringBuffer info;

        public ShowAllCompoundsTask(SimpleBasicDataset dataset) {
                networkDS = dataset;
                this.frame = new JInternalFrame("Result", true, true, true, true);
                this.tf = new JTextArea();
                this.panel = new JScrollPane(this.tf);

                this.info = new StringBuffer();
        }

        @Override
        public String getTaskDescription() {
                return "Showing compounds... ";
        }

        @Override
        public double getFinishedPercentage() {
                return finishedPercentage;
        }

        @Override
        public void cancel() {
                setStatus(TaskStatus.CANCELED);
        }

        @Override
        public void run() {
                try {
                        setStatus(TaskStatus.PROCESSING);
                        if (this.networkDS == null) {
                                setStatus(TaskStatus.ERROR);
                                NDCore.getDesktop().displayErrorMessage("You need to select a metabolic model.");
                        }

                        SBMLDocument doc = this.networkDS.getDocument();
                        Model m = doc.getModel();
                        info.append("There are ").append(m.getNumSpecies()).append(" species in the model:\n");
                        info.append("\n----------------------------------- \n");
                        this.showCompounds(m.getListOfSpecies(), m);

                        for (Species sp : m.getListOfSpecies()) {
                                info.append(sp.getId()).append(" - ").append(sp.getName()).append("\n");
                        }

                        this.tf.setText(info.toString());

                        frame.setSize(new Dimension(700, 500));
                        frame.add(this.panel);
                        NDCore.getDesktop().addInternalFrame(frame);


                        setStatus(TaskStatus.FINISHED);
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        private void showCompounds(List<Species> possibleReactions, Model m) {
                float count = 0;
                for (Species sp : possibleReactions) {
                        info.append(sp.getId()).append(" - ").append(sp.getName());
                        info.append("\nPresent in: ");
                        for (Reaction r : m.getListOfReactions()) {
                                if (r.hasReactant(sp) || r.hasProduct(sp)) {
                                        info.append(r.getId()).append(", ");
                                }
                        }
                        this.finishedPercentage = count / m.getNumSpecies();
                        count++;
                        info.append("\n----------------------------------- \n");
                }
        }
}
