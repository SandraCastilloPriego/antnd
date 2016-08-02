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
package ND.modules.analysis.VisualizeCofactors;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.desktop.impl.PrintPaths;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class VisualizeCofactorsTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private double finishedPercentage = 0.0f;
    private final JInternalFrame frame;
    private final JScrollPane panel;
    private final JPanel pn;

    public VisualizeCofactorsTask(Dataset dataset, SimpleParameterSet parameters) {
        networkDS = (SimpleBasicDataset) dataset;
        this.frame = new JInternalFrame("Result", true, true, true, true);
        this.pn = new JPanel();
        this.panel = new JScrollPane(pn);
    }

    @Override
    public String getTaskDescription() {
        return "Visualizing transport of cofactors... ";
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

            Model m = this.networkDS.getDocument().getModel();
            Graph g = getCofactorGraph(m);
            PrintPaths p = new PrintPaths(m);
            this.pn.add(p.printPathwayInFrame(g));

            frame.setSize(new Dimension(700, 500));
            frame.add(this.panel);
            NDCore.getDesktop().addInternalFrame(frame);
            finishedPercentage = 1.0f;
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }

    private Graph getCofactorGraph(Model model) {
        List<Reaction> reactions = new ArrayList<>();
        for (Reaction r : model.getListOfReactions()) {
            if (isCofactorTransport(r.getName())) {
                reactions.add(r);
            }
        }
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        for (Compartment c : model.getListOfCompartments()) {
            Node n = new Node(c.getName(), c.getId());
            n.setColor(Color.red);
            nodes.add(n);
        }

        for (Reaction r : reactions) {
            KineticLaw law = r.getKineticLaw();
            double flux = law.getLocalParameter("FLUX_VALUE").getValue();
            if (Math.abs(flux) >= 0.001) {
                String nodeSource = null, nodeDestination = null, value = "";
                for (SpeciesReference spr : r.getListOfReactants()) {
                    Species sp = spr.getSpeciesInstance();
                    Compartment comp = sp.getCompartmentInstance();
                    value = sp.getName();
                    if (flux > 0) {
                        nodeSource = comp.getName();
                    } else {
                        nodeDestination = comp.getName();
                    }
                }
                for (SpeciesReference spr : r.getListOfProducts()) {
                    Species sp = spr.getSpeciesInstance();
                    Compartment comp = sp.getCompartmentInstance();
                    value = sp.getName();
                    if (flux > 0) {
                        nodeDestination = comp.getName();
                    } else {
                        nodeSource = comp.getName();
                    }
                }
                DecimalFormat df = new DecimalFormat("#.000"); 
                Node middle = new Node(value, String.valueOf(df.format(Math.abs(flux))));
                nodes.add(middle);
                Edge e = new Edge(" - " + uniqueId.nextId(), this.getNode(nodes, nodeSource), middle);
                Edge e2 = new Edge(" - " + uniqueId.nextId(), middle, this.getNode(nodes, nodeDestination));
                edges.add(e);
                edges.add(e2);
            }
        }

        return new Graph(nodes, edges);
    }

    private Node getNode(List<Node> nodes, String node) {
        for (Node n : nodes) {
            if (n.getId().equals(node)) {
                return n;
            }
        }
        return null;
    }

    private boolean isCofactorTransport(String name) {
        if ((name.startsWith("CO2") || name.startsWith("O2") || name.startsWith("NAD") || name.startsWith("carbon dioxide") || name.contains("ATP")) && name.contains("port")) {
            return true;
        } else {
            return false;
        }
    }

}
