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
package ND.modules.simulation.Dynamic;

import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import java.io.File;
import org.COPASI.*;

/**
 *
 * @author scsandra
 */
public class DynamicTask extends AbstractTask {

    private double finishedPercentage = 0.0f;
    private final File copasiFile;

    public DynamicTask(SimpleParameterSet parameters) {
        this.copasiFile = parameters.getParameter(DynamicParameters.filename).getValue();

    }

    @Override
    public String getTaskDescription() {
        return "Starting Dynamic Simulation... ";
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
        assert CCopasiRootContainer.getRoot() != null;
        // create a new datamodel
        CCopasiDataModel dataModel = CCopasiRootContainer.addDatamodel();
        assert CCopasiRootContainer.getDatamodelList().size() == 1;
        // the only argument to the main routine should be the name of a CPS file

        String filename = this.copasiFile.getAbsolutePath();
        try {
            // load the model without progress report
            dataModel.loadModel(filename);
        } catch (java.lang.Exception ex) {
            System.err.println("Error while loading the model from file named \"" + filename + "\".");
            System.exit(1);
        }
        CModel model = dataModel.getModel();
        assert model != null;
        System.out.println("Model statistics for model \"" + model.getObjectName() + "\".");

        // output number and names of all compartments
        int i, iMax = (int) model.getCompartments().size();
        System.out.println("Number of Compartments: " + (new Integer(iMax)).toString());
        System.out.println("Compartments: ");
        for (i = 0; i < iMax; ++i) {
            CCompartment compartment = model.getCompartment(i);
            assert compartment != null;
            System.out.println("\t" + compartment.getObjectName());
        }

        // output number and names of all metabolites
        iMax = (int) model.getMetabolites().size();
        System.out.println("Number of Metabolites: " + (new Integer(iMax)).toString());
        System.out.println("Metabolites: ");
        for (i = 0; i < iMax; ++i) {
            CMetab metab = model.getMetabolite(i);
            assert metab != null;
            System.out.println("\t" + metab.getObjectName());
        }

        // output number and names of all reactions
        iMax = (int) model.getReactions().size();
        System.out.println("Number of Reactions: " + (new Integer(iMax)).toString());
        System.out.println("Reactions: ");
        for (i = 0; i < iMax; ++i) {
            CReaction reaction = model.getReaction(i);
            assert reaction != null;
            System.out.println("\t" + reaction.getObjectName());
        }

        
        
        // REPORT
        
        
        CReportDefinitionVector reports = dataModel.getReportDefinitionList();
        // create a new report definition object
        CReportDefinition report = reports.createReportDefinition("Report", "Output for timecourse");
        // set the task type for the report definition to timecourse
        report.setTaskType(CCopasiTask.timeCourse);
        // we don't want a table
        report.setIsTable(false);
        // the entries in the output should be seperated by a ", "
        report.setSeparator(new CCopasiReportSeparator(", "));

          // we need a handle to the header and the body
        // the header will display the ids of the metabolites and "time" for
        // the first column
        // the body will contain the actual timecourse data
        ReportItemVector header = report.getHeaderAddr();
        ReportItemVector body = report.getBodyAddr();

        body.add(new CRegisteredObjectName(model.getObject(new CCopasiObjectName("Reference=Time")).getCN().getString()));
        body.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
        header.add(new CRegisteredObjectName(new CCopasiStaticString("time").getCN().getString()));
        header.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));

        i = 0;
        iMax = (int) model.getMetabolites().size();
        for (i = 0; i < iMax; ++i) {
            CMetab metab = model.getMetabolite(i);
            assert metab != null;
            // we don't want output for FIXED metabolites right now
            if (metab.getStatus() != CModelEntity.FIXED) {
                  // we want the concentration oin the output
                // alternatively, we could use "Reference=Amount" to get the
                // particle number
                body.add(new CRegisteredObjectName(metab.getObject(new CCopasiObjectName("Reference=Concentration")).getCN().getString()));
                // add the corresponding id to the header
                header.add(new CRegisteredObjectName(new CCopasiStaticString(metab.getSBMLId()).getCN().getString()));
                // after each entry, we need a seperator
                if (i != iMax - 1) {
                    body.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
                    header.add(new CRegisteredObjectName(report.getSeparator().getCN().getString()));
                }

            }
        }

        // get the trajectory task object
        CTrajectoryTask trajectoryTask = (CTrajectoryTask) dataModel.getTask("Time-Course");
        assert trajectoryTask != null;
        // if there isn't one
        if (trajectoryTask == null) {
            // create a new one
            trajectoryTask = new CTrajectoryTask();

              // add the new time course task to the task list
            // this method makes sure that the object is now owned 
            // by the list and that it does not get deleted by SWIG
            dataModel.getTaskList().addAndOwn(trajectoryTask);
        }

        // run a deterministic time course
        trajectoryTask.setMethodType(CCopasiMethod.deterministic);

        // pass a pointer of the model to the problem
        trajectoryTask.getProblem().setModel(dataModel.getModel());

          // actiavate the task so that it will be run when the model is saved
        // and passed to CopasiSE
        trajectoryTask.setScheduled(true);

        // set the report for the task
        trajectoryTask.getReport().setReportDefinition(report);
        // set the output filename
        trajectoryTask.getReport().setTarget("example3.txt");
        // don't append output if the file exists, but overwrite the file
        trajectoryTask.getReport().setAppend(false);

        // get the problem for the task to set some parameters
        CTrajectoryProblem problem = (CTrajectoryProblem) trajectoryTask.getProblem();

        // simulate 100 steps
        problem.setStepNumber(200);
        // start at time 0
        dataModel.getModel().setInitialTime(0.0);
        // simulate a duration of 10 time units
        problem.setDuration(20);
        // tell the problem to actually generate time series data
        problem.setTimeSeriesRequested(true);

        // set some parameters for the LSODA method through the method
        CTrajectoryMethod method = (CTrajectoryMethod) trajectoryTask.getMethod();

        CCopasiParameter parameter = method.getParameter("Absolute Tolerance");
        assert parameter != null;
        assert parameter.getType() == CCopasiParameter.DOUBLE;
        parameter.setDblValue(1.0e-12);

        boolean result = true;
        try {
            // now we run the actual trajectory
            result = trajectoryTask.processWithOutputFlags(true, (int) CCopasiTask.ONLY_TIME_SERIES);
        } catch (java.lang.Exception ex) {
            System.err.println("Error. Running the time course simulation failed.");
            String lastError = trajectoryTask.getProcessError();
            // check if there are additional error messages
            if (lastError.length() > 0) {
                // print the messages in chronological order
                System.err.println(lastError);
            }
            System.exit(1);
        }
        if (result == false) {
            System.err.println("An error occured while running the time course simulation.");
            // check if there are additional error messages
            if (CCopasiMessage.size() > 0) {
                // print the messages in chronological order
                System.err.println(CCopasiMessage.getAllMessageText(true));
            }
            System.exit(1);
        }

        
        // look at the timeseries
          CTimeSeries timeSeries = trajectoryTask.getTimeSeries();
          // we simulated 100 steps, including the initial state, this should be
          // 101 step in the timeseries
          assert timeSeries.getRecordedSteps() == 101;
          System.out.println( "The time series consists of " + (new Long(timeSeries.getRecordedSteps())).toString() + "." );
          System.out.println( "Each step contains " + (new Long(timeSeries.getNumVariables())).toString() + " variables." );
          System.out.println( "The final state is: " );
          iMax = (int)timeSeries.getNumVariables();
          int lastIndex = (int)timeSeries.getRecordedSteps() - 1;
          for (i = 0;i < iMax;++i)
          {
              // here we get the particle number (at least for the species)
              // the unit of the other variables may not be particle numbers
              // the concentration data can be acquired with getConcentrationData
              System.out.println(timeSeries.getTitle(i) + ": " + (new Double(timeSeries.getData(lastIndex, i))).toString() );
          }

        
        setStatus(TaskStatus.FINISHED);

    }

}
