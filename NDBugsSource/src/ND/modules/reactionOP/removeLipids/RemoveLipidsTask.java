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
package ND.modules.reactionOP.removeLipids;

import ND.modules.reactionOP.compoundFlux.*;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.util.ArrayList;
import java.util.List;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class RemoveLipidsTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final double finishedPercentage = 0.0f;

    public RemoveLipidsTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
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
            Model m = this.networkDS.getDocument().getModel();
            Graph g = this.networkDS.getGraph();
            for (Species r : m.getListOfSpecies()) {
                if (r.getName().contains("membrane") || r.getName().contains("lipid")|| r.getName().contains("endoplasmic")|| r.getName().contains("cell envelope")) {
                        Node n = g.getNode(r.getId());
                        if (n != null) {
                            g.removeNode(n.getId());
                        }                 
                }
                
            }
            
            
            
            
            for (Reaction r : m.getListOfReactions()) {
                for(SpeciesReference sp : r.getListOfProducts()){
                    Species specie = sp.getSpeciesInstance();
                    if (specie.getName().contains("membrane") || specie .getName().contains("lipid")|| specie .getName().contains("endoplasmic")|| r.getName().contains("cell envelope")) {
                       Node n = g.getNode(r.getId());
                        if (n != null) {
                            g.removeNode(n.getId());
                        }
                    }
                }
                for(SpeciesReference sp : r.getListOfReactants()){
                    Species specie = sp.getSpeciesInstance();
                    if (specie .getName().contains("membrane") || specie .getName().contains("lipid")|| specie .getName().contains("endoplasmic")|| r.getName().contains("cell envelope")) {
                        Node n = g.getNode(r.getId());
                        if (n != null) {
                            g.removeNode(n.getId());
                        }
                    }
                }
            }
            
            
          
            setStatus(TaskStatus.FINISHED);

        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

}
