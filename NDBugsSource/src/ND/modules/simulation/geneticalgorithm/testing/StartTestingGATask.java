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
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.w3c.dom.Document;

/**
 *
 * @author scsandra
 */
public class StartTestingGATask extends AbstractTask {
    
    private final Dataset training;
    private final List<String> reactions;
    private final GetInfoAndTools tools;
    
    public StartTestingGATask(Dataset dataset, SimpleParameterSet parameters) {
        training = dataset;
        String reactionsToTest = parameters.getParameter(StartTestingGAParameters.reactions).getValue();
        this.reactions = new ArrayList<>();
        if (reactionsToTest.contains(",")) {
            reactions.addAll(Arrays.asList(reactionsToTest.split(",")));
        } else {
            reactions.addAll(Arrays.asList(reactionsToTest.split(" - ")));
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
            modifyModel();
            this.tools.createDataFile(null, this.training, null, this.training.getSources(), false);
            
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            System.out.println(e.toString());
            setStatus(TaskStatus.ERROR);
        }
    }
    
    private void modifyModel() {
        Model m = this.training.getDocument().getModel().clone();
        for (String reaction : this.reactions) {
            Reaction r = m.getReaction(reaction);
            r.getKineticLaw().getLocalParameter("UPPER_BOUND").setValue(0.001);
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
        NDCore.getDesktop().AddNewFile(dataset);
    }
    
}
