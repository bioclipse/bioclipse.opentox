/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.opentox.ui.wizards;

import java.net.URL;
import java.util.List;

import net.bioclipse.opentox.OpenToxService;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

public class CreateDatasetPage extends WizardPage {

	private Combo cboLicense;
	private IFile file;
	private Text txtTitle;
	private Text customLicense;

	protected CreateDatasetPage() {
		super("Create Dataset");
		setTitle("Create OpenTox Dataset");
		setDescription("Select metadata and upload dataset to an OpenTox server.");
	}

	public CreateDatasetPage(IFile file) {
		this();
		this.file=file;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;

		//Dataset info
		Label fromLabel = new Label(container, SWT.NULL);
		fromLabel.setText("File:");   
		fromLabel.setLayoutData(new GridData(GridData.BEGINNING));

		Label lblDSinfo = new Label(container, SWT.NULL);
		lblDSinfo.setText(file.getName());   
		lblDSinfo.setLayoutData(new GridData(GridData.BEGINNING));
		
		//License
		Label lblServer= new Label(container, SWT.NULL);
		lblServer.setText("Server:");   
		lblServer.setLayoutData(new GridData(GridData.BEGINNING));

        Combo cboServer = new Combo(container, SWT.NONE);
        GridData gds=new GridData();
//        gds.widthHint=170;
        cboServer.setLayoutData(gds); 

		//Get the registered services
		List<OpenToxService> OTservices 
						 = net.bioclipse.opentox.Activator.getOpenToxServices();

		for (OpenToxService service : OTservices){
	        cboServer.add( service.getName() );
		}

        cboServer.addSelectionListener( new SelectionListener(){

            public void widgetDefaultSelected( SelectionEvent e ) {
            }

            public void widgetSelected( SelectionEvent e ) {
                Combo cbo=(Combo) e.getSource();

                int ix = cbo.getSelectionIndex();
                String service = net.bioclipse.opentox.Activator.getOpenToxServices().get(ix).getService();
            	((CreateDatasetWizard)getWizard()).setService(service);

            }
        });
        cboServer.select(0);
        String service = net.bioclipse.opentox.Activator.getOpenToxServices().get(0).getService();
    	((CreateDatasetWizard)getWizard()).setService(service);

		//Title
		Label lblTitle = new Label(container, SWT.NULL);
		lblTitle.setText("Title:");   
		lblTitle.setLayoutData(new GridData(GridData.BEGINNING));
		
		txtTitle = new Text(container, SWT.BORDER | SWT.SINGLE);
		txtTitle.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				((CreateDatasetWizard)getWizard()).setTitle(txtTitle.getText());
				dialogChanged();
			}
		});
		txtTitle.setText("MyTitle");
		txtTitle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//License
		Label lblLicense = new Label(container, SWT.NULL);
		lblLicense.setText("License:");   
		lblLicense.setLayoutData(new GridData(GridData.BEGINNING));

        cboLicense = new Combo(container, SWT.NONE);
        GridData gdAutoBuild=new GridData();
//        gdAutoBuild.widthHint=170;
        cboLicense.setLayoutData(gdAutoBuild); 

        cboLicense.add( "Creative Commons Zero" ); // http://creativecommons.org/publicdomain/zero/1.0/
        cboLicense.add( "ODC Public Domain Dedication and Licence" ); // http://www.opendatacommons.org/licenses/pddl/1-0/
        cboLicense.add( "Open Database License" ); // http://opendatacommons.org/licenses/odbl/1.0/
        cboLicense.add( "Open Data Commons Attribution License" ); // http://opendatacommons.org/licenses/by/1.0/
        cboLicense.add( "None" );
        cboLicense.add( "Custom" );

        cboLicense.addSelectionListener( new SelectionListener(){

            public void widgetDefaultSelected( SelectionEvent e ) {
            }

            public void widgetSelected( SelectionEvent e ) {
                Combo cbo=(Combo) e.getSource();
                if (cbo.getSelectionIndex()==0){
                	((CreateDatasetWizard)getWizard()).setLicense("http://creativecommons.org/publicdomain/zero/1.0/");
                	customLicense.setEnabled(false);
                }
                else if (cbo.getSelectionIndex()==1){
                	((CreateDatasetWizard)getWizard()).setLicense("http://www.opendatacommons.org/licenses/pddl/1.0/");
                	customLicense.setEnabled(false);
                }
                else if (cbo.getSelectionIndex()==2){
                	((CreateDatasetWizard)getWizard()).setLicense("http://opendatacommons.org/licenses/odbl/1.0/");
                	customLicense.setEnabled(false);
                }
                else if (cbo.getSelectionIndex()==3){
                	((CreateDatasetWizard)getWizard()).setLicense("http://opendatacommons.org/licenses/by/1.0/");
                	customLicense.setEnabled(false);
                }
                else if (cbo.getSelectionIndex()==4){
                	((CreateDatasetWizard)getWizard()).setLicense(null);
                	customLicense.setEnabled(false);
                }
                else if (cbo.getSelectionIndex()==5){
                	customLicense.setEnabled(true);
                }
                else{
                	System.out.println("license combo OUT OF BOUNDS");
                }
            }
        });
        cboLicense.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        cboLicense.select(0);
    	((CreateDatasetWizard)getWizard()).setLicense("http://creativecommons.org/publicdomain/zero/1.0/");

    	// custom license line with an empty label
    	lblTitle = new Label(container, SWT.NULL);
		lblTitle.setText("Custom:");   
		lblTitle.setLayoutData(new GridData(GridData.BEGINNING));
    	customLicense = new Text(container, SWT.BORDER | SWT.SINGLE);
    	customLicense.setEnabled(false); // by default license is CC0, not 'Custom'
    	customLicense.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String customURL = customLicense.getText();
				if (customURL.length() > 0) {
					try {
						URL url = new URL(customURL);
						if (url.getHost().length() > 0) {
							setErrorMessage(null);
							setPageComplete(true);
							((CreateDatasetWizard)getWizard()).setLicense(
								customLicense.getText()
							);
							return;
						}
					} catch (Exception e1) {}
				}
				setErrorMessage("Custom license must be a valid URL.");
				setPageComplete(false);
				((CreateDatasetWizard)getWizard()).setLicense(null);
			}
		});
    	customLicense.setText("");
    	customLicense.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(container);
		dialogChanged();
	}


	private void dialogChanged() {
		if (txtTitle.getText().isEmpty()){
			updateStatus("Title cannot be empty");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

}
