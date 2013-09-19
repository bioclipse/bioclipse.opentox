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

import java.util.List;
import java.util.Map;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.managers.business.IBioclipseManager;

@PublishedClass(
    value="Manager that maps the OpenTox API 1.1 to manager methods.",
    doi={"10.1186/1758-2946-2-7","10.1186/1756-0500-4-487"}
)
public interface IOpentoxManager extends IBioclipseManager {

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Returns the security token for the service."
    )
    public String getToken();

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Logs in on OpenTox. Returns true if the login worked.",
        params="String user, String password"
    )
    public boolean login(String user, String password) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Logs in on OpenTox using the authorized service specified in authService. " +
            "Returns true if the login worked. If you are allready logged in to another " +
            "authorized service you will be logged out from that.",
        params="String user, String password, String authService"
    )
    public boolean login(String user, String pass, String authService) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Logs out on OpenTox."
    )
    public void logout() throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary="Returns the adress to the authorization server or " +
        		"null if it cant be found, e.g. no user is logged in."
    )
    public String getAuthorizationServer();
    
    @Recorded
    @PublishedMethod(
        methodSummary="Sets the authorization server to the one in account " +
        		"settings and log in to it, if logged in to the user account. " +
        		"Else it is left empty."
    )
    public void resetAuthorizationServer();
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the predictive models available from the given service.",
        params="String ontologyServer"
    )
    public List<String> listModels(String ontologyServer) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Calculates a descriptor value for a set of molecules.",
        params="String service, String descriptor, List<? extends IMolecule> molecules"
    )
    public List<String> calculateDescriptor(String service, String descriptor, List<? extends IMolecule> molecules) throws Exception;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Calculates a descriptor value for a single molecule.",
        params="String service, String descriptor, IMolecule molecule"
    )
    public List<String> calculateDescriptor(String service, String descriptor, IMolecule molecule) throws Exception;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Predicts modeled properties for the given list of molecules.",
        params="String service, String model, List<? extends IMolecule> molecules"
    )
    public List<String> predictWithModel(String service, String model, List<? extends IMolecule> molecules);

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Predicts modeled properties for the given molecule.",
        params="String service, String model, IMolecule molecule"
    )
    public List<String> predictWithModel(String service, String model, IMolecule molecule) throws Exception;
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Predicts modeled properties for the given molecule.",
        params="String service, String model, List<? extends IMolecule> molecules"
    )
    public StringMatrix predictWithModelWithLabel(String service, String model, List<? extends IMolecule> molecules) throws Exception;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Predicts modeled properties for the given molecule.",
        params="String service, String model, IMolecule molecule"
    )
    public Map<String,String> predictWithModelWithLabel(String service, String model, IMolecule molecule);


    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the available information on the feature from the given OpenTox ontology server's SPARQL " +
            "end point.",
        params="String ontologyServer, String feature"
    )
    public Map<String,String> getFeatureInfo(String ontologyServer, String feature);
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the available information on the feature from the given OpenTox ontology server's SPARQL " +
            "end point.",
        params="String ontologyServer, List<String> features"
    )
    public Map<String,Map<String,String>> getFeatureInfo(String ontologyServer, List<String> features);
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the available information on the model from the given OpenTox ontology server's SPARQL " +
            "end point.",
        params="String ontologyServer, String model"
    )
    public Map<String,String> getModelInfo(String ontologyServer, String model);
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the available information on the models from the given OpenTox ontology server's SPARQL " +
            "end point.",
        params="String ontologyServer, List<String> models"
    )
    public Map<String,Map<String,String>> getModelInfo(String ontologyServer, List<String> models);
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the available information on the algorithm from the given OpenTox ontology server's SPARQL " +
            "end point.",
        params="String ontologyServer, String algorithm"
    )
    public Map<String,String> getAlgorithmInfo(String ontologyServer, String algorithm);
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the available information on the algorithm from the given OpenTox ontology server's SPARQL " +
            "end point.",
        params="String ontologyServer, List<String> algorithms"
    )
    public Map<String,Map<String,String>> getAlgorithmInfo(String ontologyServer, List<String> algorithms);
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the algorithms available from the given OpenTox ontology server's SPARQL " +
            "end point.",
        params="String ontologyServer"
    )
    public List<String> listAlgorithms(String ontologyServer) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the descriptors available from the given OpenTox ontology server's SPARQL " +
            "end point.",
        params="String ontologyServer"
    )
    public IStringMatrix listDescriptors(String ontologyServer) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the data sets available from the given service.",
        params="String service"
    )
    public List<String> listDataSets(String service) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Lists the features available from the given service.",
        params="String service"
    )
    public List<String> listFeatures(String service) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Search the data sets available from the given service that " +
            "match the given title search string.",
        params="String ontologyServer, String query"
    )
    public IStringMatrix searchDataSets(String ontologyServer, String query) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Search the descriptors available from the given service that " +
            "match the given title search string.",
        params="String ontologyServer, String query"
    )
    public IStringMatrix searchDescriptors(String ontologyServer, String query) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Search the models available from the given service that " +
            "match the given title search string.",
        params="String ontologyServer, String query"
    )
    public IStringMatrix searchModels(String ontologyServer, String query) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Creates a new dataset.",
        params="String service"
    )
    public String createDataset(String service) throws BioclipseException;
    public void createDataset(String service, BioclipseUIJob<String> uiJob) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Creates a new dataset.",
        params="String service, List<? extends IMolecule> molecules"
    )
    public String createDataset(String service, List<? extends IMolecule> molecules) throws BioclipseException;
    public void createDataset(String service, List<? extends IMolecule> molecules, BioclipseUIJob<String> uiJob) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Creates a new dataset.",
        params="String service, IMolecule molecule"
    )
    public String createDataset(String service, IMolecule molecule) throws BioclipseException;
    public void createDataset(String service, IMolecule molecule, BioclipseUIJob<String> uiJob) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Sets the license of the data set. The license String " +
        	"must be a URI.",
        params="String datasetURI, String license"
    )
    public String setDatasetLicense(String datasetURI, String license) throws Exception;
    public void setDatasetLicense(String datasetURI, String license, BioclipseUIJob<String> uiJob) throws Exception;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Sets the rights holder for the data set. The value " +
        	"must be a URI.",
        params="String datasetURI, String holder"
    )
    public String setDatasetRightsHolder(String datasetURI, String holder) throws Exception;
    public void setDatasetRightsHolder(String datasetURI, String holder, BioclipseUIJob<String> uiJob) throws Exception;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Sets the title of the data set.",
        params="String datasetURI, String title"
    )
    public String setDatasetTitle(String datasetURI, String title) throws Exception;
    public void setDatasetTitle(String datasetURI, String title, BioclipseUIJob<String> uiJob) throws Exception;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Adds a molecule to an existing dataset.",
        params="String datasetURI, IMolecule mol"
    )
    public void addMolecule(String datasetURI, IMolecule mol) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary="Adds a list of molecules to an existing dataset.",
        params="String datasetURI, List<? extends IMolecule> molecules"
    )
    public void addMolecules(String datasetURI, List<? extends IMolecule> molecules);
    
    @Recorded
    @PublishedMethod(
        methodSummary="Deletes a dataset.",
        params="String datasetURI"
    )
    public void deleteDataset(String datasetURI) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the compounds available from the given data set.",
        params="String service, Integer dataSet"
    )
    public List<Integer> listCompounds(String service, Integer dataSet) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the compounds available from the given data set.",
        params="String dataSet"
    )
    public List<String> listCompounds(String dataSet) throws BioclipseException;
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Downloads a compound and returns it as a MDL molfile formated " +
            "String.",
        params="String service, String dataSet, Integer compound"
    )
    public String downloadCompoundAsMDLMolfile(String service, String dataSet,
        Integer compound) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Downloads a compound and returns it as a MDL molfile formated " +
            "String.",
        params="String compoundURI"
    )
    public String downloadCompoundAsMDLMolfile(String compoundURI) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Downloads a data set and saves it as a MDL SD file formated " +
            "file with the given filename.",
        params="String service, String dataSet, String filename"
    )
    public String downloadDataSetAsMDLSDfile(String service, String dataSet,
        String filename) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Searches the OpenTox network for the given molecule.",
        params="String service, IMolecule molecule"
    )
    public List<String> search(String service, IMolecule molecule) throws BioclipseException;

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Searches the OpenTox network for the given molecule.",
        params="String service, String inchi"
    )
    public List<String> search(String service, String inchi) throws BioclipseException;

    @Recorded
    @PublishedMethod(
    	methodSummary="Creates a new regression model using the given algorithm, for the " +
    			"given data set, with the features as independent variables, and the " +
    			"prediction features as the dependent feature.",
    	params="String algoURI, String datasetURI, List<String> featureURIs, String predictionFeatureURI"
    )
    public String createModel(
    	String algoURI, String datasetURI, List<String> featureURIs, String predictionFeatureURI)
    throws BioclipseException;
}
