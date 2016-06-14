/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.DB.Visualize;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class MiniGraph {

    private final List<MiniNode> nodes;
    private final List<MiniEdge> edges;

    public MiniGraph(List<MiniNode> nodes, List<MiniEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<MiniNode> getNodes() {
        return nodes;
    }

    public List<MiniEdge> getEdges() {
        return edges;
    }

    void addEdge(MiniEdge me) {
        this.edges.add(me);
    }

    void addNode(MiniNode mn) {
        this.nodes.add(mn);
    }

    void removeVertex(String c) {
        MiniNode selected = null;
        for (MiniNode n : nodes) {
            if (n.self.contains(c)) {
                selected = n;
                break;
            }
        }
        if (selected != null) {
            this.nodes.remove(selected);
        }
        
    }
    public MiniGraph clone(){
        List<MiniNode> ln = new ArrayList<>();
        ln.addAll(nodes);
        List<MiniEdge> le = new ArrayList<>();
        le.addAll(edges);
        
        MiniGraph newG = new MiniGraph(ln,le);
        return newG;
    }

    boolean contains(String c) {
        for(MiniNode n : nodes){
            if(n.self.equals(c)) return true;
        }
        return false;
    }
}
