package net.bioclipse.opentox.ui.handlers;


import net.bioclipse.opentox.ui.wizards.CreateDatasetWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class CreateDatasetHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection sel = HandlerUtil.getCurrentSelection(event);
		
		//The following should not be necessary, but you can never be too sure...
		if (sel.isEmpty()) return null;
		if (!( sel instanceof IStructuredSelection )) return null;
		Object obj = ((IStructuredSelection) sel).getFirstElement();
		if (!(obj instanceof IFile)) return null;
		
		IFile file = (IFile) obj;
		try {
			CreateDatasetWizard wiz = new CreateDatasetWizard(file);
			WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wiz);
			dialog.open();
		} catch ( Exception e ) {
			e.printStackTrace();
			throw new RuntimeException( e.getMessage() );
		}

		// TODO Auto-generated method stub
		return null;
	}
}
