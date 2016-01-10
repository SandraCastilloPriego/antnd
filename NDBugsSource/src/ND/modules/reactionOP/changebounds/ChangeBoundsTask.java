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
package ND.modules.reactionOP.changebounds;

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

/**
 *
 * @author scsandra
 */
public class ChangeBoundsTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final String reactionName;
    private final double lb, ub;
    private double finishedPercentage = 0.0f;

    public ChangeBoundsTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        networkDS = dataset;
        this.reactionName = parameters.getParameter(ChangeBoundsParameters.reactionName).getValue();
        this.lb = parameters.getParameter(ChangeBoundsParameters.lb).getValue();
        this.ub = parameters.getParameter(ChangeBoundsParameters.ub).getValue();
    }

    @Override
    public String getTaskDescription() {
        return "Changing bounds... ";
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

            Reaction r = m.getReaction(reactionName);
            if (r != null) {
                KineticLaw law = new KineticLaw();
                LocalParameter lbound = new LocalParameter("LOWER_BOUND");
                lbound.setValue(Double.valueOf(lb));
                law.addLocalParameter(lbound);
                LocalParameter ubound = new LocalParameter("UPPER_BOUND");
                ubound.setValue(Double.valueOf(ub));
                law.addLocalParameter(ubound);
                r.setKineticLaw(law);
                this.networkDS.setDocument(doc);
                this.networkDS.addInfo("Changing bounds in reaction: " + this.reactionName + "\nlb: " + lb + "\n up: " + ub + "\n--------------------------");
            } else {
                NDCore.getDesktop().displayMessage("The reaction " + reactionName + " doesn't exist in this model.");
            }

            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }
}
