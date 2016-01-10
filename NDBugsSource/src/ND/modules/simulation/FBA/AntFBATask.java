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

/**
 *
 * @author scsandra
 */
public class AntFBATask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private double finishedPercentage = 0.0f;

    private final String objectiveID;
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
    private final List<String> cofactors;
    private List<String> doneFixes;
    Simulation simulation;

    public AntFBATask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.objectiveID = parameters.getParameter(AntFBAParameters.objectiveReaction).getValue();
        this.iterations = parameters.getParameter(AntFBAParameters.iterations).getValue();
        String cofactorsString = parameters.getParameter(AntFBAParameters.cofactors).getValue();
        this.cofactors = new ArrayList<>();
        for (String cofactor : cofactorsString.split(",")) {
            this.cofactors.add(cofactor.trim());
        }
        this.rand = new Random();
        Date date = new Date();
        long time = date.getTime();
        this.objectives = new ArrayList<>();
        this.objectives.add(objectiveID);
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
        simulation = new Simulation(this.networkDS, this.cofactors, this.bounds, this.sources, this.sourcesList);

        System.out.println("Creating world");
        simulation.createWorld();
        System.out.println("Starting simulation");
        frame.setSize(new Dimension(700, 500));
        frame.add(this.panel);
        NDCore.getDesktop().addInternalFrame(frame);

        run(this.objectiveID);

        if (getStatus() == TaskStatus.PROCESSING) {
            for (String obj : this.results.keySet()) {
                if (this.graph == null) {
                    this.graph = this.results.get(obj).getGraph();
                } else {
                    this.graph.addGraph(this.results.get(obj).getGraph());
                }
            }

            List<String> deadEnds = this.graph.getDeadEnds();
            if (deadEnds.size() > 0) {
                this.doneFixes.add(deadEnds.get(0));
                this.fixDeadEnds(deadEnds.get(0));
            }

            this.tools.createDataFile(graph, networkDS, objectiveID, sourcesList, false);
            //  LinearProgramming lp = new LinearProgramming(graph, objectiveID, sources, reactions);
            //System.out.println("Solution: " + lp.getObjectiveValue());
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

    private void run(String objective) {
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

        for (String obj : simulation.getNewObjectives()) {
            if (!this.objectives.contains(obj)) {
                this.objectives.add(obj);
                simulation.reset();
                run(obj);
            }
        }
    }

    private void fixDeadEnds(String deadEnd) {
        String id = deadEnd.split(" : ")[0];
        Map<String, SpeciesFA> compounds = simulation.getCompounds();
        System.out.println("id:" + id);
        Ant selectedAnt = null;
        for (String sp : compounds.keySet()) {
            SpeciesFA c = compounds.get(sp);
            Ant ant = c.getAnt();
            if (ant != null) {
                if (ant.getGraph().IsInSource(id)) {
                    if (selectedAnt == null || ant.contains("boundary") || ant.contains("extracellular")) {
                        if (selectedAnt == null || ant.getGraph().getDeadEnds().size() < selectedAnt.getGraph().getDeadEnds().size()) {
                            selectedAnt = ant;
                        }
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
}
