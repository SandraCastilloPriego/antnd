/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author scsandra
 */
public class Graph {

        private final List<Node> nodes;
        private final List<Edge> edges;       
        
        public Graph(List<Node> nodes, List<Edge> edges) {
                if (nodes == null) {
                        this.nodes = new ArrayList<>();
                } else {
                        this.nodes = nodes;
                }

                if (edges == null) {
                        this.edges = new ArrayList<>();
                } else {
                        this.edges = edges;
                }
        }

        public List<Node> getNodes() {
                return nodes;
        }

        public List<Edge> getEdges() {
                return edges;
        }

        public void addNode(Node node) {
                if (!this.nodes.contains(node)) {
                        this.nodes.add(node);
                }
        }

        public void addEdges(Edge edge) {
                if (!this.edges.contains(edge)) {
                        this.edges.add(edge);
                }
        }

        public double getScore() {
                double sum = 0, product = 1;
                for (Node node : this.nodes) {
                        sum += node.getWeight();
                }
                /*for (Edge edge : this.edges) {
                        product *= edge.getWeight();
                }*/

                return Math.abs(sum * product);// / this.getNumberOfNodes();
        }

        public int getNumberOfNodes() {
                return this.nodes.size();
        }
        
        public int getNumberOfEdges() {
                return this.edges.size();
        }

        public Node getRandomNode(Random rand) {
                if (this.nodes.size() > 0) {
                        return this.nodes.get(rand.nextInt(this.nodes.size()));
                } else {
                        return null;
                }
        }

        public Node getNode(int index) {
                return this.nodes.get(index);
        }

        public Node getNode(String id) {
                for (Node n : this.nodes) {
                        if (n.getId() == null ? id == null : n.getId().equals(id)) {
                                return n;
                        }
                }
                return null;
        }

        @Override
        public Graph clone() {
                Graph g = new Graph(null, null);
                for (Edge e : this.edges) {
                        Edge en = new Edge();
                        Node source = e.getSource();
                        Node destination = e.getDestination();
                        en.setId(e.getId());
                        en.setWeight(e.getWeight());
                        Node nSource = g.getNode(source.getId());
                        Node nDestination = g.getNode(destination.getId());
                        if (nSource == null) {
                                nSource = source.clone();
                                g.addNode(nSource);
                        }
                        if (nDestination == null) {
                                nDestination = destination.clone();
                                g.addNode(nDestination);
                        }

                        en.setSource(nSource);
                        en.setDestination(nDestination);
                        g.addEdges(en);
                }


                return g;
        }

        public Edge getRandomEdge(Random rand, Node n) {
                List<Edge> es = new ArrayList<>();
                for (Edge e : this.edges) {
                        if (e.getSource() == n) {
                                es.add(e);
                        }
                }
                if (es.size() > 0) {
                        return es.get(rand.nextInt(es.size()));
                } else {
                        return null;
                }
        }

        public void mutate(Random rand) {
                if (this.nodes.size() > 0) {
                        List<Node> atEdge = this.getNodesAtEdges();
                        if (atEdge.size() > 0) {
                                Node n = atEdge.get(rand.nextInt(atEdge.size()));
                                List<Edge> es = new ArrayList<>();
                                for (Edge e : this.edges) {
                                        if (e.getSource() == n || e.getDestination() == n) {
                                                es.add(e);
                                        }
                                }
                                this.nodes.remove(n);
                                for (Edge e : es) {
                                        this.edges.remove(e);
                                }
                        }

                }
        }

        public List<Node> getNodesAtEdges() {
                List<Node> atEdges = new ArrayList<>();
                for (Node n : this.nodes) {
                        boolean source = false;
                        boolean destination = false;
                        for (Edge e : this.edges) {
                                if (e.getSource() == n) {
                                        source = true;
                                }
                                if (e.getDestination() == n) {
                                        destination = true;
                                }
                        }
                        if (!source || !destination) {
                                atEdges.add(n);
                        }
                }
                return atEdges;
        }
        
        public String toString(){
                String text = "";

                String initial = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:yed=\"http://www.yworks.com/xml/yed/3\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\">";
                text = text.concat(initial + "\n");



                String opening = "\t<key for=\"graphml\" id=\"d0\" yfiles.type=\"resources\"/>"
                        + "\t<key for=\"port\" id=\"d1\" yfiles.type=\"portgraphics\"/>"
                        + "\t<key for=\"port\" id=\"d2\" yfiles.type=\"portgeometry\"/>"
                        + "\t<key for=\"port\" id=\"d3\" yfiles.type=\"portuserdata\"/>"
                        + "\t<key attr.name=\"url\" attr.type=\"string\" for=\"node\" id=\"d4\"/>"
                        + "\t<key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/>"
                        + "\t<key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/>"
                        + "\t<key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d7\"/>"
                        + "\t<key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d8\"/>"
                        + "\t<key for=\"edge\" id=\"d9\" yfiles.type=\"edgegraphics\"/>"
                        + "\t<graph edgedefault=\"directed\" id=\"G\">";
                text = text.concat(opening + "\n");




                for (Node node : this.nodes) {
                        String n = "\t <node id=\"" + node.getId() + "\">\n\t\t<data key=\"d6\">\n"
                                + "\t\t\t<y:ShapeNode>\n"
                                + "\t\t\t\t<y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"20\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\"  modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" visible=\"true\">" + node.getId() + "</y:NodeLabel>\n"
                                + "\t\t\t</y:ShapeNode>\n"
                                + "\t\t</data>\n"
                                + "\t</node>";
                        text = text.concat(n + "\n");
                }



                for (Edge e : this.edges) {
                        String source = null, destination = null, arrowSource = "none", arrowTarget = "none";
                        if (e.getId().contains("rev")) {
                                destination = e.getSource().getId();
                                source = e.getDestination().getId();
                                arrowSource = "standard";
                        } else {
                                source = e.getSource().getId();
                                destination = e.getDestination().getId();
                                arrowTarget = "standard";
                        }
                        String ed = "\t<edge id=\"" + e.getId() + "\" source=\"" + source + "\" target=\"" + destination + "\">\n\t\t<data key=\"d9\">\n"
                                + "\t\t\t<y:PolyLineEdge>\n"
                                + "\t\t\t\t<y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n"
                                + "\t\t\t\t<y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n"
                                + "\t\t\t\t<y:Arrows source=\"" + arrowSource + "\" target=\"" + arrowTarget + "\"/>\n"
                                + "\t\t\t\t<y:BendStyle smoothed=\"false\"/>\n"
                                + "\t\t\t</y:PolyLineEdge>\n"
                                + "\t\t</data>\n"
                                + "\t</edge>";
                        text = text.concat(ed + "\n");
                }

                String fin = "</graph>\n</graphml>";
                text = text.concat(fin + "\n");

                return text;
        }
}
