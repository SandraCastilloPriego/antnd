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
package ND.modules.simulation.antNoGraph;

import ND.desktop.impl.PrintPaths;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.network.Edge;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.sbml.jsbml.ListOf;
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

        private final SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private final File exchangeReactions, boundsFile;
        private final String biomassID;
        private final Random rand;
        private final HashMap<String, ReactionFA> reactions;
        private final HashMap<String, SpeciesFA> compounds;
        private HashMap<String, String[]> bounds;
        private Map<String, Double> sources;
        private final List<String> sourcesList;
        private final JInternalFrame frame;
        private final JScrollPane panel;
        private final JPanel pn;
        private int shortestPath = Integer.MAX_VALUE;
        private Graph graph;
        private final int iterations, pheromones, removePheromones;

        public AntModuleTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {    
                this.networkDS = dataset;
                this.exchangeReactions = parameters.getParameter(AntModuleParameters.exchangeReactions).getValue();
                this.biomassID = parameters.getParameter(AntModuleParameters.objectiveReaction).getValue();
                this.boundsFile = parameters.getParameter(AntModuleParameters.bounds).getValue();
                this.iterations = parameters.getParameter(AntModuleParameters.numberOfIterations).getValue();
                this.pheromones = parameters.getParameter(AntModuleParameters.addPheromones).getValue();
                this.removePheromones = parameters.getParameter(AntModuleParameters.removePheromones).getValue();

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
                return "Starting Ant Simulation... ";
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
                        if (this.networkDS == null) {
                                setStatus(TaskStatus.ERROR);
                                NDCore.getDesktop().displayErrorMessage("You need to select a metabolic model.");
                        }
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

                        for (int i = 0; i < iterations; i++) {
                                this.cicle();
                                finishedPercentage = (double) i / iterations;
                                if (getStatus() == TaskStatus.CANCELED || getStatus() == TaskStatus.ERROR) {
                                        break;
                                }
                        }
                        if (getStatus() == TaskStatus.PROCESSING) {
                                /* for (String key : this.reactions.keySet()) {
                                 System.out.println(key + "," + this.reactions.get(key).getPheromones());
                                 }*/
                                PrintPaths print = new PrintPaths(this.sourcesList, this.biomassID, this.networkDS.getDocument().getModel());
                                try {
                                        this.pn.add(print.printPathwayInFrame(this.graph));
                                } catch (NullPointerException ex) {
                                        System.out.println(ex.toString());
                                }
                        }
                        if (this.graph == null) {
                                NDCore.getDesktop().displayMessage("No path was found.");
                        }
                        /* String info = "";
                         if (this.graph != null) {
                         info = "Simulation\n" + this.parameters.toString() + "\nResult: " + this.graph.toString();
                         } else {
                         info = "Simulation\n" + this.parameters.toString() + "\nResult: No path found";
                         }
                         this.networkDS.setInfo(info + "\n--------------------------");*/
                        createDataFile();
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
                                                //totalSource += Double.parseDouble(exchangeRow[1]);
                                        } catch (IOException | NumberFormatException e) {
                                                e.printStackTrace();
                                        }
                                }
                        } catch (IOException ex) {
                                Logger.getLogger(ND.modules.simulation.antNoGraph.AntModuleTask.class.getName()).log(Level.SEVERE, null, ex);
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
                        //add the number of initial ants using the sources.. and add them
                        // in the list of nodes with ants
                        if (this.sources.containsKey(s.getId())) {
                                // double amount = this.sources.get(s.getId());
                                double antAmount = 50;
                                if (s.getId().contains("C00001")) {
                                        antAmount = 5000;
                                }


                                for (int i = 0; i < antAmount; i++) {
                                        Ant ant = new Ant(specie.getId());
                                        ant.initAnt();
                                        specie.addAnt(ant);
                                }
                        }
                        this.compounds.put(s.getId(), specie);
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
                        SBMLDocument doc = this.networkDS.getDocument();
                        Model m = doc.getModel();

                        CsvReader reader = new CsvReader(new FileReader(this.boundsFile.getAbsolutePath()));

                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                String reactionName = data[0].replace("-", "");
                                b.put(reactionName, data);

                                Reaction r = m.getReaction(reactionName);
                                if (r != null) {
                                        KineticLaw law = new KineticLaw();
                                        LocalParameter lbound = new LocalParameter("LOWER_BOUND");
                                        lbound.setValue(Double.valueOf(data[3]));
                                        law.addLocalParameter(lbound);
                                        LocalParameter ubound = new LocalParameter("UPPER_BOUND");
                                        ubound.setValue(Double.valueOf(data[4]));
                                        law.addLocalParameter(ubound);
                                        r.setKineticLaw(law);
                                }
                        }
                } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(AntModuleParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(AntModuleParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return b;
        }

        public void cicle() {

                for (String compound : compounds.keySet()) {
                        List<String> possibleReactions = getPossibleReactions(compound);

                        if (possibleReactions.size() > 0) {
                                String reactionChoosen = chooseReactions(possibleReactions);


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
                                        if (a.contains(rc.getId())) {
                                                rc.resetPheromones();
                                        }
                                        combinedAnts.put(a, s);
                                }

                                /*  for (String s : toBeRemoved) {
                                 SpeciesFA spfa = this.compounds.get(s);
                                 for (int i = 0; i < rc.getStoichiometry(s); i++) {
                                 Ant a = spfa.getAnt();
                                 if (a != null) {
                                 spfa.removeAnt(a);
                                 }
                                 }

                                 }*/
                                superAnt.joinGraphs(reactionChoosen, combinedAnts);

                                if (!superAnt.isLost()) {
                                        // move the ants to the products...   
                                        for (String s : toBeAdded) {
                                                SpeciesFA spfa = this.compounds.get(s);
                                                for (int i = 0; i < rc.getStoichiometry(s); i++) {
                                                        Ant newAnt;
                                                        try {
                                                                newAnt = superAnt.clone();
                                                        } catch (CloneNotSupportedException ex) {
                                                                newAnt = superAnt;
                                                        }
                                                        newAnt.setLocation(spfa.getId());
                                                        spfa.addAnt(newAnt);
                                                }
                                        }

                                }
                                // When the ants arrive to the biomass
                                if (toBeAdded.contains(this.biomassID)) {
                                        List<Ant> antsBiomass = new ArrayList<>();
                                        //System.out.println("Biomass produced!: " + rc.getId());

                                        SpeciesFA spFA = this.compounds.get(this.biomassID);
                                        antsBiomass.add(spFA.getAnt());
                                        for (Ant a : antsBiomass) {
                                                List<String> localPath = a.getPath();
                                                // Adding pheromones
                                                for (String r : localPath) {
                                                        if (this.reactions.containsKey(r.split(" - ")[0])) {
                                                                this.reactions.get(r.split(" - ")[0]).addPheromones(this.pheromones);
                                                        }
                                                }

                                                // saving the shortest path
                                                if (a.getPathSize() < shortestPath) {
                                                        this.shortestPath = a.getPathSize();
                                                        this.graph = a.getGraph();

                                                        Node biomass = new Node(this.biomassID);
                                                        this.graph.addNode(biomass);
                                                        Node lastNode = this.graph.getNode(reactionChoosen);
                                                        Edge edge = new Edge(this.biomassID, lastNode, biomass);
                                                        this.graph.addEdge(edge);
                                                        a.print();
                                                }

                                        }
                                }


                        } else {
                                //   this.compounds.get(compound).addCount();
                        }

                }

                // Evaporating pheromones (this part could be more sophisticated: evaporating the last reactions in the path first)
                for (String key : this.reactions.keySet()) {
                        ReactionFA r = this.reactions.get(key);
                        r.removePheromones(this.removePheromones);
                }


                // Adding new ants to the sources..
                for (String key : this.sources.keySet()) {
                        if (this.compounds.containsKey(key)) {
                                SpeciesFA specie = this.compounds.get(key);
                                double amount = 50;
                                if (specie.getId().contains("C00001")) {
                                        amount = 1000;
                                }
                                double antAmount = amount - 1;

                                if (antAmount < 1) {
                                        antAmount = 0;
                                }
                                for (int i = 0; i < antAmount; i++) {
                                        Ant ant = new Ant(specie.getId());
                                        ant.initAnt();
                                        specie.addAnt(ant);
                                }
                        }
                }


        }

        private List<String> getPossibleReactions(String node) {

                List<String> possibleReactions = new ArrayList<>();
                SpeciesFA sp = this.compounds.get(node);
                List<String> connectedReactions = sp.getReactions();
                Ant ant = sp.getAnt();
                if (ant == null) {
                        return possibleReactions;
                }
                for (String reaction : connectedReactions) {
                        ReactionFA r = this.reactions.get(reaction);

                        boolean isPossible = true;
                        if (r.hasReactant(node)) {
                                if (r.getub() > 0) {
                                        List<String> reactants = r.getReactants();
                                        for (String reactant : reactants) {
                                                if (!allEnoughAnts(reactant, r.getStoichiometry(reactant), reaction)) {
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

                                                if (!allEnoughAnts(product, r.getStoichiometry(product), reaction)) {
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

        private boolean allEnoughAnts(String species, double stoichiometry, String reaction) {
                SpeciesFA s = this.compounds.get(species);
                Ant ant = s.getAnt();
                if (ant == null || ant.contains(reaction)) {
                        return false;
                }
                if (s.getAnt() != null) {
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
         private void createDataFile() {
                if (this.graph != null) {
                        SBMLDocument newDoc = this.networkDS.getDocument().clone();
                        Model m = this.networkDS.getDocument().getModel();
                        Model newModel = newDoc.getModel();

                        for (Reaction reaction : m.getListOfReactions()) {
                                if (!isInGraph(reaction.getId())) {
                                        newModel.removeReaction(reaction.getId());
                                }
                        }


                        for (Species sp : m.getListOfSpecies()) {
                                if (!this.isInReactions(newModel.getListOfReactions(), sp)) {
                                        newModel.removeSpecies(sp.getId());
                                }
                        }




                        SimpleBasicDataset dataset = new SimpleBasicDataset();

                        dataset.setDocument(newDoc);
                        dataset.setDatasetName(newModel.getId());
                        Path path = Paths.get(this.networkDS.getPath());
                        Path fileName = path.getFileName();
                        String name = fileName.toString();
                        String p = this.networkDS.getPath().replace(name, "");
                        p = p + newModel.getId();
                        dataset.setPath(p);

                        NDCore.getDesktop().AddNewFile(dataset);

                        dataset.setGraph(this.graph);
                        dataset.setSources(sourcesList);
                        dataset.setBiomass(biomassID);
                }
        }

        private boolean isInGraph(String id) {
                for(Node n : this.graph.getNodes()){
                        if(n.getId().contains(id)){
                                return true;
                        }
                }
                return false;
        }

        private boolean isInReactions(ListOf<Reaction> listOfReactions, Species sp) {
                for (Reaction r : listOfReactions) {
                        if (r.hasProduct(sp) || r.hasReactant(sp)) {
                                return true;
                        }
                }
                return false;
        }
}
