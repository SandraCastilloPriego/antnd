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
package ND.modules.analysis.Report;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.NDModuleCategory;
import ND.modules.NDProcessingModule;
import ND.parameters.ParameterSet;
import ND.taskcontrol.Task;

/**
 *
 * @author scsandra
 */
public class ReportFBAModule implements NDProcessingModule {

    public static final String MODULE_NAME = "Write Report";
    private final ReportFBAParameters parameters = new ReportFBAParameters();

    @Override
    public ParameterSet getParameterSet() {
        return parameters;
    }

    @Override
    public String toString() {
        return MODULE_NAME;
    }

    @Override
    public Task[] runModule(ParameterSet parameters) {
        SimpleBasicDataset selectedFiles = (SimpleBasicDataset) NDCore.getDesktop().getSelectedDataFiles()[0];

        // prepare a new group of tasks
        Task tasks[] = new ReportFBATask[1];
        tasks[0] = new ReportFBATask(selectedFiles, parameters);

        NDCore.getTaskController().addTasks(tasks);

        return tasks;
    }

    @Override
    public NDModuleCategory getModuleCategory() {
        return NDModuleCategory.ANALYSIS;
    }

    @Override
    public String getIcon() {
        return "icons/loadProject.png";
    }

    @Override
    public boolean setSeparator() {
        return false;
    }
}
