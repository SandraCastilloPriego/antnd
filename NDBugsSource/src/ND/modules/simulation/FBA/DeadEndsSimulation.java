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

import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.modules.simulation.antNoGraph.uniqueId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Sandra Castillo <Sandra.castillo@vtt.fi>
 */
public class DeadEndsSimulation {

  
    private final List<String> objectives;
    private final List<String> cofactors;

    private final HashMap<String, ReactionFA> reactions;
    private final HashMap<String, SpeciesFA> compounds;

    private double shortestPath = Integer.MAX_VALUE;
    private Ant antResult;
    private final String deadEnd;

    public DeadEndsSimulation(HashMap<String,ReactionFA> reactions,HashMap<String, SpeciesFA>compounds, List<String> cofactors, String deadEnd) {
        this.cofactors = cofactors;
        this.objectives = new ArrayList();

        this.reactions = reactions;
        
        this.compounds = compounds;
        
        this.deadEnd = deadEnd;
    }

   

    public void cicle(String objectiveID) {
        for (String compound : compounds.keySet()) {

            List<String> possibleReactions = getPossibleReactions(compound);

            for (String reactionChoosen : possibleReactions) {

                ReactionFA rc = this.reactions.get(reactionChoosen);
                double bound;
                List<String> toBeAdded, toBeRemoved;
                if (rc.hasReactant(compound)) {
                    bound = Math.abs(rc.getub());
                    toBeAdded = rc.getProducts();
                    toBeRemoved = rc.getReactants();
                } else {
                    bound = Math.abs(rc.getlb());
                    toBeAdded = rc.getReactants();
                    toBeRemoved = rc.getProducts();
                }

                // get the ants that must be removed from the reactants ..
                // creates a superAnt with all the paths until this reaction joined..
                Ant superAnt = new Ant(null);
                HashMap<Ant, String> combinedAnts = new HashMap<>();
                double lastFlux = bound;
                for (String s : toBeRemoved) {
                    SpeciesFA spfa = this.compounds.get(s);

                    Ant a = spfa.getAnt();
                    if (a == null) {
                        a = new Ant(spfa.getId() + " : " + spfa.getName());
                       
                    }
                    // spfa.addAnt(a);

                    double f = a.getFlux() / rc.getStoichiometry(spfa.getId());
                    if (f < lastFlux) {
                        lastFlux = f;
                    }
                    if (a != null) {
                        combinedAnts.put(a, s);
                    }
                }

                /* for (String s : toBeRemoved) {
                 SpeciesFA spfa = this.compounds.get(s);
                 Ant a = spfa.getAnt();
                 if (a != null) {
                 a.setFlux(lastFlux);
                 }
                 }*/
                superAnt.joinGraphs(reactionChoosen, combinedAnts, rc);

                if (!superAnt.isLost()) {
                    // move the ants to the products...   
                    for (String s : toBeAdded) {
                        SpeciesFA spfa = this.compounds.get(s);
                        for (int e = 0; e < rc.getStoichiometry(s); e++) {
                            Ant newAnt = superAnt.clone();
                            newAnt.setLocation(spfa.getId() + " : " + spfa.getName(), rc);
                            spfa.addAnt(newAnt);
                        }
                    }

                }
                // When the ants arrive to the biomass
                if (toBeAdded.contains(objectiveID)) {
                    //System.out.println("Biomass produced!: " + rc.getId());

                    SpeciesFA spFA = this.compounds.get(objectiveID);

                    Ant a = spFA.getAnt();
                    if (a != null) {
                        // saving the shortest path
                        if (a.getPathSize() < shortestPath) {
                            System.out.println(a.getPathSize());
                            this.shortestPath = a.getPathSize();
                            Graph antGraph = a.getGraph();
                            Node objectiveNode = antGraph.getNode(spFA.getId());
                            if (objectiveNode == null) {
                                objectiveNode = new Node(spFA.getId() + " : " + spFA.getName());
                            }
                            antGraph.addNode2(objectiveNode);
                            Node lastNode = antGraph.getNode(reactionChoosen+ " : " + a.getFlux());
                            if(objectiveID.contains("s_0434")){
                                System.out.println(reactionChoosen);
                            }
//                            if (lastNode == null) {
//                                lastNode = new Node(reactionChoosen + " : " + a.getFlux() + " - " + uniqueId.nextId());
//                            }
                            Edge edge = new Edge(objectiveID + " : " + a.getFlux() + " - " + uniqueId.nextId(), lastNode, objectiveNode);
                            antGraph.addEdge(edge);
                            a.setGraph(antGraph);
                            a.print();
                            this.antResult = a;
                        }

                    }
                }
            }
        }
    }

    private List<String> getPossibleReactions(String node) {

        List<String> possibleReactions = new ArrayList<>();
        SpeciesFA sp = this.compounds.get(node);

        List<String> connectedReactions = sp.getReactions();
        for (String reaction : connectedReactions) {
            ReactionFA r = this.reactions.get(reaction);

            boolean isPossible = true;

            if (r.hasReactant(node)) {

                if (r.getub() > 0) {
                    List<String> reactants = r.getReactants();
                    for (String reactant : reactants) {

                        if (!allEnoughAnts(reactant, reaction)) {
                            isPossible = false;
                            break;
                        }

                    }

                } else {
                    isPossible = false;
                }

            } else {
                if (r.getlb() < 0) {
                    List<String> products = r.getProducts();
                    for (String product : products) {
                        if (!allEnoughAnts(product, reaction)) {
                            isPossible = false;
                            break;
                        }

                    }
                } else {
                    isPossible = false;
                }

            }
            if (isPossible) {
                possibleReactions.add(reaction);
            }

        }
        return possibleReactions;
    }

    private boolean allEnoughAnts(String species, String reaction) {
        SpeciesFA s = this.compounds.get(species);
        Ant ant = s.getAnt();
        if (ant != null) {
            return !ant.contains(reaction);
        } else if (cofactors.contains(species)) {
            this.objectives.add(species);
            return true;
        }
        return false;
    }

    public List<String> getNewObjectives() {
        return this.objectives;
    }

    public Ant getResult() {
        return this.antResult;
    }

    public void reset() {
        this.shortestPath = Integer.MAX_VALUE;
    }
}
