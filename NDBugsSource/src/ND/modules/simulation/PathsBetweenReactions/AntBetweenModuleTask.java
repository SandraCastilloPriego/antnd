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
package ND.modules.simulation.PathsBetweenReactions;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.desktop.impl.PrintPaths;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.antNoGraph.*;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
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
public class AntBetweenModuleTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final HashMap<String, String[]> bounds;
    private double finishedPercentage = 0.0f;
    private final String biomassID, sourceID, excluded;
    private final Random rand;
    private HashMap<String, ReactionFA> reactions;
    private HashMap<String, ND.modules.simulation.FBA.SpeciesFA> compounds;
    private final List<String> sourcesList, cofactors;
    private final JInternalFrame frame;
    private final JScrollPane panel;
    private final JPanel pn;
    private int shortestPath = Integer.MAX_VALUE;
    private ND.modules.simulation.FBA.Ant ant;
    private final GetInfoAndTools tools;
    private Map<String, Double[]> sources;
    private Model m;

    public AntBetweenModuleTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.biomassID = parameters.getParameter(AntBetweenModuleParameters.objectiveReaction).getValue();
        this.sourceID = parameters.getParameter(AntBetweenModuleParameters.sourceReaction).getValue();
        this.excluded = parameters.getParameter(AntBetweenModuleParameters.excluded).getValue();

        this.rand = new Random();
        Date date = new Date();
        long time = date.getTime();

        this.reactions = new HashMap<>();
        this.compounds = new HashMap<>();

        this.sourcesList = new ArrayList<>();
        this.cofactors = new ArrayList<>();

        this.frame = new JInternalFrame("Result", true, true, true, true);
        this.pn = new JPanel();
        this.panel = new JScrollPane(pn);

        this.tools = new GetInfoAndTools();
        this.bounds = tools.readBounds(networkDS);
        this.sources = tools.GetSourcesInfo();
        // Initialize the random number generator using the
        // time from above.
        rand.setSeed(time);

    }

    @Override
    public String getTaskDescription() {
        return "Starting Ant Simulation... ";
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

            String[] excludedCompounds = this.excluded.split(",");
            for (String cofactor : excludedCompounds) {
                this.cofactors.add(cofactor);
                this.sourcesList.add(cofactor);
            }
            this.sourcesList.add(sourceID);

            System.out.println("Creating world");
            this.createWorld();
            System.out.println("Starting simulation");
            frame.setSize(new Dimension(700, 500));
            frame.add(this.panel);
            NDCore.getDesktop().addInternalFrame(frame);

            for (int i = 0; i < 15; i++) {
                this.cicle();
                finishedPercentage = (double) i / 15;
                if (getStatus() == TaskStatus.CANCELED || getStatus() == TaskStatus.ERROR) {
                    break;
                }
            }
            if (getStatus() == TaskStatus.PROCESSING) {
                this.ant = this.compounds.get(this.biomassID).getAnt();
                Graph g = this.createGraph(ant.getPath());
                this.tools.createDataFile(g, networkDS, biomassID, sourcesList, false);
                PrintPaths print = new PrintPaths(this.tools.getModel());
                try {
                    this.pn.add(print.printPathwayInFrame(g));
                } catch (NullPointerException ex) {
                    System.out.println(ex.toString());
                }
            }
            if (this.ant == null) {
                NDCore.getDesktop().displayMessage("No path was found.");
            }

            setStatus(TaskStatus.FINISHED);

        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

    private void createWorld() {
        SBMLDocument doc = this.networkDS.getDocument();
        Model m = doc.getModel();
        for (Species s : m.getListOfSpecies()) {
            ND.modules.simulation.FBA.SpeciesFA specie = new ND.modules.simulation.FBA.SpeciesFA(s.getId(), s.getName());
            this.compounds.put(s.getId(), specie);

            if (this.sourceID.equals(s.getId())) {
                ND.modules.simulation.FBA.Ant ant = new ND.modules.simulation.FBA.Ant(specie.getId());
                ant.initAnt(-1);
                specie.addAnt(ant);
                this.sourcesList.add(s.getId());
            }
        }

        for (Reaction r : m.getListOfReactions()) {
            boolean biomass = false;

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

                reaction.addReactant(sp.getId(), sp.getName(), s.getStoichiometry());
                ND.modules.simulation.FBA.SpeciesFA spFA = this.compounds.get(sp.getId());
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

                if (sp.getName().contains("boundary") && reaction.getlb() < 0) {
                    ND.modules.simulation.FBA.SpeciesFA specie = this.compounds.get(sp.getId());
                    if (specie.getAnt() == null) {
                        ND.modules.simulation.FBA.Ant ant = new ND.modules.simulation.FBA.Ant(specie.getId());
                        Double[] sb = new Double[2];
                        sb[0] = reaction.getlb();
                        sb[1] = reaction.getub();
                        ant.initAnt(Math.abs(reaction.getlb()));
                        specie.addAnt(ant);
                        this.sourcesList.add(sp.getId());
                    }
                }

                reaction.addProduct(sp.getId(), sp.getName(), s.getStoichiometry());
                ND.modules.simulation.FBA.SpeciesFA spFA = this.compounds.get(sp.getId());
                if (spFA != null) {
                    spFA.addReaction(r.getId());
                } else {
                    System.out.println(sp.getId());
                }
            }

            if (r.getListOfProducts().isEmpty()) {
                for (SpeciesReference s : r.getListOfReactants()) {
                    Species sp = s.getSpeciesInstance();
                    ND.modules.simulation.FBA.SpeciesFA specie = this.compounds.get(sp.getId());
                    if (specie.getAnt() == null) {
                        ND.modules.simulation.FBA.Ant ant = new ND.modules.simulation.FBA.Ant(specie.getId());
                        Double[] sb = new Double[2];
                        sb[0] = reaction.getlb();
                        sb[1] = reaction.getub();
                        if (sb[0] > 0.0 && sb[1] > 0.0) {
                            ant.initAnt(Math.abs(reaction.getlb()));
                            specie.addAnt(ant);
                            this.sourcesList.add(sp.getId());
                        }
                    }
                }
            }
            this.reactions.put(r.getId(), reaction);
        }

        List<String> toBeRemoved = new ArrayList<>();
        for (String compound : compounds.keySet()) {
            if (compounds.get(compound).getReactions().isEmpty()) {
                toBeRemoved.add(compound);
            }
        }
        for (String compound : toBeRemoved) {
            this.compounds.remove(compound);
        }

    }

    public void cicle() {
        for (String compound : compounds.keySet()) {
            if (this.compounds.get(compound).getAnt() == null) {
                continue;
            }
            List<String> possibleReactions = getPossibleReactions(compound);

            for (String reactionChoosen : possibleReactions) {

                ReactionFA rc = this.reactions.get(reactionChoosen);
                Boolean direction = true;
                List<String> toBeAdded, toBeRemoved;
                if (rc.hasReactant(compound)) {
                    toBeAdded = rc.getProducts();
                    toBeRemoved = rc.getReactants();
                } else {
                    toBeAdded = rc.getReactants();
                    toBeRemoved = rc.getProducts();
                    direction = false;

                }

                // get the ants that must be removed from the reactants ..
                // creates a superAnt with all the paths until this reaction joined..
                //  List<List<Ant>> paths = new ArrayList<>();
                List<ND.modules.simulation.FBA.Ant> com = new ArrayList<>();
                for (String s : toBeRemoved) {
                    ND.modules.simulation.FBA.SpeciesFA spfa = this.compounds.get(s);
                    if (spfa.getAnt() != null/* && !this.cofactors.contains(s)*/) {
                        com.add(spfa.getAnt());
                    }

                }

                ND.modules.simulation.FBA.Ant superAnt = new ND.modules.simulation.FBA.Ant(null);

                superAnt.joinGraphs(reactionChoosen, direction, com, rc);

                for (String s : toBeAdded) {
                    ND.modules.simulation.FBA.SpeciesFA spfa = this.compounds.get(s);
                    ND.modules.simulation.FBA.Ant newAnt = superAnt.clone();
                    if (!hasOutput(newAnt, spfa)) {
                        newAnt.setLocation(compound);
                        spfa.addAnt(newAnt);
                    }
                }

            }
        }
    }

    private boolean hasOutput(ND.modules.simulation.FBA.Ant ant, ND.modules.simulation.FBA.SpeciesFA sp) {
        boolean hasOutput = false;
        for (String r : ant.getPath().keySet()) {
            if (reactions.containsKey(r)) {
                ReactionFA reaction = this.reactions.get(r);
                if ((ant.getPath().get(r) && reaction.hasReactant(sp.getId())) || (!ant.getPath().get(r) && reaction.hasProduct(sp.getId()))) {
                    //if (!reaction.isBidirecctional()) {
                    hasOutput = true;
                    //}

                }
            }

        }
        return hasOutput;
    }

    private List<String> getPossibleReactions(String compound) {

        List<String> possibleReactions = new ArrayList<>();
        ND.modules.simulation.FBA.SpeciesFA sp = this.compounds.get(compound);
        ND.modules.simulation.FBA.Ant ant = sp.getAnt();
        if (!this.sourceID.equals(compound) && ant == null) {
            return possibleReactions;
        }

        List<String> connectedReactions = sp.getReactions();
        for (String reaction : connectedReactions) {

            ReactionFA r = this.reactions.get(reaction);
            if (r == null) {
                continue;
            }
            boolean isPossible = true;

            if (r.getlb() == 0 && r.getub() == 0) {
                isPossible = false;
            }

            if (r.hasReactant(compound)) {

                if (r.getub() > 0) {
                    List<String> reactants = r.getReactants();
                    boolean all = true;
                    for (String reactant : reactants) {

                        if (!allEnoughAnts(reactant, reaction)) {
                            isPossible = false;
                            break;
                        }

                        if (!cofactors.contains(reactant)) {
                            all = false;
                        }
                    }
                    if (all) {
                        isPossible = false;
                    }
                } else {
                    isPossible = false;
                }

            } else {
                if (r.getlb() < 0) {
                    List<String> products = r.getProducts();
                    boolean all = true;
                    for (String product : products) {
                        if (!allEnoughAnts(product, reaction)) {
                            isPossible = false;
                            break;
                        }

                        if (!cofactors.contains(product)) {
                            all = false;
                        }
                    }
                    if (all) {
                        isPossible = false;
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
        ND.modules.simulation.FBA.SpeciesFA s = this.compounds.get(species);
        ND.modules.simulation.FBA.Ant ant = s.getAnt();
        if (ant != null) {
            return !ant.contains(reaction);
        } else if (cofactors.contains(species)) {
            //this.objectives.add(species);           
            return true;
        }
        return false;
    }

    private Graph createGraph(Map<String, Boolean> path) {
        Graph g = new Graph(null, null);
        for (String r : path.keySet()) {
            ReactionFA reaction = reactions.get(r);
            if (reaction != null) {
                Node reactionNode = new Node(reaction.getId(), String.valueOf(reaction.getFlux()));
                g.addNode2(reactionNode);
                for (String reactant : reaction.getReactants()) {
                    ND.modules.simulation.FBA.SpeciesFA sp = compounds.get(reactant);
                    Node reactantNode = g.getNode(reactant);
                    if (reactantNode == null) {
                        reactantNode = new Node(reactant, sp.getName());
                    }
                    g.addNode2(reactantNode);
                    Edge e;
                    if (path.get(r)) {
                        e = new Edge(r + " - " + uniqueId.nextId(), reactantNode, reactionNode);
                    } else {
                        e = new Edge(r + " - " + uniqueId.nextId(), reactionNode, reactantNode);
                    }
                    g.addEdge(e);
                }
                for (String product : reaction.getProducts()) {
                    ND.modules.simulation.FBA.SpeciesFA sp = compounds.get(product);
                    Node reactantNode = g.getNode(product);
                    if (reactantNode == null) {
                        reactantNode = new Node(product, sp.getName());
                    }
                    g.addNode2(reactantNode);
                    Edge e;
                    if (path.get(r)) {
                        e = new Edge(r + " - " + uniqueId.nextId(), reactionNode, reactantNode);
                    } else {
                        e = new Edge(r + " - " + uniqueId.nextId(), reactantNode, reactionNode);
                    }
                    g.addEdge(e);
                }
            }
        }
        return g;
    }
}
