/* Copyright (C) 2010  Egon Willighagen <egonw@users.sf.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. All we ask is that proper credit is given for our work,
 * which includes - but is not limited to - adding the above copyright notice to
 * the beginning of your source code files, and to any copyright notice that you
 * may distribute with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.bioclipse.opentox.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.RDFManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.io.SDFWriter;

public class Dataset {

	private static final Logger logger = Logger.getLogger(Dataset.class);
	
    private final static String QUERY_PREDICTED_FEATURES =
        "SELECT ?desc ?label ?numval {" +
        "  ?entry a <http://www.opentox.org/api/1.1#DataEntry> ;" +
        "     <http://www.opentox.org/api/1.1#values> ?value ." +
        "  ?value <http://www.opentox.org/api/1.1#feature> ?feature;" +
        "     <http://www.opentox.org/api/1.1#value> ?numval ." +
        "  ?feature <http://www.opentox.org/api/1.1#hasSource> ?desc ." +
        "  ?feature <http://purl.org/dc/elements/1.1/title> ?label ." +
        "}";

	static CDKManager cdk = new CDKManager();
	static RDFManager rdf = new RDFManager();
	
	@SuppressWarnings("serial")
	public static List<String> getListOfAvailableDatasets(String service)
	throws IOException {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(normalizeURI(service) + "dataset");
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{ put("Accept", "text/uri-list"); }}
		);
		client.executeMethod(method);

		List<String> datasets = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(
			new StringReader(method.getResponseBodyAsString())
		);
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() > 0) datasets.add(line);
		}
		reader.close();
		method.releaseConnection();
		return datasets;
	}

	public static String normalizeURI(String datasetURI) {
		datasetURI = datasetURI.replaceAll("\\n", "");
		datasetURI = datasetURI.replaceAll("\\r", "");
		if (!datasetURI.endsWith("/")) datasetURI += "/";
		return datasetURI;
	}
	
	@SuppressWarnings("serial")
	public static List<String> getCompoundList(String datasetURI)
	throws IOException {
		HttpClient client = new HttpClient();
		datasetURI = normalizeURI(datasetURI);
		GetMethod method = new GetMethod(datasetURI + "compounds");
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
	}

	@SuppressWarnings("serial")
	public static StringMatrix listPredictedFeatures(String datasetURI)
	throws Exception {
		logger.debug("Listing features for: " + datasetURI);
		datasetURI = datasetURI.replaceAll("\n", "");
		if (datasetURI.contains("feature_uris[]=")) {
			String baseURI = datasetURI.substring(0, datasetURI.indexOf("feature_uris[]="));
			String featureURIs = datasetURI.substring(datasetURI.indexOf("feature_uris[]=")+15);
			featureURIs = URIUtil.decode(featureURIs);
			String fullURI = baseURI + "feature_uris[]=" + featureURIs;
			datasetURI = URIUtil.encodeQuery(fullURI);
		}
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(datasetURI);
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{ put("Accept", "application/rdf+xml"); }}
		);
		client.executeMethod(method);
		String result = method.getResponseBodyAsString(); // without this things will fail??
		IRDFStore store = rdf.createInMemoryStore();
		rdf.importFromStream(store, method.getResponseBodyAsStream(), "RDF/XML", null);
		method.releaseConnection();
		String dump = rdf.asRDFN3(store);
		StringMatrix matrix = rdf.sparql(store, QUERY_PREDICTED_FEATURES);
		return matrix;
	}

	public static void deleteDataset(String datasetURI)
	throws Exception {
		HttpClient client = new HttpClient();
		DeleteMethod method = new DeleteMethod(datasetURI);
		HttpMethodHelper.addMethodHeaders(method, null);
		client.executeMethod(method);
		int status = method.getStatusCode();
		method.releaseConnection();
		if (status == 404)
			throw new IllegalArgumentException(
				"Dataset does not exist."
			);
		if (status == 503)
			throw new IllegalStateException("Service error: " + status);
	}

	public static void addMolecule(String datasetURI, IMolecule mol)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		writer.write(cdk.asCDKMolecule(mol).getAtomContainer());
		writer.close();
		addMolecules(datasetURI, strWriter.toString(), null);
	}

	public static void addMolecules(String datasetURI, List<IMolecule> mols)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		for (IMolecule mol : mols) {
			writer.write(cdk.asCDKMolecule(mol).getAtomContainer());
		}
		writer.close();
		addMolecules(datasetURI, strWriter.toString(), null);
	}

	public static void setMetadata(String datasetURI, String predicate, String value)
	throws Exception {
		HttpClient client = new HttpClient();
		PutMethod method = new PutMethod(normalizeURI(datasetURI) + "metadata");
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{ put("Content-type", "text/n3"); }}
		);
		String triples =
			"<" + datasetURI +
			"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
			"<http://www.opentox.org/api/1.1#Dataset> .\n" +
			"<" + datasetURI +
			"> <" + predicate + "> " + value +
			" .";
		System.out.println("Triples:\n" + triples);
		method.setRequestBody(triples);
		client.executeMethod(method);
		int status = method.getStatusCode();
		if (status == 200) {
			// OK, that was quick!
			String response = method.getResponseBodyAsString();
			System.out.println("Set value response: " + response);
		} else if (status == 202) {
			// OK, we got a task... let's wait until it is done
			String task = method.getResponseBodyAsString();
			Thread.sleep(1000); // let's be friendly, and wait 1 sec
			TaskState state = Task.getState(task);
			while (!state.isFinished()) {
				Thread.sleep(3000); // let's be friendly, and wait 3 sec
				state = Task.getState(task);
				if (state.isRedirected()) {
					task = state.getResults();
				}
			}
			// OK, it should be finished now
			String dataset = state.getResults();
		} else {
			throw new BioclipseException("Status : " + status);
		}
		method.releaseConnection();
	}

	public static void setLicense(String datasetURI, String license)
	throws Exception {
		setMetadata(
			datasetURI,
			"http://purl.org/dc/terms/license",
			"<" + license + ">"
		);
	}

	public static void setRightsHolder(String datasetURI, String holder)
	throws Exception {
		setMetadata(
			datasetURI,
			"http://purl.org/dc/terms/rightsHolder",
			"<" + holder + ">"
		);
	}

	public static void setTitle(String datasetURI, String title)
	throws Exception {
		setMetadata(
			datasetURI,
			"http://purl.org/dc/elements/1.1/title",
			"\"" + title + "\""
		);
	}

	@SuppressWarnings("serial")
	public static void addMolecules(String datasetURI, String sdFile, IProgressMonitor monitor)
	throws Exception {
		if (monitor == null) monitor = new NullProgressMonitor();

		HttpClient client = new HttpClient();
		datasetURI = normalizeURI(datasetURI);
		PutMethod method = new PutMethod(datasetURI);
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{
				put("Accept", "text/uri-list");
				put("Content-type", "chemical/x-mdl-sdfile");
			}}
		);
		method.setRequestBody(sdFile);
		client.executeMethod(method);
		int status = method.getStatusCode();
		String dataset = "";
		String responseString = method.getResponseBodyAsString();
		logger.debug("Response: " + responseString);
		int tailing = 1;
		if (status == 200) {
			// OK, that was quick!
			dataset = method.getResponseBodyAsString();
			logger.debug("No Task, Data set: " + dataset);
		} else if (status == 202 || status == 201) {
			// OK, we got a task... let's wait until it is done
			String task = method.getResponseBodyAsString();
			Thread.sleep(1000); // let's be friendly, and wait 1 sec
			TaskState state = Task.getState(task);
			while (!state.isFinished() && !monitor.isCanceled()) {
				// let's be friendly, and wait 2 secs and a bit and increase
				// that time after each wait
				int waitingTime = andABit(2000*tailing);
				logger.debug("Waiting " + waitingTime + "ms.");
				waitUnlessInterrupted(waitingTime, monitor);
				state = Task.getState(task);
				if (state.isRedirected()) {
					task = state.getResults();
					logger.debug("  new task, new task!!: " + task);
				}
				// but wait at most 20 secs and a bit
				if (tailing < 10) tailing++;
			}
			if (monitor.isCanceled()) Task.delete(task);
			// OK, it should be finished now
			dataset = state.getResults();
		} else {
			logger.warn("Unexpected return code when adding molecules: " + status);
		}
		method.releaseConnection();
	}

	public static String createNewDataset(String service,
		List<IMolecule> molecules, IProgressMonitor monitor)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		for (IMolecule mol : molecules) {
			writer.write(cdk.asCDKMolecule(mol).getAtomContainer());
		}
		writer.close();
		return createNewDataset(normalizeURI(service), strWriter.toString(), monitor);
	}

	public static String createNewDataset(String service, IMolecule mol,
		IProgressMonitor monitor)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		writer.write(cdk.asCDKMolecule(mol).getAtomContainer());
		writer.close();
		return createNewDataset(service, strWriter.toString(), monitor);
	}

	public static String createNewDataset(String service, IProgressMonitor monitor)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		writer.write(new AtomContainer());
		writer.close();
		return createNewDataset(service, strWriter.toString(), monitor);
	}

	public static String createNewDataset(
		String service, String sdFile, IProgressMonitor monitor)
	throws Exception {
		if (monitor == null) monitor = new NullProgressMonitor();

		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(normalizeURI(service) + "dataset");
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{
				put("Accept", "text/uri-list");
				put("Content-type", "chemical/x-mdl-sdfile");
			}}
		);
		System.out.println("Method: " + method.toString());
		method.setRequestBody(sdFile);
		client.executeMethod(method);
		int status = method.getStatusCode();
		String dataset = "";
		String responseString = method.getResponseBodyAsString();
		logger.debug("Response: " + responseString);
		int tailing = 1;
		if (status == 200 || status == 201 || status == 202) {
			if (responseString.contains("/task/")) {
				logger.debug("Task: " + responseString);
				// OK, we got a task... let's wait until it is done
				String task = method.getResponseBodyAsString();
				Thread.sleep(1000); // let's be friendly, and wait 1 sec
				TaskState state = Task.getState(task);
				while (!state.isFinished() && !monitor.isCanceled()) {
					// let's be friendly, and wait 2 secs and a bit and increase
					// that time after each wait
					int waitingTime = andABit(2000*tailing);
					logger.debug("Waiting " + waitingTime + "ms.");
					waitUnlessInterrupted(waitingTime, monitor);
					state = Task.getState(task);
					if (state.isRedirected()) {
						task = state.getResults();
						logger.debug("  new task, new task!!: " + task);
					}
					// but wait at most 20 secs and a bit
					if (tailing < 10) tailing++;
				}
				if (monitor.isCanceled()) Task.delete(task);
				// OK, it should be finished now
				dataset = state.getResults();
			} else {
				// OK, that was quick!
				dataset = method.getResponseBodyAsString();
				logger.debug("No Task, Data set: " + dataset);
			}
		}
		method.releaseConnection();
		if (monitor.isCanceled()) return "";
		logger.debug("Data set: " + dataset);
		dataset = dataset.replaceAll("\n", "");
		return dataset;
	}

	private static void waitUnlessInterrupted(
		int waitingTime, IProgressMonitor monitor)
	throws InterruptedException {
		int passed = 0;
		final int step = 300; // every 0.3 sec check if the process is canceled 
		while (passed < waitingTime && !monitor.isCanceled()) {
			Thread.sleep(step);
			passed += step;
		}
	}

	private static int andABit(int minimum) {
		return (minimum + (int)Math.round(minimum*Math.random()));
	}

	public static void main(String[] args) throws Exception {
//		String service = "http://194.141.0.136:8080/";
		String service = "http://apps.ideaconsult.net:8080/ambit2/";
//		List<String> sets = getListOfAvailableDatasets(service);
//		for (String set : sets) System.out.println(set);
		String dataset = createNewDataset(service, null);
		List<IMolecule> mols = new ArrayList<IMolecule>();
		mols.add(cdk.fromSMILES("COC"));
		mols.add(cdk.fromSMILES("CNC"));
		mols.add(cdk.fromSMILES("CC"));
		addMolecules(dataset, mols);
//		deleteDataset(dataset);
	}

}
