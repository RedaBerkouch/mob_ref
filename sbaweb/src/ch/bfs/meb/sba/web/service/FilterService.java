/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: FilterService.java 590 2010-02-02 12:33:53Z jfu $
 */
package ch.bfs.meb.sba.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.bfs.meb.sba.web.frontend.dto.SearchRequest;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sba.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sba.web.ws.sbafilter.Filter;
import ch.bfs.meb.sba.web.ws.sbafilter.FilterListResult;
import ch.bfs.meb.sba.web.ws.sbafilter.FilterResult;
import ch.bfs.meb.sba.web.ws.sbafilter.Parameter;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebParameter;
import ch.bfs.meb.web.commons.util.IFilterService;
import org.springframework.util.CollectionUtils;

/**
 * Beispielservice fuer Testzwecke
 * 
 */
@Service("filterService")
public class FilterService implements IFilterService {

    @Autowired
    private WebServiceClientFactory webServiceClientFactory;
    @Autowired
    private WebLocalizationManager localizationManager;

    private WebParameter convertToWebParameter(Parameter parameter) {
        WebParameter webParam = new WebParameter();
        webParam.setParameterId(parameter.getParameterId());
        webParam.setFilterId(parameter.getFilterId());
        webParam.setPlausiId(parameter.getPlausiId());
        webParam.setExportId(parameter.getExportId());
        webParam.setUniqueName(parameter.getUniqueName());
        webParam.setNameDe(parameter.getNameDe());
        webParam.setNameFr(parameter.getNameFr());
        webParam.setNameIt(parameter.getNameIt());
        webParam.setDefaultValue(parameter.getDefaultValue());
        webParam.setParameterOrder(parameter.getParameterOrder());
        return webParam;
    }

    private Parameter convertToParameter(WebParameter webParameter) {
        Parameter parameter = new Parameter();
        parameter.setParameterId(webParameter.getParameterId());
        parameter.setFilterId(webParameter.getFilterId());
        parameter.setPlausiId(webParameter.getPlausiId());
        parameter.setExportId(webParameter.getExportId());
        parameter.setUniqueName(webParameter.getUniqueName());
        parameter.setNameDe(webParameter.getNameDe());
        parameter.setNameFr(webParameter.getNameFr());
        parameter.setNameIt(webParameter.getNameIt());
        parameter.setDefaultValue(webParameter.getDefaultValue());
        parameter.setParameterOrder(webParameter.getParameterOrder());
        return parameter;
    }

    private Filter convertToFilter(WebFilter webFilter) {
        Filter filter = new Filter();
        filter.setFilterId(webFilter.getFilterId());
        filter.setDescriptionDe(webFilter.getDescriptionDe());
        filter.setDescriptionFr(webFilter.getDescriptionFr());
        filter.setDescriptionIt(webFilter.getDescriptionIt());
        filter.setNameDe(webFilter.getNameDe());
        filter.setNameFr(webFilter.getNameFr());
        filter.setNameIt(webFilter.getNameIt());
        filter.setRefObject(webFilter.getRefObject());
        filter.setSource(webFilter.getSource());
        filter.setAuthorisationLevel(webFilter.getAuthorisationLevel());
        filter.setIsActive(webFilter.getIsActive());
        filter.setIsDefault(webFilter.getIsDefault());
        filter.setFilterOrder(webFilter.getFilterOrder());
        for (WebParameter webParameter : webFilter.getParameters()) {
            filter.getParameters().add(convertToParameter(webParameter));
        }
        return filter;
    }

    private WebFilter convertToWebFilter(Filter filter) {
        WebFilter webFilter = new WebFilter();
        webFilter.setFilterId(filter.getFilterId());
        webFilter.setDescriptionDe(filter.getDescriptionDe());
        webFilter.setDescriptionFr(filter.getDescriptionFr());
        webFilter.setDescriptionIt(filter.getDescriptionIt());
        webFilter.setNameDe(filter.getNameDe());
        webFilter.setNameFr(filter.getNameFr());
        webFilter.setNameIt(filter.getNameIt());
        webFilter.setRefObject(filter.getRefObject());
        webFilter.setSource(filter.getSource());
        webFilter.setAuthorisationLevel(filter.getAuthorisationLevel());
        webFilter.setIsActive(filter.isIsActive());
        webFilter.setIsDefault(filter.isIsDefault());
        webFilter.setFilterOrder(filter.getFilterOrder());
        for (Parameter param : filter.getParameters()) {
            webFilter.getParameters().add(convertToWebParameter(param));
        }
        return webFilter;
    }

