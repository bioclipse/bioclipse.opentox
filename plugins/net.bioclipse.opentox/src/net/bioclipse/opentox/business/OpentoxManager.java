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

import net.bioclipse.business.BioclipsePlatformManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.RDFManager;
import net.bioclipse.rdf.model.IStringMatrix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class OpentoxManager implements IBioclipseManager {

    private RDFManager rdf = new RDFManager(); 
    private BioclipsePlatformManager bioclipse = new BioclipsePlatformManager();

    private final static String QUERY_ALGORITHMS =
        "SELECT ?algo WHERE {" +
        "  ?algo a <http://www.opentox.org/api/1.1#Algorithm>." +
        "}";

    private final static String QUERY_DESCRIPTORS =
        "SELECT ?desc WHERE {" +
        "  ?desc a <http://www.opentox.org/algorithmTypes.owl#DescriptorCalculation> ." +
        "}";

    private final static String QUERY_DATASETS =
        "SELECT ?set ?id WHERE {" +
        "  ?set a <http://www.opentox.org/api/1.1#Dataset>;" +
        "       <http://purl.org/dc/elements/1.1/identifier> ?id ." +
        "}";

    private final static String QUERY_COMPOUNDS =
        "SELECT ?compound ?id WHERE {" +
        "  ?set a <http://www.opentox.org/api/1.1#Compound>;" +
        "       <http://purl.org/dc/elements/1.1/identifier> ?id ." +
        "}";
    
    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "opentox";
    }
    
    public List<Integer> listDataSets(String service, IProgressMonitor monitor)
        throws BioclipseException {
        List<Integer> dataSets = new ArrayList<Integer>();

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Requesting available data sets...", 3);
        IRDFStore store = rdf.createInMemoryStore();
        try {
            // download the list of data sets as RDF
            rdf.importURL(store, service + "dataset", monitor);
            monitor.worked(1);

            // query the downloaded RDF
            IStringMatrix results = rdf.sparql(store, QUERY_DATASETS);
            monitor.worked(1);

            // return the data set identifiers
            for (String setURI : results.getColumn("set")) {
                dataSets.add(
                    Integer.valueOf(setURI.substring(setURI.lastIndexOf('/')+1))
                );
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

    public List<String> listAlgorithms(String service, IProgressMonitor monitor)
    throws BioclipseException {
    	List<String> dataSets = new ArrayList<String>();

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Requesting available algorithms...", 3);
        IRDFStore store = rdf.createInMemoryStore();
        try {
            // download the list of data sets as RDF
            rdf.importURL(store, service + "algorithm", monitor);
            monitor.worked(1);

            // query the downloaded RDF
            IStringMatrix results = rdf.sparql(store, QUERY_ALGORITHMS);
            System.out.println(results);
            monitor.worked(1);

            // return the data set identifiers
            dataSets = results.getColumn("algo");
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

    public List<String> listDescriptors(String service, IProgressMonitor monitor)
    throws BioclipseException {
    	List<String> dataSets = new ArrayList<String>();

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Requesting available descriptors...", 3);
        IRDFStore store = rdf.createInMemoryStore();
        try {
            // download the list of data sets as RDF
            rdf.importURL(store, service + "algorithm", monitor);
            System.out.println(rdf.dump(store));
            monitor.worked(1);

            // query the downloaded RDF
            IStringMatrix results = rdf.sparql(store, QUERY_DESCRIPTORS);
            monitor.worked(1);

            // return the data set identifiers
            dataSets = results.getColumn("desc");
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

    public List<Integer> listCompounds(String service, Integer dataSet,
            IProgressMonitor monitor) throws BioclipseException {
        List<Integer> compounds = new ArrayList<Integer>();

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Looking up compound identifiers...", 3);
        IRDFStore store = rdf.createInMemoryStore();
        try {
            // download the list of compounds as RDF
            rdf.importURL(
                store,
                service + "dataset/" + dataSet + "/compound",
                monitor
            );
            monitor.worked(1);

            // query the downloaded RDF
            IStringMatrix results = rdf.sparql(store, QUERY_COMPOUNDS);
            monitor.worked(1);

            // return the data set identifiers
            for (String compound : results.getColumn("compound")) {
                compounds.add(
                    Integer.valueOf(compound.substring(compound.lastIndexOf('/')+1))
                );
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
        return compounds;
    }

    public String downloadCompoundAsMDLMolfile(String service, Integer dataSet,
            Integer compound, IProgressMonitor monitor)
        throws BioclipseException {

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Downloading compound...", 1);

        String url = service + "dataset/" + dataSet + "/compound/" + compound;
        String result = bioclipse.download(
            url, "chemical/x-mdl-molfile", monitor
        );
        monitor.done();

        return result;
    }

    public IFile downloadDataSetAsMDLSDfile(String service, Integer dataSet,
            IFile file, IProgressMonitor monitor)
        throws BioclipseException {

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Downloading data set...", 1);

        String url = service + "dataset/" + dataSet;
        IFile result = bioclipse.downloadAsFile(
            url, "chemical/x-mdl-sdfile", file, monitor
        );
        monitor.done();

        return result;
    }

}
