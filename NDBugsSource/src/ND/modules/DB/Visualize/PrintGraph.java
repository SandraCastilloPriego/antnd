/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.DB.Visualize;

import ND.main.NDCore;
import ND.modules.configuration.db.DBConfParameters;
import ND.modules.simulation.antNoGraph.uniqueId;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
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
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.ws.rs.core.MediaType;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;
import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONException;
import org.neo4j.shell.util.json.JSONObject;

/**
 *
 * @author scsandra
 */
public class PrintGraph implements KeyListener, GraphMouseListener,
    ChangeListener {

    private final List<String> selectedNode, selectedEdge;
    private edu.uci.ics.jung.graph.Graph<String, String> g;
    private MiniGraph graph, original;
    private VisualizationViewer<String, String> vv;
    //EditingModalGraphMouse gm;
    //private JPopupMenu popupMenu;
    //JPanel topPanel;
    // JColorChooser tcc;
    //JButton banner;
    //Color selectedColor;
    //Map<String, Color> colors;
    String URI;
    JTextArea text;
    private Map<String, List<String>> connections;

    public PrintGraph() {
        this.selectedNode = new ArrayList<>();
        this.selectedEdge = new ArrayList<>();
        //this.popupMenu = new JPopupMenu();
        this.URI = NDCore.getDBParameters().getParameter(DBConfParameters.URI).getValue();
        if (!URI.contains("http")) {
            this.URI = "http://" + URI + ":7474/db/data/";
        } else {
            this.URI = URI + ":7474/db/data/";
        }
        this.connections = new HashMap<>();
    }

    public VisualizationViewer printPathwayInFrame(final MiniGraph graph) {
        g = new SparseMultigraph<>();
        this.graph = graph;
        this.original = (MiniGraph) graph.clone();
        List<MiniNode> nodes = graph.getNodes();
        List<MiniEdge> edges = graph.getEdges();
        // colors = new HashMap<>();
        SpringLayout layout = new SpringLayout(g);
        vv = new VisualizationViewer<>(layout);

        for (MiniNode node : nodes) {
            if (node != null) {
                g.addVertex(node.self);
            }
        }

        for (MiniEdge edge : edges) {
            if (edge != null) {
                try {
                    g.addEdge(edge.self, edge.start, edge.end, EdgeType.DIRECTED);

                } catch (Exception e) {
                }
            }
        }

        vv.setPreferredSize(new Dimension(1500, 1500));
        Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
            @Override
            public Paint transform(String id) {
                String name = id;
                for (MiniNode node : graph.getNodes()) {
                    if (node.self.equals(id)) {
                        name = node.Id;
                        break;
                    }
                }
                if (name.contains("Bioledge")) {
                    return new Color(251, 128, 114);
                } else if (name.contains("Name")) {
                    return new Color(190, 186, 218);
                } else if (name.contains("KEGG") && !name.contains("Reaction")) {
                    return new Color(141, 211, 199);
                } else if (name.contains("ElAtom")) {
                    return new Color(255, 255, 179);
                } else if (name.contains("Formula")) {
                    return new Color(255, 255, 179);
                } else if (name.contains("Smile") || name.contains("InChI")) {
                    return new Color(255, 255, 179);
                } else if (name.contains("MetaNetX")) {
                    return new Color(128, 177, 211);
                } else if (name.contains("ChEBI")) {
                    return new Color(252, 205, 229);
                } else if (name.contains("MetaCyc") && !name.contains("Reaction")) {
                    return new Color(179, 222, 105);
                }

                try {
                    return new Color(253, 180, 98);

                } catch (Exception e) {
                    return new Color(253, 180, 98);
                }
            }

        };

        Transformer<String, Shape> vertexShape = new Transformer<String, Shape>() {
            public Shape transform(String id) {
                try {
                    String name = id;
                    for (MiniNode node : graph.getNodes()) {
                        if (node.self.equals(id)) {
                            name = node.Id;
                            break;
                        }
                    }
                    if (name.contains("Bioledge") || name.contains("MetaNetX") || name.contains("KEGG") || name.contains("MetaCyc")) {
                        if (!name.contains("Reaction")) {
                            Ellipse2D circle = new Ellipse2D.Double(-15, -15, 40, 40);
                            return circle;
                        }
                    } else if (name.contains("Name")) {
                        Ellipse2D circle = new Ellipse2D.Double(-10, -10, 15, 15);
                        return circle;
                    } else if (name.contains("Reaction")) {
                        Ellipse2D circle = new Ellipse2D.Double(-10, -10, 15, 15);
                        return circle;
                    } else if (name.contains("Formula") || name.contains("Smile") || name.contains("ElAtom") || name.contains("InChI")) {
                        Ellipse2D circle = new Ellipse2D.Double(-9, -9, 10, 10);
                        return circle;
                    }
                    Ellipse2D circle = new Ellipse2D.Double(-12, -12, 20, 20);
                    return circle;

                } catch (Exception e) {
                    System.out.println(e.toString());
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
                    } else {
                        selectedNode.remove(vertex);
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
                        selectedEdge.add(edge);
                    } else {
                        selectedEdge.remove(edge);
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

        final Stroke vertexStroke = new BasicStroke(1.0f, BasicStroke.JOIN_ROUND,
            BasicStroke.JOIN_BEVEL, 20.0f, dash, 0.0f);
        Transformer<String, Stroke> vertexStrokeTransformer
            = new Transformer<String, Stroke>() {
                @Override
                public Stroke transform(String s) {
                    return vertexStroke;
                }
            };

        Transformer labelTransformer = new ChainedTransformer<>(new Transformer[]{
            new ToStringLabeller<>(),
            new Transformer<String, String>() {
                @Override
                public String transform(String input) {
                    String name = input;
                    for (MiniNode node : graph.getNodes()) {
                        if (node.self.equals(input)) {
                            name = node.Id;
                        }
                    }
                    return "<html><b><font color=\"black\">" + name;
                }
            }});

        vv.getRenderContext().setVertexLabelTransformer(labelTransformer);
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        //vv.getRenderContext().setVertexStrokeTransformer(vertexStrokeTransformer);
        vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        vv.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);
        // vv.getRenderContext().setEdgeLabelTransformer(labelTransformer);
        vv.getRenderContext().setVertexShapeTransformer(vertexShape);
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

        Factory<String> vertexFactory = new VertexFactory();
        Factory<String> edgeFactory = new EdgeFactory();
       // gm = new EditingModalGraphMouse(vv.getRenderContext(),
        //    vertexFactory, edgeFactory);

         DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.PICKING);
        //gm.getPopupEditingPlugin()
       vv.setGraphMouse(gm);
       
       /* JPanel panele = new JPanel();
        panele.add(gm.getModeComboBox());
        panele.setPreferredSize(new Dimension(200, 50));
        panele.setBackground(Color.white);*/
        JPanel panelInfo = new JPanel();
        panelInfo.setPreferredSize(new Dimension(1500, 50));
        panelInfo.setBackground(Color.white);

        JButton rmEdge = new JButton("Remove Edge");
        rmEdge.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!selectedEdge.isEmpty()) {
                    System.out.println(selectedEdge.get(0));
                    final JInternalFrame frame = new JInternalFrame("Are you Sure?", true, true, true, true);
                    JPanel panel = new JPanel();
                    JButton sure = new JButton("I'm sure, I'm the boss");
                    sure.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            System.out.println("Not Yet");
                        }

                    });

                    JButton cancel = new JButton("Cancel, I'm scared");
                    cancel.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            frame.dispose();
                        }

                    });
                    JLabel field = new JLabel(getInfo(selectedEdge.get(0)));

                    panel.add(field);
                    panel.add(sure);
                    panel.add(cancel);
                    panel.setBackground(Color.white);
                    frame.add(panel);
                    panel.setPreferredSize(new Dimension(600, 100));
                    frame.setSize(new Dimension(600, 100));
                    frame.toFront();
                    NDCore.getDesktop().addInternalFrame(frame);
                    frame.setVisible(true);
                }

            }

            private String getInfo(String get) {
                MiniEdge selected = null;
                for (MiniEdge e : graph.getEdges()) {
                    if (e.self.equals(get)) {
                        selected = e;
                        break;
                    }
                }
                MiniNode start = null, end = null;
                for (MiniNode n : graph.getNodes()) {
                    if (n.self.equals(selected.start)) {
                        start = n;
                    }
                    if (n.self.equals(selected.end)) {
                        end = n;
                    }
                }

                String info = "";
                if (start != null && end != null) {
                    info += "Edge connecting " + start.Id + " and " + end.Id;
                }
                return info;
            }
        });
        text = new JTextArea();
       // panelInfo.add(panele);
        panelInfo.add(rmEdge);
        panelInfo.add(text);
        vv.add(panelInfo);
        //vv.add(panele);
        vv.addKeyListener(this);
        vv.addGraphMouseListener(this);

        vv.setBackground(Color.WHITE);

        return vv;
    }

    @Override
    public void keyTyped(KeyEvent e) {
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

    private void lock() {
        Collection<String> V = g.getVertices();
        Layout<String, String> layout = vv.getGraphLayout();
        for (String v : V) {
            layout.lock(v, true);

        }
    }

    /* @Override
     public void actionPerformed(ActionEvent ae) {
     Runtime.getRuntime().freeMemory();
     String command = ae.getActionCommand();
     if (command.equals("Remove Edge")) {

     }

     }*/
    @Override
    public void graphClicked(Object v, MouseEvent me) {
        String name = (String) v;
        getDBInfo(name.substring(name.lastIndexOf("/") + 1));
    }

    @Override
    public void graphPressed(Object v, MouseEvent me) {
        if (me.getClickCount() == 2) {
            if (!this.connections.containsKey((String) v)) {
                for (MiniNode node : graph.getNodes()) {
                    if (node.self.equals(v)) {
                        String l = node.labels.replace("[", "").replace("]", "").replace("\"", "").replace(",", ":");
                        String[] id = node.Id.split(":");
                        String queryString = "match(n:" + l + " {" + id[0] + ":'" + id[1] + "'})-[r]-(m) return n,r,m";
                        // System.out.println(queryString);
                        this.getDBQuery(queryString, (String) v);
                        break;
                    }
                }
            } else {
                List<String> conn = this.connections.get((String) v);
                for (String c : conn) {
                    if (!original.contains(c)) {
                        this.g.removeVertex(c);
                        this.graph.removeVertex(c);
                    }
                }
                this.connections.remove((String) v);
            }
        }

        /* if (me.isPopupTrigger()) {
         //JPopupMenu popupMenu = new JPopupMenu();
         // GUIUtils.addMenuItem(popupMenu, "Get Info", this, "Get Info");
         //popupMenu.show(me.getComponent(), me.getX(), me.getY());*/
           // gm.setMode(ModalGraphMouse.Mode.EDITING);
            /*JPanel panel = new JPanel();
         //panel.add(gm.);
         JInternalFrame frame = new JInternalFrame("Add", true, true, true, true);
         frame.add(panel);
         panel.setPreferredSize(new Dimension(600, 100));
         frame.setSize(new Dimension(600, 100));
         frame.toFront();
         NDCore.getDesktop().addInternalFrame(frame);
         frame.setVisible(true);

         }*/
    }

    private void getDBInfo(String node) {

        final String nodeEntryPointUri = URI + "cypher";

        String query = "{\"query\" : \"match (n) where id(n)=" + node + " return n\",\n \"params\":  {} \n}";
        //System.out.println(query);
        WebResource resource = Client.create()
            .resource(nodeEntryPointUri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .entity(query)
            .post(ClientResponse.class);

        String responsestring = response.getEntity(String.class);
        response.close();
        // System.out.println(responsestring);
        JSONObject obj;
        try {
            obj = new JSONObject(responsestring);
            String info = "";
            JSONArray array = obj.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONArray a = array.getJSONArray(i);
                for (int j = 0; j < a.length(); j++) {
                    JSONObject metanode = a.getJSONObject(j).getJSONObject("metadata");
                    info += "Labels: " + metanode.getString("labels") + "\n";
                    JSONObject data = a.getJSONObject(j).getJSONObject("data");
                    info += data.toString() + "\n";
                }
            }
            info = info.replace("{", "").replace("}", "").replace("\"", "");
            text.setText(info);
        } catch (JSONException ex) {
            System.out.println(ex.toString());
        }

    }

    private void getDBQuery(String queryString, String nodeG) {
        final String nodeEntryPointUri = URI + "cypher";

        String query = "{\"query\" : \"" + queryString + "\",\n \"params\":  {} \n}";
        //System.out.println(query);
        WebResource resource = Client.create()
            .resource(nodeEntryPointUri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .entity(query)
            .post(ClientResponse.class);

        String responsestring = response.getEntity(String.class);
        response.close();
        // System.out.println(responsestring);
        JSONObject obj;
        try {
            obj = new JSONObject(responsestring);

            JSONArray columns = obj.getJSONArray("columns");
            String[] col = columns.toString().replace("[", "").replace("]", "").replace("\"", "").split(",");
            JSONArray array = obj.getJSONArray("data");
            List<String> newNodes = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONArray a = array.getJSONArray(i);
                for (int j = 0; j < a.length(); j++) {
                    String metanode = a.getJSONObject(j).getJSONObject("metadata").toString();
                    String node = null;
                    if (metanode.contains("type")) {
                        node = a.getJSONObject(j).getJSONObject("metadata").get("type").toString();
                        String start = a.getJSONObject(j).get("start").toString();
                        String end = a.getJSONObject(j).get("end").toString();
                        String self = a.getJSONObject(j).get("self").toString();
                        MiniEdge me = new MiniEdge();
                        me.type = node;
                        me.start = start;
                        me.end = end;
                        me.self = self;
                        g.addEdge(self, me.start, me.end, EdgeType.DIRECTED);
                        graph.addEdge(me);

                    } else {
                        JSONObject nod = a.getJSONObject(j);
                        String[] knode = nod.getJSONObject("data").toString().replace("\"", "").replace("{", "").replace("}", "").split(",");
                        for (String k : knode) {
                            if (k.contains("Id")) {
                                node = k;
                            }
                        }
                        String labels = a.getJSONObject(j).getJSONObject("metadata").get("labels").toString();
                        String self = nod.get("self").toString();
                        MiniNode mn = new MiniNode();
                        mn.Id = node;
                        mn.self = self;
                        mn.labels = labels;
                        g.addVertex(mn.self);

                        graph.addNode(mn);
                        newNodes.add(mn.self);

                    }
                }
            }
            this.connections.put(nodeG, newNodes);
        } catch (JSONException ex) {
            System.out.println(ex.toString());
        } catch (ConcurrentModificationException eex) {
        }

    }

    @Override
    public void graphReleased(Object v, MouseEvent me) {

    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        /*Color newColor = tcc.getColor();
         this.banner.setBackground(newColor);
         this.selectedColor = newColor;*/
    }

//    public void writeJPEGImage(File file) {
//        int width = 1000;
//        int height = 1000;
//        BufferedImage bi = new BufferedImage(width, height,
//            BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics = bi.createGraphics();
//        vv.paint(graphics);
//        graphics.dispose();
//        try {
//            ImageIO.write(bi, "jpeg", new File("/home/scsandra/Pictures/example.jpg"));
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//    }
//
//    public int print(java.awt.Graphics graphics,
//        java.awt.print.PageFormat pageFormat, int pageIndex)
//        throws java.awt.print.PrinterException {
//
//        if (pageIndex > 0) {
//
//            return (Printable.NO_SUCH_PAGE);
//
//        } else {
//
//            java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
//
//            vv.setDoubleBuffered(false);
//
//            g2d.translate(pageFormat.getImageableX(), pageFormat
//                .getImageableY());
//
//            vv.paint(g2d);
//
//            vv.setDoubleBuffered(true);
//
//            return (Printable.PAGE_EXISTS);
//
//        }
//
//    }
}

class VertexFactory implements Factory<String> {

    String i;

    public String create() {
        return "hola" + uniqueId.nextId();
    }
}

class EdgeFactory implements Factory<String> {

    String i;

    public String create() {
        return "" + uniqueId.nextId();
    }
}
