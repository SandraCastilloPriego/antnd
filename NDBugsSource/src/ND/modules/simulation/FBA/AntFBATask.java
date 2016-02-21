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

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.network.Edge;
import ND.desktop.impl.PrintPaths;
import ND.main.NDCore;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.data.parser.impl.BasicFilesParserSBML;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;

/**
 *
 * @author scsandra
 */
public class AntFBATask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private double finishedPercentage = 0.0f;

    //private final String objectiveID;
    private final int iterations;
    private final Random rand;
    private HashMap<String, String[]> bounds;
    private Map<String, Double[]> sources;
    private final List<String> objectives;
    private final List<String> sourcesList;
    private final JInternalFrame frame;
    private final JScrollPane panel;
    private final JPanel pn;
    private Graph graph;
    private final GetInfoAndTools tools;
    private final Map<String, Ant> results;
    private final List<String> cofactors, cofactors2;
    private List<String> doneFixes;
    Simulation simulation;

    public AntFBATask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
     //   this.objectiveID = parameters.getParameter(AntFBAParameters.objectiveReaction).getValue();
        this.iterations = parameters.getParameter(AntFBAParameters.iterations).getValue();
        String cofactorsString = parameters.getParameter(AntFBAParameters.cofactors).getValue();
        this.cofactors = new ArrayList<>();
        for (String cofactor : cofactorsString.split(",")) {
            this.cofactors.add(cofactor.trim());
        }

        //   String cofactors2String = parameters.getParameter(AntFBAParameters.cofactors2).getValue();
        this.cofactors2 = new ArrayList<>();
        /**
         * for (String cofactor : cofactors2String.split(",")) {
         * this.cofactors2.add(cofactor.trim()); }
         */
        this.rand = new Random();
        Date date = new Date();
        long time = date.getTime();
        this.objectives = new ArrayList<>();
       // this.objectives.add(objectiveID);
        this.bounds = new HashMap<>();
        this.sourcesList = new ArrayList<>();
        this.results = new HashMap<>();
        this.doneFixes = new ArrayList<>();

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

        if (this.networkDS == null) {
            setStatus(TaskStatus.ERROR);
            NDCore.getDesktop().displayErrorMessage("You need to select a metabolic model.");
        }
        System.out.println("Reading sources");
        this.sources = tools.GetSourcesInfo();
        System.out.println("Reading bounds");
        this.bounds = tools.readBounds(networkDS);

        /*   BasicFilesParserSBML parser = new BasicFilesParserSBML("/home/scsandra/Documents/CellFactory2015/YeastModel/yeast_7.00/ymn.sbml");
         parser.createDataset("ymn2.smbl");
         SimpleBasicDataset dataset = (SimpleBasicDataset) parser.getDataset();
         if (dataset.getDocument() != null) {
         NDCore.getDesktop().AddNewFile(dataset);
         }                               
                                
       
         Simulation simulation2 = new Simulation(dataset, this.cofactors, this.bounds, this.sources, this.sourcesList);
         simulation2.createWorld();
         Map<String, ReactionFA>  reactions = simulation2.getReactions();
         reactions.put("Ex_s_1203", this.createReaction("s_1203"));
         reactions.put("Ex_s_1198", this.createReaction("s_1198"));
         reactions.put("Ex_s_0394", this.createReaction("s_0394"));
         reactions.put("Ex_s_0434", this.createReaction("s_0434"));
         reactions.put("Ex_s_0794", this.createReaction("s_0794"));
         reactions.put("Ex_s_0383", this.createReaction("s_0383"));
         reactions.put("Ex_s_0796", this.createReaction("s_0796"));
        
         LinearProgramming lp = new LinearProgramming(simulation2.getCompounds(), simulation2.getReactions());
        
         */
        simulation = new Simulation(this.networkDS, this.cofactors, this.cofactors2, this.bounds, this.sources, this.sourcesList, null);

        System.out.println("Creating world");
        simulation.createWorld();
        System.out.println("Starting simulation");

        //run(this.objectiveID);
        for (int i = 0; i < this.iterations; i++) {
            simulation.cicle();
            finishedPercentage = (double) i / this.iterations;
            if (getStatus() == TaskStatus.CANCELED || getStatus() == TaskStatus.ERROR) {
                break;
            }
        }
        for (String compound : simulation.getCompounds().keySet()) {
            SpeciesFA specie = simulation.getCompounds().get(compound);
            if (specie.getAnt() != null) {
                simulation.getFlux(specie.getAnt(), compound, true, false);
            }
        }
        
       // simulation.getFlux(simulation.getCompounds().get("s_0075").getAnt(), "s_0075", true, true);
        
