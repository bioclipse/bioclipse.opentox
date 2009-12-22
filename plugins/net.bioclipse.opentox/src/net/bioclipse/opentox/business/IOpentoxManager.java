/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.opentox.business;

import java.util.List;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.managers.business.IBioclipseManager;

@PublishedClass(
    value="Manager that maps the OpenTox API 1.1 to manager methods."
)
public interface IOpentoxManager extends IBioclipseManager {

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the data sets available from the given service.",
        params="String service"
    )
    public List<Integer> listDataSets(String service);
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Lists the compounds available from the given data set.",
        params="String service, Integer dataSet"
    )
    public List<Integer> listCompounds(String service, Integer dataSet);
    
    @Recorded
    @PublishedMethod(
        methodSummary=
            "Downloads a compound and returns it as a MDL molfile formated " +
            "String.",
        params="String service, Integer dataSet, Integer compound"
    )
    public String downloadCompoundAsMDLMolfile(String service, Integer dataSet,
        Integer compound);

    @Recorded
    @PublishedMethod(
        methodSummary=
            "Downloads a data set and saves it as a MDL SD file formated " +
            "file with the given filename.",
        params="String service, Integer dataSet, String filename"
    )
    public String downloadDataSetAsMDLSDfile(String service, Integer dataSet,
        String filename);

}
