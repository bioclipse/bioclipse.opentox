package net.bioclipse.opentox.qsar;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.opentox.Activator;
import net.bioclipse.opentox.business.IOpentoxManager;
import net.bioclipse.qsar.descriptor.IDescriptorCalculator;
import net.bioclipse.qsar.descriptor.model.DescriptorImpl;
import net.bioclipse.qsar.descriptor.model.DescriptorProvider;
import net.bioclipse.qsar.discovery.IDiscoveryService;

/**
 * A class to discover providers and their descriptor implementations
 * for OpenTox
 * 
 * @author ola
 *
 */
public class OpenToxProviderDiscovery implements IDiscoveryService{

    private static final Logger logger = Logger.getLogger(
    		OpenToxProviderDiscovery.class);

    //Keep a list of the endpoints here
	private static List<OpenToxProvider> providers;

	@Override
	public String getName() {
		return "OpenTox";
	}

	
	/**
	 * 
	 */
	@Override
	public List<DescriptorProvider> discoverProvidersAndImpls() {
		
        List<DescriptorProvider> returnList=new ArrayList<DescriptorProvider>();

		
		
		IOpentoxManager opentox=Activator.getDefault().getJavaOpentoxManager();

		//Discover OpenTox endpoints
		logger.debug("Discovering OpenTox providers...");
		providers = discoverProviders();
		for (OpenToxProvider provider : providers){
			logger.debug("  - found OpenTox provider: " + provider.name);
		}

		//Discover descriptors per provider
		for (OpenToxProvider provider : providers){
			
			//Set up Provider
			DescriptorProvider dp = new DescriptorProvider(provider.getId(),
					provider.getName());
			
			dp.setShortName(provider.getName());

			//TODO: use some lookup to get more info for provider, 
			// such as shortname, icon etc
			
			//Add an OTDescriptorCalculator with this service
            IDescriptorCalculator calculator = new OpenToxDescriptorCalculator(
            		provider.getId(), provider.getService());
            dp.setCalculator(calculator);

            //Discover descriptor impls
			List<DescriptorImpl> impls = new ArrayList<DescriptorImpl>();
			
			logger.debug("Discovering OpenTox dscriptors from provider: " 
					+ provider.getService());
			
			//Read descriptors from SPARQL using RDF in OpenTox manager
			IStringMatrix stringMat = opentox.listDescriptors(
					provider.getServiceSPARQL());

			for (int i=0; i< stringMat.getRowCount(); i++){

				String implID=stringMat.get(i, 1);
				String bodo=stringMat.get(i, 2);
				String implName="";
				if (bodo.length()>4)
					implName=bodo.substring( bodo.indexOf( "#" )+1 );

				if (implID==null || implID.length()<1){
					logger.error("Discovered descriptor with no ID: " + implID + ", " + bodo + ", " + ", " + implName);
				}else if (bodo==null || bodo.length()<1) {
					logger.error("Discovered descriptor with no BODO: " + implID + ", " + bodo + ", " + ", " + implName);
				}else{

					DescriptorImpl impl = new DescriptorImpl(implID, implName);
					impl.setDefinition(bodo);
					impl.setProvider(dp);

					//TODO: discover these options too...				
					//				impl.setDescription("");
					//				impl.setIcon("");
					//				impl.setNamespace("");
					//				impl.setRequires3D("");

					impls.add(impl);
					logger.debug("  - Added OT desc impl: " + impl.getId() + " == " + impl.getDefinition());
				}


			}
			
			dp.setDescriptorImpls(impls);
            
			returnList.add(dp);
			
		}
		
		return returnList;
	}


	private ArrayList<OpenToxProvider> discoverProviders() {

		ArrayList<OpenToxProvider> endpoints = new ArrayList<OpenToxProvider>();
			
			//Add a service
			OpenToxProvider s1 = new OpenToxProvider(
					"opentox.provider",
					"Opentox",
					"http://apps.ideaconsult.net:8080/ambit2/",
					"http://apps.ideaconsult.net:8080/ontology/");
			
			endpoints.add(s1);

			//TODO: add more here or discover it


		return endpoints;
	}
	
}