    private WebFilterListResult convertToWebFilterListResult(FilterListResult filterListResult) {
        WebFilterListResult webFilterListResult = new WebFilterListResult();
        List<WebFilter> webFilters = new ArrayList<WebFilter>();
        if (filterListResult.getFilters() != null) {
            for (Filter filter : filterListResult.getFilters()) {
                webFilters.add(convertToWebFilter(filter));
            }
        }
        webFilterListResult.setFilters(webFilters);
        webFilterListResult.setMessage(filterListResult.getMessage());
        webFilterListResult.setState(filterListResult.getState());
        return webFilterListResult;
    }

    private WebFilterResult convertToWebFilterResult(FilterResult filterResult) {
        WebFilterResult webFilterResult = new WebFilterResult();
        webFilterResult.setFilter(filterResult.getFilter() == null ? null : convertToWebFilter(filterResult.getFilter()));
        webFilterResult.setMessage(filterResult.getMessage());
        webFilterResult.setState(filterResult.getState());
        return webFilterResult;
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public WebFilterListResult getFilters() {
        return convertToWebFilterListResult(webServiceClientFactory.getFilterWebService().getFilters());
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public WebFilterResult getFilterById(Long filterId) {
        return convertToWebFilterResult(webServiceClientFactory.getFilterWebService().getFilterById(filterId));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public WebFilterListResult getFiltersForRefObjectAndNameDe(Long refObject, String nameDe) {
        return convertToWebFilterListResult(webServiceClientFactory.getFilterWebService().getFiltersForRefObjectAndNameDe(refObject, nameDe));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public WebFilterListResult getFiltersForRefObject(Long refObject) {
        return convertToWebFilterListResult(webServiceClientFactory.getFilterWebService().getFiltersForRefObject(refObject));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public WebFilterResult updateFilter(WebFilter filter) {
        return convertToWebFilterResult(webServiceClientFactory.getFilterWebService().updateFilter(convertToFilter(filter)));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public WebFilterResult insertFilter(WebFilter filter) {
        return convertToWebFilterResult(webServiceClientFactory.getFilterWebService().insertFilter(convertToFilter(filter)));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_EA + "')")
    public WebFilterResult deleteFilter(WebFilter filter) {
        return convertToWebFilterResult(webServiceClientFactory.getFilterWebService().deleteFilter(convertToFilter(filter)));
    }

    public WebFilterContext filterContextFromSearchRequest(SearchRequest request, Long refObject) {
        List<WebFilter> filteredWebFilters = new ArrayList<>();

        // Jointure des filtres basée sur filterId
        if (!CollectionUtils.isEmpty(request.getWebFilters())) {
            // Récupérer tous les filtres disponibles pour cet objet de référence
            List<WebFilter> actualWebFilters = getFiltersForRefObject(refObject).getFilters();

            // Créer une Map pour un accès rapide aux filtres du body par filterId
            Map<Long, WebFilter> bodyFiltersMap = request.getWebFilters().stream()
                    .collect(Collectors.toMap(WebFilter::getFilterId, Function.identity()));

            // Ne garder que les filtres qui sont dans le body
            filteredWebFilters = actualWebFilters.stream()
                    .filter(actualFilter -> bodyFiltersMap.containsKey(actualFilter.getFilterId()))
                    .peek(actualFilter -> {
                        WebFilter bodyFilter = bodyFiltersMap.get(actualFilter.getFilterId());
                        actualFilter.setIsDefault(true);
                        // Créer une Map des paramètres du body pour accès rapide par parameterId
                        if (bodyFilter.getParameters() != null && !bodyFilter.getParameters().isEmpty()) {
                            Map<Long, WebParameter> bodyParamsMap = bodyFilter.getParameters().stream()
                                    .collect(Collectors.toMap(WebParameter::getParameterId, Function.identity()));

                            // Surcharger les defaultValue des paramètres du filtre actuel
                            if (actualFilter.getParameters() != null) {
                                actualFilter.getParameters().forEach(actualParam -> {
                                    WebParameter bodyParam = bodyParamsMap.get(actualParam.getParameterId());
                                    if (bodyParam != null && bodyParam.getDefaultValue() != null) {
                                        actualParam.setDefaultValue(bodyParam.getDefaultValue());
                                    }
                                });
                            }
                        }

                    })
                    .collect(Collectors.toList());
        }

        WebFilterContext filterContext = new WebFilterContext();
        filterContext.setFilter(filteredWebFilters);
        filterContext.setWhereFilter(request.getWhereFilters());
        filterContext.setLocale(localizationManager.getLocale().toString());
        return filterContext;
    }
}
