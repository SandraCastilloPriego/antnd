/*
 * Copyright 2007-2013 VTT Biotechnology
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
package ND.modules.configuration.general;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.configuration.sources.SourcesConfParameters;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;

/**
 *
 * @author scsandra
 */
public class GetInfoAndTools {

        File sources, boundsFile;

        public GetInfoAndTools() {
                SourcesConfParameters sourcesParameters = new SourcesConfParameters();
                this.sources = sourcesParameters.getParameter(SourcesConfParameters.exchange).getValue();
                this.boundsFile = sourcesParameters.getParameter(SourcesConfParameters.bounds).getValue();
        }

        public File getBoundsFile() {
                return this.boundsFile;
        }

        public File getSourcesFile() {
                return this.sources;
        }

        public Map<String, Double[]> GetSourcesInfo() {

                Map<String, Double[]> exchangeMap = new HashMap<>();
                try {
                        CsvReader exchange = new CsvReader(new FileReader(this.sources), '\t');

                        try {
                                while (exchange.readRecord()) {
                                        try {
                                                String[] exchangeRow = exchange.getValues();
                                                Double[] bounds = new Double[2];
                                                bounds[0] = Double.parseDouble(exchangeRow[1]);
                                                bounds[1] = Double.parseDouble(exchangeRow[2]);
                                                exchangeMap.put(exchangeRow[0], bounds);
                                        } catch (IOException | NumberFormatException e) {
                                                System.out.println(e.toString());
                                        }
                                }
                        } catch (IOException ex) {
                                Logger.getLogger(ND.modules.configuration.general.GetInfoAndTools.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        return exchangeMap;
                } catch (FileNotFoundException ex) {
                        Logger.getLogger(ND.modules.configuration.general.GetInfoAndTools.class.getName()).log(Level.SEVERE, null, ex);
                }
                return exchangeMap;
        }

        public HashMap<String, String[]> readBounds(Dataset networkDS) {
                HashMap<String, String[]> b = new HashMap<>();
                try {
                        SBMLDocument doc = networkDS.getDocument();
                        Model m = doc.getModel();

                        CsvReader reader = new CsvReader(new FileReader(this.boundsFile));

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
                        java.util.logging.Logger.getLogger(ND.modules.configuration.general.GetInfoAndTools.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(ND.modules.configuration.general.GetInfoAndTools.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                return b;
        }

        public SimpleBasicDataset createDataFile(Graph graph, Dataset networkDS, String biomassID, List<String> sourcesList, boolean isCluster) {
                if (graph != null) {
                        SBMLDocument newDoc = networkDS.getDocument().clone();
                        Model m = networkDS.getDocument().getModel();
                        Model newModel = newDoc.getModel();

                        for (Reaction reaction : m.getListOfReactions()) {
                                if (!isInGraph(reaction.getId(), graph)) {
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
                        dataset.setDatasetName(biomassID + " - " + newModel.getId() + ".sbml");
                        dataset.SetCluster(isCluster);
                        Path path = Paths.get(networkDS.getPath());
                        Path fileName = path.getFileName();
                        String name = fileName.toString();
                        String p = networkDS.getPath().replace(name, "");
                        p = p + newModel.getId();
                        dataset.setPath(p);

                        NDCore.getDesktop().AddNewFile(dataset);

                        dataset.setGraph(graph);
                        dataset.setSources(sourcesList);
                        dataset.setBiomass(biomassID);
                        return dataset;
                }
                return null;
        }

        private boolean isInGraph(String id, Graph graph) {
                for (Node n : graph.getNodes()) {
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
