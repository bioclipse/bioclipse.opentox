/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.opentox;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.opentox.business.IOpentoxManager;
import net.bioclipse.opentox.business.IJavaOpentoxManager;
import net.bioclipse.opentox.business.IJavaScriptOpentoxManager;

import org.apache.log4j.Logger;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The Activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID="net.bioclipse.opentox";

    private static final Logger logger = Logger.getLogger(Activator.class);

    // The shared instance
    private static Activator plugin;

    //A list of OpenTox services in order
    private static List<OpenToxService> openToxServices;

    // Trackers for getting the managers
    private ServiceTracker javaFinderTracker;
    private ServiceTracker jsFinderTracker;

    public Activator() {
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        javaFinderTracker
            = new ServiceTracker( context,
                                  IJavaOpentoxManager.class.getName(),
                                  null );

        javaFinderTracker.open();
        jsFinderTracker
            = new ServiceTracker( context,
                                  IJavaScriptOpentoxManager.class.getName(),
                                  null );

        jsFinderTracker.open();
        
        //Read in OT services from EP and preferences
        openToxServices=new ArrayList<OpenToxService>();
        openToxServices.addAll(ServiceReader.readServicesFromExtensionPoints());
        openToxServices.addAll(ServiceReader.readServicesFromPreferences());

        logger.debug("After init, we have " + openToxServices.size() + 
        		" opentox services registered");
        
    }


	
	public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public IOpentoxManager getJavaOpentoxManager() {
        IOpentoxManager manager = null;
        try {
            manager = (IOpentoxManager)
                      javaFinderTracker.waitForService(1000*10);
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(
                          "Could not get the Java OpentoxManager",
                          e );
        }
        if (manager == null) {
            throw new IllegalStateException(
                          "Could not get the Java OpentoxManager");
        }
        return manager;
    }

    public IJavaScriptOpentoxManager getJavaScriptOpentoxManager() {
        IJavaScriptOpentoxManager manager = null;
        try {
            manager = (IJavaScriptOpentoxManager)
                      jsFinderTracker.waitForService(1000*10);
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(
                          "Could not get the JavaScript OpentoxManager",
                          e );
        }
        if (manager == null) {
            throw new IllegalStateException(
                          "Could not get the JavaScript OpentoxManager");
        }
        return manager;
    }


    public static List<OpenToxService> getOpenToxServices() {
		return openToxServices;
	}

	public static void setOpenToxServices(List<OpenToxService> openToxServices2) {
		openToxServices = openToxServices2;
	}

	/**
	 * the current OT service is the one first in the list
	 * @return
	 */
	public static OpenToxService getCurrentDSService() {
		if (openToxServices==null || openToxServices.size()<=0)
			return null;
		else
			return openToxServices.get(0);
	}
    
    
}
