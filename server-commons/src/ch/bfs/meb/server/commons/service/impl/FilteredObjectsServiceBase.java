/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: FilteredObjectsServiceBase.java  24.02.2010 09:45:20 msc $

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.ArrayList;
import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.util.CodegroupUtility;

public class FilteredObjectsServiceBase {
    protected IFilterServiceProvider _filterServiceProvider;

    public void setFilterServiceProvider(IFilterServiceProvider filterServiceProvider) {
        _filterServiceProvider = filterServiceProvider;
    }

    protected void completeFilterParams(FilterContext filterContext) {
        if (filterContext == null || filterContext.getFilter() == null) {
            return;
        }

        for (Filter filter : filterContext.getFilter()) {
            Filter loadedFilter = _filterServiceProvider.getFilterById(filter.getFilterId());

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
                            newParam.setDefaultValue(param.getDefaultValue());
                            isSet = true;
                        }
                    }
                    if (!isSet) {
                        // not given from presentation layer -> set internal MEB parameter
                        if (newParam.getDefaultValue().equals(CodegroupUtility.MEB_PARAM_LANGUAGE_NAME)) {
                            newParam.setDefaultValue(filterContext.getLocale());
                        }
                    }
                }

                filter.setParameters(newParams);
            }
        }
    }
}
