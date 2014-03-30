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
package ND.modules.simulation.superAntAdvanced;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.Ant;
import ND.desktop.impl.PrintPaths;
import ND.modules.simulation.antNoGraph.ReactionFA;
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
public class SuperAntAdvancedModuleTask extends AbstractTask {

        private SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private File exchangeReactions, boundsFile;
        private String biomassID;
        private Random rand;
        private HashMap<String, ReactionFA> reactions;
        private HashMap<String, SpeciesFA> compounds;
        private HashMap<String, String[]> bounds;
        private Map<String, Double> sources;
        private List<String> sourcesList;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JPanel pn;
        private int shortestPath = Integer.MAX_VALUE;
        private Graph graph;
        private int iterations;
        private String middleReactions;
        private String[] mReactions;

        public SuperAntAdvancedModuleTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                this.networkDS = dataset;
                this.exchangeReactions = parameters.getParameter(SuperAntModuleAdvancedParameters.exchangeReactions).getValue();
                this.biomassID = parameters.getParameter(SuperAntModuleAdvancedParameters.objectiveReaction).getValue();
                this.boundsFile = parameters.getParameter(SuperAntModuleAdvancedParameters.bounds).getValue();
                this.iterations = parameters.getParameter(SuperAntModuleAdvancedParameters.numberOfIterations).getValue();
                this.middleReactions = parameters.getParameter(SuperAntModuleAdvancedParameters.middleReactions).getValue();
                this.mReactions = this.middleReactions.replace(" ", "").split(",");

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
                        /*String realbiomass = this.biomassID;

                         ReactionFA r = this.reactions.get(this.mReactions[0]);
                         /* List<Graph> graphs = new ArrayList<>();
                         List<Graph> graphs2 = new ArrayList<>();
                         if (r.getub() > 0) {
                         System.out.println("here 1");
                         for (String p : r.getProducts()) {

                         if (this.sources.containsKey(p)) {
                         Graph g = new Graph(null, null);
                         g.addNode(new Node(p + " - " + uniqueId.nextId()));
                         } else {
                         this.graph = null;
                         this.biomassID = p;
                         for (int i = 0; i < iterations; i++) {
                         this.createWorld();
                         this.cicle();
                         }
                         if (this.graph == null) {
                         graphs.clear();
                         break;
                         } else {
                         graphs.add(this.graph);
                         }
                         }
                         }
                         }
                         if (r.getlb() < 0) {
                         System.out.println("here 2");
                         for (String p : r.getReactants()) {
                         if (this.sources.containsKey(p)) {
                         Graph g = new Graph(null, null);
                         g.addNode(new Node(p + " - " + uniqueId.nextId()));
                         graphs2.add(g);
                         } else {
                         this.graph = null;
                         this.biomassID = p;
                         for (int i = 0; i < iterations; i++) {
                         this.createWorld();
                         this.cicle();

                         }
                         if (this.graph == null) {
                         graphs2.clear();
                         break;
                         } else {
                         graphs2.add(this.graph);
                         }
                         }
                         }
                         }

                         Map<String, Double> realSources = this.sources;
                         if (graphs.size() > 0) {
                         System.out.println("here 12");
                         this.graph = null;
                         for (String s : r.getProducts()) {
                         this.sources.put(s, 1.0);
                         }
                         for (int i = 0; i < iterations; i++) {
                         this.createWorld();
                         this.biomassID = realbiomass;
                         this.cicle();
                         }

                         if (this.graph != null && graph.getNode(this.mReactions[0]) == null) {
                         for (Graph g : graphs) {
                         this.graph.addGraph(g);
                         }
                         System.out.println("graph 12 :" +this.graph.toString());

                         }
                         }
                         if (graphs2.size() > 0) {
                         System.out.println("here 22");
                         this.graph = null;
                         this.sources = realSources;
                         for (String s : r.getReactants()) {
                         this.sources.put(s, 1.0);
                         }
                         for (int i = 0; i < iterations; i++) {
                         this.createWorld();
                         this.biomassID = realbiomass;
                         this.cicle();
                         }

                         if (this.graph != null && graph.getNode(this.mReactions[0]) == null) {
                         for (Graph g : graphs2) {
                         this.graph.addGraph(g);

                         }
                         System.out.println("graph 22 :" +this.graph.toString());
                         }
                         }
                         * 
                         * 

                         System.out.println(graphs.size() + " - " + graphs2.size());*/

                        for (int i = 0; i < iterations; i++) {
                                this.cicle();
                                finishedPercentage = (double) i / iterations;
                                if (getStatus() == TaskStatus.CANCELED || getStatus() == TaskStatus.ERROR) {
                                        break;
                                }
                        }

                        if (getStatus() == TaskStatus.PROCESSING) {
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
                                        } catch (IOException | NumberFormatException e) {
                                                e.printStackTrace();
                                        }
                                }
                        } catch (IOException ex) {
                                Logger.getLogger(ND.modules.simulation.antNoGraph.AntModuleTask.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        return exchangeMap;
                } catch (FileNotFoundException ex) {
                        Logger.getLogger(SuperAntAdvancedModuleTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
        }

        private void createWorld() {
                SBMLDocument doc = this.networkDS.getDocument();
                Model m = doc.getModel();
                for (Species s : m.getListOfSpecies()) {
                        SpeciesFA specie = new SpeciesFA(s.getId());
                        if (s.getId().contains(this.biomassID)) {
                                specie.setIsBiomass();
                        }
                        //add the number of initial ants using the sources.. and add them
                        // in the list of nodes with ants
                        if (this.sources.containsKey(s.getId())) {
                                double antAmount = 1;
                                for (int i = 0; i < antAmount; i++) {
                                        Ant ant = new Ant(specie.getId());
                                        ant.initAnt();
                                        specie.addAnt(ant, this.mReactions);
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
                                if (r != null && r.getKineticLaw() == null) {
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
                        java.util.logging.Logger.getLogger(SuperAntModuleAdvancedParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(SuperAntModuleAdvancedParameters.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return b;
        }

        public void cicle() {

                for (String compound : compounds.keySet()) {

                        List<String> possibleReactions = getPossibleReactions(compound);

                        for (String reactionChoosen : possibleReactions) {
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

                                superAnt.joinGraphs(reactionChoosen, combinedAnts);

                                if (!superAnt.isLost()) {
                                        // move the ants to the products...   
                                        for (String s : toBeAdded) {
                                                SpeciesFA spfa = this.compounds.get(s);
                                                for (int e = 0; e < rc.getStoichiometry(s); e++) {
                                                        Ant newAnt = superAnt.clone();
                                                        newAnt.setLocation(spfa.getId());
                                                        spfa.addAnt(newAnt, this.mReactions);
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
                        }

                }

        }

        private List<String> getPossibleReactions(String node) {

                List<String> possibleReactions = new ArrayList<>();
                SpeciesFA sp = this.compounds.get(node);
                Ant ant = sp.getAnt();
                if (!this.sources.containsKey(node) && ant == null) {
                        return possibleReactions;
                }


                List<String> connectedReactions = sp.getReactions();
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
                                                        break;
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

        private boolean allEnoughAnts(String species, double stoichiometry, String reaction) {
                SpeciesFA s = this.compounds.get(species);
                Ant ant = s.getAnt();
                if (ant != null && ant.contains(reaction)) {
                        return false;
                }

                if (ant != null) {
                        return true;
                }
                return false;
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
                for (Node n : this.graph.getNodes()) {
                        if (n.getId().contains(id)) {
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
