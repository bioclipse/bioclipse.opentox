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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.opentox.Activator;
import net.bioclipse.opentox.OpenToxConstants;
import net.bioclipse.opentox.business.OpentoxManager;
import net.bioclipse.ui.prefs.IPreferenceConstants;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * 
 * @author ola
 *
 */
public class ServicesPreferencePage extends PreferencePage implements
IWorkbenchPreferencePage {

    private static final Logger logger = 
            Logger.getLogger(ServicesPreferencePage.class.toString());

    private List<String[]> appList;
    private TableViewer checkboxTableViewer;

    public ServicesPreferencePage() {
        super();
    }

    /**
     * The label provider for the table that displays 3 columns: name, service,
     * and serviceSPARQL
     * @author ola
     *
     */
    class ApplicationsLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int index) {
            if (!(element instanceof String[])) return "Wrong type in column text";
            String[] retList = (String[]) element;

            if (index==0){
                if (retList.length>0)
                    return retList[0];
                else
                    return "NA";
            }
            else if (index==1){
                if (retList.length>1)
                    return retList[1];
                else
                    return "NA";
            }
            else if (index==2){
                if (retList.length>2)
                    return retList[2];
                else
                    return "NA";
            }
            else
                return "???";
        }

    }

    public class ApplicationsContentProvider implements IStructuredContentProvider {
        @SuppressWarnings("unchecked")
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof ArrayList) {
                ArrayList retList = (ArrayList) inputElement;
                return retList.toArray();
            }
            return new Object[0];

        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }


    public Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        setSize(new Point(600,420));
        container.setSize(600,420);
        container.setLayout(new FormLayout());

        //		checkboxTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
        checkboxTableViewer = new TableViewer(container, SWT.BORDER | SWT.SINGLE);
        checkboxTableViewer.setContentProvider(new ApplicationsContentProvider());
        checkboxTableViewer.setLabelProvider(new ApplicationsLabelProvider());
        final Table table = checkboxTableViewer.getTable();
        FormData formData = new FormData(700,300);
        formData.left = new FormAttachment(0, 11);
        formData.top = new FormAttachment(0, 20);
        table.setLayoutData(formData);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
        tableColumn.setText("Name");
        tableColumn.setWidth(100);
        TableColumn tableColumn2 = new TableColumn(table, SWT.LEFT);
        tableColumn2.setText("Service");
        tableColumn2.setWidth(300);
        TableColumn tableColumn3 = new TableColumn(table, SWT.LEFT);
        tableColumn3.setText("ServiceSPARQL");
        tableColumn3.setWidth(300);

        appList=getPreferencesFromStore();
        checkboxTableViewer.setInput(appList);
        
        final Button editButton = new Button(container, SWT.NONE);
        FormData formData_2 = new FormData();
        formData_2.top = new FormAttachment(table, 0, SWT.TOP);
        formData_2.left = new FormAttachment(table, 6);
        formData_2.right = new FormAttachment(100, -9);
        editButton.setLayoutData(formData_2);
        editButton.setText("Edit");
        editButton.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {

                //Get selection from viewer
                ISelection sel=checkboxTableViewer.getSelection();
                if (!(sel instanceof IStructuredSelection)) {
                    logger.debug("Item of wrong type selected.");
                    showMessage("Please select an entry to edit first.");
                    return;
                }

                IStructuredSelection ssel = (IStructuredSelection) sel;
                Object obj=ssel.getFirstElement();

                if (!(obj instanceof String[])) {
                    logger.debug("Object of wrong type selected.");
                    showMessage("Please select an entry to edit first.");
                    return;
                }

                String[] chosen = (String[]) obj;
                /* TODO Fix this in a better way. Without the if-statement below
                 * we'll get an unexpected ArrayIndexOutOfBoundsException in 
                 * some cases, 'cos the last element is missing in the selection */
                if (chosen.length < 3) {
                    for(int i= chosen.length;i<3;i++) {
                        chosen[i] = "NA";
                    }
                }    
                
                ServicesEditDialog dlg=new ServicesEditDialog(getShell(), chosen[0], chosen[1], chosen[2]);

                dlg.open();

                String[] ret=dlg.getServiceInfo();
                //If OK pressed
                if (dlg.getReturnCode()==0){
                    if (ret.length==3){
                        chosen[0]=ret[0]; //name
                        chosen[1]=ret[1]; //service
                        chosen[2]=ret[2]; //serviceSPARQL
                        checkboxTableViewer.refresh();
                    }
                    else{
                        logger.debug("Error getting result from dialog!");
                        showMessage("Error getting result from dialog.");
                    }
                }

            }
        });

        final Button removeButton = new Button(container, SWT.NONE);
        FormData formData_3 = new FormData();
        formData_3.right = new FormAttachment(100, -9);
        formData_3.left = new FormAttachment(table, 6);
        formData_3.top = new FormAttachment(editButton, 10);
        removeButton.setLayoutData(formData_3);
        removeButton.setText("Remove");
        removeButton.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {

                //Get selection from viewer
                if(checkboxTableViewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)checkboxTableViewer.getSelection();
                    Object[] objSelection=selection.toArray();

                    for (int i=0;i<objSelection.length;i++){
                        if (objSelection[i] instanceof String[]) {
                            String[] row = (String[]) objSelection[i];
                            if (appList.contains(row)){
                                appList.remove(row);
                            }
                        }
                    }
                    checkboxTableViewer.refresh();
                }

            }
        });

        final Button addButton = new Button(container, SWT.NONE);
        formData.right = new FormAttachment(100, -89);
        FormData formData_1 = new FormData();
        formData_1.right = new FormAttachment(100, -9);
        formData_1.left = new FormAttachment(table, 6);
        formData_1.top = new FormAttachment(removeButton, 10);
        addButton.setLayoutData(formData_1);
        addButton.setText("Add");
        addButton.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {

                ServicesEditDialog dlg=new ServicesEditDialog(getShell());
                dlg.open();

                String[] ret=dlg.getServiceInfo();
                if (ret.length==3){
                    appList.add(ret);
                    checkboxTableViewer.refresh();
                }
            }
        });
        
        final Button upButton = new Button(container, SWT.NONE);
        FormData formData_4 = new FormData();
        formData_4.right = new FormAttachment(100, -9);
        formData_4.left = new FormAttachment(table, 6);
        formData_4.top = new FormAttachment(addButton, 25);
        upButton.setLayoutData( formData_4 );
        upButton.setText( "Up" );//"\u25E2\u25E3" );
        upButton.addSelectionListener( new SelectionListener() {
            
            @Override
            public void widgetSelected( SelectionEvent e ) {
                if(checkboxTableViewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)checkboxTableViewer.getSelection();
                    Object[] objSelection=selection.toArray();

                    for (int i=0;i<objSelection.length;i++){
                        if (objSelection[i] instanceof String[]) {
                            String[] row = (String[]) objSelection[i];
                            int selRow = appList.indexOf( row );
                            if (selRow > 0)
                                Collections.swap( appList, selRow, selRow - 1 );
                            checkboxTableViewer.refresh();
                        }
                    }
                }
            }
            
            @Override
            public void widgetDefaultSelected( SelectionEvent e ) {     }
        } );
        
        final Button downButton = new Button(container, SWT.NONE);
        FormData formData_5 = new FormData();
        formData_5.right = new FormAttachment(100, -9);
        formData_5.left = new FormAttachment(table, 6);
        formData_5.top = new FormAttachment(upButton, 0);
        downButton.setLayoutData( formData_5 );
        downButton.setText("Down" );// "\u25E5\u25E4" );
        downButton.addSelectionListener( new SelectionListener() {
            
            @Override
            public void widgetSelected( SelectionEvent e ) {
                if(checkboxTableViewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection)checkboxTableViewer.getSelection();
                    Object[] objSelection=selection.toArray();

                    for (int i=0;i<objSelection.length;i++){
                        if (objSelection[i] instanceof String[]) {
                            String[] row = (String[]) objSelection[i];
                            int selRow = appList.indexOf( row );
                            if (selRow < appList.size()-1)
                                Collections.swap( appList, selRow, selRow + 1 );
                            checkboxTableViewer.refresh();
                        }
                    }
                }
            }
            
            @Override
            public void widgetDefaultSelected( SelectionEvent e ) {     }
        } );
        
        Label infoText = new Label(container, SWT.NONE);
        infoText.setText( "Only the service site on the top row is used, " +
                "please use the buttons to the right to move the items." );
        FormData formData_6 = new FormData();
        formData_6.left = new FormAttachment(0,10);
        formData_6.top = new FormAttachment(0,0);
        infoText.setLayoutData( formData_6 );
        Display display = container.getDisplay();
        String fontName = display.getSystemFont().getFontData()[0].getName();
        Font font1 = new Font(display, fontName, 12, SWT.NORMAL);
        infoText.setFont( font1 );
        
        if (table.getItemCount()>0)
            table.setSelection(0);
        container.pack();
        parent.pack();
        return container;
    }


    public void init(IWorkbench workbench) {
        ScopedPreferenceStore prefStore = 
        new ScopedPreferenceStore( ConfigurationScope.INSTANCE, 
                                                      OpenToxConstants.PLUGIN_ID );
        
        prefStore.setSearchContexts( null );
        setPreferenceStore( prefStore );
    }

    /**
     * Override to store results
     */
    public boolean performOk() {

        String value=convertToPreferenceString(appList);
        logger.debug("Update sites prefs to store: " + value);
        
        //Save prefs as this must be done explicitly
        Preferences pref =  InstanceScope.INSTANCE.getNode( OpenToxConstants.PLUGIN_ID );
        pref.put( OpenToxConstants.SERVICES, value );
        try {
            pref.flush();
        } catch ( BackingStoreException e ) {
            logger.error( "Faild to store preference",e );
        }
        return true;
    }

    /**
     * @return List<String[]> containing the preferences
     * 
     */
    public static List<String[]> getPreferencesFromStore() {

        IPreferencesService service = Platform.getPreferencesService();
        String entireString=service.getString(OpenToxConstants.PLUGIN_ID,OpenToxConstants.SERVICES,"n/a",null);
        return convertPreferenceStringToArraylist(entireString);
    }


    /**
     * @return List<String[]> containing the default preferences
     * 
     */
    public static List<String[]> getDefaultPreferencesFromStore() {
        
        Preferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String entireString = node.get( OpenToxConstants.SERVICES, "n/a" );
        
        return convertPreferenceStringToArraylist(entireString);
    }

    /**
     * Converts input to arraylist using delimiters from BioclipseConstants
     * @param entireString
     * @return
     */
    public static List<String[]> convertPreferenceStringToArraylist(String entireString) {
        List<String[]> myList=new ArrayList<String[]>();
        //		logger.debug("prefs read from store: " + entireString);
        String[] ret=entireString.split(IPreferenceConstants.PREFERENCES_OBJECT_DELIMITER);
        String[] partString=new String[0];
        for (int i = 0; i < ret.length; i++) {
            partString = ret[i].split(IPreferenceConstants.PREFERENCES_DELIMITER);
            myList.add(partString);
        }

        if (ret.length==1){
            if (partString.length<3){
                logger.debug("OpenTox prefs is not in correct format, hence cleared");
                myList.clear();
            }
        }
        return myList;

    }


    /**
     * 
     * 
     * @param appList2
     * @return
     */
    public static String convertToPreferenceString(List<String[]> appList2) {
        Iterator<String[]> it=appList2.iterator();
        String ret="";

        // TODO: update to handle short and empty strings

        while (it.hasNext()){
            String[] str=(String[]) it.next();
            String singleRet="";
            for (int i=0; i<str.length;i++){
                singleRet = singleRet + str[i];
                if ((i+1)<str.length) { // there is another column
                    singleRet += IPreferenceConstants.PREFERENCES_DELIMITER;
                }
            }
            ret=ret + singleRet;
            if (it.hasNext()) { // there is another row
                ret += IPreferenceConstants.PREFERENCES_OBJECT_DELIMITER;
            }
        }
        return ret;
    }

    protected void performDefaults() {
        super.performDefaults();
        //String values = getPreferenceStore().getString( OpenToxConstants.SERVICES );
        String values = DefaultScope.INSTANCE.getNode( OpenToxConstants.PLUGIN_ID ).get( OpenToxConstants.SERVICES, "n/a" );
        appList=convertPreferenceStringToArraylist(values);
        checkboxTableViewer.setInput(appList);

    }

    private void showMessage(String message) {
        MessageDialog.openInformation(
        		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
        		"OpenTox Preferences",
        		message);
    }
    
}
