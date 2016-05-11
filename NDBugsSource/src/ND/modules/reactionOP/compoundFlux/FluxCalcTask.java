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
package ND.modules.reactionOP.compoundFlux;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;

/**
 *
 * @author scsandra
 */
public class FluxCalcTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final double finishedPercentage = 0.0f;
    private final String compound;

    public FluxCalcTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.compound = parameters.getParameter(FluxCalcParameters.compound).getValue();
    }

    @Override
    public String getTaskDescription() {
        return "Starting Fluxes visualization... ";
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
            double totalFlux = 0;
            Model m = this.networkDS.getDocument().getModel();
            for (Reaction r : m.getListOfReactions()) {
                if (r.hasProduct(m.getSpecies(this.compound))) {
                    
                    double flux = r.getKineticLaw().getLocalParameter("FLUX_VALUE").getValue();
                    if (flux > 0) {
                        System.out.println(r.getId() +"-->"+ flux);
                        totalFlux += Math.abs(flux);
                    }
                } else if (r.hasReactant(m.getSpecies(this.compound))) {
                    double flux = r.getKineticLaw().getLocalParameter("FLUX_VALUE").getValue();
                    if (flux < 0) {
                        System.out.println(r.getId() +"<--"+ flux);
                        totalFlux += Math.abs(flux);
                    }
                }
            }
            
            System.out.println(totalFlux);

            setStatus(TaskStatus.FINISHED);

        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

}
