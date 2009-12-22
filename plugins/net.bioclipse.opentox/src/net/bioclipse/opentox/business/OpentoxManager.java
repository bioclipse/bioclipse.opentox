/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.opentox.business;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.RDFManager;

public class OpentoxManager implements IBioclipseManager {

    private RDFManager rdf = new RDFManager(); 

    private final static String QUERY_DATASETS =
        "SELECT ?set ?id WHERE {" +
        "  ?set a <http://www.opentox.org/api/1.1#Dataset>;" +
        "       <http://purl.org/dc/elements/1.1/identifier> ?id ." +
        "}";

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "opentox";
    }
    
    public List<String> listDataSets(String service, IProgressMonitor monitor)
        throws BioclipseException {
        List<String> dataSets = new ArrayList<String>();

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Requesting available data sets...", 3);
        IRDFStore store = rdf.createStore();
        try {
            // download the list of data sets as RDF
            rdf.importURL(store, service + "dataset", monitor);
            monitor.worked(1);

            // query the downloaded RDF
            List<List<String>> results = rdf.sparql(store, QUERY_DATASETS);
            monitor.worked(1);

            // return the data set identifiers
            for (List<String> set : results) {
                dataSets.add(set.get(0));
            }
            monitor.worked(1);
        } catch (BioclipseException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing RDF API of service",
                exception
            );
        }

        monitor.done();
        return dataSets;
    }
}
