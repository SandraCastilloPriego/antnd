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
package ND.modules.reactionOP.deadends;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.util.ArrayList;
import java.util.List;
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
public class DeadEndsTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private double finishedPercentage = 0.0f;

    public DeadEndsTask(SimpleBasicDataset dataset) {
        networkDS = dataset;
    }

    @Override
    public String getTaskDescription() {
        return "Searchin dead ends... ";
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
            List<String> deadEnd = new ArrayList<>();
            for(Species sp : m.getListOfSpecies()){
                boolean isInSource = false;
                boolean isInDestination = false;
                for(Reaction r : m.getListOfReactions()){
                    for(SpeciesReference sprf : r.getListOfReactants()){
                        if(sprf.getSpeciesInstance() == sp){
                            isInSource = true;
                        }
                    }
                    for(SpeciesReference sprf : r.getListOfProducts()){
                        if(sprf.getSpeciesInstance() == sp){
                            isInDestination = true;
                        }
                    }
                }
                if(!isInSource || !isInDestination){
                    deadEnd.add(sp.getId());
                    System.out.println(sp.getId());
                }
            }

            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }
}
