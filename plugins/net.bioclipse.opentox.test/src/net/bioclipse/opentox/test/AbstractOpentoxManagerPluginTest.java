/*******************************************************************************
 * Copyright (c) 2011  Egon Willighagen <egon.willighagen@ki.se>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.opentox.test;

import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.opentox.business.IOpentoxManager;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractOpentoxManagerPluginTest
extends AbstractManagerTest {

    protected static IOpentoxManager managerNamespace;
    
    @Test public void testAuthentication() {
        opentox.logout();
        Assert.assertNull(opentox.getToken());
        opentox.login("guest", "guest"); // the official test account
        String token = opentox.getToken();
        Assert.assertNotNull(token);
        Assert.assertNotSame(0,token.length());
        opentox.logout();
        Assert.assertNull(opentox.getToken());
    }

    public Class<? extends IBioclipseManager> getManagerInterface() {
    	return IOpentoxManager.class;
    }
}
