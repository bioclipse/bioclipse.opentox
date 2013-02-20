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
import net.bioclipse.opentox.OpenToxConstants;
import net.bioclipse.opentox.OpenToxLogInOutListener;

import org.apache.commons.httpclient.HttpMethodBase;

public class HttpMethodHelper {

	public static HttpMethodBase addMethodHeaders(HttpMethodBase method,
			Map<String, String> extraHeaders) {
	    // set the time out
	    method.getParams().setParameter(
	    	"http.socket.timeout", 
	        Activator.getDefault().getPreferenceStore().getInt(OpenToxConstants.HTTP_TIMEOUT) * 1000
	    );
	    OpenToxLogInOutListener openToxLogInOutListener;
	    try {
	        openToxLogInOutListener = OpenToxLogInOutListener.getInstance();

	        // log in on OpenTox if needed...
	        if (openToxLogInOutListener.getToken() != null) {
	            method.setRequestHeader("subjectid", 
	                                    openToxLogInOutListener.getToken());
	        }
	        // add other headers
	        if (extraHeaders != null) {
	            for (String header : extraHeaders.keySet()) {
	                method.setRequestHeader( header, extraHeaders.get(header) );
	            }
	        }
	        return method;
	    } catch ( InstantiationException e ) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }

        return null;
	}

}
