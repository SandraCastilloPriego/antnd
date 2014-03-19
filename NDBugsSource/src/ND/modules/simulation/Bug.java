/*
 * Copyright 2011
 * This file is part of XXXXXX.
 * XXXXXX is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * XXXXXX is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * XXXXXX; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.simulation;

import ND.modules.Network.Edge;
import ND.modules.Network.Graph;
import ND.modules.Network.Node;
import java.util.List;
import java.util.Random;

/**
 *
 * @author bicha
 */
public final class Bug {

        private Graph network;
        private boolean isAlive = true;
        private double weight = 0.0;
        private int life = 3;

        public Bug() {
        }

        public Bug(Node n) {
                this.network = new Graph(null, null);
                this.network.addNode(n);
                this.restartScore();
        }

        public double getWeight() {
                return weight;
        }

        public void restartScore() {
                if (this.getSize() == 0) {
                        this.isAlive = false;
                }
                if (!this.isAlive) {
                        this.weight = 0;
                } else {
                        this.weight = this.network.getScore();
                }
        }

        public Graph getSubNetwork() {
                return network;
        }

        public Node getRandomNode(Random rand) {
                return this.network.getRandomNode(rand);
        }

        @Override
        public Bug clone() {
                Bug b = new Bug();
                // b.network = network;
                b.network = network.clone();
                b.restartScore();
                return b;
        }

        public void addNode(Node n) {
                this.network.addNode(n);
        }

        public void addEdge(Edge e) {
                this.network.addEdges(e);
        }

        public void setLifeStatus(Boolean isAlive) {
                this.isAlive = isAlive;
        }

        public boolean isAlive() {
                if (this.life <= 0) {
                        this.isAlive = false;
                }
                return this.isAlive;
        }

        @Override
        public String toString() {
                String text = "graph [\n";
                if (this.network != null) {
                        for (Node n : this.network.getNodes()) {
                                text += n.getId() + ',';
                        }
                        text += "\n]";

                }
                text += "\n " + this.getWeight();
                text += "\n ----------------------------------------------------------\n";
                return text;
        }

        public int getSize() {
                return this.network.getNumberOfNodes();
        }

        void mutate(Random rand) {
                this.network.mutate(rand);
                Node n = getRandomNode(rand);
                if (n != null) {
                        Edge e = this.network.getRandomEdge(rand, n);
                        if (e != null) {
                                addEdge(e);
                                addNode(e.getDestination());
                                restartScore();
                        }
                }
        }
        
        public List<Node> getNodesAtEdges(){
                return this.network.getNodesAtEdges();
        }

        public void reduceLife() {
                this.life--;
        }

        public void augmentLife() {
                this.life++;
        }
}
