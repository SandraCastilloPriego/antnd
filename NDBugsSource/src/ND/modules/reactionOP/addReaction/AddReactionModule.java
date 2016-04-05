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
package ND.modules.reactionOP.addReaction;

import ND.data.impl.datasets.SimpleBasicDataset;
import ND.main.NDCore;
import ND.modules.NDModuleCategory;
import ND.modules.NDProcessingModule;
import ND.parameters.ParameterSet;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.Task;

/**
 *
 * @author scsandra
 */
public class AddReactionModule implements NDProcessingModule {

        public static final String MODULE_NAME = "Add Reaction";
        private AddReactionParameters parameters = new AddReactionParameters();

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

                // prepare a new group of tasks
                Task tasks[] = new AddReactionTask[1];
                SimpleBasicDataset dataset = null;
                try {
                        dataset = (SimpleBasicDataset) NDCore.getDesktop().getSelectedDataFiles()[0];
                } catch (Exception e) {
                }
                tasks[0] = new AddReactionTask(dataset, (SimpleParameterSet) parameters);

                NDCore.getTaskController().addTasks(tasks);

                return tasks;
        }

        @Override
        public NDModuleCategory getModuleCategory() {
                return NDModuleCategory.REACTION;
        }

        @Override
        public String getIcon() {
                return "icons/AddReaction.png";
        }

        @Override
        public boolean setSeparator() {
                return false;
        }
}
