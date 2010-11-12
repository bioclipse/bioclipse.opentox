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
package net.bioclipse.opentox.ds.prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.bioclipse.opentox.OpenToxConstants;
import net.bioclipse.opentox.ds.Activator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A preference page for OpenTox models
 * 
 * @author ola
 *
 */
public class OpenToxModelsPrefsPage extends FieldEditorPreferencePage 
implements IWorkbenchPreferencePage {

	public static final String OT_MODELS_PREFS = "OT_MODELS_PREFS";
	protected static final String PREFS_SEPERATOR = "__SEP__";

	public OpenToxModelsPrefsPage() {
		super(FieldEditorPreferencePage.GRID);
		
		// Set the preference store for the preference page.
		IPreferenceStore store =
			Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}
	
	@Override
	protected void createFieldEditors() {

		
		ModelsListEditor listeditor=new ModelsListEditor("OpenTox Models",
				OT_MODELS_PREFS,
				getFieldEditorParent()) {
			
			@Override
			protected String[] parseString(String stringList) {

		        StringTokenizer st = 
		            new StringTokenizer(stringList, PREFS_SEPERATOR);
		        List<String> v = new ArrayList<String>();
		        while (st.hasMoreElements()) {
		            v.add((String) st.nextElement());
		        }

		        return (String[])v.toArray(new String[v.size()]);

			}
			
			@Override
			protected String createList(String[] items) {
				StringBuffer path = new StringBuffer("");

				for (int i = 0; i < items.length; i++) {
					path.append(items[i]);
					path.append(PREFS_SEPERATOR);
				}
				return path.toString();
			}
		};

		
		addField(listeditor);
		GridData gd=new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint=200;
		listeditor.getListControl(getFieldEditorParent()).setLayoutData(gd);

		
	}

	public void init(IWorkbench workbench) {
	}
	

    public static String createPreferenceStringFromItems(String[] items) {

        StringBuffer path = new StringBuffer("");//$NON-NLS-1$

        for (int i = 0; i < items.length; i++) {
            path.append(items[i]);
            path.append(OpenToxConstants.PREFS_SEPERATOR);
        }
        return path.toString();
    }

    public static String[] parsePreferenceString(String stringList) {
        StringTokenizer st = 
            new StringTokenizer(stringList, OpenToxConstants.PREFS_SEPERATOR);
        List<String> v = new ArrayList<String>();
        while (st.hasMoreElements()) {
            v.add((String) st.nextElement());
        }
        return (String[])v.toArray(new String[v.size()]);
    }


}
