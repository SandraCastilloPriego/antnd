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
package ND.modules.simulation.FBA;

import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.simulation.antNoGraph.uniqueId;
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
    double flux = 0.0;

    public Ant(String location) {
        this.path = new ArrayList<>();
        this.location = location;
        this.g = new Graph(null, null);
    }

    public void initAnt(double flux) {
        String l = location + " - " + uniqueId.nextId();
        Node initNode = new Node(l);
        g.addNode2(initNode);
        this.path.add(l);
        this.flux = flux;
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
        ant.setFlux(this.flux);
        return ant;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;

    }

    public void setLocation(String location, ReactionFA rc) {
        this.location = location;
        this.setFlux(this.flux * rc.getStoichiometry(location));
    }

    public void print() {
        System.out.print("size: " + this.getPathSize() + " - location: " + this.location + "//");
        for (String p : this.path) {
            System.out.print(" - " + p.split(" - ")[0]);
        }
        System.out.print("\n");
    }

    public double joinGraphs(String reactionChoosen, HashMap<Ant, String> combinedAnts, double bound, ReactionFA rc) {
        double localFlux = bound;
        for (Ant ant : combinedAnts.keySet()) {
            //  System.out.println(reactionChoosen +"  -  " + combinedAnts.get(ant)+ " - " +bound + " - "+localFlux +" -" +ant.getFlux());

            double f = ant.getFlux() / rc.getStoichiometry(ant.getLocation().split(" : ")[0]);
           //  System.out.println(rc.getStoichiometry(ant.getLocation().split(" : ")[0]) + " - "+f);

            if (f < localFlux) {
                localFlux = f;
            }

//            System.out.println(localFlux);

        }
       // rc.setFlux(localFlux);
        Node node = new Node(reactionChoosen + " : " + localFlux + " - " + uniqueId.nextId());

        for (Ant ant : combinedAnts.keySet()) {
            this.pathsize = this.pathsize + ant.getPathSize();
            g.addNode2(node);
            Graph antGraph = ant.getGraph();

            for (Node n : antGraph.getNodes()) {
                g.addNode2(n);
            }
            for (Edge e : antGraph.getEdges()) {
                try {
                    Node inGSource = this.g.getNode(e.getSource().getId().split(" - ")[0]);
                    Node inGDestination = this.g.getNode(e.getDestination().getId().split(" - ")[0]);
                    if (inGSource != null) {
                        e.setSource(inGSource);
                    }
                    if (inGDestination != null) {
                        e.setDestination(inGDestination);
                    }
                    g.addEdge(e);
                } catch (NullPointerException exception) {
                }
            }
            //System.out.println(ant.getPath().get(ant.getPath().size() - 1));

            Node lastNode = antGraph.getNode(ant.getPath().get(ant.getPath().size() - 1).split(" - ")[0]);
            Node inGNode = this.g.getNode(lastNode.getId().split(" - ")[0]);
            if (inGNode != null) {
                lastNode = inGNode;
            }
            // System.out.println(lastNode);
            Edge edge = new Edge(combinedAnts.get(ant) + " : " + localFlux + " - " + uniqueId.nextId(), lastNode, node);
            g.addEdge(edge);

            for (String p : ant.getPath()) {
                this.path.add(p);
            }
        }
        //System.out.println("-------------------------");
        this.pathsize++;
        this.path.add(node.getId());
        this.flux = localFlux;

        if (this.getPathSize() > 500) {
            this.lost = true;
        }
        return localFlux;
    }

    public void joinObjectiveGraphs(Ant newAnt, List<String> sources) {
        this.g.addGraph(newAnt.getGraph());
        List<Node> sourceNodes = new ArrayList<>();
        for (String source : sources) {
            Node s = this.g.getNode(source);
            if (s != null) {
                sourceNodes.add(s);
            }
        }

        for (Node sourceNode : sourceNodes) {
            List<Node> connNodes = this.g.getConnectedAsSource(sourceNode);
            for (Node rNode : connNodes) {
                updateFlux(rNode);
            }
        }
    }

    public void updateFlux(Node rNode) {
        List<Node> reactants = this.g.getConnectedAsDestination(rNode);
        double localFlux = Double.MAX_VALUE;
        for (Node reactant : reactants) {
            double reactantFlux = Double.valueOf(reactant.getId().split(" - ")[0].split(" : ")[1]);
            if (localFlux > reactantFlux) {
                localFlux = reactantFlux;
            }
            String newId = reactant.getId().split(" - ")[0].split(" : ")[0];
            newId = newId + " : " + String.valueOf(localFlux) + " - " + reactant.getId().split(" - ")[1];
            reactant.setId(newId);
            this.flux = localFlux;
            updateFlux(reactant);
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

    public int getPathSize() {
        return this.pathsize;
    }

    public boolean compare(Ant ant) {
        for (String p : this.getPath()) {
            for (String op : ant.getPath()) {
                if (!p.split(" - ")[0].equals(op.split(" - ")[0])) {
                    return false;
                }
            }
        }
        return true;
    }

    public double getFlux() {
        return this.flux;
    }

    public void setFlux(double flux) {
        this.flux = flux;
    }
}
