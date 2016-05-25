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
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.modules.simulation.geneticalgorithm.tools.FBA;
import ND.modules.simulation.geneticalgorithmDirections.tools.Bug;
import ND.modules.simulation.geneticalgorithmDirections.tools.Bug.status;
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
    private final String objective;
    private HashMap<String, ReactionFA> reactions;
    private Map<String, status> reactionDirections;

    public StartReducingGATask(Dataset dataset, SimpleParameterSet parameters) {
        training = dataset;
        String reactionsToTest = parameters.getParameter(StartReducingGAParameters.reactions).getValue();
        this.variables = new ArrayList<>();
        if (reactionsToTest.contains(",") && !reactionsToTest.contains(" - ")) {
            variables.addAll(Arrays.asList(reactionsToTest.split(",")));
        } else if (!reactionsToTest.contains(",") && reactionsToTest.contains(" - ")) {
            variables.addAll(Arrays.asList(reactionsToTest.split(" - ")));
        } else if (reactionsToTest.contains(",") && reactionsToTest.contains(" - ")) {
            variables.addAll(Arrays.asList(reactionsToTest.split(",")));
            for (String reaction : variables) {
                String[] r = reaction.split(" - ");
                Bug.status stt = Bug.status.KO;
                if (r[1].contains("LB")) {
                    stt = Bug.status.LB;
                } else if (r[1].contains("UP")) {
                    stt = Bug.status.UP;
                }
                this.reactionDirections.put(r[0], stt);
            }
            this.variables.clear();
        }
        this.objective = parameters.getParameter(StartReducingGAParameters.objective).getValue();
        this.rows = new ArrayList<>();
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
            for (String var : variables) {
                this.rows.add(reactions.get(var));
            }

            List<String> solution = new ArrayList<>();
            for (String var : this.variables) {
                boolean score = this.evaluate(0.4, 1004.77, var);
                if (!score) {
                    System.out.println("FALSE");
                    solution.add(var);
                } else {
                    System.out.println("TRUE");
                }
            }
            
//            for (String var : this.reactionDirections.keySet()) {
//                boolean score = this.evaluateDirections(0.4, 1004.77, var, this.reactionDirections.get(var));
//                if (!score) {
//                    System.out.println("FALSE");
//                    solution.add(var);
//                } else {
//                    System.out.println("TRUE");
//                }
//            }

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
                LocalParameter objective = law.getLocalParameter("OBJECTIVE_COEFFICIENT");
                reaction.setObjective(objective.getValue());
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
            //reaction.setObjective(0.0);            
            this.reactions.put(r.getId(), reaction);
        }

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
                if (this.reactions.containsKey(r) && this.reactions.get(r).hasProduct(this.objective) /*|| this.reactions.get(r).hasReactant(this.objective)*/) {
                    if (soln.get(r) > 0) {
                        flux += soln.get(r);
                    }
                }
                if (this.reactions.containsKey(r) && this.reactions.get(r).hasReactant(this.objective)) {
                    if (soln.get(r) < 0) {
                        flux -= soln.get(r);
                    }
                }
            }
            System.out.println(reactionToDelete + " - " + flux + " - " + fba.getMaxObj());

            if (flux < referenceObjective) {
                return false;
            }
            if (fba.getMaxObj() < referenceBiomass) {
                return false;
            }
            return true;

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return false;
    }

//    private boolean evaluateDirections(double referenceBiomass, double referenceObjective, String var, status stt) {
//        List<ReactionFA> testing = new ArrayList<>();
//
//        for (ReactionFA reaction : rows) {
//            if (!reaction.getId().equals(var)) {
//                testing.add(reaction);
//            }else{
//                if(stt == status.LB){
//                    ReactionFA newReaction = reaction.clone();
//                    reaction.setBounds(0, reaction.getub());                    
//                }else if(stt == status.UP){
//                    reaction.setBounds(reaction.getlb(), 0);
//                    
//                }
//            }
//        }
//
//        FBA fba = new FBA();
//        fba.setModel(this.reactions, this.training.getDocument().getModel(), testing);
//        try {
//            double flux = 0.0;
//            Map<String, Double> soln = fba.run();
//            for (String r : soln.keySet()) {
//                //System.out.println(r);
//                if (this.reactions.containsKey(r) && this.reactions.get(r).hasProduct(this.objective) /*|| this.reactions.get(r).hasReactant(this.objective)*/) {
//                    if (soln.get(r) > 0) {
//                        flux += soln.get(r);
//                    }
//                }
//                if (this.reactions.containsKey(r) && this.reactions.get(r).hasReactant(this.objective)) {
//                    if (soln.get(r) < 0) {
//                        flux -= soln.get(r);
//                    }
//                }
//            }
//            System.out.println(var + " - " + flux + " - " + fba.getMaxObj());
//
//            if (flux < referenceObjective) {
//                return false;
//            }
//            if (fba.getMaxObj() < referenceBiomass) {
//                return false;
//            }
//            return true;
//
//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
//        return false;  }

}
