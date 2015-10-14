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
package ND.desktop.impl;

import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.uniqueId;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class PrintPaths implements KeyListener {

        private final List<String> initialIds;
        private final String finalId;
        private final Model m;
        private TransFrame transFrame = null;
        private String selectedEdge;
        private edu.uci.ics.jung.graph.Graph<String, String> g;
        Map<String, Color> clusters;
        private boolean showInfo = false;
        private Graph graph;
        private JPanel pn;

        public PrintPaths(List<String> initialIds, String finalId, Model m) {
                this.m = m;
                this.initialIds = initialIds;
                this.finalId = finalId;
                this.clusters = new HashMap<>();
        }

        public VisualizationViewer printPathwayInFrame(Graph graph) {
                g = new SparseMultigraph<>();
                this.graph = graph;
                List<Node> nodes = graph.getNodes();
                List<Edge> edges = graph.getEdges();
                final Map<String, Color> colors = graph.getColors();

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

                Layout<String, String> layout = new KKLayout(g);
                layout.setSize(new Dimension(1400, 900)); // sets the initial size of the space
                VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout);
                vv.setPreferredSize(new Dimension(1400, 1000));
                Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
                        @Override
                        public Paint transform(String id) {

                                String name = id.split(" - ")[0];

                                if (id.contains("Ex_")) {
                                        return new Color(29, 140, 243);
                                } else if (colors.containsKey(name)) {
                                        return colors.get(name);
                                } else if (initialIds != null && initialIds.contains(id.replace("sp:", "").split(" - ")[0])) {
                                        return new Color(29, 140, 243);
                                } else if (finalId != null && id.split("-")[0].contains(finalId)) {
                                        return new Color(255, 0, 0);
                                } else if (id.contains("sp:")) {
                                        return Color.RED;
                                }

                                return new Color(156, 244, 125);

                        }
                };

                vv.addKeyListener(this);

                final PickedState<String> pickedState = vv.getPickedVertexState();
                pickedState.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                                Object subject = e.getItem();
                                if (subject instanceof String) {
                                        String vertex = (String) subject;

                                        if (pickedState.isPicked(vertex)) {
                                                selectedEdge = vertex;
                                                if (m != null && showInfo) {
                                                        if (vertex.contains(" / ")) {
                                                                vertex = vertex.split(" / ")[0];
                                                        }
                                                        transFrame = new TransFrame(vertex.replace("sp:", "").split(" - ")[0]);
                                                } else {
                                                        System.out.println("Vertex " + vertex
                                                                + " is now selected");
                                                }
                                        } else {
                                                selectedEdge = null;
                                                if (transFrame != null && showInfo) {
                                                        transFrame.setVisible(false);
                                                        transFrame.dispose();
                                                } else {
                                                        System.out.println("Vertex " + vertex
                                                                + " no longer selected");
                                                }
                                        }
                                }
                        }
                });

                final PickedState<String> pickedEdgeState = vv.getPickedEdgeState();
                pickedEdgeState.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                                Object subject = e.getItem();
                                if (subject instanceof String) {
                                        String edge = (String) subject;

                                        if (pickedEdgeState.isPicked(edge)) {
                                                selectedEdge = edge;
                                                if (m != null && showInfo) {
                                                        transFrame = new TransFrame(edge.replace("sp:", "").split(" - ")[0]);
                                                } else {
                                                        System.out.println("Edge " + edge
                                                                + " is now selected");
                                                }
                                        } else {
                                                selectedEdge = null;
                                                if (transFrame != null && showInfo) {
                                                        transFrame.setVisible(false);
                                                        transFrame.dispose();
                                                } else {
                                                        System.out.println("Edge " + edge
                                                                + " no longer selected");
                                                }
                                        }
                                }
                        }
                });

                float dash[] = {1.0f};
                final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                Transformer<String, Stroke> edgeStrokeTransformer
                        = new Transformer<String, Stroke>() {
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

                JPanel panel = new JPanel();
                final JButton button = new JButton("Show Node Info");
                button.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (showInfo == false) {
                                        showInfo = true;
                                        button.setText("Hide Node Info");
                                } else {
                                        showInfo = false;
                                        button.setText("Show Node Info");
                                }
                        }
                });
                panel.add(button);
                panel.setPreferredSize(new Dimension(150, 40));
                panel.setBackground(Color.WHITE);
                vv.add(panel);
                vv.setBackground(Color.WHITE);
                return vv;
        }

        public VisualizationViewer printClusteredPathwayInFrame(Graph graph) {
                g = new SparseMultigraph<>();

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

                Layout<String, String> layout = new KKLayout(g);
                layout.setSize(new Dimension(1400, 900)); // sets the initial size of the space
                VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout);
                vv.setPreferredSize(new Dimension(1400, 1000));
                Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
                        @Override
                        public Paint transform(String id) {
                                if (initialIds != null && initialIds.contains(id.replace("sp:", "").split(" - ")[0])) {
                                        return new Color(29, 140, 243);
                                } else if (finalId != null && id.split("-")[0].contains(finalId)) {
                                        return new Color(255, 0, 0);
                                } else {

                                        Random rand = new Random();
                                        String cluster = id.split(" - ")[2];
                                        if (cluster != null) {
                                                if (clusters.containsKey(cluster)) {
                                                        return clusters.get(cluster);
                                                } else {
                                                        float r = rand.nextFloat();
                                                        float g = rand.nextFloat();
                                                        float b = rand.nextFloat();
                                                        Color randomColor = new Color(r, g, b);
                                                        clusters.put(cluster, randomColor);
                                                        return randomColor;
                                                }
                                        }

                                }
                                return new Color(255, 255, 255);
                        }
                };

                vv.addKeyListener(this);

                final PickedState<String> pickedState = vv.getPickedVertexState();
                pickedState.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                                Object subject = e.getItem();
                                if (subject instanceof String) {
                                        String vertex = (String) subject;

                                        if (pickedState.isPicked(vertex)) {
                                                selectedEdge = vertex;
                                                if (m != null && showInfo) {
                                                        if (vertex.contains(" / ")) {
                                                                vertex = vertex.split(" / ")[0];
                                                        }
                                                        transFrame = new TransFrame(vertex.replace("sp:", "").split(" - ")[0]);
                                                } else {
                                                        System.out.println("Vertex " + vertex
                                                                + " is now selected");
                                                }
                                        } else {
                                                selectedEdge = null;
                                                if (transFrame != null && showInfo) {
                                                        transFrame.setVisible(false);
                                                        transFrame.dispose();
                                                } else {
                                                        System.out.println("Vertex " + vertex
                                                                + " no longer selected");
                                                }
                                        }
                                }
                        }
                });

                final PickedState<String> pickedEdgeState = vv.getPickedEdgeState();
                pickedEdgeState.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                                Object subject = e.getItem();
                                if (subject instanceof String) {
                                        String edge = (String) subject;

                                        if (pickedEdgeState.isPicked(edge)) {
                                                selectedEdge = edge;
                                                if (m != null && showInfo) {
                                                        transFrame = new TransFrame(edge.replace("sp:", "").split(" - ")[0]);
                                                } else {
                                                        System.out.println("Edge " + edge
                                                                + " is now selected");
                                                }
                                        } else {
                                                selectedEdge = null;
                                                if (transFrame != null && showInfo) {
                                                        transFrame.setVisible(false);
                                                        transFrame.dispose();
                                                } else {
                                                        System.out.println("Edge " + edge
                                                                + " no longer selected");
                                                }
                                        }
                                }
                        }
                });

                float dash[] = {1.0f};
                final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                Transformer<String, Stroke> edgeStrokeTransformer
                        = new Transformer<String, Stroke>() {
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

                JPanel panel = new JPanel();
                final JButton button = new JButton("Show Node Info");
                button.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (showInfo == false) {
                                        showInfo = true;
                                        button.setText("Hide Node Info");
                                } else {
                                        showInfo = false;
                                        button.setText("Show Node Info");
                                }
                        }
                });
                panel.add(button);
                panel.setPreferredSize(new Dimension(150, 40));
                panel.setBackground(Color.WHITE);
                vv.add(panel);
                vv.setBackground(Color.WHITE);
                return vv;
        }

        @Override
        public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\u0008' || e.getKeyChar() == '\u007F') {
                        if (this.selectedEdge != null) {
                                g.removeVertex(this.selectedEdge);
                        }
                }
                if (e.getKeyChar() == 'e') {
                        showReactions(this.selectedEdge);
                }
        }

        @Override
        public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '\u0008' || e.getKeyChar() == '\u007F') {
                        if (this.selectedEdge != null) {
                                g.removeVertex(this.selectedEdge);
                        }
                }

        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        private void showReactions(String node) {
                Model mInit = NDCore.getDesktop().getSelectedDataFiles()[0].getDocument().getModel();
                String spID = node.split(" - ")[0];

                Species sp = mInit.getSpecies(spID);
                if (sp == null) {
                        return;
                }
                Collection<String> Edges = g.getNeighbors(node);

                for (Reaction r : mInit.getListOfReactions()) {

                        if (r.hasReactant(sp) || r.hasProduct(sp)) {
                                double lb = Double.NEGATIVE_INFINITY, ub = Double.POSITIVE_INFINITY;
                                // read bounds to know the direction of the edges
                                if (r.getKineticLaw() != null) {
                                        KineticLaw law = r.getKineticLaw();
                                        LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                                        lb = lbound.getValue();
                                        LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                                        ub = ubound.getValue();
                                }

                                // adds the new reaction node with and edge from the extended node
                                String reactionName = r.getId() + " - " + uniqueId.nextId();
                                String initSPName = sp.getId() + " - " + uniqueId.nextId();
                                boolean isThere = false;
                                for (String readyEdges : Edges) {
                                        if (readyEdges.contains(r.getId())) {
                                                isThere = true;
                                                break;
                                        }
                                }

                                // adds the rest of the compounds in the reaction, the direction of the edges 
                                // should depend on the boundaries of the reaction
                                if (!isThere) {
                                        g.addVertex(reactionName);

                                        EdgeType eType = EdgeType.UNDIRECTED;
                                        for (SpeciesReference sr : r.getListOfReactants()) {
                                                Species sps = sr.getSpeciesInstance();
                                                String spName = sps.getId();
                                                if (lb == 0) {
                                                        eType = EdgeType.DIRECTED;
                                                }
                                                if (!spName.equals(spID)) {
                                                        String eName = spName + " - " + uniqueId.nextId();
                                                        String vName = eName + " - " + sps.getName();
                                                        g.addVertex(vName);
                                                        if (lb == 0) {
                                                                g.addEdge(eName, vName, reactionName, eType);
                                                        } else {
                                                                g.addEdge(eName, reactionName, vName, eType);
                                                        }
                                                } else {
                                                        if (lb == 0) {
                                                                g.addEdge(initSPName, node, reactionName, eType);
                                                        } else {
                                                                g.addEdge(initSPName, reactionName, node, eType);
                                                        }
                                                }

                                        }

                                        for (SpeciesReference sr : r.getListOfProducts()) {
                                                Species sps = sr.getSpeciesInstance();
                                                String spName = sps.getId();
                                                if (!spName.equals(spID)) {
                                                        String eName = spName + " - " + uniqueId.nextId();
                                                        String vName = eName + " - " + sps.getName();
                                                        g.addVertex(vName);
                                                        if (lb == 0) {
                                                                g.addEdge(eName, reactionName, vName, EdgeType.DIRECTED);
                                                        } else {
                                                                g.addEdge(eName, vName, reactionName, EdgeType.DIRECTED);
                                                        }
                                                } else {
                                                        if (lb == 0) {
                                                                g.addEdge(initSPName, reactionName, node, eType);

                                                        } else {
                                                                g.addEdge(initSPName, node, reactionName, eType);
                                                        }
                                                }

                                        }
                                }
                        }
                }

        }

}
