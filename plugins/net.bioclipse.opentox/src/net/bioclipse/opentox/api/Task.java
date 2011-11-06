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
import java.security.GeneralSecurityException;
import java.util.HashMap;

import net.bioclipse.opentox.Activator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

public class Task {

	private static final Logger logger = Logger.getLogger(Task.class);

	public static void delete(String task) throws IOException, GeneralSecurityException {
		HttpClient client = new HttpClient();
		DeleteMethod method = new DeleteMethod(task);
		method.getParams().setParameter("http.socket.timeout", new Integer(Activator.TIME_OUT));
		client.executeMethod(method);
		int status = method.getStatusCode();
		switch (status) {
			case 200:
				// excellent, it worked
				break;
			case 401:
				throw new GeneralSecurityException("Not authorized");
			case 404:
				// not found, well, I guess equals 'deleted'
				break;
			case 503:
				throw new IOException("Service unavailable");
			default:
				throw new IOException("Unknown server state: " + status);
		}
	}

	public static TaskState getState(String task)
	throws IOException {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(task);
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{ put("Accept", "text/uri-list"); }}
		);
		method.getParams().setParameter("http.socket.timeout", new Integer(Activator.TIME_OUT));
		method.setRequestHeader("Accept", "text/uri-list");
		client.executeMethod(method);
		int status = method.getStatusCode();
		logger.debug("Task status: " + status);
		
		TaskState state = new TaskState();
		logger.debug("Task: " + task);
		logger.debug(" -> " + status);
		switch (status) {
		case 404:
			logger.error("Task gone missing (404): " + task);
			state.setExists(false);
			break;
		case 200:
			String result = method.getResponseBodyAsString();
			if (result == null || result.length() == 0)
				throw new IOException("Missing dataset URI for finished (200) Task.");
			state.setFinished(true);
			state.setResults(result);
			break;
		case 201:
			state.setFinished(true);
			state.setRedirected(true);
			state.setResults(method.getResponseBodyAsString());
			break;
		case 202:
			state.setFinished(false);
			break;
		default:
			logger.error("Task error (" + status + "): " + task);
			throw new IllegalStateException(
				"Service error: " + status + ":\n  " +
				method.getStatusText()
			);
		}
		
		method.releaseConnection();
		return state;
	}

	public static void main(String[] args) throws Exception {
		String task = "http://apps.ideaconsult.net:8080/ambit2/task/1";
		// getListOfAvailableDatasets(service);
		TaskState state = getState(task);
	}

}
