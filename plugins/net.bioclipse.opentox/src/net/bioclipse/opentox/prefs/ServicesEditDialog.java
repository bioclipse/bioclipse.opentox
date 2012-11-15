/* *****************************************************************************
 *Copyright (c) 2010 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth - core API and implementation
 *******************************************************************************/
package net.bioclipse.opentox.prefs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.layout.GridLayout;

/**
 * 
 * @author ola
 *
 */
public class ServicesEditDialog extends TitleAreaDialog{

	private String[] serviceInfo = new String[3];

	private Text txtName;
	private Text txtService;
	private Text txtServiceSparql;
	private String name;
	private String service;
	private String serviceSparql;


	/**
	 * @wbp.parser.constructor
	 */
	public ServicesEditDialog(Shell parentShell) {
		this(parentShell,"","","");
	}

	public ServicesEditDialog(Shell shell, String name, 
							  String service, String serviceSparql) {
		super(shell);
		this.name=name;
		this.service=service;
		this.serviceSparql=serviceSparql;
	}

	protected Control createDialogArea(Composite parent) {

		setTitle("OpenTox Service Endpoint");
		setMessage("Enter details for the OpenTox Service Endpoint. \n");


		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		final Label lblName = new Label(container, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name:");

		txtName = new Text(container, SWT.BORDER);
		GridData gridData_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData_1.widthHint = 100;
		txtName.setLayoutData(gridData_1);
		txtName.setText(name);

		final Label lblURL = new Label(container, SWT.NONE);
		lblURL.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblURL.setText("Service:");

		txtService = new Text(container, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 250;
		txtService.setLayoutData(gridData);
		txtService.setText(service);

		final Label lblServiceSPARQL = new Label(container, SWT.NONE);
		lblServiceSPARQL.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblServiceSPARQL.setText("ServiceSPARQL:");

		txtServiceSparql = new Text(container, SWT.BORDER);
		GridData gridData2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData2.widthHint = 250;
		txtServiceSparql.setLayoutData(gridData2);
		txtServiceSparql.setText(serviceSparql);
		return area;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {

			if (txtName.getText().length()<=0){
				showMessage("Name cannot be empty");
				return;
			}
			if (txtService.getText().length()<=0){
				showMessage("Service cannot be empty");
				return;
			}
			if (txtServiceSparql.getText().length()<=0){
				showMessage("ServiceSPARQL cannot be empty");
				return;
			}

			serviceInfo[0]=txtName.getText();
			serviceInfo[1]=txtService.getText();
			serviceInfo[2]=txtServiceSparql.getText();
			okPressed();
			return;
		}
		super.buttonPressed(buttonId);
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"OpenTox Service Endpoints",
				message);
	}



	public void setTxtService(String service) {
		this.txtService.setText(service);
	}

	public void setTxtServiceSparql(String serviceSparql) {
		this.txtServiceSparql.setText(serviceSparql);
	}

	public void setTxtName(String name) {
		this.txtName.setText(name);
	}
	
	public String[] getServiceInfo() {
		return serviceInfo;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}