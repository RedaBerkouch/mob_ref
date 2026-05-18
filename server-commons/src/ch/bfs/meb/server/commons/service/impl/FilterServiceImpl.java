/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: FilterServiceImpl.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.FilterListResult;
import ch.bfs.meb.server.commons.integration.dto.FilterResult;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.util.CodegroupUtility;

@Service
public class FilterServiceImpl implements IFilterService {
    protected static final String FILTER_OBJECT_EMPTY_MESSAGE = "filter.objectempty.message";
    protected static final String FILTER_AUTHORISATION_EMPTY_MESSAGE = "filter.authorisationempty.message";
    protected static final String FILTER_SOURCE_EMPTY_MESSAGE = "filter.sourceempty.message";

    IFilterServiceProvider _filterServiceProvider;

    public void setFilterServiceProvider(IFilterServiceProvider filterServiceProvider) {
        _filterServiceProvider = filterServiceProvider;
    }

    @Transactional(readOnly = true)
    public FilterListResult getFilters() {
        List<Filter> filters = _filterServiceProvider.getFilters();
        List<Filter> externalFilters = new ArrayList<Filter>();

        // Remove internal parameters
        for (Filter filter : filters) {
            // Clone Filter so that parameters of peristent filter are not changed
            Filter externalFilter = new Filter(filter);
            List<Parameter> parameters = filter.getParameters();
            // Remove internal params
            List<Parameter> externalParams = new ArrayList<Parameter>();
            for (Parameter param : parameters) {
                if (!(param.getDefaultValue() != null && param.getDefaultValue().equals(CodegroupUtility.MEB_PARAM_LANGUAGE_NAME))) {

                    externalParams.add(param);
                }
            }
            externalFilter.setParameters(externalParams);
            externalFilters.add(externalFilter);
        }

        return new FilterListResult(externalFilters);
    }

    @Transactional(readOnly = true)
    public FilterListResult getFiltersForRefObjectAndNameDe(Long refObject, String nameDe) {
        return new FilterListResult(_filterServiceProvider.getFiltersForRefObjectAndNameDe(refObject, nameDe));
    }

    @Transactional(readOnly = true)
    public FilterListResult getFiltersForRefObject(Long refObject) {
        return new FilterListResult(_filterServiceProvider.getFiltersForRefObject(refObject));
    }

    @Transactional(readOnly = true)
    public FilterResult getFilterById(Long filterId) {
        Filter filter = _filterServiceProvider.getFilterById(filterId);
        if (filter == null) {
            return new FilterResult("Could not find filter with id: " + filterId);
        } else {
            return new FilterResult(filter);
        }
    }

    protected String checkFilter(Filter filter) {
        if (filter.getRefObject() == null) {
            return FILTER_OBJECT_EMPTY_MESSAGE;
        }
        if (filter.getAuthorisationLevel() == null) {
            return FILTER_AUTHORISATION_EMPTY_MESSAGE;
        }
        if (filter.getSource() == null || filter.getSource().trim().equals("")) {
            return FILTER_SOURCE_EMPTY_MESSAGE;
        }
        return null;
    }

    @Transactional
    public FilterResult updateFilter(Filter filter) {
        String message = checkFilter(filter);
        if (message != null) {
            return new FilterResult(message);
        }
        return new FilterResult(_filterServiceProvider.updateFilter(filter));
    }

    @Transactional
    public FilterResult insertFilter(Filter filter) {
        String message = checkFilter(filter);
        if (message != null) {
            return new FilterResult(message);
        }
        filter.setIsActive(true);
        return new FilterResult(_filterServiceProvider.insertFilter(filter));
    }

    @Transactional
    public FilterResult deleteFilter(Filter filter) {
        _filterServiceProvider.deleteFilter(filter);
        return new FilterResult();
    }
}
