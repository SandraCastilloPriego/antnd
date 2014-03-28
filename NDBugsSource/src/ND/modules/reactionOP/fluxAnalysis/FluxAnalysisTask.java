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
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.PrintPaths;
import ND.modules.simulation.antNoGraph.network.Edge;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import ND.modules.simulation.antNoGraph.uniqueId;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

        private SimpleBasicDataset networkDS;
        private double finishedPercentage = 0.0f;
        private File fluxesFile, exchangeFile;
        private List<String> sourcesList;
        private Map<String, Double> fluxes;
        private JInternalFrame frame;
        private JScrollPane panel;
        private JPanel pn;

        public FluxAnalysisTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                this.networkDS = dataset;
                this.fluxesFile = parameters.getParameter(FluxAnalysisParameters.fluxes).getValue();
                this.exchangeFile = parameters.getParameter(FluxAnalysisParameters.exchange).getValue();

                this.frame = new JInternalFrame("Result", true, true, true, true);
                this.pn = new JPanel();
                this.panel = new JScrollPane(pn);
                this.sourcesList = new ArrayList<>();
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
                        this.readExchangeReactions();
                        this.fluxes = this.readFluxes();
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
                        dataset.setDatasetName(newModel.getId());
                        Path path = Paths.get(this.networkDS.getPath());
                        Path fileName = path.getFileName();
                        String name = fileName.toString();
                        String p = this.networkDS.getPath().replace(name, "");
                        p = p + newModel.getId();
                        dataset.setPath(p);

                        NDCore.getDesktop().AddNewFile(dataset);

                        frame.setSize(new Dimension(700, 500));
                        frame.add(this.panel);
                        NDCore.getDesktop().addInternalFrame(frame);
                        PrintPaths print = new PrintPaths(this.sourcesList, null, newModel);
                        this.pn.add(print.printPathwayInFrame(this.createGraph(newModel)));

                        setStatus(TaskStatus.FINISHED);

                } catch (Exception e) {
                        System.out.println(e.toString());
                        setStatus(TaskStatus.ERROR);
                }
        }

        private void readExchangeReactions() {
                try {
                        CsvReader exchange = new CsvReader(new FileReader(this.exchangeFile), '\t');

                        try {
                                while (exchange.readRecord()) {
                                        try {
                                                String[] exchangeRow = exchange.getValues();
                                                this.sourcesList.add(exchangeRow[0]);

                                        } catch (IOException | NumberFormatException e) {
                                                e.printStackTrace();
                                        }
                                }
                        } catch (IOException ex) {
                                Logger.getLogger(ND.modules.reactionOP.fluxAnalysis.FluxAnalysisTask.class.getName()).log(Level.SEVERE, null, ex);
                        }


                } catch (FileNotFoundException ex) {
                        Logger.getLogger(ND.modules.reactionOP.fluxAnalysis.FluxAnalysisTask.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        private HashMap<String, Double> readFluxes() {
                HashMap<String, Double> f = new HashMap<>();
                try {
                        CsvReader reader = new CsvReader(new FileReader(this.fluxesFile.getAbsolutePath()));
                        reader.readHeaders();
                        while (reader.readRecord()) {
                                String[] data = reader.getValues();
                                String reactionName = data[2].replace("-", "");
                                if (Math.abs(Double.valueOf(data[1])) > 0.000001) {
                                        f.put(reactionName, Double.valueOf(data[1]));
                                }
                        }
                } catch (FileNotFoundException ex) {
                } catch (IOException ex) {
                }
                return f;
        }

        private Graph createGraph(Model newModel) {
                List<Node> nodes = new ArrayList<>();
                List<Edge> edges = new ArrayList<>();
                for (Reaction r : newModel.getListOfReactions()) {
                        Node n = new Node(r.getId() + " / " + this.fluxes.get(r.getId()));
                        nodes.add(n);
                        List<SpeciesReference> reactants;
                        List<SpeciesReference> products;
                        if (this.fluxes.get(r.getId()) > 0) {
                                reactants = r.getListOfReactants();
                                products = r.getListOfProducts();
                        } else {
                                products = r.getListOfReactants();
                                reactants = r.getListOfProducts();
                        }

                        for (SpeciesReference sp : reactants) {
                                if (sp.getSpeciesInstance().getId().contains("C00001") || sp.getSpeciesInstance().getId().contains("C00011")) {
                                        continue;
                                }
                                Node newNode = this.getNode(nodes, sp.getSpeciesInstance().getId());
                                if (newNode == null) {
                                        newNode = new Node("sp:" + sp.getSpeciesInstance().getId());
                                }
                                nodes.add(newNode);
                                Edge edge = new Edge(sp.getSpeciesInstance().getId() + "-" + uniqueId.nextId(), newNode, n);
                                edges.add(edge);
                        }

                        for (SpeciesReference sp : products) {
                                if (sp.getSpeciesInstance().getId().contains("C00001") || sp.getSpeciesInstance().getId().contains("C00011")) {
                                        continue;
                                }
                                Node newNode = this.getNode(nodes, sp.getSpeciesInstance().getId());
                                if (newNode == null) {
                                        newNode = new Node("sp:" + sp.getSpeciesInstance().getId());
                                }
                                nodes.add(newNode);
                                Edge edge = new Edge(sp.getSpeciesInstance().getId() + "-" + uniqueId.nextId(), n, newNode);
                                edges.add(edge);
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

        private boolean isInReactions(ListOf<Reaction> listOfReactions, Species sp) {
                for (Reaction r : listOfReactions) {
                        if (r.hasProduct(sp) || r.hasReactant(sp)) {
                                return true;
                        }
                }
                return false;
        }
}
