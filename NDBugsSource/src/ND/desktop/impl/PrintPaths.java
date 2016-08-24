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
import ND.util.GUIUtils;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.svg.SVGGraphics2D;
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
public class PrintPaths implements KeyListener, GraphMouseListener, ActionListener,
    ChangeListener {

    private final Model m;
    private TransFrame transFrame = null;
    private final List<String> selectedNode;
    private edu.uci.ics.jung.graph.Graph<String, String> g;
    Map<String, Color> clusters;
    private boolean showInfo = false;
    private Graph graph;
    private VisualizationViewer<String, String> vv;
    SpringLayout layout;
    private JPopupMenu popupMenu;
    JPanel topPanel;
    JColorChooser tcc;
    JButton banner;
    Color selectedColor;
    Map<String, Color> colors;

    public PrintPaths(Model m) {
        this.m = m;
        this.clusters = new HashMap<>();
        this.selectedNode = new ArrayList<>();
        this.popupMenu = new JPopupMenu();
    }

    public VisualizationViewer printPathwayInFrame(final Graph graph) {
        g = new SparseMultigraph<>();
        this.graph = graph;
        List<Node> nodes = graph.getNodes();
        List<Edge> edges = graph.getEdges();
        colors = new HashMap<>();
        layout = new SpringLayout<>(g);

        //layout = new KKLayout(g);       
        layout.setSize(new Dimension(2400, 1900)); // sets the initial size of the space
        vv = new VisualizationViewer<>(layout);

        for (Node node : nodes) {
            if (node != null) {
                String name = node.getCompleteId();
                g.addVertex(name);
                if (node.getPosition() != null) {
                    layout.transform(name).setLocation(node.getPosition());
                    // System.out.println("Position: " + name + " : " + node.getPosition().toString());
                    vv.getGraphLayout().lock(name, true);
                }
                if (node.getColor() != null) {
                    colors.put(name, node.getColor());
                }
            }
        }

        for (Edge edge : edges) {
            if (edge != null) {
                try {
                    if (edge.getDirection()) {
                        g.addEdge(edge.getId(), edge.getSource().getCompleteId(), edge.getDestination().getCompleteId(), EdgeType.DIRECTED);
                    } else {
                        g.addEdge(edge.getId(), edge.getSource().getCompleteId(), edge.getDestination().getCompleteId(), EdgeType.UNDIRECTED);
                    }
                } catch (Exception e) {
                }
            }
        }

        vv.setPreferredSize(new Dimension(2400, 2000));
        Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
            @Override
            public Paint transform(String id) {

                String name = id.split(" - ")[0];
                String r = id.split(" : ")[0];
                try {
                    if (colors.containsKey(id)) {
                        return colors.get(id);
                    } else if (m.getReaction(r.trim()) != null || m.getReaction(name.trim()) != null) {
                        if (id.split(" : ").length > 1) {
                            return makeItDarKer(id.split(" : ")[1]);
                        } else {
                            return new Color(102, 194, 164);
                        }
                    } else if (id.contains("H+") || id.contains("H2O") || id.contains(" : phosphate ") || id.contains(" : ADP")
                        || id.contains(" : ATP") || id.contains(" : NAD") || id.contains(" : CO2") || id.contains(" : oxygen")
                        || id.contains(": AMP") || id.contains(" : diphosphate ") || id.contains(" : carbon dioxide ") || id.contains(" : potassium ")) {
                        return Color.ORANGE;
                    } else {
                        return new Color(156, 244, 125);
                    }
                } catch (Exception e) {
                    return new Color(156, 244, 125);
                }
            }

            private Paint makeItDarKer(String split) {
                try {
                    double flux = Double.valueOf(split);
                    if (Math.abs(flux) < 0.001) {
                        return new Color(102, 194, 164);
                    } else if (Math.abs(flux) < 0.001) {
                        return PrintPaths.lighter(new Color(190, 226, 133), 0.65);
                    } else if (Math.abs(flux) < 0.01) {
                        return PrintPaths.lighter(new Color(90, 226, 133), 0.35);
                    } else if (Math.abs(flux) < 0.1) {
                        return PrintPaths.lighter(new Color(90, 226, 133), 0.1);
                    } else if (Math.abs(flux) < 1) {
                        return PrintPaths.darken(new Color(90, 226, 133), 0.15);
                    } else if (Math.abs(flux) < 2) {
                        return PrintPaths.darken(new Color(90, 226, 133), 0.30);
                    } else if (Math.abs(flux) > 2) {
                        return PrintPaths.darken(new Color(90, 226, 133), 0.45);
                    }
                } catch (Exception e) {
                }
                return new Color(102, 194, 164);
            }
        };

        Transformer<String, Shape> vertexShape = new Transformer<String, Shape>() {
            public Shape transform(String v) {
                String name = v.split(" - ")[0];
                try {
                    String r = v.split(" : ")[0];
                    if (m.getReaction(r.trim()) != null || m.getReaction(name.trim()) != null) {
                        Rectangle2D circle = new Rectangle2D.Double(-15.0, -15.0, 50.0, 25.0);
                        return circle;
                    } else {
                        Ellipse2D circle = new Ellipse2D.Double(-15, -15, 20, 20);
                        return circle;
                    }
                } catch (Exception e) {
                    Ellipse2D circle = new Ellipse2D.Double(-15, -15, 20, 20);
                    return circle;
                }
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
                        selectedNode.add(vertex);
                        //Update node position in the graph

                        if (m != null && showInfo) {
                            if (vertex.contains(" / ")) {
                                vertex = vertex.split(" / ")[0];
                            }
                            String name = vertex.replace("sp:", "").split(" - ")[0];
                            if (name.contains(" : ")) {
                                name = name.split(" : ")[0];
                            }
                            transFrame = new TransFrame(name);

                        } else {
                            System.out.println("Vertex " + vertex
                                + " is now selected");
                        }
                    } else {
                        selectedNode.remove(vertex);
                        //  System.out.println("Position:" + vertex);
                        Node n = graph.getNode(vertex.split(" : ")[0]);
                        if (n != null) {
                            n.setPosition(layout.getX(vertex), layout.getY(vertex));
                            //  System.out.println("New Position:" + vertex + " : " + layout.getX(vertex) + " - " + layout.getY(vertex));
                        }
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
                        selectedNode.add(edge);
                        if (m != null && showInfo) {
                            String name = edge.replace("sp:", "").split(" - ")[0];
                            if (name.contains(" : ")) {
                                name = name.split(" : ")[0];
                            }
                            transFrame = new TransFrame(name);
                        } else {
                            System.out.println("Edge " + edge
                                + " is now selected");
                        }
                    } else {
                        selectedNode.remove(edge);
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

                    String name = input.split(" - ")[0];
                    return "<html><b><font color=\"red\">" + name;
                }
            }});
        Transformer labelTransformer2 = new ChainedTransformer<>(new Transformer[]{
            new ToStringLabeller<>(),
            new Transformer<String, String>() {
                @Override
                public String transform(String input) {

                    String name = input.split(" - ")[0];
                    return "<html><b><font color=\"black\">" + name;

                }
            }});

        vv.getRenderContext().setVertexLabelTransformer(labelTransformer2);
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        vv.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);
        vv.getRenderContext().setEdgeLabelTransformer(labelTransformer);
        vv.getRenderContext().setVertexShapeTransformer(vertexShape);
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(gm);

        vv.addKeyListener(this);
        vv.addGraphMouseListener(this);

        topPanel = new JPanel();

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

        final JButton saveButton = new JButton("Save Graph");
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showSaveDialog(topPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    saveImage3(file.getAbsolutePath());
                }
            }
        });
        //Set up color chooser for setting text color
        tcc = new JColorChooser();
        tcc.getSelectionModel().addChangeListener(this);
        tcc.setBorder(BorderFactory.createTitledBorder("Choose Text Color"));
        tcc.setAlignmentX(1000);
        tcc.setVisible(false);
        tcc.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    tcc.setVisible(false);
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }

        });
        vv.add(tcc);

        banner = new JButton("Selected Color");
        banner.setBackground(Color.white);
        banner.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tcc.setVisible(true);
            }
        });
        final JTextField field = new JTextField("");
        field.setPreferredSize(new Dimension(350, 30));
        field.setBackground(Color.LIGHT_GRAY);
        field.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    String[] reactions = field.getText().split(",");
                    for (String r : reactions) {
                        Collection<String> V = g.getVertices();
                        for (String v : V) {
                            if (v.contains(r)) {
                                colors.put(v, selectedColor);
                                String spID = v;

                                if (v.contains(" : ")) {
                                    spID = v.split(" : ")[0];
                                }
                                Node n = graph.getNode(spID);
                                if (n != null) {
                                    n.setColor(selectedColor);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }

        });
        topPanel.add(field);
        topPanel.add(banner);
        topPanel.add(button);
        topPanel.add(saveButton);
        topPanel.setPreferredSize(new Dimension(1000, 40));
        topPanel.setBackground(Color.WHITE);
        vv.add(topPanel);
        vv.setBackground(Color.WHITE);
        return vv;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '\u0008' || e.getKeyChar() == '\u007F') {
            if (this.selectedNode != null) {
                for (String v : this.selectedNode) {
                    g.removeVertex(v);

                    String name = v.split(" - ")[0];
                    if (name.contains(" : ")) {
                        name = name.split((" : "))[0];
                    }
                    if (m.getReaction(name) != null) {
                        this.m.removeReaction(name);

                    }
                    this.graph.removeNode(name);

                }
            }
        }
        if (e.getKeyChar() == 'e') {
            if (!this.selectedNode.isEmpty()) {
                showReactions(this.selectedNode.get(0), null);
            }
        }

        if (e.getKeyChar() == 'c') {
            removeCofactors();
        }

        if (e.getKeyChar() == 'l') {
            this.lock();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private void showReactions(String initialStringNode, String reaction) {
        Collection<String> V = g.getVertices();

        // Gets the selected model. It is the source model for the reactions
        Model mInit = NDCore.getDesktop().getSelectedDataFiles()[0].getDocument().getModel();

        String spID = initialStringNode;

        if (initialStringNode.contains(" : ")) {
            spID = initialStringNode.split(" : ")[0];
        }
        Node initNode = graph.getNode(spID);

        Species sp = mInit.getSpecies(spID);
        if (sp == null) {
            return;
        }

        for (Reaction r : mInit.getListOfReactions()) {
            if (reaction != null && !reaction.contains(r.getId())) {
                continue;
            }
//            if (r.getId().contains("Ex")) {
//                continue;
//            }
            if (r.hasReactant(sp) || r.hasProduct(sp)) {
                double lb = Double.NEGATIVE_INFINITY;
                double ub = Double.POSITIVE_INFINITY;
                Double flux = null;
                // read bounds to know the direction of the edges
                if (r.getKineticLaw() != null) {
                    KineticLaw law = r.getKineticLaw();
                    LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                    lb = lbound.getValue();
                    LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                    ub = ubound.getValue();
                    LocalParameter rflux = law.getLocalParameter("FLUX_VALUE");
                    flux = rflux.getValue();
                }

                // adds the new reaction node with and edge from the extended node
                String reactionName = null;
                if (flux != null) {
                    DecimalFormat df = new DecimalFormat("#.####");
                    reactionName = r.getId() + " : " + df.format(flux) + " - " + uniqueId.nextId();
                } else {
                    reactionName = r.getId() + " - " + uniqueId.nextId();
                }

                //  String initSPName = sp.getId() + " : " + sp.getName() + " - " + uniqueId.nextId();
                String initSPName = initialStringNode;

                String isThere = this.isThere(V, r.getId());

                // adds the rest of the compounds in the reaction, the direction of the edges 
                // should depend on the boundaries of the reaction
                if (isThere == null) {

                    //adds the new reaction to the new model
                    AddReaction(r);

                    // adds the new reaction to the visualization graph
                    g.addVertex(reactionName);

                    // Creates the node for the ANT graph
                    Node reactionNode = new Node(reactionName);
                    graph.addNode(reactionNode);

                    EdgeType eType = EdgeType.UNDIRECTED;
                    boolean direction = false;
                    if (lb == 0 || ub == 0) {
                        eType = EdgeType.DIRECTED;
                        direction = true;
                    }
                    for (SpeciesReference sr : r.getListOfReactants()) {
                        Species sps = sr.getSpeciesInstance();

                        //                 if (!this.m.containsSpecies(sp.getId())) {
                        //                   this.m.addSpecies(sps);
                        //             }
                        String spName = sps.getId();
                        String nodeReactant = isThere(V, spName);

                        if (nodeReactant == null) {
                            if (!spName.equals(spID)) {
                                String vName = spName + " : " + sps.getName() + " - " + uniqueId.nextId();
                                String eName = spName + " - " + uniqueId.nextId();

                                g.addVertex(vName);
                                //adds the node to the graph
                                Node n = new Node(vName);
                                graph.addNode(n);
                                if (lb == 0) {
                                    g.addEdge(eName, vName, reactionName, eType);
                                    graph.addEdge(new Edge(eName, n, reactionNode, direction));
                                } else {
                                    g.addEdge(eName, reactionName, vName, eType);
                                    graph.addEdge(new Edge(eName, reactionNode, n, direction));
                                }
                            } else {
                                if (lb == 0) {
                                    g.addEdge(initSPName, initialStringNode, reactionName, eType);
                                    graph.addEdge(new Edge(sp.getId(), initNode, reactionNode, direction));
                                } else {
                                    g.addEdge(initSPName, reactionName, initialStringNode, eType);
                                    graph.addEdge(new Edge(sp.getId(), reactionNode, initNode, direction));
                                }
                            }
                        } else {

                            Node reactantNode = graph.getNode(spName);
                            String eName = spName + " - " + uniqueId.nextId();
                            if (lb == 0) {
                                g.addEdge(eName, nodeReactant, reactionName, eType);
                                graph.addEdge(new Edge(eName, reactantNode, reactionNode, direction));
                            } else {
                                g.addEdge(eName, reactionName, nodeReactant, eType);
                                graph.addEdge(new Edge(eName, reactionNode, reactantNode, direction));
                            }

                        }
                    }

                    for (SpeciesReference sr : r.getListOfProducts()) {
                        Species sps = sr.getSpeciesInstance();

                        //    if (!this.m.containsSpecies(sp.getId())) {
                        //      this.m.addSpecies(sps);
                        //}
                        String spName = sps.getId();
                        String nodeProduct = isThere(V, spName);
                        if (nodeProduct == null) {

                            if (!spName.equals(spID)) {
                                String vName = spName + " : " + sps.getName() + " - " + uniqueId.nextId();
                                String eName = spName + " - " + uniqueId.nextId();

                                g.addVertex(vName);
                                //adds the node to the graph
                                Node n = new Node(vName);
                                graph.addNode(n);
                                if (lb == 0) {
                                    g.addEdge(eName, reactionName, vName, eType);
                                    graph.addEdge(new Edge(eName, reactionNode, n, direction));
                                } else {
                                    g.addEdge(eName, vName, reactionName, eType);
                                    graph.addEdge(new Edge(eName, n, reactionNode, direction));
                                }
                            } else {
                                if (lb == 0) {
                                    g.addEdge(initSPName, initialStringNode, reactionName, eType);
                                    graph.addEdge(new Edge(sp.getId(), initNode, reactionNode, direction));

                                } else {
                                    g.addEdge(initSPName, reactionName, initialStringNode, eType);
                                    graph.addEdge(new Edge(sp.getId(), reactionNode, initNode, direction));
                                }
                            }
                        } else {
                            Node productNode = graph.getNode(spName);
                            String eName = spName + " - " + uniqueId.nextId();
                            if (lb == 0) {
                                g.addEdge(eName, reactionName, nodeProduct, eType);
                                graph.addEdge(new Edge(eName, reactionNode, productNode, direction));
                            } else {
                                g.addEdge(eName, nodeProduct, reactionName, eType);
                                graph.addEdge(new Edge(eName, productNode, reactionNode, direction));
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
            if (node.contains("H+") || node.contains("H2O") || node.contains(" : phosphate ") || node.contains(" : ADP")
                || node.contains(" : ATP") || node.contains(" : NAD") || node.contains(" : CO2") || node.contains(" : oxygen")
                || node.contains(": AMP") || node.contains(" : diphosphate ") || node.contains(" : carbon dioxide ") || node.contains(" : potassium ")) {
                g.removeVertex(node);
                //   graph.removeNode(node);
                removeCofactors();
                break;
            }
        }

    }

    private void lock() {
        Collection<String> V = g.getVertices();
        Layout<String, String> layout = vv.getGraphLayout();
        for (String v : V) {
            layout.lock(v, true);
            Node n = graph.getNode(v.split(" : ")[0]);
            if (n != null) {
                n.setPosition(this.layout.getX(v), this.layout.getY(v));
                //  System.out.println("New Position:" + vertex + " : " + layout.getX(vertex) + " - " + layout.getY(vertex));
            }
        }
    }

    private void AddReaction(Reaction reaction) {
        Reaction r = new Reaction(reaction.getId());
        r.setId(reaction.getId());
        r.setName(reaction.getName());

        for (SpeciesReference sp : reaction.getListOfReactants()) {
            SpeciesReference spref = new SpeciesReference();
            spref.setStoichiometry(sp.getCalculatedStoichiometry());

            if (m.containsSpecies(sp.getSpecies())) {
                spref.setSpecies(m.getSpecies(sp.getSpecies()));
            } else {
                Species specie = sp.getSpeciesInstance().clone();
                m.addSpecies(specie);
                spref.setSpecies(specie);
            }
            r.addReactant(spref);
        }

        for (SpeciesReference sp : reaction.getListOfProducts()) {

            SpeciesReference spref = new SpeciesReference();
            spref.setStoichiometry(sp.getCalculatedStoichiometry());
            if (m.containsSpecies(sp.getSpecies())) {
                spref.setSpecies(m.getSpecies(sp.getSpecies()));
            } else {
                Species specie = sp.getSpeciesInstance().clone();
                m.addSpecies(specie);
                spref.setSpecies(specie);
            }
            r.addProduct(spref);
        }

        KineticLaw law = new KineticLaw();
        LocalParameter lboundP = new LocalParameter("LOWER_BOUND");
        if (reaction.getKineticLaw() != null) {
            if (reaction.getKineticLaw().getLocalParameter("LOWER_BOUND") != null) {
                lboundP.setValue(reaction.getKineticLaw().getLocalParameter("LOWER_BOUND").getValue());

                law.addLocalParameter(lboundP);
                LocalParameter uboundP = new LocalParameter("UPPER_BOUND");
                uboundP.setValue(reaction.getKineticLaw().getLocalParameter("LOWER_BOUND").getValue());
                law.addLocalParameter(uboundP);
                r.setKineticLaw(law);
            }
        }
        m.addReaction(r);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Runtime.getRuntime().freeMemory();
        String command = ae.getActionCommand();
        if (command.equals("All")) {
            if (!this.selectedNode.isEmpty()) {
                showReactions(this.selectedNode.get(0), null);
            }
        } else {
            if (!this.selectedNode.isEmpty()) {
                String reaction = command.split(" - ")[0];
                showReactions(this.selectedNode.get(0), command);
            }
        }
    }

    @Override
    public void graphClicked(Object v, MouseEvent me) {
        if (me.getClickCount() == 2) {
            this.colors.put((String) v, selectedColor);
            String name = ((String) v).split(" - ")[0];
            if (name.contains(" : ")) {
                name = name.split((" : "))[0];
            }
            Node node = this.graph.getNode(name);
            if (node != null) {
                node.setColor(selectedColor);
            }
        }
    }

    @Override
    public void graphPressed(Object v, MouseEvent me) {
        if (me.isPopupTrigger()) {
            popupMenu = new JPopupMenu();
            Model mInit = NDCore.getDesktop().getSelectedDataFiles()[0].getDocument().getModel();
            String spID = (String) v;

            if (spID.contains(" : ")) {
                spID = spID.split(" : ")[0];
            }
            Species sp = mInit.getSpecies(spID);
            if (sp == null) {
                return;
            }
            GUIUtils.addMenuItem(popupMenu, "All", this, "All");
            int i = 0;
            for (Reaction r : mInit.getListOfReactions()) {
                if (r.hasReactant(sp) || r.hasProduct(sp)) {
                    String reaction = r.getId() + " - " +r.getName();
                    GUIUtils.addMenuItem(popupMenu, reaction, this, reaction);
                    i++;
                }
                if (i > 35) {
                    GUIUtils.addMenuItem(popupMenu, "...", this, "...");
                    break;
                }
            }

            popupMenu.show(me.getComponent(), me.getX(), me.getY());
            System.out.println(v);
        }
    }

    @Override
    public void graphReleased(Object v, MouseEvent me) {

    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        Color newColor = tcc.getColor();
        this.banner.setBackground(newColor);
        this.selectedColor = newColor;
    }

    public static Color darken(Color color, double fraction) {

        int red = (int) Math.round(Math.max(0, color.getRed() - 255 * fraction));
        int green = (int) Math.round(Math.max(0, color.getGreen() - 255 * fraction));
        int blue = (int) Math.round(Math.max(0, color.getBlue() - 255 * fraction));

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);

    }
    
    public static Color lighter(Color color, double fraction) {

        int red = (int) Math.round(Math.max(0, color.getRed() + 255 * fraction));
        int green = (int) Math.round(Math.max(0, color.getGreen() + 255 * fraction));
        int blue = (int) Math.round(Math.max(0, color.getBlue() + 255 * fraction));

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);

    }

    public void saveImage(String path) {
        // Create the VisualizationImageServer
// vv is the VisualizationViewer containing my graph
        VisualizationImageServer<String, String> vis
            = new VisualizationImageServer<String, String>(vv.getGraphLayout(),
                vv.getSize());

// Configure the VisualizationImageServer the same way
// you did your VisualizationViewer. In my case e.g.
        vis.setBackground(Color.WHITE);
       /* vis.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<>());
        vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<>());
        vis.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<>());
        vis.getRenderer().getVertexLabelRenderer()
            .setPosition(Renderer.VertexLabel.Position.CNTR);*/

// Create the buffered image
        BufferedImage image = (BufferedImage) vis.getImage(
            new Point2D.Double(vv.getGraphLayout().getSize().getWidth(),
                vv.getSize().getHeight()),
            new Dimension(vv.getSize()));

// Write image to a png file
        File outputfile = new File(path);

        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            // Exception handling
        }

    }

    public void saveImage2() {
        try {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.setBackground(Color.WHITE);
            panel.add(vv);

            Properties p = new Properties();
            p.setProperty("PageSize", "A4");

// vv is the VirtualizationViewer
            VectorGraphics g = new SVGGraphics2D(new File("/home/scsandra/Pictures/Network.svg"), vv);

            g.setProperties(p);
            g.startExport();
            panel.print(g);
            g.endExport();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrintPaths.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrintPaths.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveImage3(String path) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(path, "UTF-8");
            writer.println("Creator \"AntND\"");
            writer.println("Version	1.0");
            writer.println("graph\t[");
            Map<Node, String> indexes = new HashMap<>();
            List<Node> nodes = graph.getNodes();
            int i = 1;
            for (Node node : nodes) {
                indexes.put(node, String.valueOf(i));
                writer.println("\tnode\t[");
                writer.println("\t\troot_index\t" + i);
                writer.println("\t\tid\t" + i++);
                writer.println("\t\tgraphics\t[");
                if (node.getPosition() != null) {
                    writer.println("\t\t\tx\t" + node.getPosition().getX());
                    writer.println("\t\t\ty\t" + node.getPosition().getY());
                }
                writer.println("\t\t\tw\t35.0");
                writer.println("\t\t\th\t35.0");
                if (node.getColor() != null) {
                    String hex = "#" + Integer.toHexString(node.getColor().getRGB()).substring(2);
                    writer.println("\t\t\tfill\t\"" + hex + "\"");
                }
                writer.println("\t\t\ttype\t\"ellipse\"");
                writer.println("\t\t\toutline\t\"#3333ff\"");
                writer.println("\t\t\toutline_width\t5.0");
                writer.println("\t\t]");
                writer.println("\t\tlabel\t\"" + node.getCompleteId() + "\"");
                writer.println("\t]");
            }

            List<Edge> edges = graph.getEdges();

            for (Edge edge : edges) {
                writer.println("\tedge\t[");
                writer.println("\t\troot_index\t" + i++);
                writer.println("\t\ttarget\t" + indexes.get(edge.getDestination()));
                writer.println("\t\tsource\t" + indexes.get(edge.getSource()));
                writer.println("\t]");
            }
            writer.println("]");

            writer.println("Title\t\"" + this.m.getId() + "\"");
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrintPaths.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PrintPaths.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
