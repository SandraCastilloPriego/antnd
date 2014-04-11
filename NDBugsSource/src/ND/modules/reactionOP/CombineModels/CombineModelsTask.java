package ND.modules.reactionOP.CombineModels;

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
import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.network.Edge;
import ND.modules.simulation.antNoGraph.network.Graph;
import ND.modules.simulation.antNoGraph.network.Node;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class CombineModelsTask extends AbstractTask {

        private final Dataset[] networkDS;

        public CombineModelsTask(Dataset[] dataset) {
                networkDS = dataset;
        }

        @Override
        public String getTaskDescription() {
                return "Combining models... ";
        }

        @Override
        public double getFinishedPercentage() {
                return 0.0f;
        }

        @Override
        public void cancel() {
                setStatus(TaskStatus.CANCELED);
        }

        @Override
        public void run() {
                try {
                        setStatus(TaskStatus.PROCESSING);
                        List<Graph> graphs = new ArrayList<>();
                        for (int i = 0; i < this.networkDS.length; i++) {
                                Graph g = this.networkDS[i].getGraph();
                                if (g != null) {
                                        graphs.add(g);
                                }
                        }
                        if (!graphs.isEmpty()) {
                                Graph g = joinGraphs(graphs);
                                createDataFile(g);
                        } else {
                                createDataFile(null);
                        }



                        setStatus(TaskStatus.FINISHED);
                } catch (Exception e) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = e.toString();
                }
        }

        private Graph joinGraphs(List<Graph> graphs) {
                System.out.println("joining graphs");
                Graph g = new Graph(null, null);
                for (Graph grph : graphs) {
                        if (grph != null) {
                                grph.toString();
                                for (Node n : grph.getNodes()) {
                                        //System.out.print(", " + n.getId());
                                        g.addNode2(n);
                                }
                                for (Edge e : grph.getEdges()) {
                                        try {
                                                Node source = e.getSource();
                                                e.setSource(g.getNode(source.getId().split(" - ")[0]));
                                                Node destination = e.getDestination();
                                                e.setDestination(g.getNode(destination.getId().split(" - ")[0]));
                                                g.addEdge2(e);
                                        } catch (Exception ex) {
                                                System.out.println(ex.toString());
                                        }
                                }
                        }
                }
                //  System.out.println(g.getNumberOfNodes() + " - " + g.getNumberOfEdges());
                // System.out.println("\n" + g.toString());
                return g;
        }

        private void createDataFile(Graph graph) {      
                SBMLDocument newDoc = this.networkDS[0].getDocument().clone();
                Model newModel = newDoc.getModel();

                for (int i = 1; i < this.networkDS.length; i++) {
                        Model m2 = this.networkDS[i].getDocument().getModel();

                        for (Reaction reaction : m2.getListOfReactions()) {
                                if (newModel.getReaction(reaction.getId()) == null) {
                                        newModel.addReaction(getReaction(newModel, reaction));
                                }
                        }
                }

                SimpleBasicDataset dataset = new SimpleBasicDataset();

                dataset.setDocument(newDoc);
                dataset.setDatasetName("Combined - " + this.networkDS[0].getBiomassId()+".sbml");
                Path path = Paths.get(this.networkDS[0].getPath());
                Path fileName = path.getFileName();
                String name = fileName.toString();
                String p = this.networkDS[0].getPath().replace(name, "");
                p = p + newModel.getId() + "-Combined";


                dataset.setPath(p);
                NDCore.getDesktop().AddNewFile(dataset);
                if (graph != null) {
                        dataset.setGraph(graph);
                }    
                
                dataset.setSources(this.networkDS[0].getSources());
                dataset.setBiomass(this.networkDS[0].getBiomassId());

        }

        private Reaction getReaction(Model m, Reaction r) {              
                Reaction reaction = r.clone();
                System.out.println(reaction.getId());
                for (SpeciesReference sr : r.getListOfReactants()) {
                        Species sp = sr.getSpeciesInstance();                       
                        if (sp != null && !m.containsSpecies(sp.getId())) {
                                m.addSpecies(sp.clone());
                        }
                }

                for (SpeciesReference sr : r.getListOfProducts()) {
                        Species sp = sr.getSpeciesInstance();
                        if (sp != null && !m.containsSpecies(sp.getId())) {
                                m.addSpecies(sp.clone());
                        }
                }
                return reaction;
        }
}
