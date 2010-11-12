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
package net.bioclipse.opentox.prefs;


import net.bioclipse.opentox.Activator;
import net.bioclipse.opentox.OpenToxConstants;
import net.bioclipse.ui.prefs.IPreferenceConstants;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 
 * @author ola
 *
 */
@Deprecated
public class OpenToxPreferenceInitializer extends AbstractPreferenceInitializer {

	private static final Logger logger = Logger.getLogger(OpenToxPreferenceInitializer.class);

	public OpenToxPreferenceInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {

		//Discover available providers
		//TODO: now just hardcoded
		//Form: Name=Service=ServiceSPARQL__sep__

		String defstr="Ambit2" 
			+ IPreferenceConstants.PREFERENCES_DELIMITER 
			+ "http://apps.ideaconsult.net:8080/ambit2/"
			+ IPreferenceConstants.PREFERENCES_DELIMITER 
			+ "http://apps.ideaconsult.net:8080/ontology/";

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(OpenToxConstants.SERVICES, defstr);

		logger.debug( "Initialized default OpenTox services to: " + defstr );

	}



}
