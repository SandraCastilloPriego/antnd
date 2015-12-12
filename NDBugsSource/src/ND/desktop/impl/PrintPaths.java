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
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
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
    private String selectedNode;
    private edu.uci.ics.jung.graph.Graph<String, String> g;
    Map<String, Color> clusters;
    private boolean showInfo = false;
    private Graph graph;
    private JPanel pn;
    private VisualizationViewer<String, String> vv;

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

        SpringLayout<String, String> layout = new SpringLayout<>(g);
        //layout = new KKLayout(g);       
        layout.setSize(new Dimension(1400, 900)); // sets the initial size of the space
        vv = new VisualizationViewer<>(layout);

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

        final PickedState<String> pickedState = vv.getPickedVertexState();
        pickedState.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object subject = e.getItem();
                if (subject instanceof String) {
                    String vertex = (String) subject;

                    if (pickedState.isPicked(vertex)) {
                        selectedNode = vertex;
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
                        selectedNode = null;
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
                        selectedNode = edge;
                        if (m != null && showInfo) {
                            transFrame = new TransFrame(edge.replace("sp:", "").split(" - ")[0]);
                        } else {
                            System.out.println("Edge " + edge
                                    + " is now selected");
                        }
                    } else {
                        selectedNode = null;
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

        vv.addKeyListener(this);

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
        vv = new VisualizationViewer<>(layout);
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

        final PickedState<String> pickedState = vv.getPickedVertexState();
        pickedState.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object subject = e.getItem();
                if (subject instanceof String) {
                    String vertex = (String) subject;

                    if (pickedState.isPicked(vertex)) {
                        selectedNode = vertex;
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
                        selectedNode = null;
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
                        selectedNode = edge;
                        if (m != null && showInfo) {
                            transFrame = new TransFrame(edge.replace("sp:", "").split(" - ")[0]);
                        } else {
                            System.out.println("Edge " + edge
                                    + " is now selected");
                        }
                    } else {
                        selectedNode = null;
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
        vv.addKeyListener(this);

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
            if (this.selectedNode != null) {
                g.removeVertex(this.selectedNode);
            }
        }
        if (e.getKeyChar() == 'e') {
            showReactions(this.selectedNode);
        }

        if (e.getKeyChar() == 'c') {
            removeCofactors();
        }
        
        if(e.getKeyChar() == 'l'){
            this.lock();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private void showReactions(String initialStringNode) {
        Collection<String> V = g.getVertices();

        Model mInit = NDCore.getDesktop().getSelectedDataFiles()[0].getDocument().getModel();

        String spID = initialStringNode;
        if (initialStringNode.contains(" - ")) {
            spID = initialStringNode.split(" - ")[0];
        }
        Node initNode = graph.getNode(spID);

        Species sp = mInit.getSpecies(spID);
        if (sp == null) {
            return;
        }
        //  Collection<String> Edges = g.getNeighbors(node);

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
                String isThere = this.isThere(V, r.getId());

                // adds the rest of the compounds in the reaction, the direction of the edges 
                // should depend on the boundaries of the reaction
                if (isThere == null) {
                    g.addVertex(reactionName);

                    // Creates the node for the grap
                    Node reactionNode = new Node(r.getId());
                    graph.addNode(initNode);

                    EdgeType eType = EdgeType.UNDIRECTED;
                    for (SpeciesReference sr : r.getListOfReactants()) {
                        Species sps = sr.getSpeciesInstance();
                        String spName = sps.getId();
                        String nodeReactant = isThere(V, spName);
                        if (lb == 0) {
                            eType = EdgeType.DIRECTED;
                        }
                        if (nodeReactant == null) {
                            if (!spName.equals(spID)) {
                                String eName = spName + " - " + uniqueId.nextId();
                                String vName = eName + " - " + sps.getName();
                                g.addVertex(vName);
                                //adds the node to the graph
                                Node n = new Node(spName + " - " + sps.getName());
                                graph.addNode(n);
                                if (lb == 0) {
                                    g.addEdge(eName, vName, reactionName, eType);
                                    graph.addEdge(new Edge(eName, n, reactionNode));
                                } else {
                                    g.addEdge(eName, reactionName, vName, eType);
                                    graph.addEdge(new Edge(eName, reactionNode, n));
                                }
                            } else {
                                if (lb == 0) {
                                    g.addEdge(initSPName, initialStringNode, reactionName, eType);
                                    graph.addEdge(new Edge(sp.getId(), initNode, reactionNode));
                                } else {
                                    g.addEdge(initSPName, reactionName, initialStringNode, eType);
                                    graph.addEdge(new Edge(sp.getId(), reactionNode, initNode));
                                }
                            }
                        } else {

                            Node reactantNode = graph.getNode(spName);
                            String eName = spName + " - " + uniqueId.nextId();
                            if (lb == 0) {
                                g.addEdge(eName, nodeReactant, reactionName, eType);
                                graph.addEdge(new Edge(eName, reactantNode, reactionNode));
                            } else {
                                g.addEdge(eName, reactionName, nodeReactant, eType);
                                graph.addEdge(new Edge(eName, reactionNode, reactantNode));
                            }

                        }
                    }

                    for (SpeciesReference sr : r.getListOfProducts()) {
                        Species sps = sr.getSpeciesInstance();
                        String spName = sps.getId();
                        String nodeProduct = isThere(V, spName);
                        if (nodeProduct == null) {

                            if (!spName.equals(spID)) {
                                String eName = spName + " - " + uniqueId.nextId();
                                String vName = eName + " - " + sps.getName();
                                g.addVertex(vName);
                                //adds the node to the graph
                                Node n = new Node(spName + " - " + sps.getName());
                                graph.addNode(n);
                                if (lb == 0) {
                                    g.addEdge(eName, reactionName, vName, EdgeType.DIRECTED);
                                    graph.addEdge(new Edge(eName, n, reactionNode));
                                } else {
                                    g.addEdge(eName, vName, reactionName, EdgeType.DIRECTED);
                                    graph.addEdge(new Edge(eName, reactionNode, n));
                                }
                            } else {
                                if (lb == 0) {
                                    g.addEdge(initSPName, reactionName, initialStringNode, eType);
                                    graph.addEdge(new Edge(sp.getId(), reactionNode, initNode));

                                } else {
                                    g.addEdge(initSPName, initialStringNode, reactionName, eType);
                                    graph.addEdge(new Edge(sp.getId(), initNode, reactionNode));
                                }
                            }
                        } else {
                            Node productNode = graph.getNode(spName);
                            String eName = spName + " - " + uniqueId.nextId();
                            if (lb == 0) {
                                g.addEdge(eName, reactionName, nodeProduct, EdgeType.DIRECTED);
                                graph.addEdge(new Edge(eName, reactionNode, productNode));
                            } else {
                                g.addEdge(eName, nodeProduct, reactionName, EdgeType.DIRECTED);
                                graph.addEdge(new Edge(eName, productNode, reactionNode));
                            }
                        }
                    }
                }
            }
        }

    }

    private String isThere(Collection<String> V, String node) {
        for (String v : V) {
            if (v.contains(node)) {
                return v;
            }
        }
        return null;
    }

    private void removeCofactors() {

        Collection<String> Vertices = g.getVertices();
        for (String node : Vertices) {
            if (node.contains("H+") || node.contains("H2O") || node.contains(" - phosphate ") || node.contains(" - ADP")
                    || node.contains(" - ATP") || node.contains(" - NAD") || node.contains(" - CO2") || node.contains("- oxygen")) {
                g.removeVertex(node);
                this.removeCofactors();
                break;
            }
        }

    }

    private void lock() {
        Collection<String> V = g.getVertices();
        Layout<String, String> layout = vv.getGraphLayout();
        for (String v : V) {
            layout.lock(v, true);
        }
    }
}
