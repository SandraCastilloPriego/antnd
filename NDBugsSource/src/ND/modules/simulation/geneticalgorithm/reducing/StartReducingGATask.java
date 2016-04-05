/*
 * Copyright 2010 - 2012 VTT Biotechnology
 * This file is part of ALVS.
 *
 * ALVS is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * ALVS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ALVS; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.simulation.geneticalgorithm.reducing;

import ND.data.Dataset;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.modules.simulation.geneticalgorithm.tools.FBA;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.util.*;
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
public class StartReducingGATask extends AbstractTask {

    private final Dataset training;
    private final List<String> variables;
    private List<ReactionFA> rows;
    private final String biomass, objective;
    private final GetInfoAndTools tools;
    private HashMap<String, ReactionFA> reactions;
    private double referenceObjective;

    public StartReducingGATask(Dataset dataset, SimpleParameterSet parameters) {
        training = dataset;
        String reactionsToTest = parameters.getParameter(StartReducingGAParameters.reactions).getValue();
        this.variables = new ArrayList<>();
        if (reactionsToTest.contains(",")) {
            variables.addAll(Arrays.asList(reactionsToTest.split(",")));
        } else {
            variables.addAll(Arrays.asList(reactionsToTest.split(" - ")));
        }
        this.biomass = parameters.getParameter(StartReducingGAParameters.biomass).getValue();
        this.objective = parameters.getParameter(StartReducingGAParameters.objective).getValue();
        this.rows = new ArrayList<>();
        this.tools = new GetInfoAndTools();
    }

    @Override
    public String getTaskDescription() {
        return "Start Testing... ";
    }

    @Override
    public double getFinishedPercentage() {
        return 0.0f;
    }

    @Override
    public void cancel() {
        setStatus(TaskStatus.CANCELED);
    }

    @Override
    public void run() {
        try {
            setStatus(TaskStatus.PROCESSING);
            createReactions();
            
            this.setBiomassObjective();
            double referenceBiomass = this.getReference();
            
            List<String> solution = new ArrayList<>();
            for (String var : this.variables) {
               boolean score = this.evaluate(referenceBiomass, referenceObjective, var);
                if (!score) {
                    solution.add(var);
                }
            }

            for (String s : solution) {
                System.out.print(s + ",");
            }
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

    private void createReactions() {
        System.out.println("Creating reactions");
        SBMLDocument doc = this.training.getDocument();
        this.reactions = new HashMap<>();
        Model m = doc.getModel();

        for (Reaction r : m.getListOfReactions()) {
            ReactionFA reaction = new ReactionFA(r.getId());

            try {
                KineticLaw law = r.getKineticLaw();
                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
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
            reaction.setObjective(0.0);
            if (this.variables.contains(reaction.getId())) {
                this.rows.add(reaction);
            }
            this.reactions.put(r.getId(), reaction);
        }

    }

    public void setBiomassObjective() {
        ReactionFA objectiveReaction = new ReactionFA("objective");
        objectiveReaction.addReactant(this.biomass, 1.0);
        objectiveReaction.setBounds(0, 1000);
        objectiveReaction.setObjective(1.0);
        this.reactions.put("objective", objectiveReaction);
    }

    public void setObjectiveObjective() {
        ReactionFA objectiveReaction = new ReactionFA("objective");
        objectiveReaction.addReactant(this.objective, 1.0);
        objectiveReaction.setBounds(0, 1000);
        objectiveReaction.setObjective(1.0);
        this.reactions.put("objective", objectiveReaction);
    }

    public double getReference() {
        FBA fba = new FBA();
        this.referenceObjective = 0;
        fba.setModel(this.reactions, this.training.getDocument().getModel(), this.rows);
        try {
            Map<String, Double> soln = fba.run();
            for (String r : soln.keySet()) {
                //System.out.println(r);
                if (this.reactions.containsKey(r) && this.reactions.get(r).hasProduct(this.objective) && fba.getMaxObj() > 0.00001 /*|| this.reactions.get(r).hasReactant(this.objective))*/) {
                    this.referenceObjective+= soln.get(r);
                }
            }
            return fba.getMaxObj();

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return 0.0;
    }

    private boolean evaluate(double referenceBiomass, double referenceObjective, String reactionToDelete) {
        List<ReactionFA> testing = new ArrayList<>();

        for (ReactionFA reaction : rows) {
            if (!reaction.getId().equals(reactionToDelete)) {
                testing.add(reaction);
            }
        }

        FBA fba = new FBA();
        fba.setModel(this.reactions, this.training.getDocument().getModel(), testing);
        try {
            double flux = 0.0;
            Map<String, Double> soln = fba.run();
            for (String r : soln.keySet()) {
                //System.out.println(r);
                if (this.reactions.containsKey(r) && this.reactions.get(r).hasProduct(this.objective) && fba.getMaxObj() > 0.00001 /*|| this.reactions.get(r).hasReactant(this.objective))*/) {
                    flux += soln.get(r);
                }
            }

            if(flux < referenceObjective) return false;
            if(fba.getMaxObj() < referenceBiomass) return false;
            return true;

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return false;
    }

}
