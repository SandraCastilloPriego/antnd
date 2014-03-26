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
package ND.modules.simulation.antNoGraph.network;

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
        
        @Override
        public String toString(){
                String str = "";
                for(Node n : this.nodes){
                        str = str + n.getId().split(" - ")[0] + " - ";
                }
                return str;
        }
}
