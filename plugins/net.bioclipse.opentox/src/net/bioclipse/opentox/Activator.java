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
import net.bioclipse.opentox.prefs.ServicesPreferencePage;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The Activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID="net.bioclipse.opentox";

    private static final Logger logger = Logger.getLogger(Activator.class);
    
    /** HTTP time out in milliseconds. */
    public static final Integer TIME_OUT = 5000; 

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
        
        /*
         * INITIALIZE OPENTOX SERVICES
         */
        
        //Read in OT services from EP and preferences
        openToxServices=new ArrayList<OpenToxService>();
        
        logger.debug("Initializing OpenTox services");
        
        //First we read from preferences
        List<OpenToxService> prefss = ServiceReader.readServicesFromPreferences();
        openToxServices.addAll(prefss);
        logger.debug("Read " + prefss.size() + " services from prefs");

        //Second, add services from EP if not in prefs (new extensions)
        List<OpenToxService> epservices = ServiceReader.readServicesFromExtensionPoints();
        for (OpenToxService eps : epservices){
        	if (!openToxServices.contains(eps)){
        		//TODO: Add it
                openToxServices.add(eps);
                logger.debug("Added new service from EP: " + eps);
        	}
        }
        
        //Save the, possibly changed, list of services to prefs
        //This way we have the list openToxServices synced with the prefs
        List<String[]> toPrefs=new ArrayList<String[]>();
        for (OpenToxService eps : epservices){
        	String[] entry = new String[3];
        	entry[0]=eps.getName();
        	entry[1]=eps.getService();
        	entry[2]=eps.getServiceSPARQL();
        	
        	toPrefs.add(entry);
        }
        String toPrefsString = ServicesPreferencePage.convertToPreferenceString(toPrefs);

        //Save the serialized services to preferences
        IPreferenceStore prefsStore=Activator.getDefault().getPreferenceStore();
        prefsStore.setValue(OpenToxConstants.SERVICES, toPrefsString);
        
        logger.debug("Saved the serialized services prefs string: " + toPrefsString);

        logger.debug("OpenTox initialization ended");
        
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
