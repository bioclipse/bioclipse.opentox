/* Copyright (C) 2010  Egon Willighagen <egonw@users.sf.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. All we ask is that proper credit is given for our work,
 * which includes - but is not limited to - adding the above copyright notice to
 * the beginning of your source code files, and to any copyright notice that you
 * may distribute with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.bioclipse.opentox.api;

public class TaskState {
	
	/** The status of the task. */
	enum STATUS {
		CANCELLED,
		COMPLETED,
		RUNNING,
		ERROR,
		UNKNOWN
	}
	
	private STATUS status = STATUS.UNKNOWN;
	private boolean exists = false;
	private boolean isRedirected = false; 
	private String results = null;
	private float percentageCompleted = 0.0f;

	public boolean isFinished() {
		return status == STATUS.ERROR || status == STATUS.COMPLETED ;
	}
	public void setStatus(STATUS status) {
		this.status = status;
	}
	public STATUS getStatus() {
		return this.status;
	}
	public void setFinished(boolean isFinished) {
		if (isFinished) {
			this.status = STATUS.COMPLETED;
		} else {
			this.status = STATUS.RUNNING;
		}
	}
	public boolean isRedirected() {
		return isRedirected;
	}
	public void setRedirected(boolean isRedirected) {
		this.isRedirected = isRedirected;
	}
	public String getResults() {
		return results;
	}
	public void setResults(String results) {
		this.results = results;
	}
	public void setExists(boolean exists) {
		this.exists = exists;
	}
	public boolean exists() {
		return exists;
	}
	public void setPercentageCompleted(float percentageCompleted) {
		this.percentageCompleted = percentageCompleted;
	}
	public float getPercentageCompleted() {
		return percentageCompleted;
	}

}
