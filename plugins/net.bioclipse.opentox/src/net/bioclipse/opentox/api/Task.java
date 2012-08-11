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
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.opentox.Activator;
import net.bioclipse.opentox.api.TaskState.STATUS;
import net.bioclipse.rdf.business.IRDFStore;
import net.bioclipse.rdf.business.RDFManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

public class Task {

	private static final Logger logger = Logger.getLogger(Task.class);
	
	private static RDFManager rdf = new RDFManager();
	
	private final static String QUERY_TASK_DETAILS =
        "PREFIX ot: <http://www.opentox.org/api/1.1#>" +
        "" +
        "SELECT * WHERE {" +
        "  ?task ot:hasStatus ?status ." +
        "  OPTIONAL { ?task ot:percentageCompleted ?completed }" +
        "  OPTIONAL { ?task ot:resultURI ?result }" +
        "}";

	private final static String QUERY_ERROR_REPORT =
        "PREFIX ot: <http://www.opentox.org/api/1.1#>" +
        "" +
        "SELECT ?task ?message WHERE {" +
        "  ?task ot:errorReport ?report ." +
        "  ?report ot:message ?message ." +
        "}";

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

	@SuppressWarnings("serial")
	public static TaskState getState(String task)
	throws IOException {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(task);
		HttpMethodHelper.addMethodHeaders(method,
			new HashMap<String,String>() {{ put("Accept", "application/rdf+xml"); }}
		);
		method.getParams().setParameter("http.socket.timeout", new Integer(Activator.TIME_OUT));
		method.setRequestHeader("Accept", "application/rdf+xml");
		client.executeMethod(method);
		int status = method.getStatusCode();
		logger.debug("Task status: " + status);
		
		TaskState state = new TaskState();
		logger.debug("Task: " + task);
		logger.debug(" -> " + status);
		InputStream result = method.getResponseBodyAsStream();
		// logger.debug("RDF: " + result);
		switch (status) {
		case 404:
			logger.error("Task gone missing (404): " + task);
			state.setExists(false);
			break;
		case 200:
			if (result == null)
				throw new IOException("Missing dataset URI for finished (200) Task: " + task);
			state.setFinished(true);
			state.setResults(getResultSetURI(createStore(result)));
			break;
		case 201:
			state.setFinished(true);
			state.setRedirected(true);
			state.setResults(getResultSetURI(createStore(result)));
			break;
		case 202:
			state.setFinished(false);
			state.setPercentageCompleted(getPercentageCompleted(createStore(result)));
			break;
		case 500:
			state.setFinished(true);
			state.setStatus(STATUS.ERROR);
			IRDFStore store = createStore(result);
			try {
				logger.debug("RDF: " + rdf.asRDFN3(store));
			} catch (BioclipseException e) {}
			String error = getErrorMessage(store);
			throw new IllegalStateException(
				"Service error (500) for " + task + ": " + error
			);
		default:
			logger.error("Task error (" + status + "): " + task);
			logger.debug("Response: " + result);
			throw new IllegalStateException(
				"Service error: " + status + ":\n  " +
				method.getStatusText()
			);
		}
		
		method.releaseConnection();
		return state;
	}
	
	private static String getErrorMessage(IRDFStore store) {
		try {
			StringMatrix matrix = rdf.sparql(store, QUERY_ERROR_REPORT);
			logger.debug("SPARQL results (error): " + matrix);
			if (matrix != null && matrix.getRowCount() != 0 &&
			    matrix.hasColumn("message")) {
				String message = matrix.get(1, "message"); 
				if (message.contains("^^"))
					message = message.substring(0, message.lastIndexOf("^^"));
				return message;
			}
		} catch (Exception e) {
			logger.debug("Error while getting the error message: " + e.getMessage());
		}
		return "unknown error";
	}

	private static float getPercentageCompleted(IRDFStore store) {
		try {
			StringMatrix matrix = rdf.sparql(store, QUERY_TASK_DETAILS);
			if (matrix != null && matrix.getRowCount() != 0 &&
			    matrix.hasColumn("completed")) {
				String floatStr = matrix.get(1, "completed"); 
				if (floatStr.contains("^^"))
					floatStr = floatStr.substring(0, floatStr.indexOf("^^"));
				logger.debug("Found completed: " + floatStr);
				return Float.parseFloat(floatStr);
			} else {
				return 0.0f;
			}
		} catch (Exception e) {
			logger.debug("Error while getting the percentage: " + e.getMessage());
		}
		return 0.0f;
	}

	private static IRDFStore createStore(InputStream rdfResults) {
		IRDFStore store = rdf.createInMemoryStore();
		try {
			return rdf.importFromStream(store, rdfResults, "RDF/XML", null);
		} catch (Exception e) {
			logger.debug("Error while creating RDF from String: " + e.getMessage());
			logger.debug(e);
		}
		throw new IllegalStateException(
			"Service error: unexpected RDF content:\n" + rdfResults
		);
	}
	
	private static String getResultSetURI(IRDFStore store) {
		try {
			StringMatrix matrix = rdf.sparql(store, QUERY_TASK_DETAILS);
			if (matrix != null && matrix.getRowCount() != 0 &&
				matrix.hasColumn("result")) {
				String uri = matrix.get(1, "result"); 
				if (uri.contains("^^")) uri = uri.substring(0, uri.indexOf("^^"));
				logger.debug("Found uri: " + uri);
				return uri;
			}
		} catch (Exception e) {
			logger.debug("Error while getting the result set URI: " + e.getMessage());
			logger.debug(e);
		}
		throw new IllegalStateException(
			"Service error: missing result URI"
		);
	}

}
