/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.Network2;

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
                for (Edge edge : this.edges) {
                        product *= edge.getWeight();
                }

                return Math.abs(sum * product);// / this.getNumberOfNodes();
        }

        public int getNumberOfNodes() {
                return this.nodes.size();
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

        public List<Edge> getPossiblePaths(String node) {
                List<Edge> paths = new ArrayList<>();
                for (Edge e : this.edges) {
                        if (e.contains(node)) {
                                paths.add(e);
                        }
                }
                return paths;
        }

        public void reduceAnts(String nodeId) {
                for (Node n : this.nodes) {
                        if (n.getId().equals(nodeId)) {
                                n.reduceAnts();
                                break;
                        }
                }
        }

        public void augmentAnts(String nodeId) {
                for (Node n : this.nodes) {
                        if (n.getId().equals(nodeId)) {
                                n.augmentAnts();
                                break;
                        }
                }
        }

        public void addAnt(String string) {
                try {
                        Node n = this.getNode(string);
                        n.augmentAnts();
                } catch (NullPointerException e) {
                        e.printStackTrace();
                }
        }
}
