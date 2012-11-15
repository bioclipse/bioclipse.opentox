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

import java.util.List;

import net.bioclipse.opentox.business.IJavaOpentoxManager;
import net.bioclipse.opentox.business.IJavaScriptOpentoxManager;
import net.bioclipse.opentox.business.IOpentoxManager;
import net.bioclipse.usermanager.business.IUserManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.opentox.aa.opensso.OpenSSOToken;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The Activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID="net.bioclipse.opentox";
    
    /** HTTP time out in milliseconds. */
    public static final Integer TIME_OUT = 5000; 

    // The shared instance
    private static Activator plugin;

    //A list of OpenTox services in order
    private static List<OpenToxService> openToxServices;

    //A list of OpenTox services in order
    private static OpenSSOToken token = null;

    // Trackers for getting the managers
    private ServiceTracker javaFinderTracker;
    private ServiceTracker jsFinderTracker;
    
    public Activator() {
    	IUserManager userManager = net.bioclipse.usermanager.Activator
    		.getDefault().getUserManager();
    	OpenToxLogInOutListener listener = new OpenToxLogInOutListener(userManager);
    	userManager.addListener(listener);
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

    public static boolean login(String user, String pass)
    throws Exception {
    	if (Activator.token == null) {
        	Activator.token = new OpenSSOToken(
        		"http://opensso.in-silico.ch/opensso/identity"
        	);
    	}
    	return token.login(user, pass);
    }
    
    public static void logout()
    throws Exception {
    	if (Activator.token == null) // already logged out
    		return;

        Activator.token.logout();
        Activator.token = null;
    }
    
    public static String getToken() {
    	if (Activator.token == null) return null;
    	
    	return Activator.token.getToken();
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
