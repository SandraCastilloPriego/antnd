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
package ND.modules.reactionOP.AddInfo;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.metacyc.Javacyc;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.xml.XMLNode;

/**
 *
 * @author scsandra
 */
public class AddInfoTask extends AbstractTask {

        private final SimpleBasicDataset networkDS;
        private final File fileName, databaseDir;
        private double finishedPercentage = 0.0f;
        private ArrayList<String> pathways;

        public AddInfoTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
                networkDS = dataset;
                this.fileName = parameters.getParameter(AddInfoParameters.fileName).getValue();
                this.databaseDir = parameters.getParameter(AddInfoParameters.dirName).getValue();
        }

        @Override
        public String getTaskDescription() {
                return "Adding information to the model... ";
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

                        SBMLDocument doc = this.networkDS.getDocument();
                        Model m = doc.getModel();

                        CsvReader lines;

                        lines = new CsvReader(new FileReader(this.fileName.getAbsolutePath()), ',');
                        lines.readRecord();
                        String[] headers = lines.getValues();
                        float count = 0;
                        while (lines.readRecord()) {
                                String[] line = lines.getValues();
                                this.ReadPathways(line);
                                // processLine(line, m, headers);

                        }
                        this.createdb(m);

                        setStatus(TaskStatus.FINISHED);
                } catch (FileNotFoundException ex) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = ex.toString();
                } catch (IOException ex) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = ex.toString();
                } catch (Exception ex) {
                        setStatus(TaskStatus.ERROR);
                        errorMessage = ex.toString();
                }
        }

        private void ReadPathways(String[] line) {
                this.pathways = new ArrayList<>();
                String p = line[3];
                String[] paths = p.split(";");
                for (String path : paths) {
                        if (!this.pathways.contains(path)) {
                                this.pathways.add(path);
                        }
                }
        }

        private void createdb(Model m) {
              //  GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(this.databaseDir.getAbsolutePath());

               // registerShutdownHook(graphDb);
Javacyc cyc = new Javacyc("META");
                        ArrayList<String> reactionsMeta = cyc.allRxns();
                        //Add Metacyc reactions
                        for (String r : reactionsMeta) {
                                System.out.println(r);
                        }
                        
              //  addModel(graphDb, m);

        }

        private void addModel(GraphDatabaseService graphDb, Model m) {
                Map<String, Node> nodes = new HashMap<>();
                Transaction tx = graphDb.beginTx();
                try {

                        //Add organismNode 
                        Node orgNode = graphDb.createNode();
                        orgNode.setProperty("Id", m.getId());
                        orgNode.setProperty("Type", "Organism");

                        //Add Metacyc
                        Node metaNode = graphDb.createNode();
                        metaNode.setProperty("Id", "Metacyc");
                        metaNode.setProperty("Type", "Organism");

                        //Add compartments
                        for (Compartment c : m.getListOfCompartments()) {
                                Node n = graphDb.createNode();
                                n.setProperty("Id", c.getId());
                                n.setProperty("Name", c.getName());
                                n.setProperty("Type", "Compartment");
                                nodes.put(c.getId(), n);
                                orgNode.createRelationshipTo(n, RelTypes.HASCOMPARTMENT);

                        }
                        Javacyc cyc = new Javacyc("META");
                        ArrayList<String> reactionsMeta = cyc.allRxns();
                        //Add Metacyc reactions
                        for (String r : reactionsMeta) {
                                Node n = graphDb.createNode();
                                n.setProperty("Id", r);
                                n.setProperty("Name", r);
                                n.setProperty("Type", "Reaction");
                                nodes.put(r, n);
                                metaNode.createRelationshipTo(n, RelTypes.HASREACTION);
                        }
                        
                        
                        //Add pathways
                        ArrayList<String> pathwaysMeta = cyc.allPathways();
                        for (String pathway : pathwaysMeta) {
                                Node n = graphDb.createNode();
                                n.setProperty("Id", pathway);

                              //  cyc.n.setProperty("Name",);
                        }

                        //Add Species
                        for (Species s : m.getListOfSpecies()) {
                                Node n = graphDb.createNode();
                                n.setProperty("Id", s.getId());
                                n.setProperty("Name", s.getName());
                                n.setProperty("Type", "Species");
                                nodes.put(s.getId(), n);

                        }

                        for (Reaction r : m.getListOfReactions()) {
                                Node n = graphDb.createNode();
                                n.setProperty("Id", r.getId());
                                n.setProperty("Name", r.getName());
                                n.setProperty("Type", "Reaction");
                                nodes.put(r.getId(), n);
                                orgNode.createRelationshipTo(n, RelTypes.HASREACTION);
                        }

                        for (Reaction r : m.getListOfReactions()) {
                                if (nodes.containsKey(r.getId())) {
                                        Node rNode = nodes.get(r.getId());
                                        for (SpeciesReference s : r.getListOfReactants()) {
                                                if (nodes.containsKey(s.getSpecies())) {
                                                        Node sNode = nodes.get(s.getSpecies());
                                                        sNode.createRelationshipTo(rNode, RelTypes.ISREACTAN);
                                                }
                                        }

                                        for (SpeciesReference s : r.getListOfProducts()) {
                                                if (nodes.containsKey(s.getSpecies())) {
                                                        Node sNode = nodes.get(s.getSpecies());
                                                        rNode.createRelationshipTo(sNode, RelTypes.ISPRODUCT);
                                                }
                                        }

                                }
                        }
                        // Database operations go here
                        tx.success();
                } finally {
                        tx.finish();

                }
        }

        private static void registerShutdownHook(final GraphDatabaseService graphDb) {
// Registers a shutdown hook for the Neo4j instance so that it
// shuts down nicely when the VM exits (even if you "Ctrl-C" the
// running application).
                Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                                graphDb.shutdown();
                        }
                });
        }

        private static enum RelTypes implements RelationshipType {

                ISREACTAN, ISPRODUCT, BELONGTOPATHWAY, HASREACTION, ISINCOMPARTMENT, HASCOMPARTMENT
        }

        private void processLine(String[] line, Model m, String[] header) {
                String id = line[0];
                System.out.println(id);
                Reaction r = m.getReaction(id);
                if (r == null) {
                        Species s = m.getSpecies(id);
                        if (s != null) {
                                for (int i = 1; i < line.length; i++) {
                                        if (line[i] != null && !line[i].equals("NA")) {
                                                s.appendNotes(header[i] + ":" + line[i]);
                                        }
                                }
                        }
                } else {
                        for (int i = 1; i < line.length; i++) {
                                System.out.println(header[i]);
                                if (line[i] != null && !line[i].equals("NA")) {
                                        if (r.getNotes() != null) {
                                                r.appendNotes("<p>" + header[i] + ":" + line[i] + "</p>");
                                                System.out.println(r.getNotesString());
                                                XMLNode node = r.getNotes();
                                                XMLNode child = node.getChildAt(2);
                                                System.out.println(child.toXMLString());
                                                node.addChild(child);
                                        } else {
                                                r.setNotes(header[i] + ":" + line[i]);
                                        }
                                }

                        }

                }

        }

}
