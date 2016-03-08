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
import ND.modules.configuration.general.GetInfoAndTools;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.util.HashMap;
import java.util.Map;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
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

    private final GetInfoAndTools tools;
    private Double objective;

    private HashMap<String, ReactionFA> reactions;

    public LPTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.objectiveSpecie = parameters.getParameter(LPParameters.objective).getValue();
        this.tools = new GetInfoAndTools();

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
        setStatus(TaskStatus.PROCESSING);
        finishedPercentage = 0.1f;
        Graph g = optimize();
        if (g != null) {
            this.tools.createDataFile(g, networkDS, this.objectiveSpecie, this.networkDS.getSources(), false);
            String info = "Objective value of " + this.objectiveSpecie + ": " + this.objective;
            this.networkDS.addInfo(info);
        }
        finishedPercentage = 1f;
        setStatus(TaskStatus.FINISHED);
    }

    private Graph optimize() {
        createReactions();
        objective = this.getFlux(objectiveSpecie);

        if (objective > 0.0) {
            Graph g = createGraph();
            return g;
        }
        return null;

    }

    private Graph createGraph() {
        Model m = this.networkDS.getDocument().getModel();
        Graph g = new Graph(null, null);
        Graph previousG = this.networkDS.getGraph();
        for (String r : reactions.keySet()) {
            ReactionFA reaction = reactions.get(r);
            if (reaction != null && Math.abs(reaction.getFlux()) > 0.0000001) {
                Node reactionNode = new Node(reaction.getId(), String.format("%.3g%n", reaction.getFlux()));
                if (previousG != null) {
                    reactionNode.setPosition(previousG.getNode(reaction.getId()).getPosition());
                }
                g.addNode2(reactionNode);
                for (String reactant : reaction.getReactants()) {
                    String name = m.getSpecies(reactant).getName();
                    Node reactantNode = g.getNode(reactant);
                    if (reactantNode == null) {
                        reactantNode = new Node(reactant, name);
                    }
                    g.addNode2(reactantNode);
                    if (previousG != null) {
                        reactantNode.setPosition(previousG.getNode(reactant).getPosition());
                    }
                    Edge e = new Edge(r + " - " + uniqueId.nextId(), reactantNode, reactionNode);

                    g.addEdge(e);
                }
                for (String product : reaction.getProducts()) {
                    String name = m.getSpecies(product).getName();
                    Node reactantNode = g.getNode(product);
                    if (reactantNode == null) {
                        reactantNode = new Node(product, name);
                    }
                    if (previousG != null) {
                        reactantNode.setPosition(previousG.getNode(product).getPosition());
                    }
                    g.addNode2(reactantNode);
                    Edge e = new Edge(r + " - " + uniqueId.nextId(), reactionNode, reactantNode);

                    g.addEdge(e);
                }
            }
        }
        return g;
    }

    public double getFlux(String objective) {
        FBA fba = new FBA();
        ReactionFA objectiveReaction = new ReactionFA("objective");
        objectiveReaction.addReactant(objective, 1.0);
        objectiveReaction.setBounds(0, 1000);
        this.reactions.put("objective", objectiveReaction);
        fba.setModel("objective", this.reactions, this.networkDS.getDocument().getModel());
        try {
            Map<String, Double> soln = fba.run();
            for (String r : soln.keySet()) {
                if (this.reactions.containsKey(r)) {
                    this.reactions.get(r).setFlux(soln.get(r));
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return fba.getMaxObj();
    }

    private void createReactions() {
        SBMLDocument doc = this.networkDS.getDocument();
        this.reactions = new HashMap<>();
        Model m = doc.getModel();

        for (Reaction r : m.getListOfReactions()) {
            ReactionFA reaction = new ReactionFA(r.getId());

            try {
                KineticLaw law = r.getKineticLaw();
                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                reaction.setBounds(lbound.getValue(), ubound.getValue());
            } catch (Exception ex) {
                reaction.setBounds(-1000, 1000);
            }

            for (SpeciesReference s : r.getListOfReactants()) {

                Species sp = s.getSpeciesInstance();
                reaction.addReactant(sp.getId(), sp.getName(), s.getStoichiometry());
            }

            for (SpeciesReference s : r.getListOfProducts()) {
                Species sp = s.getSpeciesInstance();
                reaction.addProduct(sp.getId(), sp.getName(), s.getStoichiometry());
            }
            this.reactions.put(r.getId(), reaction);
        }
    }

}
