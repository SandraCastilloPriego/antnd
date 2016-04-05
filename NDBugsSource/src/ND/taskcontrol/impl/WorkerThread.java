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
package ND.taskcontrol.impl;

import ND.main.NDCore;
import ND.taskcontrol.Task;
import ND.taskcontrol.TaskStatus;
import ND.util.ExceptionUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 * Task controller worker thread, this thread will process one task and then
 * finish
 */
class WorkerThread extends Thread {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private WrappedTask wrappedTask;
	private boolean finished = false;

	WorkerThread(WrappedTask wrappedTask) {
		super("Thread executing task " + wrappedTask);
		this.wrappedTask = wrappedTask;
		wrappedTask.assignTo(this);
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
        @Override
	public void run() {

		Task actualTask = wrappedTask.getActualTask();

		logger.finest("Starting processing of task: "
				+ actualTask.getTaskDescription());

		try {

			// Process the actual task
			actualTask.run();

			// Check if task finished with an error
			if (actualTask.getStatus() == TaskStatus.ERROR) {
				logger.severe("Task error: " + actualTask.getErrorMessage());

				String errorMsg = actualTask.getErrorMessage();
				if (errorMsg == null)
					errorMsg = "Unspecified error";

				NDCore.getDesktop().displayErrorMessage(
						"Error of task " + actualTask.getTaskDescription(),
						errorMsg);
			}

			/*
			 * This is important to allow the garbage collector to remove the
			 * task, while keeping the task description in the
			 * "Tasks in progress" window
			 */
			wrappedTask.removeTaskReference();

		} catch (Throwable e) {

			/*
			 * This should never happen, it means the task did not handle its
			 * exception properly, or there was some severe error, like
			 * OutOfMemoryError
			 */

			logger.log(Level.SEVERE,
					"Unhandled exception " + e + " while processing task "
							+ actualTask.getTaskDescription(), e);

			e.printStackTrace();

			NDCore.getDesktop().displayErrorMessage(
					"Unhandled exception in task "
							+ actualTask.getTaskDescription() + ": "
							+ ExceptionUtils.exceptionToString(e));

		}

		/*
		 * Mark this thread as finished
		 */
		finished = true;

	}

	boolean isFinished() {
		return finished;
	}

}
