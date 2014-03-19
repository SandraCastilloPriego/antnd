/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.antNoGraph;

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
import java.util.ArrayList;
import java.util.HashMap;
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
        private String cofactors[] = {"C00131", "B00002", "B00001", "C00020", "C00055", "C00105", "C00130", "C00144", "C00002", "C00044", "C00063", "C00075", "C00012",
                "C00081", "C00700", "C00008", "C00010", "C00015", "C00035", "C00068", "C00104", "C00112", "C00120", "C00194", "C01337", "C04628",
                "C05777", "C19637", "C00007", "C00013", "C00027", "C00034", "C00038", "C00070", "C00076", "C00087", "C00088", "C01327", "C00175",
                "C00205", "C00238", "C00244", "C00282", "C00283", "C00291", "C00305", "C05529", "C00533", "C00536", "C00697", "C00703", "C00704",
                "C00887", "C01330", "C01358", "C01413", "C11215", "C01528", "C02084", "C02466", "C05172", "C05684", "C05697", "C06232", "C06697",
                "C06701", "C09306", "C00094", "C14818", "C14819", "C19171", "C00192", "C00708", "C00742", "C01319", "C01324", "C01382", "C01486",
                "C01818", "C01861", "C02306", "C05361", "C05590", "C13645", "C16487", "C00080", "C00125", "C00139", "C00003", "C00006", "C00016",
                "C00061", "C00113", "C00255", "C00343", "C00828", "C16694", "C17568", "C17569", "C00399", "C00876", "C00001", "C00009", "C00059",
                "C01342", "C01328", "C00011", "C01353", "C00126", "C00138", "C00004", "C00005", "C00342", "C01007", "C01352", "C01359", "C01847",
                "C02185", "C05819", "C90001", "C90002", "C00390", "C01080", "C00288", "C00115", "C00698", "C11481", "C01478", "C00320", "C00058",
                "C00014", "C00206", "C00017", "C00018", "C00019", "C00021", "C00022", "C00023", "C00024", "C00025", "C00026", "C00032", "C00114",
                "C00145", "C00146", "C00201", "C06089", "C01417", "CHEBI16144", "CHEBI22984", "CHEBI26078", "C19970", "Cluster4564", "Cluster4563",
                "C00040", "C00069", "CHEBI17909"};
        
        private int number = 0;

        public PrintPaths(List<String> initialIds, String finalId) {
                this.initialIds = initialIds;
                this.finalId = finalId;
        }

        public VisualizationViewer printPathwayInFrame(HashMap<String, ReactionFA> reactions) {
                edu.uci.ics.jung.graph.Graph<String, String> g = new SparseMultigraph<>();
                List<ReactionFA> nodes = new ArrayList<>();
                for (String key : reactions.keySet()) {
                        ReactionFA reaction = reactions.get(key);
                        if (reaction.getPheromones() > 1) {
                                g.addVertex(key);
                                nodes.add(reaction);
                        }

                }
                for (ReactionFA node : nodes) {
                        List<String> reactants = node.getReactants();
                        reactants.addAll(node.getProducts());
                        for (String edge : reactants) {
                                if(!isCofactor(edge)){
                                        String source = node.getId();
                                        List<String> destinations = getDestinations(edge, node, nodes);
                                        for(String destination : destinations){
                                                g.addEdge(edge + "-" + String.valueOf(number++), source, destination, EdgeType.DIRECTED);
                                        }
                                }
                        }

                        
                }

                Layout<String, String> layout = new KKLayout(g);
                layout.setSize(new Dimension(1400, 1000)); // sets the initial size of the space
                VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout);
                vv.setPreferredSize(new Dimension(1400, 1000));
                Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
                        @Override
                        public Paint transform(String id) {
                                if (initialIds.contains(id) || id.contains(finalId)) {
                                        return Color.BLUE;
                                } else {
                                        return Color.GREEN;
                                }
                        }
                };

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

        private boolean isCofactor(String edge) {
                for(String cofactor : this.cofactors){
                        if(edge.equals(cofactor)){
                                return true;
                        }
                }
                return false;
        }

        private List<String> getDestinations(String edge, ReactionFA node, List<ReactionFA> reactions) {
                List<String> targets = new ArrayList<>();
                for (ReactionFA key : reactions) {
                        if(!key.getId().equals(node.getId())){
                               if(node.hasSpecies(edge)){
                                       targets.add(key.getId());
                               }
                        }
                }
                
                return targets;
        }

       
}
