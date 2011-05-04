/* Copyright (C) 2011  Egon Willighagen <egonw@users.sf.net>
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

import java.util.Map;

import net.bioclipse.opentox.Activator;

import org.apache.commons.httpclient.HttpMethodBase;

public class HttpMethodHelper {

	public static HttpMethodBase addMethodHeaders(HttpMethodBase method,
			Map<String, String> extraHeaders) {
		// set the time out
		method.getParams().setParameter(
			"http.socket.timeout", new Integer(Activator.TIME_OUT)
		);
		// log in on OpenTox if needed...
		if (Activator.getToken() != null) {
        	method.setRequestHeader("subjectid", Activator.getToken());
        }
		// add other headers
		if (extraHeaders != null) {
        	for (String header : extraHeaders.keySet()) {
        		method.setRequestHeader(
        			header, extraHeaders.get(header)
        		);
        	}
        }
		return method;
	}

}
