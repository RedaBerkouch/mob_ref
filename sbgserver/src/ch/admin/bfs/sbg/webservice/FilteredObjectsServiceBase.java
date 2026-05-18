/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: FilteredObjectsServiceBase.java 521 2008-08-26 12:39:47Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.webservice;

import java.util.ArrayList;
import java.util.List;

import ch.admin.bfs.sbg.transfer.FilterContext;
import ch.bfs.meb.sbg.server.integration.repository.IFilterRepository;
import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.service.impl.IFilterService;
import ch.bfs.meb.util.CodegroupUtility;
import lombok.Setter;

/**
 * Base class for services using filters
 *
 * @author $Author: lsc $
 * @version $Revision: 521 $
 */
public class FilteredObjectsServiceBase {
    @Setter
    protected IFilterService filterService;

    protected void completeFilterParams(IFilterRepository filterRepository, FilterContext filterContext) {
        if (filterContext == null || filterContext.getFilter() == null) {
            return;
        }
        for (Filter filter : filterContext.getFilter()) {
            Filter loadedFilter = filterRepository.getFilterById(filter.getFilterId());
            if (loadedFilter != null) {
                // Clone the parameters so that replaced values don't get saved
                Parameter newParam;
                List<Parameter> newParams = new ArrayList<Parameter>();

                for (Parameter loadedParam : loadedFilter.getParameters()) {
                    boolean isSet = false;
                    // Work on cloned newParam
                    newParam = new Parameter();
                    newParams.add(newParam);
                    newParam.setUniqueName(loadedParam.getUniqueName());
                    newParam.setDefaultValue(loadedParam.getDefaultValue());

                    for (Parameter param : filter.getParameters()) {
                        if (param.getUniqueName().equals(newParam.getUniqueName())) {
                            // take the value from exportMacro
                            newParam.setDefaultValue(param.getDefaultValue());
                            isSet = true;
                        }
                    }
                    if (!isSet) {
                        // not given from presentation layer macro -> set
                        // internal SBG parameter
                        if (newParam.getDefaultValue().equals(CodegroupUtility.SBG_PARAM_LANGUAGE_NAME)) {
                            newParam.setDefaultValue(filterContext.getLocale());
                        }
                    }
                }
                filter.setParameters(newParams);
            }
        }
    }
}