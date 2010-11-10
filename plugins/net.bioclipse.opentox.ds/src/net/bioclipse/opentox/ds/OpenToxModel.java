package net.bioclipse.opentox.ds;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;

public class OpenToxModel extends AbstractDSTest {

	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {
		// TODO Auto-generated method stub

	}

	@Override
	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
