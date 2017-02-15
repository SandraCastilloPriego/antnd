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
package ND.modules.reactionOP.setBounds;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.configuration.general.GetInfoAndTools;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;

/**
 *
 * @author scsandra
 */
public class SetBoundsTask extends AbstractTask {

    private final SimpleBasicDataset networkDS;
    private final File boundsFile;
    private double finishedPercentage = 0.0f;

    public SetBoundsTask(SimpleBasicDataset dataset, SimpleParameterSet parameters) {
        networkDS = dataset;
        this.boundsFile = parameters.getParameter(SetBoundsParameters.boundsFile).getValue();
    }

    public void readBounds(Dataset networkDS) {
        try {
            SBMLDocument doc = networkDS.getDocument();
            Model m = doc.getModel();

            CsvReader reader = new CsvReader(new FileReader(this.boundsFile));

            while (reader.readRecord()) {
                String[] data = reader.getValues();
                String reactionName = data[0].replace("-", "");
                //System.out.println(reactionName);
                Reaction r = m.getReaction(reactionName);
                if (r != null) {
                    KineticLaw law = r.getKineticLaw();
                    if (law == null) {
                        law = new KineticLaw();
                    }
                    if (law.getLocalParameter("LOWER_BOUND") == null) {
                        LocalParameter lbound = new LocalParameter("LOWER_BOUND");
                        lbound.setValue(Double.valueOf(data[3]));
                        law.addLocalParameter(lbound);
                    }
                    if (law.getLocalParameter("UPPER_BOUND") == null) {
                        LocalParameter ubound = new LocalParameter("UPPER_BOUND");
                        ubound.setValue(Double.valueOf(data[4]));
                        law.addLocalParameter(ubound);
                        r.setKineticLaw(law);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("No bounds added to the reactions");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ND.modules.configuration.general.GetInfoAndTools.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    @Override
    public String getTaskDescription() {
        return "Adding bounds... ";
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

            this.readBounds(networkDS);
            for (Reaction r : m.getListOfReactions()) {
                if (r != null && r.getKineticLaw() == null) {
                    KineticLaw law = new KineticLaw();
                    LocalParameter lbound = new LocalParameter("LOWER_BOUND");
                    lbound.setValue(-1000);
                    law.addLocalParameter(lbound);
                    LocalParameter ubound = new LocalParameter("UPPER_BOUND");
                    ubound.setValue(1000);
                    law.addLocalParameter(ubound);
                    r.setKineticLaw(law);
                }

            }

            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }
}
