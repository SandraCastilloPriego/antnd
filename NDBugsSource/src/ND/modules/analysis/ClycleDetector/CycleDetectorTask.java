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
package ND.modules.analysis.ClycleDetector;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class CycleDetectorTask extends AbstractTask {

        private final SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private final JInternalFrame frame;
        private final JScrollPane panel;
        private final JTextArea tf;
        private final StringBuffer info;

        public CycleDetectorTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                networkDS = dataset;
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
                        DirectedGraph g = null;
                        if (graph == null) {
                                g = this.getGraphForClustering(this.networkDS.getDocument().getModel());
                        } else {
                                g = this.getGraphForClustering(graph);
                        }
                        CycleDetector cyclesDetector = new CycleDetector(g);
                        Set<Node> cycles = cyclesDetector.findCycles();

                        for (Node n : cycles) {
                                info.append(n.getId()).append("\n");
                        }

                        this.tf.setText(info.toString());
                        frame.setSize(new Dimension(700, 500));
                        frame.add(this.panel);
                        NDCore.getDesktop().addInternalFrame(frame);

                        finishedPercentage = 1.0f;
                        setStatus(TaskStatus.FINISHED);
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        public DirectedGraph getGraphForClustering(Graph graph) {

                DirectedGraph<Node, Edge> jgraph = new DirectedMultigraph<>(new ClassBasedEdgeFactory<Node, Edge>(Edge.class));

                List<Node> nodes = graph.getNodes();
                List<Edge> edges = graph.getEdges();
                System.out.println("Number of nodes: " + nodes.size() + " - " + edges.size());

                for (Node node : nodes) {
                        if (node != null) {
                                jgraph.addVertex(node);
                        }
                }

                for (Edge edge : edges) {
                        if (edge != null) {
                                jgraph.addEdge(edge.getSource(), edge.getDestination());
                        }
                }
                return jgraph;
        }

        public DirectedGraph getGraphForClustering(Model model) {

                DirectedGraph<String, Edge> jgraph = new DirectedMultigraph<>(new ClassBasedEdgeFactory<String, Edge>(Edge.class));

                for (Species sp : model.getListOfSpecies()) {
                        jgraph.addVertex(sp.getId());
                }
                
                HashMap<String, String[]> bounds = new GetInfoAndTools().readBounds(this.networkDS);
                
                for (Reaction r : model.getListOfReactions()) {
                      String[] boundR = bounds.get(r.getId());
                      if(Double.valueOf(boundR[3]) < 0){
                              List<SpeciesReference> products = r.getListOfReactants();
                              
                      }
                }
                return jgraph;
        }
        

}
