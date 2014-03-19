/*
 * Copyright 2007-2010 VTT Biotechnology
 * This file is part of GopiBugs.
 *
 * GopiBugs is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * GopiBugs is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GopiBugs; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.antSimple;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.modules.Network3.Ant;
import ND.modules.Network3.Edge;
import ND.modules.Network3.Graph;
import ND.modules.Network3.Node;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class AntSimpleModuleTask extends AbstractTask {

        private SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private Graph network;
        private File exchanceReactions;
        private String biomassID;
        private Random rand;
        private int numAnt = 1000;
        private List<Ant> ants;

        public AntSimpleModuleTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {

                this.networkDS = dataset;
                this.exchanceReactions = parameters.getParameter(AntSimpleModuleParameters.exchangeReactions).getValue();
                this.biomassID = parameters.getParameter(AntSimpleModuleParameters.objectiveReaction).getValue();

                this.ants = new ArrayList<>();
                this.rand = new Random();
                Date date = new Date();
                long time = date.getTime();

                // Initialize the random number generator using the
                // time from above.
                rand.setSeed(time);


        }

        @Override
        public String getTaskDescription() {
                return "Starting simulation... ";
        }

        @Override
        public double getFinishedPercentage() {
                return finishedPercentage;
        }

        @Override
        public void cancel() {
                setStatus(TaskStatus.CANCELED);
        }

        @Override
        public void run() {
                try {
                        setStatus(TaskStatus.PROCESSING);

                        this.network = this.createGraph(this.networkDS);
                        createAnts(this.exchanceReactions, this.numAnt);

                        antSimulation(this.network);

                        setStatus(TaskStatus.FINISHED);

                } catch (Exception e) {
                        e.printStackTrace();
                        setStatus(TaskStatus.ERROR);
                }
        }

        private Graph createGraph(SimpleBasicDataset networkDataset) {

                Graph g = new Graph(null, null);
                SBMLDocument doc = networkDataset.getDocument();

                Model m = doc.getModel();

                for (Species s : m.getListOfSpecies()) {
                         g.addEdges(new Edge(s));
                }

                for (Reaction r : m.getListOfReactions()) {
                         g.addNode(new Node(r));
                }

                return g;
        }

        private void antSimulation(Graph network) {
                for (int i = 0; i < 10000; i++) {
                        System.out.println(i);
                        cycle(network);
                }

        }

        private void cycle(Graph network) {
                for (int a = 0; a < this.numAnt; a++) {
                        Ant ant = this.ants.get(this.rand.nextInt(this.numAnt));
                        
                        boolean directionForward = true;
                }
        }

        private void createAnts(File exchangeReactions, int numAnt) {
                try {
                        CsvReader exchange;
                        Map<String, Double> exchangeMap = new HashMap<>();
                        /* Read exchange reaction*/
                        double sum = 0;
                        exchange = new CsvReader(new FileReader(exchangeReactions), '\t');
                        try {
                                while (exchange.readRecord()) {
                                        try {
                                                String[] exchangeRow = exchange.getValues();
                                                exchangeMap.put(exchangeRow[1], Double.parseDouble(exchangeRow[2]));
                                                sum += Double.parseDouble(exchangeRow[2]);
                                        } catch (IOException | NumberFormatException e) {
                                                e.printStackTrace();
                                        }
                                }
                        } catch (IOException ex) {
                                Logger.getLogger(AntSimpleModuleTask.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        for (Map.Entry pairs : exchangeMap.entrySet()) {
                                double proportion = (Double) pairs.getValue() / sum;
                                int numAnts = (int) (proportion * numAnt);
                                if (numAnts == 0) {
                                        numAnts = 1;
                                }
                                for (int i = 0; i < numAnts; i++) {
                                        Ant a = new Ant((String) pairs.getKey());
                                        this.network.addAnt((String) pairs.getKey());
                                        this.ants.add(a);
                                }
                        }

                } catch (FileNotFoundException ex) {
                        Logger.getLogger(AntSimpleModuleTask.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        public List<Ant> getRandomAnt(String nodeId, int number) {
                if (number == 0) {
                        number = 1;
                }
                List<Ant> antsInNode = new ArrayList<>();
                for (Ant a : ants) {
                        if (a.getNodeId() == null ? nodeId == null : a.getNodeId().equals(nodeId)) {
                                antsInNode.add(a);
                        }
                }
                List<Ant> result = new ArrayList<>();
                for (int i = 0; i < number; i++) {
                        if (!antsInNode.isEmpty()) {
                                Ant a = antsInNode.get(rand.nextInt(antsInNode.size()));
                                result.add(a);
                                antsInNode.remove(a);
                        }
                }
                return result;
        }

        private void moveAnt(Edge selectedEdge, boolean direction) {
                ListOf<SpeciesReference> substrates, products;
                double totalEndingSize = 0.0;
                if (direction) {
                      //  substrates = selectedEdge.getSubstrates();
                        //products = selectedEdge.getProducts();
                        //totalEndingSize = selectedEdge.getNumProducts();
                } else {
                      //  substrates = selectedEdge.getProducts();
                        //products = selectedEdge.getSubstrates();
                     //   totalEndingSize = selectedEdge.getNumSubstrates();
                }
                double totalSize = 0;
                List<Ant> localAnts = new ArrayList<>();
              /*  for (SpeciesReference s : substrates) {
                        List<Ant> moving = this.getRandomAnt(s.getSpecies(), (int) s.getStoichiometry());
                        for (Ant ant : moving) {
                             //   totalSize += ant.getSize();
                                this.network.reduceAnts(s.getSpecies());
                        }
                        localAnts.addAll(moving);
                }*/

                double size = totalSize / totalEndingSize;
               
              /*  for (SpeciesReference s : products) {
                        double stoi = s.getStoichiometry();
                        if (stoi == 0) {
                                stoi = 1;
                        }
                        for (int i = 0; i < stoi; i++) {
                                Ant ant = new Ant(s.getSpecies());
                                ant.setPreviousNodeId(selectedEdge.getId());
                              //  ant.updateSize(size);
                                this.network.augmentAnts(s.getSpecies());
                                this.ants.add(ant);
                        }
                }*/
                
                this.ants.removeAll(localAnts);

        }
}
