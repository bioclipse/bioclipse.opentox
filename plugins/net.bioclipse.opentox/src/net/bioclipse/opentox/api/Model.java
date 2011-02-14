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

import java.util.HashMap;
import java.util.Map;

import net.bioclipse.core.domain.IStringMatrix;
import net.bioclipse.rdf.business.RDFManager;

public abstract class Model {

	private static RDFManager rdf = new RDFManager();

	public static Map<String,String> getProperties(String ontologyServer, String feature) {
		String propertiesQuery =
			"select ?pred ?value where {" +
			"  <" + feature + "> ?pred ?value" +
			"}";
		Map<String,String> properties = new HashMap<String, String>();
		IStringMatrix matrix = rdf.sparqlRemote(ontologyServer, propertiesQuery , null);
		for (int i=0; i<matrix.getRowCount(); i++) {
			String predicate = matrix.get(i, "pred");
			String value = matrix.get(i, "value");
			if (predicate != null && predicate.length() > 0 &&
				value != null && value.length() > 0)
				properties.put(predicate, value);
		}
		return properties;
	}

}
