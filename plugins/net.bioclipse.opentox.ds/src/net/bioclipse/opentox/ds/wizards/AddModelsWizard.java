/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.opentox.ds.wizards;

import java.util.List;
import java.util.Map;

import net.bioclipse.opentox.ds.OpenToxModel;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * A wizard to select molecules and optionally a property that is used
 * as reponse value.
 * 
 * @author ola
 *
 */
public class AddModelsWizard extends Wizard{

    private SelectModelsPage selectModelsPage;

    private List<OpenToxModel> models;

    public List<OpenToxModel> getModels() {
		return models;
	}

	public void setModels(List<OpenToxModel> models) {
		this.models = models;
	}

	@Override
    public void addPages() {

        //Page 1: Select molecular files
        selectModelsPage=new SelectModelsPage("Select models to add " +
        		"to Decision Support");
        addPage(selectModelsPage);


    }

    @Override
    public boolean performFinish() {
    	
    	//Do something here?

        return true;
    }

}
