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
package ND.modules.analysis.CompareModels;

import ND.data.Dataset;
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class CompareTask extends AbstractTask {

    private final Dataset[] networkDS;
    private double finishedPercentage = 0.0f;
    private final JInternalFrame frame;
    private final JScrollPane panel;
    private final JTextArea tf;
    private final StringBuffer info;

    public CompareTask(Dataset[] datasets, SimpleParameterSet parameters) {
        networkDS = datasets;
        this.frame = new JInternalFrame("Result", true, true, true, true);
        this.tf = new JTextArea();
        this.panel = new JScrollPane(this.tf);
        this.info = new StringBuffer();
    }

    @Override
    public String getTaskDescription() {
        return "Comparing... ";
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
                NDCore.getDesktop().displayErrorMessage("You need to select two metabolic models.");
            }

            Model model1 = this.networkDS[0].getDocument().getModel();
            Model model2 = this.networkDS[1].getDocument().getModel();
            this.info.append("Reactions present in only in ").append(this.networkDS[0].getDatasetName()).append(":\n");
            for (Reaction r : model1.getListOfReactions()) {
                if (model2.getReaction(r.getId()) == null) {
                    showReactions(r, null);
                }
            }

            this.info.append("Reactions present in only in ").append(this.networkDS[1].getDatasetName()).append(":\n");
            for (Reaction r : model2.getListOfReactions()) {
                if (model1.getReaction(r.getId()) == null) {
                    showReactions(r, null);
                }
            }

            List<String> commonReactions = new ArrayList<>();
            for (Reaction r : model1.getListOfReactions()) {
                if (model2.getReaction(r.getId()) != null) {
                    commonReactions.add(r.getId());
                }
            }

            this.info.append("Common reactions with different fluxes in ").append(this.networkDS[1].getDatasetName()).append(":\n");
            for (String re : commonReactions) {
                showReactions2(model1.getReaction(re), model2.getReaction(re));
            }
            // this.networkDS.setInfo(info.toString());
            this.tf.setText(info.toString());
            frame.setSize(new Dimension(700, 500));
            frame.add(this.panel);
            NDCore.getDesktop().addInternalFrame(frame);
            finishedPercentage = 1.0f;
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }

    private void showReactions(Reaction reaction, Reaction reaction2) {
        DecimalFormat df = new DecimalFormat("#.####");
        KineticLaw law = reaction.getKineticLaw();
        if (law != null) {
            LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
            LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
            LocalParameter flux = law.getLocalParameter("FLUX_VALUE");
            info.append(reaction.getId()).append(" - ").append(reaction.getName()).append(" lb: ").append(lbound.getValue()).append(" up: ").append(ubound.getValue()).append("\n");
            if (reaction2 == null) {
                info.append("Flux: ").append(flux.getValue()).append("\n");
            } else {
                KineticLaw law2 = reaction2.getKineticLaw();
                if (law != null && law2 != null) {
                    LocalParameter flux2 = law2.getLocalParameter("FLUX_VALUE");
                    info.append("Flux1: ").append(df.format(flux.getValue())).append(" - Flux2: ").append(df.format(flux2.getValue())).append("\n");
                }
            }
        } else {
            info.append(reaction.getId()).append(":\n");
        }
        info.append("Reactants: \n");
        for (SpeciesReference sr : reaction.getListOfReactants()) {
            Species sp = sr.getSpeciesInstance();
            info.append(sr.getStoichiometry()).append(" ").append(sp.getId()).append(" - ").append(sp.getName()).append("\n");
        }
        info.append("Products: \n");
        for (SpeciesReference sr : reaction.getListOfProducts()) {
            Species sp = sr.getSpeciesInstance();
            info.append(sr.getStoichiometry()).append(" ").append(sp.getId()).append(" - ").append(sp.getName()).append(" \n");
        }
        info.append("----------------------------------- \n");

    }

    private void showReactions2(Reaction reaction, Reaction reaction2) {
        DecimalFormat df = new DecimalFormat("#.####");
       KineticLaw law = reaction.getKineticLaw();
        if (law != null) {
             LocalParameter flux = law.getLocalParameter("FLUX_VALUE");
            if (reaction2 == null) {
                info.append(reaction.getId()).append(flux.getValue()).append("\n");
            } else {
                KineticLaw law2 = reaction2.getKineticLaw();
                if (law != null && law2 != null) {
                    LocalParameter flux2 = law2.getLocalParameter("FLUX_VALUE");
                    if(!df.format(flux.getValue()).equals(df.format(flux2.getValue()))){
                        info.append(reaction.getId()).append(":  ").append(df.format(flux.getValue())).append(" - ").append(df.format(flux2.getValue())).append(" --> ").append(reaction.getName()).append("\n");
                    }
                }
            }
        } 
    }
}
