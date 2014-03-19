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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
        private int numAnt = 10000;
        private double totalSource = 0;
        private HashMap<String, ReactionFA> reactions;
        private HashMap<String, SpeciesFA> compounds;
        private HashMap<String, String[]> bounds;
        private Map<String, Double> sources;
        private List<String> sourcesList;
        private Set<String> nodesWithAnts;
        private int n = 0;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JPanel pn;

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
                this.nodesWithAnts = new HashSet<>();
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

                        for (int i = 0; i < 100; i++) {
                                this.cicle();
                        }

                        for (String key : reactions.keySet()) {
                                ReactionFA reaction = reactions.get(key);
                                System.out.println(reaction.getId() + ", " + reaction.getPheromones());
                        }
                        PrintPaths print = new PrintPaths(this.sourcesList, this.biomassID);
                        this.pn.add(print.printPathwayInFrame(this.reactions));

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
                // System.out.println("Setting the compounds");
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
                                        specie.addAnt(new Ant());
                                }
                                this.nodesWithAnts.add(s.getId());
                        }
                }

                // System.out.println("Setting the reactions");

                for (Reaction r : m.getListOfReactions()) {

                        ReactionFA reaction = new ReactionFA(r.getId());

                        //System.out.println("Setting bounds");
                        String[] b = this.bounds.get(r.getId());
                        if (b != null) {
                                reaction.setBounds(Double.valueOf(b[3]), Double.valueOf(b[4]));
                        } else {
                                reaction.setBounds(-1000, 1000);
                        }
                        //System.out.println("List of reactants");
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

                        //System.out.println("List of products");
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
                //


                Set<String> newNodesWithFwdAnts = new HashSet<>();
                for (String node : this.nodesWithAnts) {

                        //  System.out.println("Getting possible reactions");
                        List<String> possibleReactions = getPossibleReactions(node, true);

                        if (possibleReactions.size() > 0) {
                                String reactionChoosen = chooseReactions(possibleReactions);
                                //  System.out.println(node + " - " + reactionChoosen);
                                ReactionFA rc = this.reactions.get(reactionChoosen);


                                List<String> toBeAdded, toBeRemoved;
                                if (rc.hasReactant(node)) {
                                        toBeAdded = rc.getProducts();
                                        toBeRemoved = rc.getReactants();
                                } else {
                                        toBeAdded = rc.getReactants();
                                        toBeRemoved = rc.getProducts();
                                }
                                //  System.out.println("1");
                                List<Ant> movingAnts = new ArrayList<>();
                                for (String s : toBeRemoved) {
                                        SpeciesFA spfa = this.compounds.get(s);
                                        for (int i = 0; i < rc.getStoichiometry(s); i++) {
                                                Ant ant = spfa.getAnt(rand);
                                                if (ant != null) {
                                                        ant.addReactionInPath(reactionChoosen);
                                                        movingAnts.add(ant);
                                                }
                                        }
                                }

                                // System.out.println("2");
                                if (!movingAnts.isEmpty()) {
                                        for (String s : toBeAdded) {
                                                SpeciesFA spfa = this.compounds.get(s);
                                                for (int i = 0; i < rc.getStoichiometry(s); i++) {
                                                        if (i < movingAnts.size()) {
                                                                Ant ant = movingAnts.get(i);
                                                                // ant.addReactionInPath(reactionChoosen);
                                                                spfa.addAnt(ant);
                                                        } else {
                                                                //  System.out.println("failing here 1");
                                                                int index = rand.nextInt(movingAnts.size());
                                                                // System.out.println(movingAnts.size() + " - " + i + " - " + index);
                                                                Ant ant = movingAnts.get(index);
                                                                if (ant != null) {
                                                                        ant = ant.clone();
                                                                        // ant.addReactionInPath(reactionChoosen);
                                                                        spfa.addAnt(ant);
                                                                }
                                                                //  System.out.println("failing here 2");
                                                        }
                                                }
                                        }
                                }

                                //   System.out.println("3");

                                if (toBeAdded.contains(this.biomassID)) {
                                        System.out.println("Biomass produced!: " + rc.getId());
                                        //  System.out.println("biomass 1");
                                        // Adds pheromones
                                        SpeciesFA spFA = this.compounds.get(this.biomassID);
                                        List<Ant> ants = spFA.getAnts();
                                        List<List<String>> goodPaths = new ArrayList<>();
                                        for (Ant ant : ants) {
                                                ant.addReactionInPath(rc.getId());
                                                List<String> path = ant.getPath();
                                                
                                                System.out.println(path.size());
                                                // if (path.contains(rc.getId())) {
                                                goodPaths.add(path);
                                                // }
                                                this.backToSources(ant);
                                        }
                                        List<String> shortestPath = getShortestPath(goodPaths);
                                        if (shortestPath != null) {
                                                for (String r : shortestPath) {
                                                        System.out.print(r + " - ");
                                                        this.reactions.get(r).addPheromones();
                                                }
                                                System.out.print("\n");
                                        }
                                        // System.out.println("hola2");
                                        //  System.out.println("biomass 2");
                                        // Puts the ants back to the sources



                                        //choses the species that contains the biomass compound
                                        List<String> species;
                                        if (rc.hasReactant(this.biomassID)) {
                                                species = rc.getReactants();
                                        } else {
                                                species = rc.getProducts();
                                        }

                                        // all of them must go back to sources
                                        for (String sp : species) {
                                                if (!sp.contains(this.biomassID)) {
                                                        for (int i = 0; i < rc.getStoichiometry(sp); i++) {
                                                                backToSources(sp);
                                                        }
                                                }
                                        }
                                        //    System.out.println("biomass 3");
                                } else {
                                        newNodesWithFwdAnts.addAll(toBeAdded);
                                }


                        } else {
                                newNodesWithFwdAnts.add(node);
                        }

                }


                this.nodesWithAnts = newNodesWithFwdAnts;

                this.n++;

                if (this.n > 10) {
                        this.n = 0;
                        for (String key : this.reactions.keySet()) {
                                ReactionFA r = this.reactions.get(key);
                                r.removePheromones();
                        }
                        // this.AddNewAnts();
                }



        }

        private List<String> getPossibleReactions(String node, boolean fwd) {
                List<String> possibleReactions = new ArrayList<>();
                SpeciesFA sp = this.compounds.get(node);
                if (sp == null) {
                        System.out.println("Node: " + node);
                } else {
                        List<String> connectedReactions = sp.getReactions();

                        for (String reaction : connectedReactions) {
                                ReactionFA r = this.reactions.get(reaction);
                                if (r == null) {
                                        System.out.println(reaction);

                                } else {
                                        boolean isPossible = true;
                                        if (r.hasReactant(node)) {
                                                if (fwd && r.getub() > 0 || (!fwd && r.getlb() < 0)) {
                                                        List<String> reactants = r.getReactants();
                                                        for (String reactant : reactants) {
                                                                if (!allEnoughAnts(reactant, r.getStoichiometryReactant(reactant))) {
                                                                        isPossible = false;
                                                                        break;
                                                                }
                                                        }
                                                }
                                        } else if (!r.hasReactant(node)) {
                                                if (r.getlb() < 0 || (!fwd && r.getub() > 0)) {
                                                        List<String> products = r.getProducts();
                                                        for (String product : products) {
                                                                if (!allEnoughAnts(product, r.getStoichiometryProduct(product))) {
                                                                        isPossible = false;
                                                                        break;
                                                                }
                                                        }
                                                }

                                        }
                                        if (isPossible) {
                                                possibleReactions.add(reaction);
                                        }
                                }
                        }
                }

                return possibleReactions;
        }

        private String chooseReactions(List<String> possibleReactions) {
                int sum = 0;
                for (String reaction : possibleReactions) {
                        sum = sum + this.reactions.get(reaction).getPheromones();
                }
                if (sum == 0) {
                        return possibleReactions.get(this.rand.nextInt(possibleReactions.size()));
                }
                //System.out.println(sum);
                int number = this.rand.nextInt(sum);
                sum = 0;
                for (int i = 0; i < possibleReactions.size(); i++) {
                        String reaction = possibleReactions.get(i);
                        sum = sum + this.reactions.get(reaction).getPheromones();
                        if (sum >= number) {
                                return reaction;
                        }
                }

                return null;
        }

        private boolean allEnoughAnts(String species, double stoichiometry) {
                SpeciesFA s = this.compounds.get(species);
                if (s.getNumberOfAnts() >= stoichiometry) {
                        return true;
                }
                return false;
        }

        private void AddNewAnts() {
                for (String key : this.sources.keySet()) {
                        //System.out.println(key);
                        double amount = this.sources.get(key);
                        double ant = (amount / this.totalSource) * this.numAnt;
                        if (ant < 1) {
                                ant = 1;
                        }
                        for (int i = 0; i < ant; i++) {
                                if (this.compounds.containsKey(key)) {
                                        this.compounds.get(key).addAnt(new Ant());
                                }
                        }
                        if (this.compounds.containsKey(key)) {
                                this.nodesWithAnts.add(key);
                        }
                }
        }

        /*private void turnAnts(ReactionFA reaction) {
         if (reaction.hasReactant(biomassID)) {
         List<String> reactants = reaction.getReactants();
         for (String reactant : reactants) {
         this.nodesWithBwdAnts.add(reactant);
         for (int e = 0; e < reaction.getStoichiometryReactant(reactant); e++) {
         this.compounds.get(reactant).removeFwdAnt();
         this.compounds.get(reactant).addBwdAnt();
         }
         }
         } else {
         List<String> products = reaction.getProducts();
         for (String product : products) {
         this.nodesWithBwdAnts.add(product);
         for (int e = 0; e < reaction.getStoichiometryProduct(product); e++) {
         this.compounds.get(product).removeFwdAnt();
         this.compounds.get(product).addBwdAnt();
         }
         }
         }

         }*/
        private void backToSources(String sp) {
                //System.out.println("back to sources 1");
                SpeciesFA specie = this.compounds.get(sp);
                Ant ant = specie.getAnt(rand);
                if (ant != null) {
                        backToSources(ant);
                }
                // System.out.println("back to sources 2");
        }

        private void backToSources(Ant ant) {
                // System.out.println("back to sources b 1");
                List<String> path = ant.getPath();
                if (path != null && !path.isEmpty()) {
                        ReactionFA firstReaction = this.reactions.get(path.get(0));

                        List<String> sourcesInReaction = firstReaction.getSources(this.sourcesList);
                        if (sourcesInReaction.size() > 0) {
                                String sourceFromReaction = sourcesInReaction.get(this.rand.nextInt(sourcesInReaction.size()));
                                SpeciesFA sourceSp = this.compounds.get(sourceFromReaction);
                                ant.removePath();
                                sourceSp.addAnt(ant);
                        }
                }
                // System.out.println("back to sources b 2");
        }

        private List<String> getShortestPath(List<List<String>> goodPaths) {
                int size = Integer.MAX_VALUE;
                List<String> shortest = null;
                for (List<String> path : goodPaths) {
                        if (path.size() < size && !path.isEmpty()) {
                                shortest = path;
                                size = path.size();
                        }
                }
                return shortest;
        }
}
