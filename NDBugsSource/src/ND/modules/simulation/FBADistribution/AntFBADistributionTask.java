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
package ND.modules.simulation.FBADistribution;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.modules.simulation.FBA.LP.ObjType;
import ND.modules.simulation.FBAreal.FBA;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class AntFBADistributionTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private double finishedPercentage = 0.0f;
    private HashMap<String, ReactionFA> reactions;
    private HashMap<String, List<Double>> fluxes;
    private File file;

    public AntFBADistributionTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.fluxes = new HashMap<>();
        this.file = parameters.getParameter(AntFBADistributionParameters.matrixFile).getValue();
    }

    @Override
    public String getTaskDescription() {
        return "Starting FBA Distribution... ";
    }

    @Override
    public double getFinishedPercentage() {
        return finishedPercentage;
    }

    @Override
    public void cancel() {
        setStatus(TaskStatus.CANCELED);
    }

    private ReactionFA createReaction(String name) {
        ReactionFA reaction = new ReactionFA("Ex_" + name);
        reaction.addReactant(name, 1.0);
        reaction.setBounds(-1000, 1000);
        return reaction;
    }

    @Override
    public void run() {
        //   try {
        setStatus(TaskStatus.PROCESSING);
        
        optimize();
        
        CsvWriter writer = new CsvWriter(this.file.getAbsolutePath());
       
        for(String r:this.fluxes.keySet()){
           
            try {
                List<Double> flux = this.fluxes.get(r);
                String[] record = new String[flux.size() +1];
                record[0] = r;
                int i = 1;
                for(Double f : flux){
                    record[i++] = String.valueOf(f);
                }
                writer.writeRecord(record);
            } catch (IOException ex) {
                Logger.getLogger(AntFBADistributionTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println();
        setStatus(TaskStatus.FINISHED);

        /*  } catch (Exception e) {
         System.out.println(e.toString());
         setStatus(TaskStatus.ERROR);
         }*/
    }

    private void optimize() {
        
        createReactions();
        int i = 0;
        
        for(String r : this.reactions.keySet()){
            i++;
            if(i > 15) break;
            this.reactions.get(r).setObjective(1.0);
            this.getFlux();
            this.reactions.get(r).setObjective(0.0);            
        }
    }

    public void getFlux() {
        FBA fba = new FBA();
    
        fba.setModel(this.reactions, this.networkDS.getDocument().getModel(), ObjType.Maximize);
        try {
            Map<String, Double> soln = fba.run();
            for (String r : soln.keySet()) {
                if (this.fluxes.containsKey(r)) {
                    List<Double>flux = this.fluxes.get(r);
                    if(flux == null) flux = new ArrayList<>();
                    flux.add(soln.get(r));
                    this.fluxes.put(r, flux);
                }else{
                    List<Double> flux = new ArrayList<>();
                    flux.add(soln.get(r));
                    this.fluxes.put(r, flux);
                
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
        fba = new FBA();
        
        fba.setModel(this.reactions, this.networkDS.getDocument().getModel(), ObjType.Minimize);
        try {
            Map<String, Double> soln = fba.run();
            for (String r : soln.keySet()) {
                if (this.fluxes.containsKey(r)) {
                    List<Double>flux = this.fluxes.get(r);
                    if(flux == null) flux = new ArrayList<>();
                    flux.add(soln.get(r));
                    this.fluxes.put(r, flux);
                }else{
                    List<Double> flux = new ArrayList<>();
                    flux.add(soln.get(r));
                    this.fluxes.put(r, flux);
                
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private void createReactions() {
        SBMLDocument doc = this.networkDS.getDocument();
        this.reactions = new HashMap<>();
        Model m = doc.getModel();

        for (Reaction r : m.getListOfReactions()) {
            ReactionFA reaction = new ReactionFA(r.getId());

            try {
                KineticLaw law = r.getKineticLaw();
                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                //LocalParameter objective = law.getLocalParameter("OBJECTIVE_COEFFICIENT");
                reaction.setObjective(0.0);
                reaction.setBounds(lbound.getValue(), ubound.getValue());
            } catch (Exception ex) {
                reaction.setBounds(-1000, 1000);
            }

            for (SpeciesReference s : r.getListOfReactants()) {

                Species sp = s.getSpeciesInstance();
                reaction.addReactant(sp.getId(), sp.getName(), s.getStoichiometry());
            }

            for (SpeciesReference s : r.getListOfProducts()) {
                Species sp = s.getSpeciesInstance();
                reaction.addProduct(sp.getId(), sp.getName(), s.getStoichiometry());
            }
            this.reactions.put(r.getId(), reaction);
        }
    }
}
