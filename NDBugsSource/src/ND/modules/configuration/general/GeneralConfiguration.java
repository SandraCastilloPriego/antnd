/*
 * Copyright 2007-2012 
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
package ND.modules.configuration.general;

import ND.main.NDCore;
import ND.modules.NDModuleCategory;
import ND.modules.NDProcessingModule;
import ND.parameters.ParameterSet;
import ND.taskcontrol.Task;

public class GeneralConfiguration implements NDProcessingModule {

        public static final String MODULE_NAME = "General configuration";
        private final GeneralconfigurationParameters parameters = NDCore.getPreferences();

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
                NDCore.setPreferences((GeneralconfigurationParameters) parameters);
                return null;
        }

        @Override
        public NDModuleCategory getModuleCategory() {
                return NDModuleCategory.SIMULATION;
        }

        @Override
        public String getIcon() {
                return "icons/configuration.png";
        }

        @Override
        public boolean setSeparator() {
                return false;
        }
}
