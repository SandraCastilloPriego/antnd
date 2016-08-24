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
package ND.modules.reactionOP.addReaction;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
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
public class AddReactionTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final String reactionName;
    private String compounds, stoichiometry;
    private final double finishedPercentage = 0.0f;
    private final double lb, ub;

    public AddReactionTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        networkDS = dataset;
        this.reactionName = parameters.getParameter(AddReactionParameters.reactionName).getValue();
        this.compounds = parameters.getParameter(AddReactionParameters.compounds).getValue();
        this.stoichiometry = parameters.getParameter(AddReactionParameters.stoichiometry).getValue();
        this.lb = parameters.getParameter(AddReactionParameters.lb).getValue();
        this.ub = parameters.getParameter(AddReactionParameters.ub).getValue();
    }

    @Override
    public String getTaskDescription() {
        return "Adding reaction... ";
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

            this.compounds = this.compounds.replaceAll(" ", "");
            String[] sps = this.compounds.split(",");

            this.stoichiometry = this.stoichiometry.replaceAll(" ", "");
            String[] sto = this.stoichiometry.split(",");

            if (sps.length != sto.length) {
                setStatus(TaskStatus.ERROR);
                NDCore.getDesktop().displayErrorMessage("The stoichiometry defined doesn't correspond to number of compounds.");
            }
            Reaction r = new Reaction(this.reactionName);

            for (int i = 0; i < sps.length; i++) {
                String sp = sps[i];
                String stoi = sto[i];

                SpeciesReference spref = new SpeciesReference();
                spref.setStoichiometry(Math.abs(Double.valueOf(stoi)));
                if (m.containsSpecies(sp)) {
                    spref.setSpecies(m.getSpecies(sp));
                } else {
                    Species specie = new Species(sp);
                    m.addSpecies(specie);
                    spref.setSpecies(sp);
                }
                if (Double.valueOf(stoi) < 0) {
                    r.addReactant(spref);
                } else {
                    r.addProduct(spref);
                }
            }

            KineticLaw law = new KineticLaw();
            LocalParameter lboundP = new LocalParameter("LOWER_BOUND");
            lboundP.setValue(this.lb);
            law.addLocalParameter(lboundP);
            LocalParameter uboundP = new LocalParameter("UPPER_BOUND");
            uboundP.setValue(this.ub);
            law.addLocalParameter(uboundP);
            LocalParameter objectiveP = new LocalParameter("OBJECTIVE_COEFFICIENT");
            objectiveP.setValue(0);
            law.addLocalParameter(objectiveP);
            r.setKineticLaw(law);

            if (m.getReaction(this.reactionName) != null) {
                setStatus(TaskStatus.ERROR);
                NDCore.getDesktop().displayErrorMessage("The reaction " + this.reactionName + " already exists in the model.");
            } else {
                m.addReaction(r);
                String info = "Adding reaction: " + this.reactionName + " bounds:" + this.lb + " - " + this.ub + "\nCompounds: " + this.compounds + "\nStoichiometry: " + this.stoichiometry + "\n--------------------------";
                this.networkDS.addInfo(info);

            }
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }
}
