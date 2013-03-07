/* Copyright (c) 2013 The Bioclipse Team and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.bioclipse.opentox.prefs;

import net.bioclipse.opentox.Activator;
import net.bioclipse.opentox.OpenToxConstants;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author egonw
 */
public class NetworkPreferencePage extends PreferencePage
implements IWorkbenchPreferencePage {

    private static final Logger logger = 
            Logger.getLogger(NetworkPreferencePage.class.toString());

    private IntegerFieldEditor shortestTime;
    private IntegerFieldEditor longestTime;
    private IntegerFieldEditor timeout;

    public NetworkPreferencePage() {
        super();
    }

	@Override
	public void init(IWorkbench workbench) {
	}

	protected IPreferenceStore doGetPreferenceStore() {
		System.out.println("Really getting prefs store...");
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		System.out.println("Creating controls for the OT Network prefs page...");

        timeout = new IntegerFieldEditor("HTTPTimeOut", "HTTP Time Out (in s)", parent);
        timeout.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        timeout.setValidRange(2, 100);
        timeout.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (timeout.isValid()) {
					setErrorMessage(null);
					setValid(true);
					return;
				}
				setErrorMessage(timeout.getErrorMessage());
				setValid(false);
			}
		});

        shortestTime = new IntegerFieldEditor("ShortestWaitTime", "Shortest Waiting Time (in s)", parent);
        shortestTime.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        shortestTime.setValidRange(2, 100);
        shortestTime.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (shortestTime.isValid()) {
					setErrorMessage(null);
					setValid(true);
					return;
				}
				setErrorMessage(shortestTime.getErrorMessage());
				setValid(false);
			}
		});

        longestTime = new IntegerFieldEditor("LongestWaitTime", "Longest Waiting Time (in s)", parent);
        longestTime.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        longestTime.setValidRange(2, 100);
        longestTime.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (longestTime.isValid()) {
					setErrorMessage(null);
					setValid(true);
					return;
				}
				setErrorMessage(longestTime.getErrorMessage());
				setValid(false);
			}
		});
        
        initializeValues();

		return parent;
	}

	@Override
	public boolean performOk() {
		System.out.println("Performing Ok...");
		storeValues();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		System.out.println("Setting defaults...");
		super.performDefaults();
		initializeDefaults();
	}

	private void initializeValues() {
		IPreferenceStore store = getPreferenceStore();
		System.out.println("Initializing OT Network values from store...");
		shortestTime.setStringValue(store.getString(OpenToxConstants.SHORTEST_WAIT_TIME_IN_SECS));
		longestTime.setStringValue(store.getString(OpenToxConstants.LONGEST_WAIT_TIME_IN_SECS));
		timeout.setStringValue(store.getString(OpenToxConstants.HTTP_TIMEOUT));
	}

	private void storeValues() {
		System.out.println("Storing OT Network values...");
		IPreferenceStore store = getPreferenceStore();
		store.setValue(OpenToxConstants.SHORTEST_WAIT_TIME_IN_SECS, shortestTime.getStringValue());
		store.setValue(OpenToxConstants.LONGEST_WAIT_TIME_IN_SECS, longestTime.getStringValue());
		store.setValue(OpenToxConstants.HTTP_TIMEOUT, timeout.getStringValue());
	}

	private void initializeDefaults() {
		System.out.println("Initializing OT Network value defaults...");
		IPreferenceStore store = getPreferenceStore();
		shortestTime.setStringValue(store.getDefaultString(OpenToxConstants.SHORTEST_WAIT_TIME_IN_SECS));
		longestTime.setStringValue(store.getDefaultString(OpenToxConstants.LONGEST_WAIT_TIME_IN_SECS));
		timeout.setStringValue(store.getDefaultString(OpenToxConstants.HTTP_TIMEOUT));
	}
}
