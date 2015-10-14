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
package ND.modules.analysis.ClusteringKmeans;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import edu.uci.ics.jung.algorithms.cluster.VoltageClusterer;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.awt.Dimension;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author scsandra
 */
public class ClusteringTask extends AbstractTask {

        private final SimpleBasicDataset networkDS;
        private final int numberOfClusters;
        private double finishedPercentage = 0.0f;
        private final JInternalFrame frame;
        private final JScrollPane panel;
        private final JTextArea tf;
        private final StringBuffer info;

        public ClusteringTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                networkDS = dataset;
                this.numberOfClusters = parameters.getParameter(ClusteringParameters.numberOfClusters).getValue();
                this.frame = new JInternalFrame("Result", true, true, true, true);
                this.tf = new JTextArea();
                this.panel = new JScrollPane(this.tf);
                this.info = new StringBuffer();
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
                        VoltageClusterer cluster = new VoltageClusterer(g, this.numberOfClusters);
                        Collection<Set<String>> result = cluster.cluster(numberOfClusters);
                        int i = 1;
                        for (Set<String> clust : result) {
                                info.append("Cluster ").append(i++).append(":\n");
                                for (String nodes : clust) {
                                        info.append(nodes).append("\n");
                                }
                        }
                        this.tf.setText(info.toString());
                        frame.setSize(new Dimension(700, 500));
                        frame.add(this.panel);
                        NDCore.getDesktop().addInternalFrame(frame);

                        createDataSet(result);

                        finishedPercentage = 1.0f;
                        setStatus(TaskStatus.FINISHED);
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        public edu.uci.ics.jung.graph.Graph<String, String> getGraphForClustering(Graph graph) {
                edu.uci.ics.jung.graph.Graph<String, String> g = new SparseMultigraph<>();

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

        private void createDataSet(Collection<Set<String>> result) {
                Graph graph = this.networkDS.getGraph().clone();
                List<Node> nodes = graph.getNodes();

                for (Node n : nodes) {
                        int cluster = getClusterNumber(n.getId(), result);
                        n.setId(n.getId() + " - " + cluster);
                }

                SimpleBasicDataset dataset = new GetInfoAndTools().createDataFile(graph, this.networkDS, this.networkDS.getBiomassId(), this.networkDS.getSources(), true);
                dataset.addInfo(this.info.toString());
        }

        private int getClusterNumber(String id, Collection<Set<String>> result) {
                int i = 0;
                for (Set<String> set : result) {
                        if (set.contains(id)) {
                                return i;
                        }
                        i++;
                }
                return -1;
        }
}
