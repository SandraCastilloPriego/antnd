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
package ND.modules.reactionOP.showAllReactionList;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class ShowReactionTask extends AbstractTask {

        private final SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private final JInternalFrame frame;
        private final JScrollPane panel;
        private final JTextArea tf;
        private final StringBuffer info;

        public ShowReactionTask(SimpleBasicDataset dataset) {
                networkDS = dataset;
                this.frame = new JInternalFrame("Result", true, true, true, true);
                this.tf = new JTextArea();
                this.panel = new JScrollPane(this.tf);

                this.info = new StringBuffer();
        }

        @Override
        public String getTaskDescription() {
                return "Showing all reactions... ";
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

                        info.append("The model contains ").append(m.getNumReactions()).append(" reactions:\n");
                        info.append("----------------------------------- \n");

                        this.showReactions(m.getListOfReactions());

                        for (Reaction r : m.getListOfReactions()) {
                                info.append(r.getId()).append(" - ").append(r.getName()).append("\n");
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

        private void showReactions(List<Reaction> possibleReactions) {
                float count = 0;
                for (Reaction r : possibleReactions) {

                        KineticLaw law = r.getKineticLaw();
                        if (law != null) {
                                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                                info.append(r.getId()).append(" - ").append(r.getName()).append(" lb: ").append(lbound.getValue()).append(" up: ").append(ubound.getValue()).append(":\n");
                        } else {
                                info.append(r.getId()).append(":\n");
                        }
                        info.append("Reactants: \n");
                        for (SpeciesReference sr : r.getListOfReactants()) {
                                Species sp = sr.getSpeciesInstance();
                                info.append(sr.getStoichiometry()).append(" ").append(sp.getId()).append(" - ").append(sp.getName()).append("\n");
                        }
                        info.append("Products: \n");
                        for (SpeciesReference sr : r.getListOfProducts()) {
                                Species sp = sr.getSpeciesInstance();
                                info.append(sr.getStoichiometry()).append(" ").append(sp.getId()).append(" - ").append(sp.getName()).append(" \n");
                        }
                        info.append("----------------------------------- \n");
                        this.finishedPercentage = count / possibleReactions.size();
                        count++;
                }
        }
}
