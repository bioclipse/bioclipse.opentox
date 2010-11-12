package net.bioclipse.opentox;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.opentox.prefs.ServicesPreferencePage;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 
 * @author ola
 *
 */
public class ServiceReader {
	
	private static final Logger logger = Logger.getLogger(ServiceReader.class);


	public static List<OpenToxService> readServicesFromExtensionPoints() {
		
		List<OpenToxService> services=new ArrayList<OpenToxService>();
		
        IExtensionRegistry registry = Platform.getExtensionRegistry();

        if ( registry == null ) 
            throw new UnsupportedOperationException("Extension registry is null. " +
            "Cannot read tests from EP.");
        // it likely means that the Eclipse workbench has not
        // started, for example when running tests

        IExtensionPoint serviceObjectExtensionPoint = registry
        .getExtensionPoint("net.bioclipse.opentox");

        IExtension[] serviceObjectExtensions
        = serviceObjectExtensionPoint.getExtensions();

        for(IExtension extension : serviceObjectExtensions) {
            for( IConfigurationElement element
                    : extension.getConfigurationElements() ) {

                //Read all endpoints
                if (element.getName().equals("service")){

                    String pid=element.getAttribute("id");
                    String pname=element.getAttribute("name");
                    String purl=element.getAttribute("serviceURL");
                    if (purl==null) purl="";
                    String pspql=element.getAttribute("serviceSPARQL");
                    if (pspql==null) pspql="";
                    
//                    String picon=element.getAttribute("icon");
//                    String pluginID=element.getNamespaceIdentifier();
//TODO: implement icon                    
                    
                    OpenToxService service = new OpenToxService(pname,purl, pspql);
                    services.add(service);
                    
                    logger.debug("Added OT service from EP: " + service);
                }
            }
        }
        
        return services;
		
	}

	
	/**
	 * 
	 * @return
	 */
	public static List<OpenToxService> readServicesFromPreferences() {

		logger.debug("Reading services from preferences...");

		List<OpenToxService> services=new ArrayList<OpenToxService>();

        IPreferenceStore prefsStore=Activator.getDefault().getPreferenceStore();
        String entireString = prefsStore.getString(OpenToxConstants.SERVICES);
        List<String[]> parts = ServicesPreferencePage.convertPreferenceStringToArraylist(entireString);
        for (String[] entry : parts){
        	if (entry.length==3){
        		OpenToxService service = new OpenToxService(entry[0],entry[1],entry[2]);
        		services.add(service);
        	}else{
        		logger.debug("Opentox service preference entry incorrect size (and skipped): " + entry);
        	}
        }
		
		return services; 
	}

}
