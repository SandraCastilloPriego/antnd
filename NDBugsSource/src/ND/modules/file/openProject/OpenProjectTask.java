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
package ND.modules.file.openProject;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.parser.Parser;
import ND.data.parser.impl.BasicFilesParserSBML;
import ND.main.NDCore;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.simulation.FBA.Ant;
import ND.modules.simulation.FBA.SpeciesFA;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import ND.util.StreamCopy;
import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipFile;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author scsandra
 */
public class OpenProjectTask extends AbstractTask {

    private File fileDir;
    private double finishedPercentage = 0.0f;

    public OpenProjectTask(File fileDir) {
        if (fileDir != null) {
            this.fileDir = fileDir;
        }
    }

    @Override
    public String getTaskDescription() {
        return "Opening File... ";
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
            try (ZipFile zipFile = new ZipFile(fileDir)) {
                loadSBMLFiles(zipFile);
                finishedPercentage = 0.5f;
                loadInfo(zipFile);
                finishedPercentage = 0.70f;
                loadPaths(zipFile);
                finishedPercentage = 0.99f;
            }
            setStatus(TaskStatus.FINISHED);
        } catch (IOException e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }

    private void loadSBMLFiles(ZipFile zipFile) throws IOException {
        Enumeration zipEntries = zipFile.entries();
        Pattern filePattern = Pattern
            .compile("(.*)\\.sbml$|(.*)\\.xml$");
        while (zipEntries.hasMoreElements()) {
            if (isCanceled()) {
                return;
            }
            ZipEntry entry = (ZipEntry) zipEntries.nextElement();

            String entryName = entry.getName();
            Matcher fileMatcher = filePattern.matcher(entryName);
            if (fileMatcher.matches()) {
                InputStream sbmlStream = zipFile.getInputStream(entry);
                File tempFile = File.createTempFile(entryName, ".tmp");
                try (FileOutputStream fileStream = new FileOutputStream(tempFile)) {
                    StreamCopy copyMachine = new StreamCopy();
                    copyMachine.copy(sbmlStream, fileStream);
                    Parser parser = new BasicFilesParserSBML(tempFile.getAbsolutePath());
                    parser.createDataset(entryName);
                    Dataset dataset = (SimpleBasicDataset) parser.getDataset();
                    if (dataset.getDocument() != null) {
                        NDCore.getDesktop().AddNewFile(dataset);
                    }
                    fileStream.close();
                }
                tempFile.delete();
            }
        }
    }

    private void loadInfo(ZipFile zipFile) throws IOException {
        Enumeration zipEntries = zipFile.entries();
        Pattern filePattern = Pattern
            .compile("(.*)\\.info$");
        while (zipEntries.hasMoreElements()) {
            if (isCanceled()) {
                return;
            }
            ZipEntry entry = (ZipEntry) zipEntries.nextElement();

            String entryName = entry.getName();

            Dataset data = getDataset(entryName);
            if (data != null) {
                Matcher fileMatcher = filePattern.matcher(entryName);
                if (fileMatcher.matches()) {
                    InputStream sbmlStream = zipFile.getInputStream(entry);
                    DataInputStream in = new DataInputStream(sbmlStream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    Graph g = null;
                    while ((strLine = br.readLine()) != null) {
                        if (strLine.contains("Is Parent")) {
                            data.setIsParent(true);
                        } else if (strLine.contains("Not Parent")) {
                            data.setIsParent(false);
                            data.setParent(strLine.substring(strLine.indexOf("Not Parent: ")+12));
                        } else if (strLine.contains("Biomass= ")) {
                            //data.setBiomass(strLine.split("= ")[1]);
                        } else if (strLine.contains("Sources= ")) {
                            data.addSource(strLine.split("= ")[1]);
                        } else if (strLine.contains("Nodes= ")) {
                            if (g == null) {
                                g = new Graph(null, null);
                            }
                            String nodeName = strLine.split("= ")[1];
                            String position = null;
                            int color = -1;
                            if (nodeName.contains(" // ")) {
                                String[] properties = nodeName.split(" // ");
                                position = properties[1];
                                nodeName = properties[0];
                                if (properties.length > 2 && !properties[2].contains("null")) {
                                    color = Integer.valueOf(properties[2]);
                                }
                            }
                            String id = nodeName.split(" : ")[0];
                            String name = "";
                            try {
                                name = nodeName.split(" : ")[1];
                            } catch (Exception e) {
                            }
                            Node n = new Node(id, name);
                            if (position != null && !position.contains("null")) {
                                String[] point = position.split(" , ");
                                n.setPosition(Double.valueOf(point[0]), Double.valueOf(point[1]));
                            }

                            if (color != -1) {
                                n.setColor(new Color(color));
                            }
                            g.addNode(n);

                        } else if (strLine.contains("Edges= ")) {
                            String edgeName = strLine.split("= ")[1].split(" // ")[0];
                            String[] parts = strLine.split(" // ");
                            Node source = g.getNode(parts[1].split(" \\|\\| ")[0]);
                            Node destination = g.getNode(parts[1].split(" \\|\\| ")[1]);
                            boolean direction = Boolean.getBoolean(parts[2]);
                            Edge e = new Edge(edgeName, source, destination, direction);
                            g.addEdge(e);
                        } else {
                            data.addInfo(strLine);
                        }
                    }
                    data.setGraph(g);
                    //Close the input stream
                    in.close();
                }
            }
        }
    }

    private void loadPaths(ZipFile zipFile) throws IOException {
        Enumeration zipEntries = zipFile.entries();
        Pattern filePattern = Pattern
            .compile("(.*)\\.paths$");
        while (zipEntries.hasMoreElements()) {
            if (isCanceled()) {
                return;
            }
            ZipEntry entry = (ZipEntry) zipEntries.nextElement();

            String entryName = entry.getName();

            Dataset data = getDataset(entryName);
            if (data != null) {
                Matcher fileMatcher = filePattern.matcher(entryName);
                if (fileMatcher.matches()) {
                    InputStream pathStream = zipFile.getInputStream(entry);
                    DataInputStream in = new DataInputStream(pathStream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    HashMap<String, SpeciesFA> compounds = new HashMap<>();
                    while ((strLine = br.readLine()) != null) {
                        try {
                            String compoundInfo = strLine.split(" : ")[0];
                            String pathInfo = strLine.split(" : ")[1];
                            SpeciesFA compound = new SpeciesFA(compoundInfo.split(" - ")[0], compoundInfo.split(" - ")[1]);
                            Map<String, Boolean> path = new HashMap<>();
                            String[] pathString = pathInfo.split(",");
                            for (String pS : pathString) {
                                path.put(pS.split(" - ")[0], Boolean.valueOf(pS.split(" - ")[1]));
                            }
                            Ant ant = new Ant(compound.getId());
                            ant.setPath(path);
                            ant.setPathSize(path.size());
                            compound.setAnt(ant);
                            compounds.put(compound.getId(), compound);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //Close the input stream
                    in.close();
                    data.setPaths(compounds);
                }
            }
        }
    }

    private Dataset getDataset(String entryName) {
        Dataset[] datasets = NDCore.getDesktop().getAllDataFiles();
        String name = entryName.split("\\.")[0];
        for (Dataset dataset : datasets) {
            String dataName = dataset.getDatasetName().split("\\.")[0];
            if (dataName.equals(name)) {
                return dataset;
            }
        }
        return null;
    }

}
