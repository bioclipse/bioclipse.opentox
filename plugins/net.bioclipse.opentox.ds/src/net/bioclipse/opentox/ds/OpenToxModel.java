package net.bioclipse.opentox.ds;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.opentox.Activator;
import net.bioclipse.opentox.OpenToxService;
import net.bioclipse.opentox.business.IOpentoxManager;

/**
 * DSModel for predicting an OpenTox model
 * 
 * @author ola
 *
 */
public class OpenToxModel extends AbstractDSTest {

    private static final Logger logger = Logger.getLogger(OpenToxModel.class);

	IOpentoxManager opentox;
	private String model;
	
	public OpenToxModel(String model) {
		this.model=model;
	}

	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
		opentox=Activator.getDefault().getJavaOpentoxManager();
	}

	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {

		//Use the currently selected OpenTox service
		OpenToxService otservice = Activator.getCurrentDSService();
		if (otservice==null){
			logger.error("No OpenTox service found");
			returnError("No OpenTox service found", "No OpenTox service found");
		}

		String service=otservice.getService();
		if (service==null){
			logger.error("Current OpenTox service has no service URL");
			returnError("Current OpenTox service has no service URL", 
					"Current OpenTox service has no service URL");
		}

		//Predict!
        ArrayList<net.bioclipse.ds.model.result.SimpleResult> results 
        = new ArrayList<net.bioclipse.ds.model.result.SimpleResult>();

		List<String> res = opentox.predictWithModel(service, model, cdkmol);
		for (String r : res){
	        results.add(new net.bioclipse.ds.model.result.SimpleResult(r, 
	        		ITestResult.INFORMATIVE));
		}

		return results;
	}

}
