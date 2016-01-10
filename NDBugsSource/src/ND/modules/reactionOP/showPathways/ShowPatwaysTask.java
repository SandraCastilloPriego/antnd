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
package ND.modules.reactionOP.showPathways;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.desktop.impl.PrintPaths;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.awt.Dimension;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
public class ShowPatwaysTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final String pathwayName;
    private double finishedPercentage = 0.0f;
    private final JInternalFrame frame;
    private final JScrollPane panel;
    private final JPanel pn;
    private final GetInfoAndTools tools;

    public ShowPatwaysTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        networkDS = dataset;
        this.pathwayName = parameters.getParameter(ShowPathwaysParameters.pathwayName).getValue();
        this.frame = new JInternalFrame("Result", true, true, true, true);
        this.pn = new JPanel();
        this.panel = new JScrollPane(pn);
        this.tools = new GetInfoAndTools();
    }

    @Override
    public String getTaskDescription() {
        return "Showing pathway... ";
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

            Map<String, List<String>> pathways = this.tools.GetPathwayInfo();
            if (pathways.containsKey(this.pathwayName)) {
                List<String> reactions = pathways.get(this.pathwayName);
                SBMLDocument doc = this.networkDS.getDocument();
                Model m = doc.getModel();
                List<Reaction> possibleReactions = new ArrayList<>();
                for (Reaction r : m.getListOfReactions()) {
                    for (String names : reactions) {
                        if (r.getId().contains(names)) {
                            possibleReactions.add(r);
                        }
                    }
                }

                if (possibleReactions.isEmpty()) {
                    NDCore.getDesktop().displayMessage("This model doesn't have any kegg reacction belonging to this pathway");
                } else {
                    Graph graph = createGraph(possibleReactions);
                    this.tools.createDataFile(graph, networkDS, this.pathwayName, null, false);
                    PrintPaths print = new PrintPaths(null, this.pathwayName, this.tools.getModel());
                    try {
                        this.pn.add(print.printPathwayInFrame(graph));
                        frame.setSize(new Dimension(700, 500));
                        frame.add(this.panel);
                        NDCore.getDesktop().addInternalFrame(frame);
                    } catch (NullPointerException ex) {
                        System.out.println(ex.toString());
                    }
                }
            }
            finishedPercentage = 1.0f;
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }

    private Graph createGraph(List<Reaction> possibleReactions) {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<String, Node> nodesName = new HashMap<>();
        for (Reaction r : possibleReactions) {
            double lb = Double.NEGATIVE_INFINITY;
            double ub = Double.POSITIVE_INFINITY;
            if (r.getKineticLaw() != null) {
                KineticLaw law = r.getKineticLaw();
                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                lb = lbound.getValue();
                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                ub = ubound.getValue();
            }
            boolean direction = false;
            if (lb == 0 || ub == 0) {
                direction = true;
            }
            Node reactionName = new Node(r.getId() + " - " + uniqueId.nextId());
            nodes.add(reactionName);
            for (SpeciesReference spref : r.getListOfReactants()) {
                Species sp = spref.getSpeciesInstance();
                Node spName;
                if (nodesName.containsKey(sp.getId())) {
                    spName = nodesName.get(sp.getId());
                } else {
                    spName = new Node(sp.getId() + " : " + sp.getName() + " - " + uniqueId.nextId());
                    nodesName.put(sp.getId(), spName);
                    nodes.add(spName);
                }
                if (lb == 0) {
                    Edge edge = new Edge(sp.getId() + " - " + uniqueId.nextId(), spName, reactionName, direction);
                    edges.add(edge);
                } else {
                    Edge edge = new Edge(sp.getId() + " - " + uniqueId.nextId(), reactionName, spName, direction);
                    edges.add(edge);
                }
            }

            for (SpeciesReference spref : r.getListOfProducts()) {
                Species sp = spref.getSpeciesInstance();
                Node spName;
                if (nodesName.containsKey(sp.getId())) {
                    spName = nodesName.get(sp.getId());
                } else {
                    spName = new Node(sp.getId() + " : " + sp.getName() + " - " + uniqueId.nextId());
                    nodesName.put(sp.getId(), spName);
                    nodes.add(spName);
                }
                
                if (lb == 0) {
                    Edge edge = new Edge(sp.getId() + " - " + uniqueId.nextId(), reactionName, spName, direction);
                    edges.add(edge);
                } else {
                    Edge edge = new Edge(sp.getId() + " - " + uniqueId.nextId(), spName, reactionName, direction);
                    edges.add(edge);

                }
            }
        }
        Graph g = new Graph(nodes,edges);
        return g;
    }

}
