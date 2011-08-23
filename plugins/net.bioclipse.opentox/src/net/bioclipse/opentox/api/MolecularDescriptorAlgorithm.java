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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public abstract class MolecularDescriptorAlgorithm extends Algorithm {

	private static final Logger logger = Logger.getLogger(MolecularDescriptorAlgorithm.class);

	public static String calculate(String service, String descriptor, 
		String dataSetURI, IProgressMonitor monitor)
	throws HttpException, IOException, InterruptedException {
		if (monitor == null) monitor = new NullProgressMonitor();

		HttpClient client = new HttpClient();
		dataSetURI = Dataset.normalizeURI(dataSetURI);
		PostMethod method = new PostMethod(descriptor);
		
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{ put("Accept", "text/uri-list"); }}
		);
		method.setParameter("dataset_uri", dataSetURI);
		method.setParameter("dataset_service", service + "dataset");
		logger.debug("Calculating: " + descriptor);
		logger.debug("  with data set: " + dataSetURI);
		client.executeMethod(method);
		int status = method.getStatusCode();
		logger.debug("  -> return status: " + status);
		String dataset = "";
		// FIXME: I should really start using the RDF response...
		String responseString = method.getResponseBodyAsString();
		int tailing = 1;
		if (status == 200 || status == 202) {
			if (responseString.contains("/task/")) {
				// OK, we got a task... let's wait until it is done
				String task = responseString;
				logger.debug("OK, we got a task assigned: " + task);
				Thread.sleep(andABit(500)); // let's be friendly, and wait 1 sec
				TaskState state = Task.getState(task);
				while (!state.isFinished() && !monitor.isCanceled()) {
					// let's be friendly, and wait 2 secs and a bit and increase
					// that time after each wait
					int waitingTime = andABit(2000*tailing);
					logger.debug("Waiting " + waitingTime + "ms.");
					Thread.sleep(waitingTime);
					state = Task.getState(task);
					if (state.isRedirected()) {
						task = state.getResults();
						logger.debug("  new task, new task!!: " + task);
					}
					// but wait at most 20 secs and a bit
					if (tailing < 10) tailing++;
				}
				// OK, it should be finished now
				dataset = state.getResults();
			} else {
				// OK, that was quick!
				dataset = responseString;
			}
		} else {
			throw new IllegalStateException("Service error: " + status);
		}
		method.releaseConnection();
		dataset = dataset.replaceAll("\n", "");
		return dataset;
	}

	private static int andABit(int minimum) {
		return (minimum + (int)Math.round(minimum*Math.random()));
	}
}
