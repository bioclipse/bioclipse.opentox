package net.bioclipse.opentox.business;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.opentox.Activator;
import net.bioclipse.opentox.OpenToxConstants;
import net.bioclipse.opentox.OpenToxService;
import net.bioclipse.opentox.ServiceReader;
import net.bioclipse.opentox.prefs.ServicesPreferencePage;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;


public class OpentoxPreferenceInitializer extends AbstractPreferenceInitializer {
    
    private static final Logger logger = Logger
            .getLogger(OpentoxPreferenceInitializer.class);
    
    @Override
    public void initializeDefaultPreferences() {
        
        Preferences preferences = 
                DefaultScope.INSTANCE
                .getNode( OpenToxConstants.PLUGIN_ID );    	

        logger.debug("Initializing OpenTox Network preferences");

        preferences.put(OpenToxConstants.SHORTEST_WAIT_TIME_IN_SECS, "2");
        preferences.put(OpenToxConstants.LONGEST_WAIT_TIME_IN_SECS, "20");
        preferences.put(OpenToxConstants.HTTP_TIMEOUT, "50");
    	
        logger.debug("Initializing OpenTox services");
        
        //A list of OpenTox services in order
        List<OpenToxService> openToxServices = new ArrayList<OpenToxService>();
        List<String[]> toPrefs = new ArrayList<String[]>();
        
          
        // Get the (new) services from the extension point. 
        List<OpenToxService> epservices = ServiceReader
                .readServicesFromExtensionPoints();
        for (OpenToxService eps : epservices){
            if (!openToxServices.contains(eps)){
                openToxServices.add(eps);
                logger.debug("Added new service from EP: " + eps);
            }
        }
        
        // Read the data from the services and serialize it
        for (OpenToxService eps : epservices){
            String[] entry = new String[3];
            entry[0]=eps.getName();
            entry[1]=eps.getService();
            entry[2]=eps.getServiceSPARQL();
            toPrefs.add( entry );
        }
        String toPrefsString = ServicesPreferencePage
                .convertToPreferenceString(toPrefs);
        
        //Save the serialized services to preferences
        preferences.put( OpenToxConstants.SERVICES, toPrefsString );
        try {
            preferences.flush();
        } catch ( BackingStoreException e ) {
            logger.error( e.getMessage() );
            e.printStackTrace();
        }
        
        logger.debug("Saved the serialized services prefs string: " + 
                toPrefsString);
        
        Preferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        node.put( OpenToxConstants.SERVICES, toPrefsString );
        try {
            node.flush();
        } catch ( BackingStoreException e ) {
            logger.error( e.getMessage() );
            e.printStackTrace();
        }
        logger.debug( "Set the loaded services as default" );
        
        logger.debug("OpenTox initialization ended");
    }

}
