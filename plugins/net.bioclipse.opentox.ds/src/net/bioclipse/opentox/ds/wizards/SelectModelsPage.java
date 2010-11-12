/*******************************************************************************
 * Copyright (c) 2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.opentox.ds.wizards;


import net.bioclipse.opentox.ds.Activator;
import net.bioclipse.opentox.ds.prefs.OpenToxModelsPrefsPage;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * A Wizard page to select one or many opentox services
 * 
 * @author ola
 *
 */
public class SelectModelsPage extends WizardPage {

	private CheckboxTreeViewer viewer;
	private AddModelsWizard wizard;

	//    private ArrayList<IFile> selectedFiles;


	protected SelectModelsPage(String pageName) {
		super( pageName );
	}

	public void createControl( Composite parent ) {

		wizard=(AddModelsWizard) getWizard();

		setTitle( "Select Models" );
		setDescription( "Select OpenTox models to include in the " +
		"Decision Support. " );
		//        setImageDescriptor( Activator.getImageDescriptor( "wizban/wiz_imp_mol.gif" ) );

		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout=new GridLayout();
		comp.setLayout( layout );

		viewer = new CheckboxTreeViewer(
				parent, SWT.CHECK | SWT.MULTI | SWT.H_SCROLL 
				| SWT.V_SCROLL | SWT.BORDER);

		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new ServicesContentProvider());
		viewer.setLabelProvider( new ServicesLabelProvider());

		//Get services from prefs and set as input
		IPreferenceStore store =
			Activator.getDefault().getPreferenceStore();

		String servicePrefs = store.getString(OpenToxModelsPrefsPage.OT_MODELS_PREFS);
		String[] serviceStrings = OpenToxModelsPrefsPage.parsePreferenceString(servicePrefs);
		viewer.setInput(serviceStrings);
		viewer.expandToLevel(2);
		
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace=true;
		data.grabExcessVerticalSpace=true;
		data.heightHint = 400;
		data.widthHint = 300;
		viewer.getControl().setLayoutData(data);
		viewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				System.out.println("Changed in viewer: " + event.getElement() 
						+ " - " + event.getChecked());
				//TODO: Act on this!
			}
		});
		
		setControl( comp );
	}

}
