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

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.modules.Network.Edge;
import ND.modules.Network.Graph;
import ND.modules.Network.Node;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author bicha
 */
public class World {

        private SimpleBasicDataset networkDataset;
        private Graph network;
        private Map<String, Node> location;
        private List<Bug> population;
        private Random rand;
        private int maxNumberOfNodes, maxNumberOfBugs;
        private double mutationRate;

        public World(SimpleBasicDataset networkDataset, int maxNumberOfNodes, int maxNumberOfBugs, File weightsFile, double mutationRate) {
                this.networkDataset = networkDataset;
                this.maxNumberOfNodes = maxNumberOfNodes;
                this.maxNumberOfBugs = maxNumberOfBugs;
                this.population = new ArrayList<>();
                this.rand = new Random();
                Date date = new Date();
                long time = date.getTime();

                // Initialize the random number generator using the
                // time from above.
                rand.setSeed(time);
                this.mutationRate = mutationRate;

                this.location = new HashMap<>();
                if (this.networkDataset != null) {
                        this.network = this.createGraph(this.networkDataset, getWeights(weightsFile));
                } else {
                        this.network = this.createGraph(weightsFile);
                }

                for (int i = 0; i < this.network.getNumberOfNodes(); i++) {
                        population.add(new Bug(network.getNode(i)));
                }
        }

        public List<Bug> getPopulation() {
                return this.population;
        }

        public void cicle() {
                // System.out.println("reproduction");
                addNewBabies();
                reproduction();
                purgue();


        }

        private Graph createGraph(SimpleBasicDataset networkDataset, Map<String, Double> weights) {

                Graph g = new Graph(null, null);
                SBMLDocument doc = networkDataset.getDocument();

                Model m = doc.getModel();

                for (Species s : m.getListOfSpecies()) {
                        Node n = new Node(s.getId(), s.getName(), weights.get(s.getId()));
                        g.addNode(n);
                        location.put(n.getId(), n);
                }

                for (Reaction r : m.getListOfReactions()) {
                        for (SpeciesReference re : r.getListOfReactants()) {
                                for (SpeciesReference p : r.getListOfProducts()) {
                                        addLane(g, r.getId(), location.get(re.getSpeciesInstance().getId()), location.get(p.getSpeciesInstance().getId()), weights.get(r.getId()));
                                        //addLane(g, r.getId() + "rev", location.get(p.getSpeciesInstance().getId()), location.get(re.getSpeciesInstance().getId()), weights.get(r.getId()));

                                }

                        }
                }

                return g;
        }

        private void addLane(Graph g, String laneId, Node sourceLocNo, Node destLocNo, double weight) {
                Edge lane = new Edge(laneId, sourceLocNo, destLocNo, weight);
                g.addEdges(lane);
        }

        private void growing(Bug b) {
                int count = 0;
                while (b.getSize() < this.maxNumberOfNodes) {
                        Node n = b.getRandomNode(rand);
                        if (n != null) {
                                Edge e = this.network.getRandomEdge(rand, n);
                                if (e != null) {
                                        b.addEdge(e);
                                        b.addNode(e.getDestination());
                                        b.restartScore();
                                } else if (b.getSize() <= 1) {
                                        b.setLifeStatus(false);
                                        b.restartScore();
                                }

                        }
                        if (count++ > 15) {
                                break;
                        }
                }
        }

        private boolean competition(Bug b) {
                Bug fightingB = this.population.get(rand.nextInt(this.population.size()));
                if (b.getWeight() >= fightingB.getWeight()) {
                        return true;
                } else {
                        return false;
                }

        }

        private void reproduction() {
                List<Bug> newBugs = new ArrayList<>();
                for (Bug b : this.population) {
                        this.growing(b);
                        if (competition(b)) {
                                // newBugs.add(b.clone());
                                if (this.rand.nextFloat() < this.mutationRate) {
                                        b.mutate(rand);
                                }
                                // b.augmentLife();
                        } else {
                                b.reduceLife();
                                b.restartScore();
                        }
                }

                for (Bug b : newBugs) {
                        this.population.add(b);
                }
        }

        private void purgue() {
                Collections.sort(this.population, new Comparator<Bug>() {
                        @Override
                        public int compare(Bug o1, Bug o2) {
                                if (o1.getWeight() < o2.getWeight()) {
                                        return 1;
                                } else if (o1.getWeight() == o2.getWeight()) {
                                        return 0;
                                } else {
                                        return -1;
                                }
                        }
                });
                List<Bug> death = new ArrayList<>();

                for (int i = this.maxNumberOfBugs; i < this.population.size(); i++) {
                        death.add(this.population.get(i));
                }
                for (Bug b : death) {
                        this.population.remove(b);
                }
        }

        private Map<String, Double> getWeights(File weightsFile) {
                Map<String, Double> ws = new HashMap<>();
                CsvReader lines;
                try {
                        lines = new CsvReader(new FileReader(weightsFile.getAbsolutePath()));
                        lines.getHeaders();
                        while (lines.readRecord()) {
                                String[] w = lines.getValues();
                                ws.put(w[0], Double.parseDouble(w[1]));

                        }
                } catch (IOException | NumberFormatException e) {
                }

                return ws;
        }

        private void mutation() {
                for (Bug b : this.population) {
                        if (this.rand.nextFloat() < this.mutationRate) {
                                b.mutate(rand);
                        }
                }
        }

        private void addNewBabies() {
                for (int i = 0; i < this.network.getNumberOfNodes(); i++) {
                        population.add(new Bug(network.getRandomNode(rand)));
                }
        }

        private Graph createGraph(File weightsFile) {
                System.out.println("creating graph");
                Graph g = new Graph(null, null);
                CsvReader lines;
                try {
                        lines = new CsvReader(new FileReader(weightsFile.getAbsolutePath()));
                        lines.getHeaders();
                        lines.getValues();
                        int i = 0;
                        boolean weights = false;
                        while (lines.readRecord()) {
                                String[] w = lines.getValues();
                                if (w[4].equals("Vertex type")) {
                                        weights = true;
                                        w = lines.getValues();
                                }
                                if (!weights) {
                                        try {
                                                Node n = new Node(w[1], w[1], 0.0);
                                                Node n2 = new Node(w[2], w[2], 0.0);
                                                g.addNode(n);
                                                g.addNode(n2);

                                                Edge lane = new Edge(w[6], g.getNode(n.getId()), g.getNode(n2.getId()), Double.valueOf(w[3]));
                                                g.addEdges(lane);
                                                /* if (w[4].equals("Bidirectional")) {
                                                 lane = new Edge(w[6] + "-rev", g.getNode(n2.getId()), g.getNode(n.getId()), Double.valueOf(w[3]));
                                                 g.addEdges(lane);
                                                 }*/

                                        } catch (NumberFormatException o) {
                                        }
                                } else {
                                        try {                                               
                                                Node n = g.getNode(w[5]);
                                                n.setWeight(1-Double.valueOf(w[1]));
                                        } catch (Exception o) {
                                        }
                                }

                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }

                System.out.println(g.getNumberOfNodes() + " - " + g.getNumberOfEdges());
                return g;
        }
}
