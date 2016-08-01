/*
 * Copyright 2010 - 2012 VTT Biotechnology
 * This file is part of ALVS.
 *
 * ALVS is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * ALVS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ALVS; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.simulation.geneticalgorithm.testing;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.modules.simulation.geneticalgorithmDirections.tools.Bug.status;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
public class StartTestingGATask extends AbstractTask {

    private final Dataset training;
    private final List<String> reactions;
    private final Map<String, status> reactionDirections;
    private final GetInfoAndTools tools;
    private String info = "";

    public StartTestingGATask(Dataset dataset, SimpleParameterSet parameters) {
        training = dataset;
        this.reactionDirections = new HashMap<>();
        String reactionsToTest = parameters.getParameter(StartTestingGAParameters.reactions).getValue();
        this.reactions = new ArrayList<>();
        if (reactionsToTest.contains(",") && !reactionsToTest.contains(" - ")) {
            reactions.addAll(Arrays.asList(reactionsToTest.split(",")));
        } else if (!reactionsToTest.contains(",") && reactionsToTest.contains(" - ")) {
            reactions.addAll(Arrays.asList(reactionsToTest.split(" - ")));
        } else if (reactionsToTest.contains(",") && reactionsToTest.contains(" - ")) {
            reactions.addAll(Arrays.asList(reactionsToTest.split(",")));
            for (String reaction : reactions) {
                String[] r = reaction.split(" - ");
                status stt = status.KO;
                if (r[1].contains("LB")) {
                    stt = status.LB;
                } else if (r[1].contains("UP")) {
                    stt = status.UP;
                }
                this.reactionDirections.put(r[0], stt);
            }
            this.reactions.clear();
        }else{
            this.reactions.add(reactionsToTest);
        }
        this.tools = new GetInfoAndTools();
    }

    @Override
    public String getTaskDescription() {
        return "Start Testing... ";
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
            info = info + "Modified model from " + this.training.getDatasetName() + "\nReactions changed or removed: \n";
            info = info + this.showReactions(reactions);
            modifyModel();

           //  this.tools.createDataFile(null, this.training, null, this.training.getSources(), false, true);
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }

    private void modifyModel() {
        Model m = this.training.getDocument().getModel().clone();
        for (String reaction : this.reactions) {
            System.out.println(reaction);
           // Reaction r = m.getReaction(reaction);
            m.removeReaction(reaction);
            //r.getKineticLaw().getLocalParameter("UPPER_BOUND").setValue(0.0);
            //r.getKineticLaw().getLocalParameter("LOWER_BOUND").setValue(0.0);

        }

        for (String reaction : this.reactionDirections.keySet()) {
            status stt = this.reactionDirections.get(reaction);
            if (stt == status.KO) {
                m.removeReaction(reaction);
            } else if (stt == status.LB) {
                Reaction r = m.getReaction(reaction);
                r.getKineticLaw().getLocalParameter("LOWER_BOUND").setValue(0.0);
            } else if (stt == status.UP) {
                Reaction r = m.getReaction(reaction);
                r.getKineticLaw().getLocalParameter("UPPER_BOUND").setValue(0.0);
            }

        }
        SimpleBasicDataset dataset = new SimpleBasicDataset();
        dataset.setDocument(this.training.getDocument().clone());
        dataset.getDocument().setModel(m);
        dataset.setDatasetName("Modified - " + this.training.getDatasetName());
        dataset.SetCluster(false);
        Path path = Paths.get(this.training.getPath());
        Path fileName = path.getFileName();
        String name = fileName.toString();
        String p = training.getPath().replace(name, "");
        p = p + this.training.getDatasetName();
        dataset.setPath(p);
        dataset.addInfo(info);
        NDCore.getDesktop().AddNewFile(dataset);
    }

    private String showReactions(List<String> possibleReactions) {
        Model m = this.training.getDocument().getModel();
        String infoReaction = "";
        for (String reaction : possibleReactions) {
            Reaction r = m.getReaction(reaction);
            KineticLaw law = r.getKineticLaw();
            if (law != null) {
                LocalParameter lbound = law.getLocalParameter("LOWER_BOUND");
                LocalParameter ubound = law.getLocalParameter("UPPER_BOUND");
                infoReaction = infoReaction + r.getId() + " - " + r.getName() + " lb: " + lbound.getValue() + " up: " + ubound.getValue() + ":\n";
            } else {
                infoReaction = infoReaction + r.getId() + ":\n";
            }
            infoReaction = infoReaction + "Reactants: \n";
            for (SpeciesReference sr : r.getListOfReactants()) {
                Species sp = sr.getSpeciesInstance();
                infoReaction = infoReaction + sr.getStoichiometry() + " " + sp.getId() + " - " + sp.getName() + "\n";
            }
            infoReaction = infoReaction + "Products: \n";
            for (SpeciesReference sr : r.getListOfProducts()) {
                Species sp = sr.getSpeciesInstance();
                infoReaction = infoReaction + sr.getStoichiometry() + " " + sp.getId() + " - " + sp.getName() + " \n";
            }
            infoReaction = infoReaction + "----------------------------------- \n";
        }
        return infoReaction;
    }

}
