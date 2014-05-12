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
package ND.modules.otimization.LP;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.desktop.impl.ItemSelector;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.antNoGraph.network.Edge;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class LPTask extends AbstractTask {

        private final SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private final String objectiveSpecie;
        // private final boolean maximize, sourcesEx, compoundsEx;
        private final Map<String, Double[]> exchange;
        private final GetInfoAndTools tools;

        public LPTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                this.networkDS = dataset;
                this.objectiveSpecie = parameters.getParameter(LPParameters.objective).getValue();
                //   this.maximize = parameters.getParameter(LPParameters.maximize).getValue();
                //   this.sourcesEx = parameters.getParameter(LPParameters.sources).getValue();
                //   this.compoundsEx = parameters.getParameter(LPParameters.compounds).getValue();            

                this.tools = new GetInfoAndTools();
                this.exchange = this.tools.GetSourcesInfo();
        }

        @Override
        public String getTaskDescription() {
                return "Starting LP optimization... ";
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
                        finishedPercentage = 0.1f;
                        optimize();
                        finishedPercentage = 1f;
                        setStatus(TaskStatus.FINISHED);
                } catch (IOException e) {
                        System.out.println(e.toString());
                        setStatus(TaskStatus.ERROR);
                }
        }

        private void createDataFile(Map<String, Double> solution, double objective) {

                SBMLDocument newDoc = this.networkDS.getDocument().clone();
                Model m = this.networkDS.getDocument().getModel();
                Model newModel = newDoc.getModel();

                for (Reaction reaction : m.getListOfReactions()) {
                        if (solution.containsKey(reaction.getId()) && Math.abs(solution.get(reaction.getId())) < 0.0000001) {
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
                dataset.setDatasetName("LPOptimization  - " + this.objectiveSpecie + " - " + newModel.getId() + ".sbml");
                dataset.addInfo("LP Optimization:" + "\nOjective: " + objective + "\nSolution: " + solution + "\n---------------------------");
                Path path = Paths.get(this.networkDS.getPath());
                Path fileName = path.getFileName();
                String name = fileName.toString();
                String p = this.networkDS.getPath().replace(name, "");
                p = p + dataset.getDatasetName();
                dataset.setPath(p);
                finishedPercentage = 0.75f;
                NDCore.getDesktop().AddNewFile(dataset);

                dataset.setGraph(createGraph(solution, newModel, objective));
                finishedPercentage = 0.9f;

                if (this.networkDS.getSources() != null) {
                        dataset.setSources(this.networkDS.getSources());
                } else {
                        List<String> sources = new ArrayList<>();
                        for (String s : this.exchange.keySet()) {
                                sources.add(s);
                        }
                        dataset.setSources(sources);
                }
                dataset.setBiomass(this.objectiveSpecie);

        }

        private boolean isInReactions(ListOf<Reaction> listOfReactions, Species sp) {
                for (Reaction r : listOfReactions) {
                        if (r.hasProduct(sp) || r.hasReactant(sp)) {
                                return true;
                        }
                }
                return false;
        }

        private Graph createGraph(Map<String, Double> solution, Model newModel, double objective) {
                List<Node> nodes = new ArrayList<>();
                List<Edge> edges = new ArrayList<>();
                for (Reaction r : newModel.getListOfReactions()) {
                        Node n = new Node(r.getId() + " - " + solution.get(r.getId()));
                        nodes.add(n);
                }
                for (Reaction r : newModel.getListOfReactions()) {
                        Node n = getNode(nodes, r.getId());
                        List<SpeciesReference> reactants;
                        List<SpeciesReference> products;
                        if (solution.get(r.getId()) > 0) {
                                reactants = r.getListOfReactants();
                                products = r.getListOfProducts();
                        } else {
                                products = r.getListOfReactants();
                                reactants = r.getListOfProducts();
                        }

                        for (SpeciesReference sp : reactants) {
                                String specie = sp.getSpeciesInstance().getId();
                                if (this.exchange.containsKey(specie)) {
                                        Node exchangeNode = getNode(nodes, specie);
                                        if (exchangeNode == null) {
                                                exchangeNode = new Node(specie + " - " + this.exchange.get(specie)[0]);
                                        }
                                        nodes.add(exchangeNode);
                                        Edge edge = new Edge(specie + " - " + uniqueId.nextId(), exchangeNode, n);
                                        if (!edgeExist(edge, edges)) {
                                                edges.add(edge);
                                        }
                                } else if (specie.equals(this.objectiveSpecie)) {
                                        Node objectiveNode = getNode(nodes, specie);
                                        if (objectiveNode == null) {
                                                objectiveNode = new Node(specie + " - " + objective);
                                        }
                                        nodes.add(objectiveNode);
                                        Edge edge = new Edge(specie + " - " + uniqueId.nextId(), objectiveNode, n);
                                        if (!edgeExist(edge, edges)) {
                                                edges.add(edge);
                                        }
                                } else {
                                        List<String> reactions = getReactionFromProducts(sp, newModel, solution);
                                        for (String reaction : reactions) {
                                                if (reaction.equals(r.getId())) {
                                                        continue;
                                                }
                                                Node newNode = getNode(nodes, reaction);
                                                Edge edge = new Edge(sp.getSpeciesInstance().getId() + " - " + uniqueId.nextId(), newNode, n);
                                                if (!edgeExist(edge, edges)) {
                                                        edges.add(edge);
                                                }
                                        }
                                }
                        }

                        for (SpeciesReference sp : products) {
                                String specie = sp.getSpeciesInstance().getId();
                                if (this.exchange.containsKey(specie)) {
                                        Node exchangeNode = getNode(nodes, specie);
                                        if (exchangeNode == null) {
                                                exchangeNode = new Node(specie + " - " + this.exchange.get(specie)[0]);
                                        }
                                        nodes.add(exchangeNode);
                                        Edge edge = new Edge(specie + " - " + uniqueId.nextId(), n, exchangeNode);
                                        if (!edgeExist(edge, edges)) {
                                                edges.add(edge);
                                        }
                                } else if (specie.equals(this.objectiveSpecie)) {
                                        Node objectiveNode = getNode(nodes, specie);
                                        if (objectiveNode == null) {
                                                objectiveNode = new Node(specie + " - " + objective);
                                        }
                                        nodes.add(objectiveNode);
                                        Edge edge = new Edge(specie + " - " + uniqueId.nextId(), n, objectiveNode);
                                        if (!edgeExist(edge, edges)) {
                                                edges.add(edge);
                                        }
                                } else {
                                        List<String> reactions = getReactionFromReactants(sp, newModel, solution);
                                        if (r.getId().contains("r0229YCM606")) {
                                                for (String reaction : reactions) {
                                                        System.out.println(reaction);
                                                }
                                        }
                                        for (String reaction : reactions) {
                                                if (reaction.equals(r.getId())) {
                                                        continue;
                                                }
                                                Node newNode = getNode(nodes, reaction);
                                                Edge edge = new Edge(sp.getSpeciesInstance().getId() + " - " + uniqueId.nextId(), n, newNode);
                                                if (!edgeExist(edge, edges)) {
                                                        edges.add(edge);
                                                }
                                        }
                                }

                        }

                }

                return new Graph(nodes, edges);
        }

        private Node getNode(List<Node> nodes, String s) {
                for (Node n : nodes) {
                        if (n.getId().contains(s)) {
                                return n;
                        }
                }
                return null;
        }

        public void optimize() throws IOException {
                final Rengine rEngine;
                try {
                        if (!Rengine.versionCheck()) {
                                System.err.println("** Version mismatch - Java files don't match library version.");
                                System.exit(1);
                        }
                        rEngine = RUtilities.getREngine();
                        if (!rEngine.waitForR()) {
                                System.out.println("Cannot load R");
                                return;
                        }
                } catch (Throwable t) {
                        throw new IllegalStateException(
                                "LP requires R but it couldn't be loaded (" + t.getMessage() + ')');
                }

                synchronized (RUtilities.R_SEMAPHORE) {
                        rEngine.eval("source(\"conf/FBA.R\")");
                        File tempFile = File.createTempFile(this.networkDS.getDatasetName(), ".tmp");
                        SBMLWriter writer = new SBMLWriter("AntND", "1.0");
                        try {
                                writer.write(this.networkDS.getDocument(), tempFile.getAbsolutePath());
                        } catch (XMLStreamException | FileNotFoundException | SBMLException ex) {
                                Logger.getLogger(ItemSelector.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        System.out.println("solution <- FBA(\"" + tempFile.getAbsolutePath() + "\", \"" + this.tools.getBoundsFile().getAbsolutePath() + "\", \"" + this.tools.getSourcesFile().getAbsolutePath() + "\" , \"" + this.objectiveSpecie + "\")");

                        rEngine.eval("solution <- FBA(\"" + tempFile.getAbsolutePath() + "\", \"" + this.tools.getBoundsFile().getAbsolutePath() + "\", \"" + this.tools.getSourcesFile().getAbsolutePath() + "\" , \"" + this.objectiveSpecie + "\")");

                        this.finishedPercentage = 0.4f;

                        rEngine.eval("opt <-solution[[1]]");
                        rEngine.eval("objective <-opt@lp_obj");
                        rEngine.eval("print(objective)");
                        long e = rEngine.rniParse("objective", 1);
                        long r = rEngine.rniEval(e, 0);
                        REXP x = new REXP(rEngine, r);
                        double v = x.asDouble();
                        rEngine.eval("fluxes <- solution[[2]]");
                        rEngine.eval("fluxesNames <- as.vector(fluxes[,2])");
                        System.out.println(x = rEngine.eval("fluxesNames"));
                        String[] fluxesNames = x.asStringArray();
                        rEngine.eval("fluxesValues <- as.vector(fluxes[,1])");
                        System.out.println(x = rEngine.eval("fluxesValues"));
                        String[] fluxesValue = x.asStringArray();
                        Map<String, Double> solutionMap = new HashMap<>();
                        for (int i = 0; i < fluxesNames.length; i++) {
                                solutionMap.put(fluxesNames[i], Double.valueOf(fluxesValue[i]));
                        }
                        createDataFile(solutionMap, v);
                        tempFile.delete();
                }
        }

        private boolean edgeExist(Edge edge, List<Edge> edges) {
                for (Edge e : edges) {
                        try {
                                if (e.getSource().getId().contains(edge.getSource().getId())
                                        && e.getDestination().getId().contains(edge.getDestination().getId())) {
                                        return true;
                                }
                        } catch (NullPointerException ex) {
                                return false;
                        }
                }
                return false;
        }      

        private List<String> getReactionFromProducts(SpeciesReference sp, Model model, Map<String, Double> solution) {
                List<String> reactions = new ArrayList<>();
                for (Reaction r : model.getListOfReactions()) {
                        List<SpeciesReference> products;
                        if (solution.get(r.getId()) > 0) {
                                products = r.getListOfProducts();
                        } else {
                                products = r.getListOfReactants();
                        }
                        if (compoundExists(products, sp)) {
                                reactions.add(r.getId());
                        }
                }
                return reactions;
        }

        private List<String> getReactionFromReactants(SpeciesReference sp, Model model, Map<String, Double> solution) {
                List<String> reactions = new ArrayList<>();               
                for (Reaction r : model.getListOfReactions()) {
                        List<SpeciesReference> reactants;
                        if (solution.get(r.getId()) > 0) {
                                reactants = r.getListOfReactants();
                        } else {
                                reactants = r.getListOfProducts();
                        }

                        if (compoundExists(reactants, sp)) {                               
                                reactions.add(r.getId());
                        }
                }
                return reactions;
        }
        
        private boolean compoundExists(List<SpeciesReference> reactants, SpeciesReference sp){
                for(SpeciesReference reactant: reactants){
                        if(reactant.getSpeciesInstance().getId().contains(sp.getSpeciesInstance().getId())){
                                return true;
                        }
                }
                return false;
        }

}
