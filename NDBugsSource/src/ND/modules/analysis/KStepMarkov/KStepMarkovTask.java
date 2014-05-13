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
package ND.modules.analysis.KStepMarkov;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.antNoGraph.network.Edge;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import edu.uci.ics.jung.algorithms.importance.KStepMarkov;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author scsandra
 */
public class KStepMarkovTask extends AbstractTask {

        private final SimpleBasicDataset networkDS;
        private final int K;      
        private double finishedPercentage = 0.0f;

        public KStepMarkovTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                networkDS = dataset;
                this.K = parameters.getParameter(KStepMarkovParameters.K).getValue();               
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

                        if (this.networkDS.getGraph() == null) {
                                setStatus(TaskStatus.ERROR);
                                NDCore.getDesktop().displayErrorMessage("This data set doesn't contain a valid graph.");
                        }
                        Graph graph = this.networkDS.getGraph();

                        edu.uci.ics.jung.graph.Graph<String, String> g = this.getGraphForClustering(graph);
                        KStepMarkov ranker = new KStepMarkov((DirectedSparseMultigraph) g, new HashSet<>(this.networkDS.getSources()), this.K, null);
                        ranker.evaluate();
                        ranker.printRankings(true, true);

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
                new GetInfoAndTools().createDataFile(graph, this.networkDS, this.networkDS.getBiomassId(), this.networkDS.getSources(), false);

        }

        private String findTheRoot(String rootNode, edu.uci.ics.jung.graph.Graph<String, String> g) {
                for (String s : g.getVertices()) {
                        if (s.contains(rootNode)) {
                                return s;
                        }
                }
                return rootNode;
        }
}
