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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.io.SDFWriter;

public class Dataset {

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

	public static void deleteDataset(String dataset)
	throws Exception {
		HttpClient client = new HttpClient();
		HttpMethod method = new DeleteMethod(dataset);
		client.executeMethod(method);
		int status = method.getStatusCode();
		System.out.println(status);
		if (status == 404)
			throw new IllegalArgumentException(
				"Dataset does not exist."
			);
		if (status == 503)
			throw new IllegalStateException("Service error: " + status);
		method.releaseConnection();
	}

	public static String createNewDataset(String service)
	throws Exception {
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(service + "dataset");
		method.setRequestHeader("Accept", "text/uri-list");
		method.setRequestHeader("Content-type", "chemical/x-mdl-sdfile");
		StringWriter strWriter = new StringWriter();
		SDFWriter writer = new SDFWriter(strWriter);
		writer.write(new AtomContainer());
		writer.close();
		method.setRequestBody(strWriter.toString());
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
		return dataset;
	}

	public static void main(String[] args) throws Exception {
		String service = "http://194.141.0.136:8080/";
		List<String> sets = getListOfAvailableDatasets(service);
//		for (String set : sets) System.out.println(set);
		String dataset = createNewDataset(service);
		deleteDataset(dataset);
	}

}
