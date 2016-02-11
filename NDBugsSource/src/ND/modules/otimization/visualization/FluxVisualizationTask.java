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
package ND.modules.otimization.visualization;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.desktop.impl.PrintPaths;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.FBA.Ant;
import ND.modules.simulation.FBA.SpeciesFA;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
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
public class FluxVisualizationTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final double finishedPercentage = 0.0f;
    private final HashMap<String, ReactionFA> reactions;
    private final HashMap<String, SpeciesFA> compounds;
    private final String objective;
    private final GetInfoAndTools tools;
    private final JInternalFrame frame;
    private final JScrollPane panel;
    private final JPanel pn;

    public FluxVisualizationTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.objective = parameters.getParameter(FluxVisualizationParameters.objective).getValue();
        this.reactions = this.networkDS.getReactionsFA();
        this.compounds = this.networkDS.getPaths();
        if (reactions == null || compounds == null) {
            setStatus(TaskStatus.CANCELED);
            NDCore.getDesktop().displayErrorMessage("The selected dataset doesn't contain any flux calculation.");
        }
        this.tools = new GetInfoAndTools();

        this.frame = new JInternalFrame("Result", true, true, true, true);
        this.pn = new JPanel();
        this.panel = new JScrollPane(pn);

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
            Graph g = createGraph(this.compounds.get(this.objective).getAnt().getPath());
            setStatus(TaskStatus.FINISHED);

            this.tools.createDataFile(g, networkDS, this.objective, this.networkDS.getSources(), false);
            //  LinearProgramming lp = new LinearProgramming(graph, objectiveID, sources, reactions);
            //System.out.println("Solution: " + lp.getObjectiveValue());
            frame.setSize(new Dimension(700, 500));
            frame.add(this.panel);
            NDCore.getDesktop().addInternalFrame(frame);

            PrintPaths print = new PrintPaths(this.networkDS.getSources(), this.objective, this.tools.getModel());
            try {
                System.out.println("Final graph: " + g.toString());
                this.pn.add(print.printPathwayInFrame(g));
            } catch (NullPointerException ex) {
                System.out.println("Imprimendo: " + ex.toString());
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

    private Graph createGraph(Map<String, Boolean> path) {
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

}