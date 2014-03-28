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
package ND.modules.simulation.antNoGraph;

import ND.modules.simulation.antNoGraph.network.Edge;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.List;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;

/**
 *
 * @author scsandra
 */
public class PrintPaths {

        private List<String> initialIds;
        private String finalId;

        public PrintPaths(List<String> initialIds, String finalId) {
                this.initialIds = initialIds;
                this.finalId = finalId;
        }

        public VisualizationViewer printPathwayInFrame(Graph graph) {
                edu.uci.ics.jung.graph.Graph<String, String> g = new SparseMultigraph<>();
                List<Node> nodes = graph.getNodes();
                System.out.println("Number of nodes: "+ nodes.size() + " - " + graph.getEdges().size());
                List<Edge> edges = graph.getEdges();
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

System.out.println("1");
                Layout<String, String> layout = new KKLayout(g);
                layout.setSize(new Dimension(1400, 1000)); // sets the initial size of the space
                VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout);
                vv.setPreferredSize(new Dimension(1400, 1000));
                Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
                        @Override
                        public Paint transform(String id) {                                
                                if (initialIds != null && initialIds.contains(id.replace("sp:","").split(" - ")[0])) {
                                        return Color.BLUE;
                                } else if (finalId != null && id.split("-")[0].contains(finalId)) {
                                        return Color.RED;
                                } else if (id.contains("sp:")) {
                                        return Color.RED;
                                } else {
                                        return Color.GREEN;
                                }
                        }
                };
System.out.println("2");
                float dash[] = {1.0f};
                final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                Transformer<String, Stroke> edgeStrokeTransformer =
                        new Transformer<String, Stroke>() {
                        @Override
                        public Stroke transform(String s) {
                                return edgeStroke;
                        }
                };

                Transformer labelTransformer = new ChainedTransformer<>(new Transformer[]{
                        new ToStringLabeller<>(),
                        new Transformer<String, String>() {
                                @Override
                                public String transform(String input) {
                                        return "<html><b><font color=\"red\">" + input;
                                }
                        }});
                Transformer labelTransformer2 = new ChainedTransformer<>(new Transformer[]{
                        new ToStringLabeller<>(),
                        new Transformer<String, String>() {
                                @Override
                                public String transform(String input) {
                                        return "<html><b><font color=\"black\">" + input;
                                }
                        }});
System.out.println("3");
                vv.getRenderContext().setVertexLabelTransformer(labelTransformer2);
                vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
                vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
                vv.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);
                vv.getRenderContext().setEdgeLabelTransformer(labelTransformer);
                vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
                DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
                gm.setMode(ModalGraphMouse.Mode.PICKING);
                vv.setGraphMouse(gm);
                vv.addKeyListener(gm.getModeKeyListener());
                return vv;
        }
}
