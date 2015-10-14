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
package ND.modules.reactionOP.fluxAnalysis;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class FluxAnalysisTask extends AbstractTask {

        private final SimpleBasicDataset networkDS;
        private final double finishedPercentage = 0.0f;
        private final File fluxesFile;
        private final Map<String, Double[]> exchange;
        private final double threshold;
        private final Map<String, Double> fluxes;
        private final Map<String, Color> color;
        private final GetInfoAndTools tools;

        public FluxAnalysisTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                this.networkDS = dataset;
                this.fluxesFile = parameters.getParameter(FluxAnalysisParameters.fluxes).getValue();
                this.threshold = parameters.getParameter(FluxAnalysisParameters.threshold).getValue();
                this.tools = new GetInfoAndTools();
                this.exchange = this.tools.GetSourcesInfo();
                this.color = new HashMap<>();
                this.fluxes = new HashMap<>();
        }

        @Override
        public String getTaskDescription() {
                return "Starting Fluxes visualization... ";
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

                        this.readFluxes();

                        System.out.println(this.fluxes.size());

                        SBMLDocument doc = this.networkDS.getDocument();
                        Model m = doc.getModel();

                        SBMLDocument newDoc = doc.clone();
                        Model newModel = newDoc.getModel();

                        for (Reaction reaction : m.getListOfReactions()) {
                                if (!this.fluxes.containsKey(reaction.getId())) {
                                        newModel.removeReaction(reaction.getId());
                                }
                        }

                        List<Species> toBeRemoved = new ArrayList<>();
                        for (Species sp : newModel.getListOfSpecies()) {
                                if (!this.isInReactions(newModel.getListOfReactions(), sp)) {
                                        toBeRemoved.add(sp);
                                }
                        }

                        for (Species sp : toBeRemoved) {
                                newModel.removeSpecies(sp);
                        }

                        SimpleBasicDataset dataset = new SimpleBasicDataset();

                        dataset.setDocument(newDoc);
                        dataset.setDatasetName("Fluxes - " + this.networkDS.getDatasetName());
                        Path path = Paths.get(this.networkDS.getPath());
                        Path fileName = path.getFileName();
                        String name = fileName.toString();
                        String p = this.networkDS.getPath().replace(name, "");
                        p = p + newModel.getId();
                        dataset.setPath(p);

                        NDCore.getDesktop().AddNewFile(dataset);

                        dataset.setGraph(this.createGraph(fluxes, newModel));
                        dataset.setSources(this.networkDS.getSources());

                        setStatus(TaskStatus.FINISHED);

                } catch (Exception e) {
                        System.out.println(e.toString());
                        setStatus(TaskStatus.ERROR);
                }
        }

        private void readFluxes() {

                try {
                        CsvReader reader = new CsvReader(new FileReader(this.fluxesFile.getAbsolutePath()));
                        reader.readHeaders();
                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                if (Math.abs(Double.valueOf(data[1])) > this.threshold) {
                                        fluxes.put(data[0], Double.valueOf(data[1]));
                                        try {
                                                color.put(data[0], getColor(data[2]));
                                        } catch (NullPointerException ee) {
                                        } catch (ArrayIndexOutOfBoundsException eo) {
                                        }
                                }
                        }
                } catch (FileNotFoundException ex) {
                } catch (IOException ex) {
                }

        }

        private Color getColor(String color) {
                String[] bgr = color.split(",");
                try {
                        return new Color(Integer.parseInt(bgr[0]), Integer.parseInt(bgr[1]), Integer.parseInt(bgr[1]));
                } catch (Exception e) {
                        return Color.white;
                }
        }

        private Graph createGraph(Map<String, Double> solution, Model newModel) {
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
                                } else {
                                        List<String> reactions = getReactionFromReactants(sp, newModel, solution);
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

                return new Graph(nodes, edges, color);
        }

        private Node getNode(List<Node> nodes, String s) {
                for (Node n : nodes) {
                        if (n.getId().contains(s)) {
                                return n;
                        }
                }
                return null;
        }

        private boolean isInReactions(ListOf<Reaction> listOfReactions, Species sp) {
                for (Reaction r : listOfReactions) {
                        if (r.hasProduct(sp) || r.hasReactant(sp)) {
                                return true;
                        }
                }
                return false;
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
                        try {
                                List<SpeciesReference> products;
                                if (solution.get(r.getId()) > 0) {
                                        products = r.getListOfProducts();
                                } else {
                                        products = r.getListOfReactants();
                                }
                                if (compoundExists(products, sp)) {
                                        reactions.add(r.getId());
                                }
                        } catch (Exception e) {
                                System.out.println(e.toString() + " , " + sp.getSpeciesInstance().getName());
                        }
                }
                return reactions;
        }

        private List<String> getReactionFromReactants(SpeciesReference sp, Model model, Map<String, Double> solution) {
                List<String> reactions = new ArrayList<>();
                for (Reaction r : model.getListOfReactions()) {
                        try {
                                List<SpeciesReference> reactants;
                                if (solution.get(r.getId()) > 0) {
                                        reactants = r.getListOfReactants();
                                } else {
                                        reactants = r.getListOfProducts();
                                }

                                if (compoundExists(reactants, sp)) {
                                        reactions.add(r.getId());
                                }
                        } catch (Exception e) {
                                System.out.println(e.toString() + " , " + sp.getSpeciesInstance().getName());
                        }
                }
                return reactions;
        }

        private boolean compoundExists(List<SpeciesReference> reactants, SpeciesReference sp) {
                for (SpeciesReference reactant : reactants) {
                        if (reactant.getSpeciesInstance().getId().contains(sp.getSpeciesInstance().getId())) {
                                return true;
                        }
                }
                return false;
        }
}
