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
package ND.modules.analysis.kNeighborhood;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class KNeighborhoodTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final int radiusk;
    private final String rootNode;
    private double finishedPercentage = 0.0f;
    private final List<String> cofactors;

    public KNeighborhoodTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        networkDS = dataset;
        this.radiusk = parameters.getParameter(KNeighborhoodParameters.radiusK).getValue();
        this.rootNode = parameters.getParameter(KNeighborhoodParameters.rootNode).getValue();
        String excluded = parameters.getParameter(KNeighborhoodParameters.excluded).getValue();
        this.cofactors = new ArrayList<>();
        String[] excludedCompounds = excluded.split(",");
        for (String cofactor : excludedCompounds) {
            this.cofactors.add(cofactor);
        }

    }

    @Override
    public String getTaskDescription() {
        return "Clustering... ";
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
            
            Graph graph = this.networkDS.getGraph();
            if (graph == null) {
                graph = createGraph();
            }

            edu.uci.ics.jung.graph.Graph<String, String> g = this.getGraphForClustering(graph);
            String root = findTheRoot(this.rootNode, g);
            KNeighborhoodFilter filter = new KNeighborhoodFilter(root, this.radiusk, KNeighborhoodFilter.EdgeType.OUT);
            edu.uci.ics.jung.graph.Graph<String, String> g2 = filter.transform(g);

            createDataSet(g2);

            finishedPercentage = 1.0f;
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }

    public edu.uci.ics.jung.graph.Graph<String, String> getGraphForClustering(Graph graph) {
        edu.uci.ics.jung.graph.Graph<String, String> g = new DirectedSparseMultigraph<>();

        List<Node> nodes = graph.getNodes();
        List<Edge> edges = graph.getEdges();
        System.out.println("Number of nodes: " + nodes.size() + " - " + edges.size());

        for (Node node : nodes) {
            if (node != null) {
                g.addVertex(node.getId());
            }
        }

        for (Edge edge : edges) {
            if (edge != null) {
                g.addEdge(edge.getId(), edge.getSource().getId(), edge.getDestination().getId(), EdgeType.DIRECTED);
            }
        }
        return g;

    }

    private void createDataSet(edu.uci.ics.jung.graph.Graph<String, String> g) {
        Map<String, Node> nodesMap = new HashMap<>();
        List<Node> nodes = new ArrayList<>();
        for (String n : g.getVertices()) {
            Node node = new Node(n);
            nodes.add(node);
            nodesMap.put(n, node);
        }
        List<Edge> edges = new ArrayList<>();
        for (String n : g.getEdges()) {
            // System.out.println(g.toString());
            System.out.println(n + " - " + g.getSource(n) + " - " + g.getDest(n));
            Edge e = new Edge(n, nodesMap.get(g.getSource(n)), nodesMap.get(g.getDest(n)));
            edges.add(e);
        }
        Graph graph = new Graph(nodes, edges);
        new GetInfoAndTools().createDataFile(graph, this.networkDS, "KNeighbourhood", this.networkDS.getSources(), false);

    }

    private String findTheRoot(String rootNode, edu.uci.ics.jung.graph.Graph<String, String> g) {
        for (String s : g.getVertices()) {
            if (s.equals(rootNode)) {
                return s;
            }
        }
        return rootNode;
    }

    private Graph createGraph() {
        Model m = this.networkDS.getDocument().getModel();
        Graph g = new Graph(null, null);
        for (Reaction reaction : m.getListOfReactions()) {
            Node reactionNode = new Node(reaction.getId());
            g.addNode2(reactionNode);

            for (SpeciesReference reactant : reaction.getListOfReactants()) {
                Species sp = reactant.getSpeciesInstance();
                if (!this.cofactors.contains(sp.getId())) {
                    Node reactantNode = g.getNode(sp.getId());
                    if (reactantNode == null) {
                        reactantNode = new Node(sp.getId(), sp.getName());
                    }
                    g.addNode2(reactantNode);
                    Edge e = new Edge(reaction.getId() + " - " + uniqueId.nextId(), reactantNode, reactionNode);

                    g.addEdge(e);
                }
            }
            for (SpeciesReference product : reaction.getListOfProducts()) {
                Species sp = product.getSpeciesInstance();
                if (!this.cofactors.contains(sp.getId())) {
                    Node reactantNode = g.getNode(sp.getId());
                    if (reactantNode == null) {
                        reactantNode = new Node(sp.getId(), sp.getName());
                    }
                    g.addNode2(reactantNode);
                    Edge e = new Edge(reaction.getName() + " - " + uniqueId.nextId(), reactionNode, reactantNode);

                    g.addEdge(e);
                }
            }
        }

        return g;
    }
}