//        String results = "";
//        for (String c : compounds.keySet()) {
//            SpeciesFA compoundr = compounds.get(c);
//            if (compoundr.getAnt() != null) {
//                results += c + " : " + compoundr.getName() + " --> " + simulation.getFlux(compound.getAnt(), compoundr.getId(), true, false) /* compound.getAnt().getFlux()*/ + "\n";
//            } else {
//                results += c + " : " + compoundr.getName() + "\n";
//            }
//        }
//        this.networkDS.addInfo(results);
        this.networkDS.setPaths(simulation.getCompounds());
        this.networkDS.setReactionsFA(simulation.getReactions());

         this.analyzeResults();
//        if (getStatus() == TaskStatus.PROCESSING) {
//
//            /*   List<String> deadEnds = this.graph.getDeadEnds();
//             if (deadEnds.size() > 0) {
//             this.doneFixes.add(deadEnds.get(0));
//             this.fixDeadEnds(deadEnds.get(0));
//             }*/
//            this.tools.createDataFile(graph, networkDS, objectiveID, sourcesList, false);
//            //  LinearProgramming lp = new LinearProgramming(graph, objectiveID, sources, reactions);
//            //System.out.println("Solution: " + lp.getObjectiveValue());
//            frame.setSize(new Dimension(700, 500));
//            frame.add(this.panel);
//            NDCore.getDesktop().addInternalFrame(frame);
//
//            PrintPaths print = new PrintPaths(this.sourcesList, this.objectives.get(0), this.tools.getModel());
//            try {
//                System.out.println("Final graph: " + this.graph.toString());
//                this.pn.add(print.printPathwayInFrame(this.graph));
//            } catch (NullPointerException ex) {
//                System.out.println("Imprimendo: " + ex.toString());
//            }
//        }
//        if (this.graph == null) {
//            NDCore.getDesktop().displayMessage("No path was found.");
//        }
        setStatus(TaskStatus.FINISHED);

        /*  } catch (Exception e) {
         System.out.println(e.toString());
         setStatus(TaskStatus.ERROR);
         }*/
    }

    /* private void run(String objective) {
     for (int i = 0; i < this.iterations; i++) {
     simulation.cicle(objective);
     //finishedPercentage = (double) i / this.iterations;
     if (getStatus() == TaskStatus.CANCELED || getStatus() == TaskStatus.ERROR) {
     break;
     }
     }
     Ant result = simulation.getResult();
     if (result != null) {
     this.results.put(objective, result);
     }

     for (String cofactor : this.cofactors) {
     for (String nodes : result.getPath()) {
     if (nodes.contains(cofactor)) {
     if (!this.objectives.contains(cofactor)) {
     this.objectives.add(cofactor);
     simulation.reset();
     run(cofactor);
     }
     break;
     }
     }

     }

     }*/
    /*   private void fixDeadEnds(String deadEnd) {
     String id = deadEnd.split(" : ")[0];
     Map<String, SpeciesFA> compounds = simulation.getCompounds();
     System.out.println("id:" + id);
     Ant selectedAnt = null;
     for (String sp : compounds.keySet()) {
     SpeciesFA c = compounds.get(sp);
     Ant ant = c.getAnt();
     if (ant != null) {
     if (ant.getGraph().IsInSource(id)) {
     if (selectedAnt == null || ant.getPathSize() < selectedAnt.getPathSize()) {
     selectedAnt = ant;
     }
     }
     }
     }
     System.out.println("Boundary:");
     if (selectedAnt != null) {
     selectedAnt.print();
     this.graph.addGraph(selectedAnt.getGraph());

     }
     List<String> deadEnds = this.graph.getDeadEnds();
     System.out.println(deadEnds.size());
     List<String> toRemove = new ArrayList<>();
     for (String dead : deadEnds) {
     System.out.println(dead);
     if (dead.contains(id)) {
     toRemove.add(dead);
     }
     }
     for (String r : toRemove) {
     deadEnds.remove(r);
     }
     for (String r : doneFixes) {
     deadEnds.remove(r);
     }
     if (deadEnds.size() > 0) {
     System.out.println(deadEnds.size());

     if (deadEnds.size() > 0) {
     this.doneFixes.add(deadEnds.get(0));
     this.fixDeadEnds(deadEnds.get(0));
     }

     }

     }
     */
    private void analyzeResults() {
        Map<String, SpeciesFA> compounds = this.simulation.getCompounds();
       /* SpeciesFA compound = compounds.get(this.objectiveID);

//        List<String> path = compound.combinePahts();
        Map<String, Boolean> path = compound.getShortest();
        for (String p : path.keySet()) {
            System.out.println(p);
        }*/
       // System.out.println(simulation.getFlux(compound.getAnt(), objectiveID, true, false));
        //System.out.println(compound.getAnt().getFlux());
        String results = "";
        for (String c : compounds.keySet()) {
            SpeciesFA compoundr = compounds.get(c);
            if (compoundr.getAnt() != null) {
                results += c + " : " + compoundr.getName() + " --> " + simulation.getFlux(compoundr.getAnt(), compoundr.getId(), true, false) /* compound.getAnt().getFlux()*/ + "\n";
            } else {
                results += c + " : " + compoundr.getName() + "\n";
            }
        }
        this.networkDS.addInfo(results);
        //     System.out.println(simulation.getFlux(compound.getAnt(), "s_0568"));
        //    System.out.println(simulation.getFlux(compound.getAnt(), "s_0555"));
        //     System.out.println(simulation.getFlux(compound.getAnt(), "s_0075"));
      /*  Simulation newSimulation = new Simulation(this.networkDS, this.cofactors, this.bounds, this.sources, this.sourcesList);
         newSimulation.createWorld(compound.getCombinedAnts(), "s_0629");
         // this.simulation.createWorld(compound.getCombinedAnts(), "s_0629");
         for (int i = 0; i < this.iterations; i++) {
         newSimulation.cicle();
         }
         compounds = newSimulation.getCompounds();
         compound = compounds.get(this.objectiveID);

         //        List<String> path = compound.combinePahts();
         List<String> path2 = compound.getShortest();
        
         List<String> finalPath = this.combinePahts(path, path2);*/

       // this.graph = createGraph(path);

        //this.getVertices(compound);

        /* for (Ant ant : compound.getAnts()) {
         ant.print();
         }*/
    }

    private Graph createGraph(Map<String, Boolean> path) {
        Map<String, ReactionFA> reactions = this.simulation.getReactions();
        Map<String, SpeciesFA> compounds = this.simulation.getCompounds();
        Graph g = new Graph(null, null);
        for (String r : path.keySet()) {
            ReactionFA reaction = reactions.get(r);
            if (reaction != null) {
                Node reactionNode = new Node(reaction.getId(), String.valueOf(reaction.getFlux()));
                g.addNode2(reactionNode);
                for (String reactant : reaction.getReactants()) {
                    SpeciesFA sp = compounds.get(reactant);
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
                    SpeciesFA sp = compounds.get(product);
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

    /* public List<Vertex> getVertices(SpeciesFA compound) {
     Map<String, ReactionFA> reactions = this.simulation.getReactions();
     Map<String, Vertex> verticesMap = new HashMap<>();
     List<Vertex> vertices = new ArrayList<>();

     for (String r : compound.getAnt().getPath()) {
     Vertex vr = new Vertex(r);
     vertices.add(vr);
     verticesMap.put(r, vr);

     }
     for (String r : compound.getAnt().getPath()) {
     ReactionFA reaction = reactions.get(r);
     if (reaction != null) {
     for (String reactants : reaction.getReactants()) {
     Vertex reactant = verticesMap.get(reactants);
     reactant.adjacencies.add(new DijktraEdge(verticesMap.get(r), 1.0));
     }

     for (String product : reaction.getProducts()) {
     Vertex reactionVertex = verticesMap.get(r);
     reactionVertex.adjacencies.add(new DijktraEdge(verticesMap.get(product), 1.0));
     }
     }
     }
     Dijkstra.computePaths(verticesMap.get("s_0629"));
     Dijkstra.getShortestPathTo(verticesMap.get("s_1360"));
     return vertices;
     }*/
    public List<String> combinePahts(List<String> path1, List<String> path2) {
        List<String> combined = new ArrayList<>();
        for (String path : path1) {
            if (!combined.contains(path)) {
                combined.add(path);

            }

        }
        for (String path : path2) {
            if (!combined.contains(path)) {
                combined.add(path);

            }

        }
        return combined;
    }
}
