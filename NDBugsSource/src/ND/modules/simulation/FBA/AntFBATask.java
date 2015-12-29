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
package ND.modules.simulation.FBA;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.desktop.impl.PrintPaths;
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
public class AntFBATask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private double finishedPercentage = 0.0f;

    private String objectiveID;
    private final String biomassReactionID;
    private final Random rand;
    private final HashMap<String, ReactionFA> reactions;
    private final HashMap<String, SpeciesFA> compounds;
    private HashMap<String, String[]> bounds;
    private Map<String, Double[]> sources;
    private List<String> objectives;
    private final List<String> sourcesList;
    private final JInternalFrame frame;
    private final JScrollPane panel;
    private final JPanel pn;
    private double shortestPath = 0;
    private Graph graph;
    private final GetInfoAndTools tools;
    private Reaction biomassReaction;

    public AntFBATask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.objectiveID = parameters.getParameter(AntFBAParameters.objectiveReaction).getValue();
        this.biomassReactionID = parameters.getParameter(AntFBAParameters.biomassReaction).getValue();
        this.rand = new Random();
        Date date = new Date();
        long time = date.getTime();
        this.objectives = new ArrayList<>();
        this.objectives.add(objectiveID);
        this.reactions = new HashMap<>();
        this.compounds = new HashMap<>();
        this.bounds = new HashMap<>();
        this.sourcesList = new ArrayList<>();

        this.frame = new JInternalFrame("Result", true, true, true, true);
        this.pn = new JPanel();
        this.panel = new JScrollPane(pn);

        this.tools = new GetInfoAndTools();

        // Initialize the random number generator using the
        // time from above.
        rand.setSeed(time);

    }

    @Override
    public String getTaskDescription() {
        return "Starting Ant FBA Simulation... ";
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
        //   try {
        setStatus(TaskStatus.PROCESSING);
        if (this.networkDS == null) {
            setStatus(TaskStatus.ERROR);
            NDCore.getDesktop().displayErrorMessage("You need to select a metabolic model.");
        }
        System.out.println("Reading sources");
        this.sources = tools.GetSourcesInfo();
        System.out.println(this.sources.size());
        System.out.println("Reading bounds");
        this.bounds = tools.readBounds(networkDS);
        System.out.println("Creating world");
        this.createWorld();
        System.out.println("Starting simulation");
        frame.setSize(new Dimension(700, 500));
        frame.add(this.panel);
        NDCore.getDesktop().addInternalFrame(frame);
        for (int i = 0; i < 10; i++) {
            this.cicle(i);
            finishedPercentage = (double) i / 10;
            if (getStatus() == TaskStatus.CANCELED || getStatus() == TaskStatus.ERROR) {
                break;
            }
        }
        for (String objective : this.objectives) {
            System.out.println(objective);
        }
        if (getStatus() == TaskStatus.PROCESSING) {
            this.tools.createDataFile(graph, networkDS, objectiveID, sourcesList, false);
            PrintPaths print = new PrintPaths(this.sourcesList, this.objectives.get(0), this.tools.getModel());
            try {
                System.out.println("Final graph: " + this.graph.toString());
                this.pn.add(print.printPathwayInFrame(this.graph));
            } catch (NullPointerException ex) {
                System.out.println("Imprimendo: " + ex.toString());
            }
        }
        if (this.graph == null) {
            NDCore.getDesktop().displayMessage("No path was found.");
        }

        setStatus(TaskStatus.FINISHED);

        /*  } catch (Exception e) {
         System.out.println(e.toString());
         setStatus(TaskStatus.ERROR);
         }*/
    }

    private void createWorld() {
        SBMLDocument doc = this.networkDS.getDocument();
        Model m = doc.getModel();
        for (Species s : m.getListOfSpecies()) {
            SpeciesFA specie = new SpeciesFA(s.getId(), s.getName());
            //add the number of initial ants using the sources.. and add them
            // in the list of nodes with ants
            if (this.sources.containsKey(s.getId())) {
                Ant ant = new Ant(specie.getId() + " : " + specie.getName());
                ant.initAnt(Math.abs(this.sources.get(s.getId())[0]));
                specie.addAnt(ant);
                this.sourcesList.add(s.getId());
            }
            this.compounds.put(s.getId(), specie);
        }

        for (Reaction r : m.getListOfReactions()) {
            boolean biomass = false;
            if (r.getId().equals(this.biomassReactionID)) {
                biomass = true;
                this.biomassReaction = r;
            }
            ReactionFA reaction = new ReactionFA(r.getId());
            String[] b = this.bounds.get(r.getId());
            if (b != null) {
                reaction.setBounds(Double.valueOf(b[3]), Double.valueOf(b[4]));
            } else {
                try {
                    KineticLaw law = r.getKineticLaw();
                    LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                    LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                    reaction.setBounds(lbound.getValue(), ubound.getValue());
                } catch (Exception ex) {
                    reaction.setBounds(-1000, 1000);
                }
            }
            for (SpeciesReference s : r.getListOfReactants()) {
                Species sp = s.getSpeciesInstance();
                reaction.addReactant(sp.getId(), s.getStoichiometry());
                SpeciesFA spFA = this.compounds.get(sp.getId());
                if (biomass) {
                    spFA.setPool(Math.abs(s.getStoichiometry()));
                }
                if (spFA != null) {
                    spFA.addReaction(r.getId());
                } else {
                    System.out.println(sp.getId());
                }
            }

            for (SpeciesReference s : r.getListOfProducts()) {
                Species sp = s.getSpeciesInstance();
                reaction.addProduct(sp.getId(), s.getStoichiometry());
                SpeciesFA spFA = this.compounds.get(sp.getId());
                if (spFA != null) {
                    spFA.addReaction(r.getId());
                } else {
                    System.out.println(sp.getId());
                }
            }
            this.reactions.put(r.getId(), reaction);
        }

    }

    public Ant cicle(int iteration) {

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
                        if (this.sources.containsKey(spfa.getId())) {
                            a.initAnt(Math.abs(this.sources.get(spfa.getId())[0]));
                        } else {
                            a.initAnt(spfa.getPool());
                        }

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
                double flux = superAnt.joinGraphs(reactionChoosen, combinedAnts, lastFlux, rc);

                if (!superAnt.isLost()) {
                    // move the ants to the products...   
                    for (String s : toBeAdded) {
                        SpeciesFA spfa = this.compounds.get(s);
                        for (int e = 0; e < rc.getStoichiometry(s); e++) {
                            Ant newAnt = superAnt.clone();
                            newAnt.setLocation(spfa.getId() + " : " + spfa.getName(), rc);
                            newAnt.setFlux(flux);
                            spfa.addAnt(newAnt);
                        }
                    }

                }
                // When the ants arrive to the biomass
                if (toBeAdded.contains(this.objectiveID)) {
                    //System.out.println("Biomass produced!: " + rc.getId());

                    SpeciesFA spFA = this.compounds.get(this.objectiveID);

                    Ant a = spFA.getAnt();
                    if (a != null) {
                        // saving the shortest path
                        if (a.getPathSize() > shortestPath) {
                            System.out.println(a.flux);
                            this.shortestPath = a.getFlux();
                            Graph antGraph = a.getGraph();
                            if (this.graph == null) {
                                this.graph = antGraph;
                            } else {
                                this.graph.addGraph(antGraph);
                            }
                            Node objectiveNode = this.graph.getNode(spFA.getId());
                            if(objectiveNode == null)
                                objectiveNode = new Node(spFA.getId() + " : " + spFA.getName());
                            this.graph.addNode2(objectiveNode);
                            Node lastNode = this.graph.getNode(reactionChoosen);
                            Edge edge = new Edge(this.objectiveID + " : " + a.getFlux(), lastNode, objectiveNode);
                            this.graph.addEdge(edge);
                            a.print();
                        }
                    }

                    if (iteration == 9) {
                        List<String> newObjectives = getObjective(a);
                        for (String newObjective : newObjectives) {
                            if (!this.objectives.contains(newObjective)) {
                                this.objectives.add(newObjective);
                                this.objectiveID = newObjective;
                                this.shortestPath = 0;
                                for (int i = 0; i < 10; i++) {
                                    Ant b = this.cicle(i);
                                    if (b != null && a != null) {
                                        a.joinObjectiveGraphs(b, sourcesList);
                                        this.shortestPath = a.getFlux();
                                    }
                                }

                            }
                        }
                    }
                    return a;
                }
            }
        }
        return null;
    }

    private List<String> getPossibleReactions(String node) {

        List<String> possibleReactions = new ArrayList<>();
        SpeciesFA sp = this.compounds.get(node);
        Ant ant = sp.getAnt();
        if (!this.sources.containsKey(node) && ant == null) {
            return possibleReactions;
        }

        List<String> connectedReactions = sp.getReactions();
        for (String reaction : connectedReactions) {
            if (reaction.contains(this.biomassReactionID)) {
                continue;
            }
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
        } else if (species.contains("s_1198")|| species.contains("s_0434")) {
            return true;
        }
        return false;
    }

    private List<String> getObjective(Ant a) {
        List<String> newObjectives = new ArrayList<>();
        for (SpeciesReference sp : this.biomassReaction.getListOfReactants()) {
            if (a.contains(sp.getSpecies())) {
                newObjectives.add(sp.getSpecies());
            }
        }
        return newObjectives;
    }

}
