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
package ND.modules.file.saveProject;

import ND.main.NDCore;
import ND.modules.NDModuleCategory;
import ND.modules.NDProcessingModule;
import ND.parameters.ParameterSet;
import ND.taskcontrol.Task;
import java.io.File;

/**
 *
 * @author scsandra
 */
public class SaveProjectModule implements NDProcessingModule {

        public static final String MODULE_NAME = "Save Project";
        private final SaveProjectParameters parameters = new SaveProjectParameters();

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
                File file = this.parameters.getParameter(SaveProjectParameters.fileNames).getValue();

                // prepare a new group of tasks
                Task tasks[] = new SaveProjectTask[1];
                tasks[0] = new SaveProjectTask(file);

                NDCore.getTaskController().addTasks(tasks);

                return tasks;
        }

        @Override
        public NDModuleCategory getModuleCategory() {
                return NDModuleCategory.FILE;
        }

        @Override
        public String getIcon() {
                return "icons/opensbml.png";
        }

        @Override
        public boolean setSeparator() {
                return false;
        }
}
