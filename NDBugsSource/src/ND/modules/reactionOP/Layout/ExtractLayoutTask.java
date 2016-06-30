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
package ND.modules.reactionOP.Layout;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class ExtractLayoutTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final double finishedPercentage = 0.0f;
    private final File layoutFile;
    private final GetInfoAndTools tools;

    public ExtractLayoutTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.layoutFile = parameters.getParameter(ExtractLayoutParameters.layoutFile).getValue();
        this.tools = new GetInfoAndTools();

    }

    @Override
    public String getTaskDescription() {
        return "Starting layout Extraction... ";
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
            Graph g = this.networkDS.getGraph();
            Graph newG = extractNetwork(g);
            if (newG != null) {
                this.tools.createDataFile(newG, networkDS, " ", this.networkDS.getSources(), false,false);
            }

            setStatus(TaskStatus.FINISHED);

        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

    private Graph extractNetwork(Graph g) {
        BufferedReader br = null;

        try {

            String sCurrentLine;
            List<Node> nodes = new ArrayList<>();
            List<Edge> edges = new ArrayList<>();
            br = new BufferedReader(new FileReader(this.layoutFile));
            String x = null, y = null, label = null;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains("node") || sCurrentLine.contains("edge")) {
                    if (label != null) {
                       // System.out.println(label + x + y);
                        Node node = g.getNode(label);
                        if (node != null) {
                            //System.out.println("2");
                            Node newNode = node;
                            newNode.setPosition(Double.valueOf(x), Double.valueOf(y));
                            //System.out.println("3");
                            nodes.add(newNode);
                        }
                        x = null;
                        y = null;
                        label = null;
                    }
                }
                if (sCurrentLine.contains("x	")) {
                    x = sCurrentLine.substring(sCurrentLine.indexOf("x	") + 2);
                   // System.out.println(x);
                }
                if (sCurrentLine.contains("y	")) {
                    y = sCurrentLine.substring(sCurrentLine.indexOf("y	") + 2);
                    //System.out.println(y);
                }
                if (sCurrentLine.contains("label	")) {
                    label = sCurrentLine.substring(sCurrentLine.indexOf("label	") + 6);
                    label = label.replace("\"", "");
                    if (label.contains(" : ")) {
                        label = label.split(" : ")[0];
                    }
                }

            }

            for (Node n : nodes) {
                List<Edge> e = g.getEdges(n.getId(), true);
                for (Edge edge : e) {
                    Node destination = edge.getDestination();
                    if (nodes.contains(destination)) {
                        edges.add(edge.clone());
                    }
                }
            }

            Graph newg = new Graph(nodes, edges);
            return newg;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

}
