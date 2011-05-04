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

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

public abstract class ModelAlgorithm extends Algorithm {

	private static final Logger logger = Logger.getLogger(ModelAlgorithm.class);

	public static String calculate(String service, String model, String dataSetURI)
	throws HttpException, IOException, InterruptedException {
		HttpClient client = new HttpClient();
		dataSetURI = Dataset.normalizeURI(dataSetURI);
		PostMethod method = new PostMethod(model);
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{ put("Accept", "text/uri-list"); }}
		);
		method.setParameter("dataset_uri", dataSetURI);
		method.setParameter("dataset_service", service + "dataset");
		client.executeMethod(method);
		int status = method.getStatusCode();
		String dataset = "";
		// FIXME: I should really start using the RDF response...
		String responseString = method.getResponseBodyAsString();
		logger.debug("Status: " + status);
		if (status == 200 || status == 202) {
			if (responseString.contains("/task/")) {
				// OK, we got a task... let's wait until it is done
				String task = responseString;
				logger.debug("response: " + task);
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
				dataset = state.getResults();
			} else {
				// OK, that was quick!
				dataset = responseString;
				logger.debug("No Task, Data set: " + dataset);
			}
		} else {
			throw new IllegalStateException("Service error: " + status);
		}
		method.releaseConnection();
		dataset = dataset.replaceAll("\n", "");
		return dataset;
	}

}
