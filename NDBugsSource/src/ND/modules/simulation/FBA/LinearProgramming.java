/*
 * Copyright 2007-2013 VTT Biotechnology
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
package ND.modules.simulation.FBA;

import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.simulation.antNoGraph.ReactionFA;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.javailp.Constraint;
import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactoryGLPK;
import net.sf.javailp.VarType;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;

/**
 *
 * @author Sandra Castillo <Sandra.castillo@vtt.fi>
 */
public class LinearProgramming {

    double objectiveValue;

    public LinearProgramming(Map<String, SpeciesFA> species, Map<String, ReactionFA> reactions) {

        /*    LPWizard lpw = new LPWizard();
         lpw.plus("x1",5.0).plus("x2",10.0);
         lpw.addConstraint("c1",8,"<=").plus("x1",3.0).plus("x2",1.0);
         lpw.addConstraint("c2",4,"<=").plus("x2",4.0);
         lpw.addConstraint("c3", 2, ">=").plus("x1",2.0); 
         */
        double[] b = CreateB(reactions.values());
        List<String> fluxes = new ArrayList<>();
        for (ReactionFA r : reactions.values()) {
            fluxes.add(r.getId());
        }

        LPWizard lpw = new LPWizard();
        for (ReactionFA r : reactions.values()) {
            if (r.getId().equals("r_0962")) {
                lpw.plus("r_0962", 1.0);
            } else {
                lpw.plus("r_0962", 0.0);
            }            
        }
        // LPWizardConstraint c = lpw.addConstraint("c0", 0, "=");
        System.out.print("       -> ");
        for (ReactionFA r : reactions.values()) {
            System.out.print(r.getId() + "  ");
        }
        System.out.print("\n");
        for (String sp : species.keySet()) {
            LPWizardConstraint c = lpw.addConstraint(sp, 0, "=");
            System.out.print(sp + " -> ");
            for (ReactionFA r : reactions.values()) {
                if (r.hasReactant(sp)) {
                    System.out.print(r.getStoichiometry(sp) * -1 + "        ");
                    c.plus(r.getId(), r.getStoichiometry(sp) * -1);
                } else {
                    System.out.print(r.getStoichiometry(sp) + "        ");
                    c.plus(r.getId(), r.getStoichiometry(sp));
                }
            }
            System.out.print("\n");
        }

        for (ReactionFA r : reactions.values()) {
            lpw.addConstraint(r.getId(), r.getlb(), ">=");
            lpw.addConstraint(r.getId(), r.getub(), "<=");
        }

        LinearProgramSolver solver = SolverFactory.getSolver("GLPK");

        LPSolution sol = lpw.solve(solver);
      //  objectiveValue = sol.getObjectiveValue();
        // System.out.println(objectiveValue);
        //       List<String> variables = getVariables(reactions, g, sources);
//        double[] fluxes = new double[variables.size()];
//        for (int i = 0; i < fluxes.length; i++) {
//            fluxes[i] = 1;
//        }
//        LinearProgram lp = new LinearProgram(fluxes);
//        lp.addConstraint(new LinearEqualsConstraint(fluxes, 0, "Constrain1"));
//        double[] sourceConstrains = new double[variables.size()];
//        for (String s : sources.keySet()) {
//            for (int i = 0; i < sourceConstrains.length; i++) {
//                if (s.equals(variables.get(i))) {
//                    sourceConstrains[i] = sources.get(variables.get(i))[0];
//                } else {
//                    sourceConstrains[i] = 0.0;
//                }
//            }
//            lp.addConstraint(new LinearBiggerThanEqualsConstraint(sourceConstrains, sources.get(s)[0], "c" + s));
//            lp.addConstraint(new LinearSmallerThanEqualsConstraint(sourceConstrains, 0, "c" + s + "2"));
//        }
//
//        double[] boundsConstrains = new double[variables.size()];
//        for (String v : variables) {
//            if (reactions.containsKey(v)) {
//                ReactionFA r = reactions.get(v);
//
//            }
//        }
//        lp.setMinProblem(false);
//        LinearProgramSolver solver = SolverFactory.newDefault();
//        double[] sol = solver.solve(lp);

//        LPWizard lpw = new LPWizard();
//        for (String var : variables) {
//            lpw.plus(var, 1.0);
//        }
//        for (String source : sources.keySet()) {
//            lpw.addConstraint("c" + source, sources.get(source)[0] * -1, ">=").plus(source, 1.0);
//            lpw.addConstraint("c" + source + "2", 0, "<=").plus(source, 1.0);
//        }
//        for (String v : variables) {
//            if (reactions.containsKey(v)) {
//                lpw.addConstraint("c" + v + "lb", reactions.get(v).getlb(), ">=").plus(v, 1.0);
//                lpw.addConstraint("c" + v + "ub", reactions.get(v).getub(), "<=").plus(v, 1.0);
//            }
//        }

        /* for (Node n : g.getNodes()) {
         String id = n.getId();
         id = id.split(" : ")[0];
         if (reactions.containsKey(id)) {
         List<Node> nodes = g.getConnectedAsDestination(n);
         int cont = 0;
         for (Node conNodes : nodes) {
         String newid = conNodes.getId();
         newid = newid.split(" : ")[0];
         lpw.addConstraint("c" + id + "connect" + cont++, 0, ">=").plus(newid, 1.0).plus(id, -1.0);
         }

         }
         }*/
//        lpw.setMinProblem(false);
//        LinearProgramSolver solver  = SolverFactory.newDefault(); 
//        LPSolution sol = lpw.solve(solver);
//        objectiveValue = sol.getObjectiveValue();
//        fluxes = new HashMap<>();
//        for (String var : variables) {
//            fluxes.put(var, sol.getDouble(var));
//        }
    }

//    private Result optimize(double[][] A, double[] b, double[] objective, double[] lb, double[] ub) {
//        net.sf.javailp.SolverFactory factory = new SolverFactoryGLPK();
//        //factory.setParameter(Solver.VERBOSE, 0);
//        factory.setParameter(Solver.TIMEOUT, 100);
//        Problem problem = new Problem();
//        List<String> variables = new ArrayList<>();
//        //Objective Function
//        Linear linear = new Linear();
//        for (int i = 0; i < objective.length; i++) {
//            String var = this.reactions.get(i);
//            //System.out.println(var + " - " + objective[i]);
//            variables.add(var);
//            linear.add(objective[i], var);
//        }
//        // if (maximize) {
//        problem.setObjective(linear);
//        /* } else {
//         problem.setObjective(linear;
//         }*/
//        // Inequalities
//        for (int i = 0; i < b.length; i++) {
//            linear = new Linear();
//            for (int e = 0; e < A.length; e++) {
//                linear.add(A[e][i], variables.get(e));
//            }
//            problem.add(new Constraint(this.species.get(i), linear, Operator.EQ, b[i]));
//        }
//        for (int i = 0; i < variables.size(); i++) {
//            problem.setVarLowerBound(variables.get(i), lb[i]);
//            problem.setVarUpperBound(variables.get(i), ub[i]);
//            //  System.out.println(variables.get(i) + " - " + lb[i] + " - " + ub[i]);
//        }
//        for (String var : variables) {
//            problem.setVarType(var, VarType.REAL);
//        }
//        Solver solver = factory.get(); // you should use this solver only once for one problem
//        Result result = solver.solve(problem);
//
//        //System.out.println(result.toString());
//        return result;
//    }
//
//    private double[][] createMatrix(HashMap<String, ReactionFA> reactions, List<String> variables) {
//        List<String> species = new ArrayList<>();
//        int numReactions=0;
//        for (String r : variables) {
//            if (reactions.containsKey(r)) {
//                numReactions++;
//                ReactionFA reaction = reactions.get(r);
//                for (String reactant : reaction.getReactants()) {
//                    if (!species.contains(reactant)) {
//                        species.add(reactant);
//                    }
//                }
//                for (String product : reaction.getProducts()) {
//                    if (!species.contains(product)) {
//                        species.add(product);
//                    }
//                }
//            }else{
//                species.add(r);
//            }
//        }
//
//      
//        /* for (Species s : m.getListOfSpecies()) {
//         this.species.add(s.getId() + "out");
//         }*/
//        /*int countex = 0;
//         for (String ex : exchange.keySet()) {
//         if (species.contains(ex)) {
//         countex++;
//         }
//         }*/
//        double[][] A = new double[numReactions + species.size()][species.size() /**
//                 * 2
//                 */
//                ];
//
//        /*if (!this.species.contains(this.NAD)) {
//         this.species.add(this.NAD);
//         }
//                
//         if (!this.species.contains(this.NADP)) {
//         this.species.add(this.NADP);
//         }
//                
//         if (!this.species.contains(this.ADP)) {
//         this.species.add(this.ADP);
//         }*/
//        int count = 0;
//        for (String r : variables) {
//            if(reactions.containsKey(r)){
//                ReactionFA reaction = reactions.get(r);
//            for (String reactant : reaction.getReactants()) {
//                int index = species.indexOf(reactant);
//                A[count][index] = -reaction.getStoichiometry(reactant);
//            }
//            for (String product : reaction.getProducts()) {
//                int index = species.indexOf(product);
//                A[count][index] = reaction.getStoichiometry(product);
//            }
//            count++;
//            }
//        }
//        //add exchange reactions
//        for (String ex : exchange.keySet()) {
//            if (species.contains(ex)) {
//                reactions.add(ex);
//                int index = species.indexOf(ex);
//                A[count++][index] = -1;
//            }
//        }
//
//        // System.out.println(m.getNumSpecies() + " - " + count);
//        for (Species sp : m.getListOfSpecies()) {
//            if (!exchange.containsKey(sp.getId()) /*&& !sp.getId().contains("Growth")*/) {
//                reactions.add(sp.getId());
//                int index = species.indexOf(sp.getId());
//                A[count++][index] = -1;
//            }
//        }
//                // System.out.println(m.getNumSpecies() + " - " + count);
//
//        /*for (int i = 0; i < A.length; i++) {
//         for (int e = 0; e < A[0].length; e++) {
//         System.out.print(A[i][e] + " ");
//         }
//         System.out.print("\n");
//         }
//         for (String sp : this.species) {
//         System.out.println(sp);
//         }*/
//        return A;
//    }
//
//    private double[] createB() {
//        //for each reaction 0 is added except for the exchange reactions
//        double[] b = new double[this.species.size()];
//        for (String r : species) {
//            int index = species.indexOf(r);
//            if (exchange.containsKey(r)) {
//                b[index] = 0;
//            } else {
//                b[index] = 0;
//            }
//        }
//        return b;
//    }
//
//    private double[] createObjective() {
//        double[] objective = new double[this.reactions.size()];
//        for (int index = 0; index < reactions.size(); index++) {
//            String r = reactions.get(index);
//            if (r.contains(this.objectiveReaction)) {
//                if (maximize) {
//                    objective[index] = -1;
//                } else {
//                    objective[index] = 1;
//                }
//            } else {
//                objective[index] = 0;
//            }
//            // System.out.println(objective[index]);
//        }
//        return objective;
//    }
//
//    private double[] createLB(HashMap<String, String[]> bounds) {
//        double[] lb = new double[this.reactions.size()];
//        Model m = this.networkDS.getDocument().getModel();
//        for (int index = 0; index < reactions.size(); index++) {
//            String r = reactions.get(index);
//            // System.out.println(r);
//            if (exchange.containsKey(r)) {
//                lb[index] = -exchange.get(r);
//            } else {
//                String[] b = bounds.get(r);
//                if (b == null) {
//                    Reaction reaction = m.getReaction(r);
//                    if (reaction != null) {
//                        KineticLaw law = reaction.getKineticLaw();
//                        if (law != null) {
//                            LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
//                            lb[index] = lbound.getValue();
//                        } else {
//                            lb[index] = 0.0;
//                        }
//                    } else {
//                        lb[index] = 0.0;
//                    }
//                } else {
//                    lb[index] = Double.valueOf(b[3]);
//                }
//            }
//            //  System.out.println(r + " - " + lb[index]);
//        }
//        return lb;
//    }
//
//    private double[] createUP(HashMap<String, String[]> bounds) {
//        double[] ub = new double[this.reactions.size()];
//        Model m = this.networkDS.getDocument().getModel();
//        for (int index = 0; index < reactions.size(); index++) {
//            String r = reactions.get(index);
//            if (exchange.containsKey(r)) {
//                if (r.contains("C00031")) {
//                    ub[index] = -(this.exchange.get(r) - 0.001);
//                } else {
//                    ub[index] = 1000;
//                }
//            } else {
//                String[] b = bounds.get(r);
//                if (b == null) {
//                    Reaction reaction = m.getReaction(r);
//                    if (reaction != null) {
//                        KineticLaw law = reaction.getKineticLaw();
//                        if (law != null) {
//                            LocalParameter lbound = law.getLocalParameter("UPPER_BOUND");
//                            ub[index] = lbound.getValue();
//                        } else {
//                            ub[index] = 1000.0;
//                        }
//                    } else {
//                        ub[index] = 1000.0;
//                    }
//                } else {
//                    ub[index] = Double.valueOf(b[4]);
//                }
//            }// System.out.println(r + " - " + ub[index]);
//
//        }
//        return ub;
//    }
//
//    private List<String> getVariables(HashMap<String, ReactionFA> reactions, Graph g, Map<String, Double[]> sources) {
//        List<String> variables = new ArrayList<>();
//        for (String s : sources.keySet()) {
//            variables.add(s);
//        }
//        for (Node n : g.getNodes()) {
//            String id = n.getId();
//            id = id.split(" : ")[0];
//            if (reactions.containsKey(id)) {
//                variables.add(id);
//            }
//        }
//        return variables;
//    }
    public double getObjectiveValue() {
        return this.objectiveValue;
    }

   // public Map<String, Double> getFluxes() {
    //    return this.fluxes;
    //}
    private double[] CreateB(Collection<ReactionFA> values) {
        double[] b = new double[values.size()];
        /*for(ReactionFA reaction : values){
            if(reacit("r_0962", 1.0);
        }*/
        return null;
    }
}
