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
package ND.taskcontrol;

import java.util.LinkedList;

/**
 * An abstract implementation of task which defines common methods to make Task
 * implementation easier
 */
public abstract class AbstractTask implements Task {

	private TaskStatus status = TaskStatus.WAITING;
	private LinkedList<TaskListener> taskListeners = new LinkedList<>();
	protected String errorMessage = null;

	/**
	 * Adds a TaskListener to this Task
	 * 
	 * @param t
	 *            The TaskListener to add
	 */
        @Override
	public void addTaskListener(TaskListener t) {
		taskListeners.add(t);
	}

	/**
	 * Returns all of the TaskListeners which are listening to this task.
	 * 
	 * @return An array containing the TaskListeners
	 */
        @Override
	public TaskListener[] getTaskListeners() {
		return taskListeners.toArray(new TaskListener[0]);
	}

	/**
	 * Triggers a TaskEvent and notifies the listeners
	 */
	private void fireTaskEvent() {
		TaskEvent event = new TaskEvent(this);
		for (TaskListener t : this.taskListeners) {
			t.statusChanged(event);
		}
	}

	/**
	 * @see net.sf.mzmine.taskcontrol.Task#setStatus()
	 */
	public final void setStatus(TaskStatus newStatus) {
		this.status = newStatus;
		this.fireTaskEvent();
	}

	/**
	 * Convenience method for determining if this task has been canceled. Also
	 * returns true if the task encountered an error.
	 * 
	 * @return true if this task has been canceled or stopped due to an error
	 */
	public final boolean isCanceled() {
		return (status == TaskStatus.CANCELED) || (status == TaskStatus.ERROR);
	}

	/**
	 * Convenience method for determining if this task has been completed
	 * 
	 * @return true if this task is finished
	 */
	public final boolean isFinished() {
		return status == TaskStatus.FINISHED;
	}

	/**
	 * @see net.sf.mzmine.taskcontrol.Task#cancel()
	 */
        @Override
	public void cancel() {
		setStatus(TaskStatus.CANCELED);
	}

	/**
	 * @see net.sf.mzmine.taskcontrol.Task#getErrorMessage()
	 */
        @Override
	public final String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Returns the TaskStatus of this Task
	 * 
	 * @return The current status of this task
	 */
        @Override
	public final TaskStatus getStatus() {
		return this.status;
	}

        @Override
	public Object[] getCreatedObjects() {
		return null;
	}

}
