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
import ND.data.network.Node;
import ND.main.NDCore;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
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
    Map<String, Node> nodes;

    public AddInfoTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        networkDS = dataset;
        this.fileName = parameters.getParameter(AddInfoParameters.fileName).getValue();
        this.databaseDir = parameters.getParameter(AddInfoParameters.dirName).getValue();
        this.nodes = new HashMap<>();
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

                        //CsvReader lines;
            //  lines = new CsvReader(new FileReader(this.fileName.getAbsolutePath()), ';');
            //  lines.readRecord();
            // String[] headers = lines.getValues();
            float count = 0;
                        //  while (lines.readRecord()) {
            //  String[] line = lines.getValues();
            //  this.ReadPathways(line);
            // processLine(line, m, headers);
//   }
            //                     this.createdb(m);

            setStatus(TaskStatus.FINISHED);
            /*   } catch (FileNotFoundException ex) {
             setStatus(TaskStatus.ERROR);
             errorMessage = ex.toString();
             } catch (IOException ex) {
             setStatus(TaskStatus.ERROR);
             errorMessage = ex.toString();*/
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

    /*private void createdb(Model m) {
     GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(this.databaseDir);

     registerShutdownHook(graphDb);
     Javacyc cyc = new Javacyc("META");
     Map<String, ArrayList<ArrayList>> reactionsMeta = cyc.allRxns();

     try (Transaction tx = graphDb.beginTx()) {
     //Add organismNode 
     Node orgNode = graphDb.createNode();
     orgNode.setProperty("Id", "Metacyc");
     orgNode.setProperty("Type", "Organism");

     for (Map.Entry<String, ArrayList<ArrayList>> entry : reactionsMeta.entrySet()) {
     String r = entry.getKey();
     System.out.println("Metacyc reaction:" + r);
     Node node = graphDb.createNode();
     node.setProperty("Id", r);
     node.setProperty("Type", "Reaction");
     this.nodes.put(r, node);
     orgNode.createRelationshipTo(node, RelTypes.HASREACTION);

     ArrayList<String> left = entry.getValue().get(0);
     for (String l : left) {
     System.out.println("Metacyc reaction reactant:" + l);
     Node lnode;
     if (this.nodes.containsKey(l)) {
     lnode = this.nodes.get(l);
     } else {
     lnode = graphDb.createNode();
     lnode.setProperty("Id", l);
     lnode.setProperty("Type", "Species");
     }

     node.createRelationshipTo(lnode, RelTypes.ISREACTAN);
     }
     ArrayList<String> right = entry.getValue().get(1);
     for (String l : right) {
     System.out.println("Metacyc reaction product:" + l);
     Node lnode;
     if (this.nodes.containsKey(l)) {
     lnode = this.nodes.get(l);
     } else {
     lnode = graphDb.createNode();
     lnode.setProperty("Id", l);
     lnode.setProperty("Type", "Species");
     }
     node.createRelationshipTo(lnode, RelTypes.ISPRODUCT);
                                       
     }

     }

     //Add Pathways
     Map<String, ArrayList<String>> pathways = cyc.allPathways();

     for (Map.Entry<String, ArrayList<String>> entry : pathways.entrySet()) {
     String pathwayName = entry.getKey();
     System.out.println(pathwayName);
     Node pathway = graphDb.createNode();
     pathway.setProperty("Id", pathwayName);
     pathway.setProperty("Type", "Pathway");
     this.nodes.put(pathwayName, pathway);

     for (String reaction : entry.getValue()) {
     Node reactionNode = this.nodes.get(reaction);
     pathway.createRelationshipTo(reactionNode, RelTypes.BELONGTOPATHWAY);
     }
     }

     addModel(graphDb, m);
     tx.success();
     }
     graphDb.shutdown();
     }
     private void addModel(GraphDatabaseService graphDb, Model m) {

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
     System.out.println("Compartment: " + c.getId());
     Node n = graphDb.createNode();
     n.setProperty("Id", c.getId());
     n.setProperty("Name", c.getName());
     n.setProperty("Type", "Compartment");
     nodes.put(c.getId(), n);
     orgNode.createRelationshipTo(n, RelTypes.HASCOMPARTMENT);

     }

     //Add Species
     for (Species s : m.getListOfSpecies()) {
     System.out.println("Species: " + s.getId());
     Node n = graphDb.createNode();
     n.setProperty("Id", s.getId());
     n.setProperty("Name", s.getName());
     n.setProperty("Type", "Species");
     nodes.put(s.getId(), n);

     }

     for (Reaction r : m.getListOfReactions()) {
     Node n = graphDb.createNode();
     System.out.println("Reactions: " + r.getId());
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

     //Add pathways
     Map<String, ArrayList<String>> pathways = getPathways();
     for (Map.Entry<String, ArrayList<String>> entry : pathways.entrySet()) {
     String reactionName = entry.getKey();
     // System.out.println("pathway assigment: " + reactionName);

     Node reaction = this.nodes.get(reactionName);
     ArrayList<String> paths = entry.getValue();
     for (String path : paths) {
     Node p = this.nodes.get(path);
     if (p != null) {
     p.createRelationshipTo(reaction, RelTypes.BELONGTOPATHWAY);
     } else {
     System.out.println("missing pathways: " + path);
     }
     }

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
     }*/
    private Map<String, ArrayList<String>> getPathways() {
        Map<String, ArrayList<String>> pathways = new HashMap();
        CsvReader lines;
        try {
            lines = new CsvReader(new FileReader("/home/scsandra/Documents/CellFactory2015/retropath/mappings/experiment.csv"), ',');
            lines.getHeaders();
            while (lines.readRecord()) {

                String[] reaction = lines.getValues();
                String reactionName = reaction[0];
                if (reaction[3] != null && !reaction[3].equals("NA") && !reaction[3].isEmpty()) {
                    ArrayList<String> pathsArray = new ArrayList<>();
                    String[] paths = reaction[3].split(";");
                    // System.out.println("pathway comprete:" + reaction[3]);
                    for (String p : paths) {
                        //    System.out.println("pathway:" + p);
                        if (p != null) {
                            pathsArray.add(p);
                        }
                    }
                    if (!pathsArray.isEmpty()) {
                        pathways.put(reactionName, pathsArray);
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathways;

    }

 /*   private static enum RelTypes implements RelationshipType {

        ISREACTAN, ISPRODUCT, BELONGTOPATHWAY, HASREACTION, ISINCOMPARTMENT, HASCOMPARTMENT
    }*/

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
