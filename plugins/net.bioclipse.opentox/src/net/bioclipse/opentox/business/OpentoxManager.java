/* Copyright (c) 2009-2011  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.opentox.business;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import net.bioclipse.business.BioclipsePlatformManager;
import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.IMolecule.Property;
import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.opentox.OpenToxLogInOutListener;
import net.bioclipse.opentox.api.Algorithm;
import net.bioclipse.opentox.api.Dataset;
import net.bioclipse.opentox.api.Feature;
import net.bioclipse.opentox.api.HttpMethodHelper;
import net.bioclipse.opentox.api.Model;
import net.bioclipse.opentox.api.ModelAlgorithm;
import net.bioclipse.opentox.api.MolecularDescriptorAlgorithm;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.RDFManager;
import net.bioclipse.usermanager.business.IUserManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class OpentoxManager implements IBioclipseManager {

	private static final Logger logger = Logger.getLogger(OpentoxManager.class);

    private RDFManager rdf = new RDFManager();
    private BioclipsePlatformManager bioclipse = new BioclipsePlatformManager();
    private CDKManager cdk = new CDKManager();
    private IUserManager userManager = net.bioclipse.usermanager.Activator
            .getDefault().getUserManager();
    
    private final static String QUERY_ALGORITHMS =
        "SELECT ?algo WHERE {" +
        "  ?algo a <http://www.opentox.org/api/1.1#Algorithm>." +
        "}";

    private final static String QUERY_MODELS =
        "SELECT ?model WHERE {" +
        "  ?model a <http://www.opentox.org/api/1.1#Model>." +
        "}";

    private final static String SPARQL_DESCRIPTORS =
        "SELECT ?algo ?desc WHERE {" +
   	    "  ?algo a <http://www.opentox.org/api/1.1#Algorithm> ;" +
   	    "        a <http://www.opentox.org/algorithmTypes.owl#DescriptorCalculation> ;" +
	    "     <http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#instanceOf> ?desc ." +
        "  ?desc a <http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#MolecularDescriptor> ." +
        "}";

    private final static String QUERY_DATASETS =
        "SELECT ?set WHERE {" +
        "  ?set a <http://www.opentox.org/api/1.1#Dataset> ." +
        "}";

    private final static String QUERY_FEATURES =
        "SELECT ?feature WHERE {" +
        "  ?feature a <http://www.opentox.org/api/1.1#Feature> ." +
        "}";

    private final static String QUERY_COMPOUNDS =
        "SELECT ?compound ?id WHERE {" +
        "  ?compound a <http://www.opentox.org/api/1.1#Compound> ." +
        "}";
    
    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "opentox";
    }

    private OpenToxLogInOutListener getOpenToxListener() {
        OpenToxLogInOutListener openToxLogInOutListener;
        try {
            openToxLogInOutListener = OpenToxLogInOutListener.getInstance();
        } catch ( InstantiationException e ) {
            openToxLogInOutListener = OpenToxLogInOutListener.
                    getInstance(userManager);
        }
    
    return openToxLogInOutListener;
    }
    
    public String getToken() {
        OpenToxLogInOutListener listener = this.getOpenToxListener();
    	return listener.getToken();
    }

    public void logout() throws BioclipseException {
        OpenToxLogInOutListener listener = this.getOpenToxListener();
    	try {
    	    listener.logout();
		} catch (Exception e) {
			throw new BioclipseException(
				"Error while logging out of OpenTox: " + e.getMessage(),
				e
			);
		}
    }

    public boolean login(String user, String pass) throws BioclipseException {
        /* TODO The account type below (i.e. OpenTox) should not be hard coded. 
         * It should come from the the extension point somehow */
        String authService =  userManager.getProperty( "OpenTox", "auth. service" );
        return login( user, pass, authService );
    }
    
    public boolean login(String user, String pass, String authService) throws BioclipseException {
        OpenToxLogInOutListener listener = this.getOpenToxListener();
    	try {
			return listener.login(user, pass, authService);
		} catch (Exception e) {
			throw new BioclipseException(
				"Error while logging in on OpenTox: " + e.getMessage(),
				e
			);
		}
    }
    
    public String getAuthorizationServer() {
        OpenToxLogInOutListener listener = this.getOpenToxListener();
        try {
            return listener.getAuthService();
        } catch ( LoginException e ) {
            logger.error( e );
            return null;
        }
    }
    
    public void resetAuthorizationServer() {
        OpenToxLogInOutListener listener = this.getOpenToxListener();
        listener.resetAuthService();
    }
    
    public Map<String,String> getFeatureInfo(String ontologyServer, String feature, IProgressMonitor monitor) {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Downloading feature information", 1);
    	Map<String,String> properties = Feature.getProperties(ontologyServer, feature);
    	monitor.done();
    	
    	return properties;
    }

    public Map<String,String> getModelInfo(String ontologyServer, String model, IProgressMonitor monitor) {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Downloading model information", 1);
    	Map<String,String> properties = Model.getProperties(ontologyServer, model);
    	monitor.done();
    	
    	return properties;
    }

    public Map<String,String> getAlgorithmInfo(String ontologyServer, String algorithm, IProgressMonitor monitor) {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Downloading algorithm information", 1);
    	Map<String,String> properties = Algorithm.getProperties(ontologyServer, algorithm);
    	monitor.done();
    	
    	return properties;
    }

    public Map<String,Map<String,String>> getFeatureInfo(String ontologyServer, List<String> features, IProgressMonitor monitor) {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Downloading feature information", features.size());
    	Map<String,Map<String,String>> results = new HashMap<String, Map<String,String>>();
    	for (String feature : features) {
    		results.put(feature, Feature.getProperties(ontologyServer, feature));
    		monitor.worked(1);
    	}
    	monitor.done();    	
    	return results;
    }

    public Map<String,Map<String,String>> getAlgorithmInfo(String ontologyServer, List<String> algorithms, IProgressMonitor monitor) {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Downloading algorithm information", algorithms.size());
    	Map<String,Map<String,String>> results = new HashMap<String, Map<String,String>>();
    	for (String algorithm : algorithms) {
    		results.put(algorithm, Algorithm.getProperties(ontologyServer, algorithm));
    		monitor.worked(1);
    	}
    	monitor.done();    	
    	return results;
    }

    public Map<String,Map<String,String>> getModelInfo(String ontologyServer, List<String> features, IProgressMonitor monitor) {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Downloading model information", features.size());
    	Map<String,Map<String,String>> results = new HashMap<String, Map<String,String>>();
    	for (String feature : features) {
    		results.put(feature, Model.getProperties(ontologyServer, feature));
    		monitor.worked(1);
    	}
    	monitor.done();    	
    	return results;
    }

    public List<String> listDataSets(String service, IProgressMonitor monitor)
        throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask("Requesting available data sets...", 3);

        IRDFStore store = rdf.createInMemoryStore();
        List<String> dataSets = Collections.emptyList();
        Map<String, String> extraHeaders = new HashMap<String, String>();
        OpenToxLogInOutListener listener = this.getOpenToxListener();
        
        String token = listener.getToken();
        if (token != null) {
        	extraHeaders.put("subjectid", listener.getToken());
        }
        try {
            // download the list of data sets as RDF
            rdf.importURL(store, service + "dataset", extraHeaders, monitor);
            String dump = rdf.asRDFN3(store);
            System.out.println("RDF: " + dump);
            monitor.worked(1);

            // query the downloaded RDF
            IStringMatrix results = rdf.sparql(store, QUERY_DATASETS);
            monitor.worked(1);

            if (results.getRowCount() > 0) {
            	dataSets = results.getColumn("set");
            }
            monitor.worked(1);
        } catch (BioclipseException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing RDF API of service: " + exception.getMessage(),
                exception
            );
        }

        monitor.done();
        return dataSets;
    }

    public List<String> listFeatures(String service, IProgressMonitor monitor)
    throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Requesting available features...", 3);

    	IRDFStore store = rdf.createInMemoryStore();
    	List<String> dataSets = Collections.emptyList();
    	Map<String, String> extraHeaders = new HashMap<String, String>();
    	OpenToxLogInOutListener listener = this.getOpenToxListener();
    	
    	String token = listener.getToken();
    	if (token != null) {
    		extraHeaders.put("subjectid", listener.getToken());
    	}
    	try {
    		// download the list of data sets as RDF
    		rdf.importURL(store, service + "feature", extraHeaders, monitor);
    		String dump = rdf.asRDFN3(store);
    		System.out.println("RDF: " + dump);
    		monitor.worked(1);

    		// query the downloaded RDF
    		IStringMatrix results = rdf.sparql(store, QUERY_FEATURES);
    		monitor.worked(1);

    		if (results.getRowCount() > 0) {
    			dataSets = results.getColumn("feature");
    		}
    		monitor.worked(1);
    	} catch (BioclipseException exception) {
    		throw exception;
    	} catch (Exception exception) {
    		throw new BioclipseException(
    			"Error while accessing RDF API of service: " + exception.getMessage(),
    			exception
    		);
    	}

    	monitor.done();
    	return dataSets;
    }

    public IStringMatrix searchDataSets(String ontologyServer, String query, IProgressMonitor monitor)
    throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask("Searching available data sets...", 1);

        try {
            String sparql =
            	"SELECT ?set ?title WHERE {" +
                "  ?set a <http://www.opentox.org/api/1.1#Dataset> ;" +
                "    <http://purl.org/dc/elements/1.1/title> ?title ." +
                "  FILTER regex(?title, \"" + query + "\")" +
                "}";
            IStringMatrix results = rdf.sparqlRemote(ontologyServer, sparql, monitor);
            monitor.worked(1);

            return results;
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }
    }
    
    public IStringMatrix searchDescriptors(String ontologyServer, String query, IProgressMonitor monitor)
    throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask("Searching available data sets...", 1);

        try {
            String sparql =
            	"SELECT ?set ?title WHERE {" +
                "  ?set a <http://www.opentox.org/api/1.1#Algorithm> ;" +
                "        a <http://www.opentox.org/algorithmTypes.owl#DescriptorCalculation> ;" +
                "     <http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#instanceOf> ?desc ;" +
                "    <http://purl.org/dc/elements/1.1/title> ?title ." +
                "  ?desc a <http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#MolecularDescriptor> ." +
                "  FILTER regex(?title, \"" + query + "\")" +
                "}";
            IStringMatrix results = rdf.sparqlRemote(ontologyServer, sparql, monitor);
            System.out.println("SPARQL:\n" + sparql);
            monitor.worked(1);

            return results;
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }
    }
    
    public IStringMatrix searchModels(String ontologyServer, String query, IProgressMonitor monitor)
    throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask("Searching available data sets...", 1);

        try {
            String sparql =
            	"SELECT ?set ?title WHERE {" +
                "  ?set a <http://www.opentox.org/api/1.1#Model> ;" +
                "    <http://purl.org/dc/elements/1.1/title> ?title ." +
                "  FILTER regex(?title, \"" + query + "\")" +
                "}";
            IStringMatrix results = rdf.sparqlRemote(ontologyServer, sparql, monitor);
            System.out.println("SPARQL:\n" + sparql);
            monitor.worked(1);

            return results;
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }
    }
    
    public List<String> listAlgorithms(String ontologyServer, IProgressMonitor monitor)
    throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        IStringMatrix results = new StringMatrix();

        monitor.beginTask("Requesting available algorithms...", 1);
        try {
            // download the list of data sets as RDF
        	results = rdf.sparqlRemote(ontologyServer, QUERY_ALGORITHMS, monitor);
            monitor.worked(1);
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }

        monitor.done();
        return results.getColumn("algo");
    }

    /**
     * Keep only rows whose column field contains the given substring.
     * 
     * @return
     */
    private IStringMatrix regex(IStringMatrix matrix, String column, String substring) {
        StringMatrix table = new StringMatrix();
        int rowCount = matrix.getRowCount();
    	int colCount = matrix.getColumnCount();
    	int hitCount = 0;
		for (int col=1; col<=colCount; col++) {
			table.setColumnName(col, matrix.getColumnName(col));
		}
		// do the filtering
		for (int row=1; row<=rowCount; row++) {
			String algo = matrix.get(row, column);
    		if (algo.contains(substring)) {
    			// CDK descriptor, copy row
    			hitCount++;
    			for (int col=1; col<=colCount; col++) {
    				table.set(hitCount, col, matrix.get(row, col));
    			}
    		}
    	}
        return table;
    }
    
    public IStringMatrix listDescriptors(String ontologyServer, IProgressMonitor monitor)
    throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        IStringMatrix results = new StringMatrix();

        monitor.beginTask("Requesting available descriptors...", 1);
        try {
            // download the list of data sets as RDF
        	results = regex(
        		rdf.sparqlRemote(ontologyServer, SPARQL_DESCRIPTORS, monitor),
        		"algo", "org.openscience.cdk"
        	);
            monitor.worked(1);
        } catch (Exception exception) {
            throw new BioclipseException(
            	"Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }

        monitor.done();
        return results;
    }

    public List<String> listModels(String ontologyServer, IProgressMonitor monitor)
    throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        IStringMatrix results = new StringMatrix();

        monitor.beginTask("Requesting available descriptors...", 1);
        try {
            // download the list of data sets as RDF
        	results = rdf.sparqlRemote(ontologyServer, QUERY_MODELS, monitor);
            monitor.worked(1);
        } catch (Exception exception) {
            throw new BioclipseException(
                "Error while accessing the OpenTox ontology server at: " + ontologyServer,
                exception
            );
        }

        monitor.done();
        return results.getColumn("model");
    }

    public List<Integer> listCompounds(String service, Integer dataSet,
            IProgressMonitor monitor) throws BioclipseException {
    	return listCompounds(service + "dataset/" + dataSet, monitor);
    }

    public List<Integer> listCompounds(String dataSet,
            IProgressMonitor monitor) throws BioclipseException {
        List<Integer> compounds = new ArrayList<Integer>();

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Looking up compound identifiers...", 3);
        IRDFStore store = rdf.createInMemoryStore();
        try {
            Map<String, String> extraHeaders = new HashMap<String, String>();
            OpenToxLogInOutListener listener = this.getOpenToxListener();
            String token = listener.getToken();
            if (token != null) {
            	extraHeaders.put("subjectid", listener.getToken());
            }
            // download the list of compounds as RDF
            rdf.importURL(
                store,
                dataSet + "/compound",
                extraHeaders,
                monitor
            );
            monitor.worked(1);

            // query the downloaded RDF
            System.out.println(rdf.dump(store));
            IStringMatrix results = rdf.sparql(store, QUERY_COMPOUNDS);
            monitor.worked(1);

            // return the data set identifiers
            if (results.getRowCount() > 0) {
                for (String compound : results.getColumn("compound")) {
                    compounds.add(
                        Integer.valueOf(compound.substring(compound.lastIndexOf('/')+1))
                    );
                }
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

    public String downloadCompoundAsMDLMolfile(String service, String dataSet,
            Integer compound, IProgressMonitor monitor)
        throws BioclipseException {
        return downloadCompoundAsMDLMolfile(dataSet + "/compound/" + compound, monitor);
    }

    public String downloadCompoundAsMDLMolfile(String compoundURI, IProgressMonitor monitor)
        throws BioclipseException {

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Downloading compound...", 1);

        String result = bioclipse.download(
            compoundURI, "chemical/x-mdl-molfile", monitor
        );
        monitor.done();

        return result;
    }

    public IFile downloadDataSetAsMDLSDfile(String service, String dataSet,
            IFile file, IProgressMonitor monitor)
        throws BioclipseException {

        if (monitor == null) monitor = new NullProgressMonitor();

        monitor.beginTask("Downloading data set...", 1);

        Map<String,String> extraHeaders = null;
        if (getToken() != null) {
        	extraHeaders = new HashMap<String, String>();
        	extraHeaders.put("subjectid", getToken());
        }
        
        IFile result = bioclipse.downloadAsFile(
            dataSet, "chemical/x-mdl-sdfile", file, extraHeaders, monitor
        );
        monitor.done();

        return result;
    }

    public void createDataset(String service, IReturner<String> returner, IProgressMonitor monitor)
    throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Creating an OpenTox API data set ...", 1);
    	try {
			String dataset = Dataset.createNewDataset(service, monitor);
			monitor.done();
			returner.completeReturn( dataset ); 
		} catch (Exception exc) {
			throw new BioclipseException(
				"Exception while creating dataset: " + exc.getMessage()
			);
		}
    }

    public void createDataset(String service, List<IMolecule> molecules, IReturner<String> returner, IProgressMonitor monitor)
    throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Creating an OpenTox API data set ...", 1);
    	try {
			String dataset = Dataset.createNewDataset(service, molecules, monitor);
			monitor.done();
			returner.completeReturn( dataset ); 
		} catch (Exception exc) {
			throw new BioclipseException(
				"Exception while creating dataset: " + exc.getMessage()
			);
		}
    }

    public void createDataset(String service, IMolecule molecule, IReturner<String> returner, IProgressMonitor monitor)
    throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Creating an OpenTox API data set ...", 1);
    	try {
			String dataset = Dataset.createNewDataset(service, molecule, monitor);
			monitor.done();
			returner.completeReturn( dataset ); 
		} catch (Exception exc) {
			throw new BioclipseException(
				"Exception while creating dataset: " + exc.getMessage()
			);
		}
    }

    public void addMolecule(String datasetURI, IMolecule mol, IProgressMonitor monitor)
    throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Adding a molecule to an OpenTox API data set ...", 1);
    	try {
			Dataset.addMolecule(datasetURI, mol);
			monitor.done();
		} catch (Exception exc) {
			throw new BioclipseException(
				"Exception while creating dataset: " + exc.getMessage()
			);
		}
    }

    public void addMolecules(String datasetURI, List<IMolecule> molecules, IProgressMonitor monitor)
    throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	
    	monitor.beginTask("Adding a molecule to an OpenTox API data set ...", 1);
    	try {
			Dataset.addMolecules(datasetURI, molecules);
			monitor.done();
		} catch (Exception exc) {
			throw new BioclipseException(
				"Exception while creating dataset: " + exc.getMessage()
			);
		}
    }

    public void deleteDataset(String datasetURI)
    throws BioclipseException {
    	try {
    		Dataset.deleteDataset(datasetURI);
		} catch (Exception exc) {
			throw new BioclipseException(
				"Exception while creating dataset: " + exc.getMessage()
			);
		}
    }

    public void setDatasetLicense(
    		String datasetURI, String license,
    		IProgressMonitor monitor)
    throws Exception {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Setting the data set license", 2);

    	new URI(license);
    	monitor.worked(1);
    	Dataset.setLicense(datasetURI, license);
    	monitor.worked(1);
    	
    	monitor.done();
    }
    	
    public void setDatasetRightsHolder(
    		String datasetURI, String holder,
    		IProgressMonitor monitor)
    throws Exception {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Setting the data set rights holder", 2);

    	new URI(holder);
    	monitor.worked(1);
    	Dataset.setRightsHolder(datasetURI, holder);
    	monitor.worked(1);
    	
    	monitor.done();
    }
    	
    public void setDatasetTitle(
    		String datasetURI, String title,
    		IProgressMonitor monitor)
    throws Exception {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Setting the data set title", 2);

    	monitor.worked(1);
    	Dataset.setTitle(datasetURI, title);
    	monitor.worked(1);
    	
    	monitor.done();
    }
    	
    public List<String> calculateDescriptor(
    		String service, String descriptor,
    		List<IMolecule> molecules, IProgressMonitor monitor)
    throws Exception {
    	if (service == null) throw new BioclipseException("Service is null");
    	if (descriptor== null) throw new BioclipseException("Descriptor is null");

    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Calculate descriptor for dataset", molecules.size());

    	List<String> calcResults = new ArrayList<String>();
    	for (IMolecule molecule : molecules) {
    		String dataset = Dataset.createNewDataset(service, molecule, monitor);
    		if (monitor.isCanceled()) continue;
    		String results = MolecularDescriptorAlgorithm.calculate(
 				service, descriptor, dataset, monitor
    		);
    		if (monitor.isCanceled()) continue;
    		StringMatrix features = Dataset.listPredictedFeatures(results);
    		calcResults.addAll(removeDataType(features.getColumn("numval")));
    		Dataset.deleteDataset(dataset);
    		monitor.worked(1);
    	}
    	
    	return calcResults;
    }

    public List<String> calculateDescriptor(
    		String service, String descriptor,
    		IMolecule molecule, IProgressMonitor monitor)
    throws Exception {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Calculate descriptor for molecule", 1);

    	List<String> calcResults = new ArrayList<String>();
    	logger.debug("Creating data set");
    	String dataset = Dataset.createNewDataset(service, molecule, monitor);
    	if (dataset == null) {
    		logger.error("Failed to generate a data set");
    		return calcResults;
    	}
    	logger.debug("Calculating descriptor");
    	if (monitor.isCanceled()) return Collections.emptyList();
    	String results = MolecularDescriptorAlgorithm.calculate(
    		service, descriptor, dataset, monitor
    	);
    	if (monitor.isCanceled()) return Collections.emptyList();
    	logger.debug("Listing features");
    	StringMatrix features = Dataset.listPredictedFeatures(results);
    	logger.debug("Pred: " + features);
    	calcResults.addAll(removeDataType(features.getColumn("numval")));
    	logger.debug("Deleting data set");
    	Dataset.deleteDataset(dataset);
    	monitor.worked(1);
    	
    	return calcResults;
    }

    public List<String> predictWithModel(String service, String model, List<IMolecule> molecules, IProgressMonitor monitor)
    throws Exception {
    	if (service == null) throw new BioclipseException("Service is null");
    	if (model == null) throw new BioclipseException("Model is null");

    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Calculate model for dataset", molecules.size());

    	List<String> calcResults = new ArrayList<String>();
    	for (IMolecule molecule : molecules) {
    		String dataset = Dataset.createNewDataset(service, molecule, monitor);
    		if (dataset == null) {
        		logger.error("Failed to generate a data set");
        		return calcResults;
        	}
        	if (monitor.isCanceled()) return calcResults;
    		String results = ModelAlgorithm.calculate(service, model, dataset, monitor);    		
        	if (monitor.isCanceled()) return calcResults;
    		StringMatrix features = Dataset.listPredictedFeatures(results);
    		calcResults.addAll(removeDataType(features.getColumn("numval")));
    		Dataset.deleteDataset(dataset);
    		monitor.worked(1);
    	}
    	
    	return calcResults;
    }
    
    public StringMatrix predictWithModelWithLabel(String service, String model,
    	List<IMolecule> molecules, IProgressMonitor monitor)
    throws Exception {
    	if (service == null) throw new BioclipseException("Service is null");
    	if (model == null) throw new BioclipseException("Model is null");

    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Calculate model for dataset", molecules.size());

    	StringMatrix calcResults = new StringMatrix();
    	calcResults.setSize(molecules.size(), 0);
    	int molCount = 0;
    	for (IMolecule molecule : molecules) {
    		molCount++;
    		String dataset = Dataset.createNewDataset(service, molecule, monitor);
    		if (dataset == null) {
        		logger.error("Failed to generate a data set");
        		return calcResults;
        	}
        	if (monitor.isCanceled()) return calcResults;
    		String results = ModelAlgorithm.calculate(service, model, dataset, monitor);
        	if (monitor.isCanceled()) return calcResults;
    		StringMatrix features = Dataset.listPredictedFeatures(results);
    		System.out.println("features: " + features);
    		List<String> fcol = removeDataType(features.getColumn("numval"));
    		List<String> lcol = features.getColumn("label");
    		for (int i=0; i<fcol.size(); i++){
    			String colName = lcol.get(i);
        		// ensure we have a matching column
    			int colCount = -1;
            	if (calcResults.hasColumn(colName)) {
            		colCount = calcResults.getColumnNumber(colName);
            	} else {
            		colCount = calcResults.getColumnCount() + 1;
            		calcResults.setColumnName(colCount, colName);
            	}
    			calcResults.set(molCount, colCount, fcol.get(i));
    		}
    		
    		Dataset.deleteDataset(dataset);
    		monitor.worked(1);
    	}
    	
    	return calcResults;
    }

    public List<String> predictWithModel(String service, String model,
    	IMolecule molecule, IProgressMonitor monitor)
    throws Exception {
    	if (service == null) throw new BioclipseException("Service is null");
    	if (model == null) throw new BioclipseException("Model is null");

    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Calculate model for molecule", 1);

    	List<String> calcResults = new ArrayList<String>();
    	String dataset = Dataset.createNewDataset(service, molecule, monitor);
    	if (dataset == null) {
    		logger.error("Failed to generate a data set");
    		return calcResults;
    	}
    	if (monitor.isCanceled()) return calcResults;
    	String results = ModelAlgorithm.calculate(service, model, dataset, monitor);
    	if (monitor.isCanceled()) return calcResults;
    	StringMatrix features = Dataset.listPredictedFeatures(results);
    	calcResults.addAll(removeDataType(features.getColumn("numval")));
    	Dataset.deleteDataset(dataset);
    	monitor.worked(1);
    	
    	return calcResults;
    }
    
    public Map<String,String> predictWithModelWithLabel(String service, String model,
    	IMolecule molecule, IProgressMonitor monitor)
    throws Exception {
    	if (service == null) throw new BioclipseException("Service is null");
    	if (model == null) throw new BioclipseException("Model is null");

    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Calculate model for molecule", 1);

    	Map<String,String> calcResults = new HashMap<String, String>();    	
    	String dataset = Dataset.createNewDataset(service, molecule, monitor);
    	if (dataset == null) {
    		logger.error("Failed to generate a data set");
    		return calcResults;
    	}
    	if (monitor.isCanceled()) return calcResults;
    	String results = ModelAlgorithm.calculate(service, model, dataset, monitor);
    	if (monitor.isCanceled()) return calcResults;
    	StringMatrix features = Dataset.listPredictedFeatures(results);
    	if (features.getRowCount() > 0) {
    		List<String> fcol = removeDataType(features.getColumn("numval"));
    		List<String> lcol = features.getColumn("label");
    		for (int i=0; i<lcol.size(); i++){
    			calcResults.put(lcol.get(i), fcol.get(i));
    		}
    	}

    	Dataset.deleteDataset(dataset);
    	monitor.worked(1);
    	
    	return calcResults;
    }

	private List<String> removeDataType(List<String> column) {
		List<String> cleanedData = new ArrayList<String>(column.size());
		for (String value : column) {
			if (value.contains("^^")) {
				value = value.substring(0, value.indexOf("^^"));
			}
			cleanedData.add(value);
		}
		return cleanedData;
	}
	
    public List<String> search(String service, IMolecule molecule) throws BioclipseException {
    	String inchi = cdk.asCDKMolecule(molecule).getInChI(
    		Property.USE_CACHED_OR_CALCULATED);
    	return search(service, inchi);
    }

	@SuppressWarnings("serial")
	public List<String> search(String service, String inchi) throws BioclipseException {
		try {
			URL searchURL = new URL(
				normalizeURI(service) + "query/compound/search/all?search=" +
				URLEncoder.encode(inchi, "UTF-8")
			);
			HttpClient client = new HttpClient();
			GetMethod method = new GetMethod(searchURL.toString());
			HttpMethodHelper.addMethodHeaders(method,
				new HashMap<String,String>() {{ put("Accept", "text/uri-list"); }}
			);
			client.executeMethod(method);

			List<String> compounds = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(
				new StringReader(method.getResponseBodyAsString())
			);
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) compounds.add(line);
			}
			reader.close();
			method.releaseConnection();
			return compounds;
		} catch (Exception exception) {
			throw new BioclipseException(
				"Error while creating request URI...",
				exception
			);
		}
	}

	public String createModel(
	    	String algoURI, String datasetURI, List<String> featureURIs,
	    	String predictionFeatureURI, IProgressMonitor monitor) throws BioclipseException {
    	if (monitor == null) monitor = new NullProgressMonitor();
    	monitor.beginTask("Creating a new model...", 1);

    	String modelURI;
		try {
			modelURI = ModelAlgorithm.createModel(
				algoURI, datasetURI, featureURIs, predictionFeatureURI, monitor);
	    	return modelURI;
		} catch (Exception exception) {
			throw new BioclipseException(
				"Error while creating a new prediction model: " + exception.getMessage(), exception
			);
		}
	}

	private static String normalizeURI(String datasetURI) {
		datasetURI = datasetURI.replaceAll("\\n", "");
		datasetURI = datasetURI.replaceAll("\\r", "");
		if (!datasetURI.endsWith("/")) datasetURI += "/";
		return datasetURI;
	}
}
