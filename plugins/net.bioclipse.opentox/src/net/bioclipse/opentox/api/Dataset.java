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
import java.util.List;

import net.bioclipse.cdk.business.CDKManager;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.RDFManager;
import net.bioclipse.rdf.model.StringMatrix;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.io.SDFWriter;

public class Dataset {

    private final static String QUERY_PREDICTED_FEATURES =
        "SELECT ?desc ?numval WHERE {" +
        "  ?entry a <http://www.opentox.org/api/1.1#DataEntry> ;" +
        "     <http://www.opentox.org/api/1.1#values> ?value ." +
        "  ?value <http://www.opentox.org/api/1.1#feature> ?feature;" +
        "     <http://www.opentox.org/api/1.1#value> ?numval ." +
        "  ?feature <http://purl.org/dc/elements/1.1/creator> ?desc ." +
        "}";

	static CDKManager cdk = new CDKManager();
	static RDFManager rdf = new RDFManager();
	
	public static List<String> getListOfAvailableDatasets(String service)
	throws IOException {
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(service + "dataset");
		method.setRequestHeader("Accept", "text/uri-list");
		client.executeMethod(method);
		System.out.println(method.getResponseBodyAsString());
		method.releaseConnection();
		List<String> datasets = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(
			new StringReader(method.getResponseBodyAsString())
		);
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() > 0) datasets.add(line);
		}
		return datasets;
	}

	public static String normalizeURI(String datasetURI) {
		datasetURI = datasetURI.replaceAll("\\n", "");
		datasetURI = datasetURI.replaceAll("\\r", "");
		if (!datasetURI.endsWith("/")) datasetURI += "/";
		return datasetURI;
	}
	
	public static List<String> getCompoundList(String datasetURI)
	throws IOException {
		HttpClient client = new HttpClient();
		datasetURI = normalizeURI(datasetURI);
		HttpMethod method = new GetMethod(datasetURI + "compounds");
		method.setRequestHeader("Accept", "text/uri-list");
		client.executeMethod(method);
		System.out.println(method.getResponseBodyAsString());
		method.releaseConnection();
		List<String> compounds = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(
			new StringReader(method.getResponseBodyAsString())
		);
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() > 0) compounds.add(line);
		}
		return compounds;
	}

	public static StringMatrix listPredictedFeatures(String datasetURI)
	throws Exception {
		datasetURI = normalizeURI(datasetURI);
		HttpClient client = new HttpClient();
		String fullURI = datasetURI + "?feature_uris[]=http://apps.ideaconsult.net:8080/ambit2/model/9/predicted";
		System.out.println("full uri:" + fullURI);
		fullURI = URIUtil.encodeQuery(fullURI);
		System.out.println("full uri:" + fullURI);
		HttpMethod method = new GetMethod(fullURI);
		method.setRequestHeader("Accept", "application/rdf+xml");
		client.executeMethod(method);
		System.out.println(method.getResponseBodyAsString());
		method.releaseConnection();
		IRDFStore store = rdf.createInMemoryStore();
		rdf.importFromStream(store, method.getResponseBodyAsStream(), "RDF/XML", null);
		System.out.println(rdf.asRDFN3(store));
		return rdf.sparql(store, QUERY_PREDICTED_FEATURES);
	}

	public static void deleteDataset(String datasetURI)
	throws Exception {
		HttpClient client = new HttpClient();
		HttpMethod method = new DeleteMethod(datasetURI);
		client.executeMethod(method);
		int status = method.getStatusCode();
		System.out.println(status);
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
		addMolecules(datasetURI, strWriter.toString());
	}

	public static void addMolecules(String datasetURI, List<IMolecule> mols)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		for (IMolecule mol : mols) {
			writer.write(cdk.asCDKMolecule(mol).getAtomContainer());
		}
		writer.close();
		addMolecules(datasetURI, strWriter.toString());
	}

	public static void addMolecules(String datasetURI, String sdFile)
	throws Exception {
		HttpClient client = new HttpClient();
		datasetURI = normalizeURI(datasetURI);
		PutMethod method = new PutMethod(datasetURI);
		method.setRequestHeader("Accept", "text/uri-list");
		method.setRequestHeader("Content-type", "chemical/x-mdl-sdfile");
		method.setRequestBody(sdFile);
		client.executeMethod(method);
		int status = method.getStatusCode();
		System.out.println(status);
		String dataset = "";
		if (status == 200) {
			// OK, that was quick!
			dataset = method.getResponseBodyAsString();
		} else if (status == 202) {
			// OK, we got a task... let's wait until it is done
			String task = method.getResponseBodyAsString();
			Thread.sleep(1000); // let's be friendly, and wait 1 sec
			TaskState state = Task.getState(task);
			while (!state.isFinished()) {
				System.out.println("Waiting to finish...");
				Thread.sleep(3000); // let's be friendly, and wait 3 sec
				state = Task.getState(task);
				if (state.isRedirected()) {
					task = state.getResults();
					System.out.println("Got redirected to: " + task);
				}
			}
			// OK, it should be finished now
			dataset = state.getResults();
		}
		System.out.println("Data set: " + dataset);
		method.releaseConnection();
	}

	public static String createNewDataset(String service, List<IMolecule> molecules)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		for (IMolecule mol : molecules) {
			writer.write(cdk.asCDKMolecule(mol).getAtomContainer());
		}
		writer.close();
		return createNewDataset(service, strWriter.toString());
	}

	public static String createNewDataset(String service, IMolecule mol)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		writer.write(cdk.asCDKMolecule(mol).getAtomContainer());
		writer.close();
		return createNewDataset(service, strWriter.toString());
	}

	public static String createNewDataset(String service)
	throws Exception {
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		writer.write(new AtomContainer());
		writer.close();
		return createNewDataset(service, strWriter.toString());
	}

	public static String createNewDataset(String service, String sdFile)
	throws Exception {
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(service + "dataset");
		method.setRequestHeader("Accept", "text/uri-list");
		method.setRequestHeader("Content-type", "chemical/x-mdl-sdfile");
		method.setRequestBody(sdFile);
		client.executeMethod(method);
		int status = method.getStatusCode();
		System.out.println(status);
		String dataset = "";
		String responseString = method.getResponseBodyAsString();
		if (status == 200 || status == 202) {
			if (responseString.contains("/task/")) {
				// OK, we got a task... let's wait until it is done
				String task = method.getResponseBodyAsString();
				Thread.sleep(1000); // let's be friendly, and wait 1 sec
				TaskState state = Task.getState(task);
				while (!state.isFinished()) {
					System.out.println("Waiting to finish...");
					Thread.sleep(3000); // let's be friendly, and wait 3 sec
					state = Task.getState(task);
					if (state.isRedirected()) {
						task = state.getResults();
						System.out.println("Got redirected to: " + task);
					}
				}
				// OK, it should be finished now
				dataset = state.getResults();
			} else {
				// OK, that was quick!
				dataset = method.getResponseBodyAsString();
			}
		}
		System.out.println("Data set: " + dataset);
		method.releaseConnection();
		return dataset;
	}

	public static void main(String[] args) throws Exception {
//		String service = "http://194.141.0.136:8080/";
		String service = "http://apps.ideaconsult.net:8080/ambit2/";
//		List<String> sets = getListOfAvailableDatasets(service);
//		for (String set : sets) System.out.println(set);
		String dataset = createNewDataset(service);
		List<IMolecule> mols = new ArrayList<IMolecule>();
		mols.add(cdk.fromSMILES("COC"));
		mols.add(cdk.fromSMILES("CNC"));
		mols.add(cdk.fromSMILES("CC"));
		addMolecules(dataset, mols);
//		deleteDataset(dataset);
	}

}
