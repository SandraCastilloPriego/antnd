/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.antNoGraph.network;

import java.util.ArrayList;
import java.util.List;

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

        public Node getNode(String id) {
                for (Node n : nodes) {
                        if (n.getId().contains(id)) {
                                return n;
                        }
                }
                return null;
        }

        public boolean contains(Node node) {
                for (Node n : nodes) {
                        if (n.getId().equals(node.getId())) {
                                return true;
                        }
                }
                return false;
        }

        public List<Edge> getEdges() {
                return edges;
        }

        public void addNode(Node node) {
                if (!this.nodes.contains(node)) {
                        this.nodes.add(node);
                }
        }

        public void addEdge(Edge edge) {
                if (!this.edges.contains(edge)) {
                        this.edges.add(edge);
                }
        }

        public int getNumberOfNodes() {
                return this.nodes.size();
        }

        public int getNumberOfEdges() {
                return this.edges.size();
        }

        public boolean isEmpty() {
                return this.nodes.isEmpty();
        }

        public Node getLastNode() {
                return this.nodes.get(this.nodes.size()-1);
        }
}
