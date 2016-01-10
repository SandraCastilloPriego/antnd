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
package ND.data.network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author scsandra
 */
public class Graph {

    private final List<Node> nodes;
    private final Map<String, Color> colors;
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

        this.colors = new HashMap<>();
    }

    public Graph(List<Node> nodes, List<Edge> edges, Map<String, Color> colors) {
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

        this.colors = colors;

    }

    public Map<String, Color> getColors() {
        return colors;
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

    public void addNode2(Node node) {
        if (!IsInNodes(node)) {
            this.nodes.add(node);
        }
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void addEdge2(Edge edge) {
        if (!isInEdges(edge)) {
            this.edges.add(edge);
        }
    }

    public void addEdge(Edge edge) {
        this.edges.add(edge);
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
        return this.nodes.get(this.nodes.size() - 1);
    }

    @Override
    public String toString() {
        String str = "";
        for (Node n : this.nodes) {
            str = str + n.getId().split(" - ")[0] + " - ";
        }
        return str;
    }

    public void addGraph(Graph g) {
        if (this == g) {
            return;
        }
        for (Node n : g.getNodes()) {
            if (n.getId() != null) {
                if (!this.IsInNodes(n)) {
                    this.nodes.add(n);
                }
            }
        }
        for (Edge e : g.getEdges()) {
            Node source = e.getSource();
            if (source != null) {
                // System.out.println("source : "+ source.getId());
                Node newSource = this.getNode(source.getId().split(" - ")[0]);
                if (newSource != null) {
                    e.setSource(newSource);
                }
                Node destination = e.getDestination();
                // System.out.println("destination : "+ destination.getId());
                if (destination.getId() != null) {
                    Node newDestination = this.getNode(destination.getId().split(" - ")[0]);
                    if (newDestination != null) {
                        e.setDestination(newDestination);
                    }
                   // if (!this.isInEdges(e)) {

                    // }
                }
            }
            this.edges.add(e);
        }
    }

    @Override
    public Graph clone() {
        Graph g = new Graph(null, null);
        for (Node n : this.nodes) {
            g.addNode(n.clone());
        }
        for (Edge e : this.edges) {
            Edge edge = e.clone();
            Node source = g.getNode(edge.getSource().getId());
            Node destination = g.getNode(edge.getDestination().getId());
            edge.setSource(source);
            edge.setDestination(destination);
            g.addEdge(edge);
        }

        return g;
    }

    public boolean IsInNodes(Node node) {
        for (Node n : this.nodes) {
            if (n.getId().split(" - ")[0].split(" : ")[0].equals(node.getId().split(" - ")[0].split(" : ")[0])) {
                return true;
            }
        }
        return false;
    }

    public boolean IsInNodes(String node) {
        for (Node n : this.nodes) {
            if (n.getId().split(" - ")[0].split(" : ")[0].equals(node)) {
                return true;
            }
        }
        return false;
    }

    public boolean IsInSource(String node) {
        for (Edge e : this.edges) {
            Node n = e.getSource();
            if (n == null) {
                continue;
            }
            if (n.getId().split(" - ")[0].split(" : ")[0].equals(node.split(" - ")[0].split(" : ")[0])) {
                return true;
            }
        }
        return false;
    }

    public boolean isInEdges(Edge edge) {
        for (Edge thisEdge : this.edges) {
            String source = edge.getSource().getId().split(" - ")[0];
            String destination = edge.getDestination().getId().split(" - ")[0];
            String thisSource = thisEdge.getSource().getId().split(" - ")[0];
            String thisDestination = thisEdge.getDestination().getId().split(" - ")[0];
            if (source.equals(thisSource) && destination.equals(thisDestination)) {
                return true;
            }
        }
        return false;
    }

    public List<Edge> getEdges(String n, boolean source) {
        List<Edge> edgesN = new ArrayList<>();
        for (Edge e : this.edges) {
            if (source) {
                if (e.getSource().getId().contains(n)) {
                    edgesN.add(e);
                }
            } else {
                if (e.getDestination().getId().contains(n)) {
                    edgesN.add(e);
                }
            }
        }
        return edgesN;
    }

    public List<Node> getConnectedAsSource(Node n) {
        List<Node> connectedNodes = new ArrayList<>();
        for (Edge e : edges) {
            if (e.getSource() == n) {
                connectedNodes.add(n);
            }
        }
        return connectedNodes;
    }

    public List<Node> getConnectedAsDestination(Node n) {
        List<Node> connectedNodes = new ArrayList<>();
        for (Edge e : edges) {
            if (e.getDestination() == n) {
                connectedNodes.add(n);
            }
        }
        return connectedNodes;
    }

    public List<String> getDeadEnds() {
        List<String> deadEnds = new ArrayList<>();

        for (Edge e : edges) {
            boolean isSource = false;
            Node destination = e.getDestination();
            if (destination.getId().contains("extracellular") || destination.getId().contains("boundary")) {
                continue;
            }
            for (Edge e2 : edges) {
                if (e2.getSource() != null && e2.getSource().getId().split(" - ")[0].equals(destination.getId().split(" - ")[0])) {
                    isSource = true;
                }
            }

            if (!isSource && !deadEnds.contains(destination.getId().split(" - ")[0])) {
                deadEnds.add(destination.getId().split(" - ")[0]);
            }
        }
        
        for (Edge e : edges) {
            boolean isSource = false;
            Node destination = e.getSource();
            if (destination == null) continue;
            if (destination.getId().contains("extracellular") || destination.getId().contains("boundary")) {
                continue;
            }
            for (Edge e2 : edges) {
                if (e2.getDestination() != null && e2.getDestination().getId().split(" - ")[0].equals(destination.getId().split(" - ")[0])) {
                    isSource = true;
                }
            }

            if (!isSource && !deadEnds.contains(destination.getId().split(" - ")[0])) {
                deadEnds.add(destination.getId().split(" - ")[0]);
            }
        }
        return deadEnds;
    }

    public void removeNode(String node) {
        List<Node> toBeRemove = new ArrayList<>();
        List<Edge> toBeRemoveEdge = new ArrayList<>();

        for (Node n : this.nodes) {
            if (n.getId().contains(node)) {
                toBeRemove.add(n);
            }
        }
        for (Node r : toBeRemove) {
            this.nodes.remove(r);
            for (Edge e : this.edges) {
                if (e.getSource() == r || e.getDestination() == r) {
                    toBeRemoveEdge.add(e);
                }
            }
        }
        for(Edge e : toBeRemoveEdge){
            this.edges.remove(e);
        }
    }

}
