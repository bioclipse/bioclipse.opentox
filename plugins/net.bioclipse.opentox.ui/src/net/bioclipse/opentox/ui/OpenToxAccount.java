/* *****************************************************************************
 * Copyright (c) 2007-2009 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 ******************************************************************************/

package net.bioclipse.opentox.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.bioclipse.usermanager.IAccounts;

/**
 * This class creates an open-tox account.
 * 
 * @author Klas Jšnsson
 *
 */
public class OpenToxAccount implements IAccounts {

	private Text nameTxt;
	private Text pswTxt;
	
	public OpenToxAccount() {	}
	
	/**
	 * Creates the composite where the user fill in the needed information.
	 */
	@Override
	public Composite createComposite(Composite container)  {
		int hight = 160;
		int width = 430;
		
		Composite as = new Composite(container, SWT.NONE);
		GridData gd_as = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_as.widthHint = width;
		gd_as.heightHint = hight;
		as.setLayoutData(gd_as);
				
		Label nameLable = new Label(as, SWT.NONE);
		nameLable.setText("User name:");
		nameLable.setBounds(10, 20, 65, 14);
		nameTxt = new Text(as, SWT.BORDER);
		nameTxt.setBounds(80, 17, 200, 20);
		
		Label pswLabel = new Label(as, SWT.NONE);
		pswLabel.setBounds(10, 60, 60, 14);
		pswLabel.setText("Password:");
		pswTxt = new Text(as, SWT.BORDER | SWT.PASSWORD);
		pswTxt.setBounds(80, 57, 200, 20);;
		ImageDescriptor imDesc = ImageDescriptor.createFromFile(this.getClass(), "opentox.jpg");
		Image temp = imDesc.createImage();
		Image image = new Image(container.getDisplay(), temp , SWT.IMAGE_COPY);
		int x = (width-10) - image.getBounds().width;		
		Label im_label = new Label(as, SWT.NONE);
		im_label.setBounds(x, 0, image.getBounds().width, 
				image.getBounds().height);
		im_label.setImage(image);
		nameTxt.setFocus();
		
		return as;	 
	}
	
	@Override
	public String getName() {
		return "OpenTox Account";
	}

	/**
	 * This method is responsible for create the open-tox account. 
	 */
	@Override
	public void createAccount() {
		System.out.println("Creating a open tox account...");
		System.out.println("Name: " + nameTxt.getText());
		System.out.println("Password: " + pswTxt.getText());
	}
	
	/**
	 * This method is used to set the first text-box in focus (i.e. the one 
	 * where the user is supposed to fill in the user-name to the open-tox 
	 * account).
	 */
	@Override
	public void setFocus() {
		nameTxt.setFocus();
	}

}