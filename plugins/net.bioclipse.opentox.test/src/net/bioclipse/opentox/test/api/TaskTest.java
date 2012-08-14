/* Copyright (c) 2012  Egon Willighagen <egon.willighagen@ki.se>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.opentox.test.api;

import net.bioclipse.opentox.api.Task;
import net.bioclipse.opentox.api.TaskState;

import org.junit.Assert;
import org.junit.Test;

public class TaskTest {

	@Test
	public void testGetState() throws Exception {
		String task = "http://apps.ideaconsult.net:8080/ambit2/task/1";
		// getListOfAvailableDatasets(service);
		TaskState state = Task.getState(task);
		Assert.assertNotNull(state);
	}

}
