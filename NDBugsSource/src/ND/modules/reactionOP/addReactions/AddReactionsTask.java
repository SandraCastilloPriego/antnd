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
package ND.modules.reactionOP.addReactions;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.simulation.antNoGraph.ReactionFA;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
public class AddReactionsTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final double finishedPercentage = 0.0f;
    private final File reactionFile, compoundFile;
    private List<ReactionFA> reactions;
    private Map<String, String> compounds;
    private Map<String, String> compartments;

    public AddReactionsTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        networkDS = dataset;
        this.reactionFile = parameters.getParameter(AddReactionsParameters.reactionFile).getValue();
        this.compoundFile = parameters.getParameter(AddReactionsParameters.compoundsFile).getValue();
        this.reactions = new ArrayList<>();
        this.compounds = new HashMap<>();
        this.compartments = new HashMap<>();
    }

    @Override
    public String getTaskDescription() {
        return "Adding reaction... ";
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
            this.ReadFiles();

            SBMLDocument doc = this.networkDS.getDocument();
            Model m = doc.getModel();
            for (String id : this.compounds.keySet()) {
                Species sp = new Species(id);
                sp.setName(this.compounds.get(id));
                if(compartments.containsKey(id)) sp.setCompartment(compartments.get(id));                
                m.addSpecies(sp);
                
            }

            for (ReactionFA reaction : this.reactions) {
                Reaction r = new Reaction(reaction.getId());
                r.setName(reaction.getName());

                for (String reactant : reaction.getReactants()) {
                    String sp = reactant;
                    double stoi = reaction.getStoichiometry(reactant);

                    SpeciesReference spref = new SpeciesReference();
                    spref.setStoichiometry(Math.abs(stoi));
                    if (m.containsSpecies(sp)) {
                        spref.setSpecies(m.getSpecies(sp));
                    } else {
                        Species specie = new Species(sp);
                        m.addSpecies(specie);
                        spref.setSpecies(sp);
                    }
                    r.addReactant(spref);
                }

                for (String product : reaction.getProducts()) {
                    String sp = product;
                    double stoi = reaction.getStoichiometry(product);

                    SpeciesReference spref = new SpeciesReference();
                    spref.setStoichiometry(Math.abs(stoi));
                    if (m.containsSpecies(sp)) {
                        spref.setSpecies(m.getSpecies(sp));
                    } else {
                        Species specie = new Species(sp);
                        m.addSpecies(specie);
                        spref.setSpecies(sp);
                    }
                    r.addProduct(spref);
                }

                KineticLaw law = new KineticLaw();
                LocalParameter lboundP = new LocalParameter("LOWER_BOUND");
                lboundP.setValue(reaction.getlb());
                law.addLocalParameter(lboundP);
                LocalParameter uboundP = new LocalParameter("UPPER_BOUND");
                uboundP.setValue(reaction.getub());
                law.addLocalParameter(uboundP);
                LocalParameter objectiveP = new LocalParameter("OBJECTIVE_COEFFICIENT");
                objectiveP.setValue(0);                
                law.addLocalParameter(objectiveP);
                r.setKineticLaw(law);

                m.addReaction(r);
                String compounds = this.compounds.keySet().toString().trim().replace("[","").replace("]", "");
                String stoi = "";
                for(String c : reaction.getReactants()){
                    stoi += reaction.getStoichiometry(c)+" ";
                }
                for(String c : reaction.getProducts()){
                    stoi += reaction.getStoichiometry(c)+" ";
                }
                
                String info = "Adding reaction: " + reaction.getId() + " bounds:" + reaction.getlb() + " - " + reaction.getub() + "\nCompounds: " + compounds+"\nStoichiometry: "+stoi+"\n--------------------------";
                this.networkDS.addInfo(info);

            }
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
            System.out.println(errorMessage);
        }
    }

    private void ReadFiles() {
        try {
            CsvReader lines;
            lines = new CsvReader(new FileReader(this.compoundFile));
           // System.out.println(this.compoundFile);
            while (lines.readRecord()) {
                String[] r = lines.getValues();
                this.compounds.put(r[1], r[0]);
                if(r.length == 3) this.compartments.put(r[1],r[2]);
            }
            lines.close();
            // System.out.println(this.reactionFile);
            lines = new CsvReader(new FileReader(this.reactionFile));
            while (lines.readRecord()) {
                String[] r = lines.getValues();
                ReactionFA reaction = new ReactionFA(r[0], r[1]);
                String[] sp = r[2].split(",");
                String[] sto = r[3].split(" ");
                for (int i = 0; i < sp.length; i++) {
                    if (Double.valueOf(sto[i]) < 1) {
                        reaction.addReactant(sp[i], Double.valueOf(sto[i]));
                    } else {
                        reaction.addProduct(sp[i], Double.valueOf(sto[i]));
                    }
                }
                reaction.setBounds(Double.valueOf(r[4]), Double.valueOf(r[5]));
                 System.out.println(reaction.getId());
                this.reactions.add(reaction);
            }
            lines.close();

        } catch (FileNotFoundException ex) {
            System.out.println(ex.toString());

        } catch (IOException ex) {
            System.out.println("IO: "+ ex.toString());
        }
    }
}
