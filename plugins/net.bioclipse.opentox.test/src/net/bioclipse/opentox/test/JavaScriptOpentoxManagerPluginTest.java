/*******************************************************************************
 * Copyright (c) 2011  Egon Willighagen <egon.willighagen@ki.se>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contact: http://www.bioclipse.net/    
 ******************************************************************************/
package net.bioclipse.opentox.test;

import net.bioclipse.managers.business.IBioclipseManager;

import org.junit.BeforeClass;

public class JavaScriptOpentoxManagerPluginTest
    extends AbstractOpentoxManagerPluginTest {

    @BeforeClass public static void setup() {
        managerNamespace = net.bioclipse.opentox.Activator.getDefault()
            .getJavaScriptOpentoxManager();
    }

	@Override
	public IBioclipseManager getManager() {
		return managerNamespace;
	}

}
