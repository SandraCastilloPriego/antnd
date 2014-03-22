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
package ND.modules.antNoGraph;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.antNoGraph.network.Edge;
import ND.modules.antNoGraph.network.Graph;
import ND.modules.antNoGraph.network.Node;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.awt.Dimension;
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
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class AntModuleTask extends AbstractTask {

        private SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private File exchangeReactions, boundsFile;
        private String biomassID;
        private Random rand;
        private int numAnt = 1000;
        private double totalSource = 0;
        private HashMap<String, ReactionFA> reactions;
        private HashMap<String, SpeciesFA> compounds;
        private HashMap<String, String[]> bounds;
        private Map<String, Double> sources;
        private List<String> sourcesList;
        private int n = 0;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JPanel pn;
        private int shortestPath = Integer.MAX_VALUE;
        private Graph graph;

        public AntModuleTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {

                this.networkDS = dataset;
                this.exchangeReactions = parameters.getParameter(AntModuleParameters.exchangeReactions).getValue();
                this.biomassID = parameters.getParameter(AntModuleParameters.objectiveReaction).getValue();
                this.boundsFile = parameters.getParameter(AntModuleParameters.bounds).getValue();

                this.rand = new Random();
                Date date = new Date();
                long time = date.getTime();

                this.reactions = new HashMap<>();
                this.compounds = new HashMap<>();
                this.bounds = new HashMap<>();
                this.sourcesList = new ArrayList<>();

                this.frame = new JInternalFrame("Result", true, true, true, true);
                this.pn = new JPanel();
                this.panel = new JScrollPane(pn);

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
                        System.out.println("Reading sources");
                        this.sources = this.readExchangeReactions();
                        System.out.println("Reading bounds");
                        this.bounds = this.readBounds();
                        System.out.println("Creating world");
                        this.createWorld();
                        System.out.println("Starting simulation");
                        frame.setSize(new Dimension(700, 500));
                        frame.add(this.panel);
                        NDCore.getDesktop().addInternalFrame(frame);

                        for (int i = 0; i < 20; i++) {
                                this.cicle();
                                finishedPercentage = (double) i / 100;
                                if (getStatus() == TaskStatus.CANCELED || getStatus() == TaskStatus.ERROR) {
                                        break;
                                }
                        }
                        if (getStatus() == TaskStatus.PROCESSING) {
                                PrintPaths print = new PrintPaths(this.sourcesList, this.biomassID);
                                try {
                                        this.pn.add(print.printPathwayInFrame(this.graph));
                                } catch (NullPointerException ex) {
                                        System.out.println(ex.toString());
                                }
                        }

                        setStatus(TaskStatus.FINISHED);

                } catch (Exception e) {
                        System.out.println(e.toString());
                        setStatus(TaskStatus.ERROR);
                }
        }

        private Map<String, Double> readExchangeReactions() {
                try {
                        CsvReader exchange = new CsvReader(new FileReader(this.exchangeReactions), '\t');
                        Map<String, Double> exchangeMap = new HashMap<>();

                        try {
                                while (exchange.readRecord()) {
                                        try {
                                                String[] exchangeRow = exchange.getValues();
                                                exchangeMap.put(exchangeRow[0], Double.parseDouble(exchangeRow[1]));
                                                this.sourcesList.add(exchangeRow[0]);
                                                totalSource += Double.parseDouble(exchangeRow[1]);
                                        } catch (IOException | NumberFormatException e) {
                                                e.printStackTrace();
                                        }
                                }
                        } catch (IOException ex) {
                                Logger.getLogger(ND.modules.ants.AntModuleTask.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        return exchangeMap;
                } catch (FileNotFoundException ex) {
                        Logger.getLogger(AntModuleTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
        }

        private void createWorld() {
                SBMLDocument doc = this.networkDS.getDocument();
                Model m = doc.getModel();
                for (Species s : m.getListOfSpecies()) {
                        SpeciesFA specie = new SpeciesFA(s.getId());
                        this.compounds.put(s.getId(), specie);

                        //add the number of initial ants using the sources.. and add them
                        // in the list of nodes with ants
                        if (this.sources.containsKey(s.getId())) {
                                double amount = this.sources.get(s.getId());
                                double antAmount = (amount / this.totalSource) * this.numAnt;
                                if (antAmount < 1) {
                                        antAmount = 1;
                                }
                                for (int i = 0; i < antAmount; i++) {
                                        Ant ant = new Ant(specie.getId());
                                        ant.initAnt();
                                        specie.addAnt(ant);
                                }
                        }
                }

                for (Reaction r : m.getListOfReactions()) {

                        ReactionFA reaction = new ReactionFA(r.getId());
                        String[] b = this.bounds.get(r.getId());
                        if (b != null) {
                                reaction.setBounds(Double.valueOf(b[3]), Double.valueOf(b[4]));
                        } else {
                                try {
                                        KineticLaw law = r.getKineticLaw();
                                        LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                                        LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                                        reaction.setBounds(lbound.getValue(), ubound.getValue());
                                } catch (Exception ex) {
                                        reaction.setBounds(-1000, 1000);
                                }
                        }
                        for (SpeciesReference s : r.getListOfReactants()) {
                                Species sp = s.getSpeciesInstance();
                                reaction.addReactant(sp.getId(), s.getStoichiometry());
                                SpeciesFA spFA = this.compounds.get(sp.getId());
                                if (spFA != null) {
                                        spFA.addReaction(r.getId());
                                } else {
                                        System.out.println(sp.getId());
                                }
                        }

                        for (SpeciesReference s : r.getListOfProducts()) {
                                Species sp = s.getSpeciesInstance();
                                reaction.addProduct(sp.getId(), s.getStoichiometry());
                                SpeciesFA spFA = this.compounds.get(sp.getId());
                                if (spFA != null) {
                                        spFA.addReaction(r.getId());
                                } else {
                                        System.out.println(sp.getId());
                                }
                        }
                        this.reactions.put(r.getId(), reaction);
                }

        }

        private HashMap<String, String[]> readBounds() {
                HashMap<String, String[]> b = new HashMap<>();
                try {

                        CsvReader reader = new CsvReader(new FileReader(this.boundsFile.getAbsolutePath()));

                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                b.put(data[0].replace("-", ""), data);
                        }
                } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(AntModuleParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(AntModuleParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return b;
        }

        public void cicle() {

                List<Ant> antsBiomass = new ArrayList<>();

                for (String compound : compounds.keySet()) {
                        List<String> possibleReactions = getPossibleReactions(compound);

                        if (possibleReactions.size() > 0) {
                                String reactionChoosen = chooseReactions(possibleReactions);
                                this.compounds.get(compound).removeCount();


                                ReactionFA rc = this.reactions.get(reactionChoosen);

                                List<String> toBeAdded, toBeRemoved;
                                if (rc.hasReactant(compound)) {
                                        toBeAdded = rc.getProducts();
                                        toBeRemoved = rc.getReactants();
                                } else {
                                        toBeAdded = rc.getReactants();
                                        toBeRemoved = rc.getProducts();
                                }

                                // get the ants that must be removed from the reactants ..
                                // creates a superAnt with all the paths until this reaction joined..
                                Ant superAnt = new Ant(null);
                                HashMap<Ant, String> combinedAnts = new HashMap<>();
                                for (String s : toBeRemoved) {
                                        SpeciesFA spfa = this.compounds.get(s);

                                        Ant a = spfa.getAnt();
                                        if (a != null) {
                                                //a.addReactionInPath(reactionChoosen, s, count++);
                                                //   a.print();
                                                combinedAnts.put(a, s);
                                                // spfa.removeAnt(a);

                                        } else {
                                                System.out.println("There is something wrong");
                                        }
                                        for (int i = 0; i < rc.getStoichiometry(s); i++) {
                                                a = spfa.getAnt();
                                                if (a != null) {
                                                        spfa.removeAnt(a);
                                                }
                                        }
                                }

                               
                                superAnt.joinGraphs(reactionChoosen , combinedAnts);
                                if (!superAnt.isLost()) {
                                        // superAnt.print();
                                        // superAnt.print();
                                        // move the ants to the products...   

                                        for (String s : toBeAdded) {
                                                SpeciesFA spfa = this.compounds.get(s);
                                                for (int i = 0; i < rc.getStoichiometry(s); i++) {
                                                        Ant newAnt = superAnt.clone();
                                                        newAnt.setLocation(spfa.getId());
                                                        spfa.addAnt(newAnt);
                                                }
                                        }

                                }
                                // When the ants arrive to the biomass
                                if (toBeAdded.contains(this.biomassID)) {
                                        System.out.println("Biomass produced!: " + rc.getId());

                                        SpeciesFA spFA = this.compounds.get(this.biomassID);
                                        antsBiomass.addAll(spFA.getAnts());

                                        for (Ant a : antsBiomass) {

                                                List<String> localPath = a.getPath();
                                                for (String r : localPath) {
                                                        if (this.reactions.containsKey(r)) {
                                                                this.reactions.get(r).addPheromones();
                                                        }
                                                }

                                                if (localPath.size() < shortestPath) {
                                                        this.shortestPath = localPath.size();
                                                        this.graph = a.getGraph();

                                                        Node biomass = new Node(this.biomassID);
                                                        this.graph.addNode(biomass);
                                                        Node lastNode = this.graph.getNode(reactionChoosen);
                                                        Edge edge = new Edge(this.biomassID, lastNode, biomass);
                                                        this.graph.addEdge(edge);
                                                        a.print();
                                                }
                                                spFA.removeAnt(a);
                                                addAnts();
                                        }
                                }


                        } else {
                                this.compounds.get(compound).addCount();
                        }

                }


                this.n++;

                if (this.n > 5) {
                        this.n = 0;
                        for (String key : this.reactions.keySet()) {
                                ReactionFA r = this.reactions.get(key);
                                r.removePheromones();
                        }
                }


        }

        private List<String> getPossibleReactions(String node) {

                List<String> possibleReactions = new ArrayList<>();
                SpeciesFA sp = this.compounds.get(node);
                List<String> connectedReactions = sp.getReactions();

                for (String reaction : connectedReactions) {
                        ReactionFA r = this.reactions.get(reaction);

                        boolean isPossible = true;
                        if (r.hasReactant(node)) {
                                if (r.getub() > 0) {
                                        List<String> reactants = r.getReactants();
                                        for (String reactant : reactants) {
                                                if (!allEnoughAnts(reactant, r.getStoichiometry(reactant))) {
                                                        isPossible = false;
                                                        break;
                                                }
                                        }

                                } else {
                                        isPossible = false;
                                }

                        } else {
                                if (r.getlb() < 0) {
                                        List<String> products = r.getProducts();
                                        for (String product : products) {

                                                if (!allEnoughAnts(product, r.getStoichiometry(product))) {
                                                        isPossible = false;
                                                }
                                        }
                                } else {
                                        isPossible = false;
                                }

                        }
                        if (isPossible) {
                                possibleReactions.add(reaction);
                        }

                }
                return possibleReactions;
        }

        private String chooseReactions(List<String> possibleReactions) {
                RandomSelector selector = new RandomSelector();
                for (String r : possibleReactions) {
                        selector.Add(r, this.reactions.get(r).getPheromones());
                }
                return selector.GetRandom(rand);
        }

        private boolean allEnoughAnts(String species, double stoichiometry) {
                SpeciesFA s = this.compounds.get(species);
                if (s.getNumberOfAnts() >= stoichiometry) {
                        return true;
                }
                return false;
        }

        private void addAnts() {
                for (String source : sources.keySet()) {
                        SpeciesFA sp = this.compounds.get(source);
                        if (sp != null) {
                                for (int i = 0; i < 20; i++) {
                                        Ant newAnt = new Ant(source);
                                        newAnt.initAnt();
                                        sp.addAnt(newAnt);

                                }
                        }
                }
        }
}
