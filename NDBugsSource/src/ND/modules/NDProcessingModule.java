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

package ND.modules;

import ND.parameters.ParameterSet;
import ND.taskcontrol.Task;



/**
 * Interface representing a data processing method which can be executed in a
 * batch
 */
public interface NDProcessingModule extends NDModule {

	/**
	 * Runs this method on a given items, and calls another task listener after
	 * task is complete and results have been processed.
	 * 
	 */
	public Task[] runModule(ParameterSet parameters);

	/**
	 * Returns the category of the module (e.g. raw data processing, peak
	 * picking etc.). Menu item will be created according to the category.
	 */
	public NDModuleCategory getModuleCategory();

        /**
         * Returns the path of the icon
         * @return
         */

        public String getIcon();

        public boolean setSeparator();

}
