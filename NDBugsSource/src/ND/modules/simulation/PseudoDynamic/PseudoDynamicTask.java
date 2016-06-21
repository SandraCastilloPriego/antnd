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
package ND.modules.simulation.PseudoDynamic;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class PseudoDynamicTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private double finishedPercentage = 0.0f;
    private Map<String, CompartmentPS> compartments;
    private List<ReactionPS> exchanges;
    private List<ReactionPS> reactions;
    private Map<String, CompoundPS> compounds;
    private File fileName, reactioFile;
    private Writer writer, writer2;

    public PseudoDynamicTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        this.networkDS = dataset;
        this.compartments = new HashMap<>();
        this.exchanges = new ArrayList<>();
        this.reactions = new ArrayList<>();
        this.compounds = new HashMap<>();
        this.fileName = parameters.getParameter(PseudoDynamicParameters.file).getValue();
        this.reactioFile = parameters.getParameter(PseudoDynamicParameters.reactionfile).getValue();
    }

    @Override
    public String getTaskDescription() {
        return "Starting PseudoDynamic Simulation... ";
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
            writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(this.fileName), "utf-8"));

            writer2 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(this.reactioFile), "utf-8"));
            createNetwork();

            for (int i = 0; i < 100; i++) {
                Cycle();
            }

            for (ReactionPS reaction : this.reactions) {
                String r = reaction.getId() + " - " + reaction.getName();
                r += "\t" + reaction.lb + "\t" + reaction.ub + "\t" + reaction.getFFlux() + "\t" + reaction.getBFlux() + "\n";
                writer2.write(r);
            }
            
            writer.close();
            writer2.close();
            setStatus(TaskStatus.FINISHED);

        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

    private void createNetwork() {
        Model m = this.networkDS.getDocument().getModel();
        int numCompartments = m.getNumCompartments();
        for (int i = 0; i < numCompartments; i++) {
            Compartment c = m.getCompartment(i);
            if (!c.getName().equals("boundary")) {
                CompartmentPS compartment = new CompartmentPS(c.getName());
                this.compartments.put(c.getName(), compartment);
                //System.out.println(c.getName());
            }
        }

        for (Species sp : m.getListOfSpecies()) {
            String name = sp.getName();
            System.out.println(name);

            String c = sp.getCompartment();
            Compartment compartment = m.getCompartment(c);
            c = compartment.getName();
            if (!c.equals("boundary") && !sp.getId().contains("e_")) {
                CompoundPS compound = new CompoundPS(sp.getId(), name, c);
                if (sp.getId().equals("s_0434")) {
                    compound.setPool(100);
                }
                this.compartments.get(c).addCompound(compound);
                this.compounds.put(compound.getId(), compound);
            }

        }

        for (Reaction reaction : m.getListOfReactions()) {
            ReactionPS r = new ReactionPS(reaction.getId(), reaction.getName());
            KineticLaw law = reaction.getKineticLaw();
            LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
            LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
            r.setBounds(lbound.getValue(), ubound.getValue());
            for (SpeciesReference spref : reaction.getListOfReactants()) {
                Species sp = spref.getSpeciesInstance();
                if (this.compounds.containsKey(sp.getId())) {
                    r.addReactant(this.compounds.get(sp.getId()));
                    r.setStoichiometry(this.compounds.get(sp.getId()), spref.getStoichiometry());
                }
            }
            for (SpeciesReference spref : reaction.getListOfProducts()) {
                Species sp = spref.getSpeciesInstance();
                if (this.compounds.containsKey(sp.getId())) {
                    r.addProduct(this.compounds.get(sp.getId()));
                    r.setStoichiometry(this.compounds.get(sp.getId()), spref.getStoichiometry());
                }
            }

            if (reaction.getName().contains("exchange") && lbound.getValue() < 0) {
                this.exchanges.add(r);
            } else if (!reaction.getName().contains("exchange")) {
                this.reactions.add(r);
            }

        }

        String names = "";
        for (CompoundPS compound : this.compounds.values()) {
            names += "\t" + compound.getId() + "(" + compound.getName() + ")";
        }
        names += "\n";
        System.out.println(names);
        try {
            writer.write(names);
        } catch (IOException ex) {
            Logger.getLogger(PseudoDynamicTask.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void Cycle() {
        //Exchanges
        for (ReactionPS ex : this.exchanges) {
            List<CompoundPS> reactants = ex.getReactants();
            for (CompoundPS reactant : reactants) {
                reactant.setPool(1000);
            }
        }

        // update compounds   
        String pools = "";
        for (CompoundPS compound : this.compounds.values()) {
            if (compound.getPool() > 0) {
                List<ReactionPS> possibleReactions = getPossibleReactions(compound);
                updatefluxes(possibleReactions);
            }
            pools += "\t" + compound.getPool();
            if (compound.getId().equals("s_0450")) {
                System.out.println(compound.getPool());
            }

            if (!compound.getId().equals("s_1438")
                && !compound.getId().equals("s_1468")
                && !compound.getId().equals("s_0796")
                && !compound.getId().equals("s_1277")
                && !compound.getId().equals("s_1374")
                && !compound.getId().equals("s_1324")
                && !compound.getId().equals("s_0925")
                && !compound.getId().equals("s_0420")) {

                compound.setPool(500);

            }
        }
        pools += "\n";

        try {
            writer.write(pools);
        } catch (IOException ex) {
            Logger.getLogger(PseudoDynamicTask.class.getName()).log(Level.SEVERE, null, ex);
        }

    // put biomass to 0;
        if (this.compounds.get(
            "s_0450").getPool() > 900) {
            this.compounds.get("s_0450").setPool(0);
        }

    }

    private List<ReactionPS> getPossibleReactions(CompoundPS compound) {
        List<ReactionPS> possible = new ArrayList<>();
        for (ReactionPS r : this.reactions) {
            if (r.contains(compound) && r.isPossible(compound)) {
                possible.add(r);
            }
        }
        return possible;
    }

    private void updatefluxes(List<ReactionPS> possibleReactions) {
        for (ReactionPS reaction : possibleReactions) {
            reaction.update();
        }
    }

}
