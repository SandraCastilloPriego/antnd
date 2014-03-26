/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.simulation.antNoGraph;

import ND.modules.simulation.antNoGraph.network.Edge;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class Ant {

        private Graph g;
        List<String> path;
        String location;
        boolean lost = false;
        int pathsize = 0;

        public Ant(String location) {
                this.path = new ArrayList<>();
                this.location = location;
                this.g = new Graph(null, null);
        }

        public void initAnt() {
                Node initNode = new Node(location + " - " + uniqueId.nextId());
                g.addNode(initNode);
                this.path.add(location + " - " + uniqueId.nextId());
        }

        public Graph getGraph() {
                return this.g;
        }

        public void setGraph(Graph g) {
                this.g = g;
        }

        public void removePath() {
                this.path.clear();
        }

        public List<String> getPath() {
                return this.path;
        }

        public void setPath(List<String> path) {
                this.path = path;
        }

        @Override
        public Ant clone() {
                Ant ant = new Ant(this.location);
                ant.setGraph(this.g);
                ant.setPath(path);
                ant.setPathSize(this.pathsize);
                return ant;
        }

        public String getLocation() {
                return location;
        }

        public void setLocation(String location) {
                this.location = location;
        }

        public void print() {
                System.out.print("size: "+ this.getPathSize() +" - location: " + this.location + "//");
                for (String p : this.path) {
                        System.out.print(" - " + p.split(" - ")[0]);
                }
                System.out.print("\n");
        }

        public void joinGraphs(String reactionChoosen, HashMap<Ant, String> combinedAnts) {
                Node node = new Node(reactionChoosen + " - " + uniqueId.nextId());
                
                for (Ant ant : combinedAnts.keySet()) {
                        this.pathsize = this.pathsize + ant.getPathSize();
                        g.addNode(node);
                        Graph antGraph = ant.getGraph();

                        for (Node n : antGraph.getNodes()) {
                                g.addNode(n);
                        }
                        for (Edge e : antGraph.getEdges()) {
                                g.addEdge(e);
                        }
                        //System.out.println(ant.getPath().get(ant.getPath().size() - 1));

                        Node lastNode = antGraph.getNode(ant.getPath().get(ant.getPath().size() - 1).split(" - ")[0]);
                        // System.out.println(lastNode);
                        Edge edge = new Edge(combinedAnts.get(ant) + " - " + uniqueId.nextId(), lastNode, node);
                        g.addEdge(edge);

                        for (String p : ant.getPath()) {
                                this.path.add(p);
                        }
                }
                this.pathsize++;
                this.path.add(node.getId());
                if (this.getPathSize() > 500) {
                        this.lost = true;
                }

        }

        public boolean isLost() {
                return this.lost;
        }

        public boolean contains(String id) {
                for (String p : this.path) {
                        if (p.contains(id)) {
                                return true;
                        }
                }
                return false;
        }

        public void setPathSize(int pathsize) {
                this.pathsize = pathsize;
        }
        
        public int getPathSize(){
                return this.pathsize;
        }
}
