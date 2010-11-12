package net.bioclipse.opentox;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

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
                    String pspql=element.getAttribute("serviceSPARQL");
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

	
	
	public static List<OpenToxService> readServicesFromPreferences() {
		List<OpenToxService> services=new ArrayList<OpenToxService>();

		//TODO: implement
		logger.debug("Reading services from preferences is not " +
		"yet implemented.");
		
		return services; 
	}

}
