package net.bioclipse.opentox.ds;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.DSBusinessModel;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.Endpoint;
import net.bioclipse.ds.model.IConsensusCalculator;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestDiscovery;

/**
 * Discover OpenTox Models (tests) dynamically.
 * 
 * @author ola
 *
 */
public class OpenToxTestDiscovery implements ITestDiscovery {

	public OpenToxTestDiscovery() {
	}

	/**
	 * Discover and provide a list of OpenTox tests
	 * @throws BioclipseException 
	 */
	@Override
	public List<IDSTest> discoverTests() throws BioclipseException {

		List<IDSTest> discoveredTests=new ArrayList<IDSTest>();
		
		//We need DSManager to look up existing endpoints
		IDSManager ds = Activator.getDefault().getJavaManager();

		//First, hardcoded one to demonstrate functionality
		IDSTest test= new OpenToxModel();

		test.setName("OT test");
		test.setId("ot.test");
		test.setIcon("icons/drug_cap.png");
		test.setDescription("Test OT test description");

		test.setOverride(false);
		test.setInformative(false);
		test.setVisible( true );
		test.setPluginID( net.bioclipse.opentox.ds.Activator.PLUGIN_ID);

		//Endpoints are required to exist prior to test discovery
		//now we add them using an EP extension
		//Could add discovery later too..
		String endpoint="net.bioclipse.ds.opentox";

		//Look up endpoint by id and add to test
		for (Endpoint ep : ds.getFullEndpoints()){
			if (ep.getId().equals( endpoint )){
				test.setEndpoint( ep );
				ep.addTest(test);
			}
		}

		//Consensus calculator... use default for now
		IConsensusCalculator conscalc = DSBusinessModel
		.createNewConsCalc(null);
		test.setConsensusCalculator( conscalc );

		//TODO: unused for now..                
		//              test.setPropertycalculator( ppropcalc);
		//              test.setHelppage( phelppage);
		//              test.addParameter(name,path);


		//Hardcoded one test for now...
		discoveredTests.add(test);


		return discoveredTests;
	}

}
