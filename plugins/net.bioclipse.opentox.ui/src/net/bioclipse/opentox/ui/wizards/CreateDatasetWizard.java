/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.opentox.ui.wizards;


import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.bioclipse.browser.editors.RichBrowserEditor;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.opentox.Activator;
import net.bioclipse.opentox.business.IOpentoxManager;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.part.NullEditorInput;

public class CreateDatasetWizard extends Wizard implements INewWizard {

	private CreateDatasetPage createDatasetPage;
	private static final Logger logger = Logger.getLogger(CreateDatasetWizard.class);

	private String license;
	private String title;
	private String service;
	private IFile file;

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public CreateDatasetWizard() {
		super();
		setWindowTitle("Create OpenTox Dataset");
		setNeedsProgressMonitor(true);
	}

	public CreateDatasetWizard(IFile file) {
		this();
		this.file=file;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public void addPages()  
	{  
		createDatasetPage = new CreateDatasetPage(file);
		this.addPage(createDatasetPage);

	}

	@Override
	public boolean performFinish() {

		//		System.out.println("Service: " + service);
		//		System.out.println("License: " + license);
		//		System.out.println("File: " + file);
		//		System.out.println("Title: " + title);

		//		if (true) return false;

		//Run with wizard progress monitor
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
				try {
					IOpentoxManager opentox = Activator.getDefault().getJavaOpentoxManager();
					ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

					monitor.beginTask("Creating dataset: ", 10);
					monitor.subTask("Parsing data file");
					monitor.worked(1);
					List<ICDKMolecule> mols=null;
						mols = cdk.loadMolecules(file, new SubProgressMonitor(monitor, 7));

					monitor.subTask("Uploading data");
					monitor.worked(1);
					final String datasetURI = opentox.createDataset(service, mols);
					System.out.println("Produced DS URI: " + datasetURI);

					monitor.subTask("Setting title");
					if (title != null && title.length() > 0) {
						System.out.println("Title: " + title);
						monitor.worked(1);
						opentox.setDatasetTitle(datasetURI, title);
					}

					monitor.subTask("Setting license");
					monitor.worked(1);
					if (license != null) {
						opentox.setDatasetLicense(datasetURI, license);
					}

					monitor.subTask("Opening browser");
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							IEditorPart editor;
							try {
								editor = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage()
								.openEditor( new NullEditorInput(), RichBrowserEditor.EDITOR_ID );
								if (editor!=null){
									((RichBrowserEditor)editor).setURL( datasetURI);
								}
							} catch (PartInitException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					});

					monitor.done();
				} catch (Exception exception) {
					monitor.done();
					return;
				}
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("finish!");

		return true;
	}
}
