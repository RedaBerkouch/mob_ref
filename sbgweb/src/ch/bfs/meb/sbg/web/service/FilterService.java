/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: FilterWebServiceFacade.java 364 2007-09-18 13:16:34Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.bfs.meb.sbg.web.frontend.dto.SearchRequest;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sbg.web.ws.sbgfilter.Filter;
import ch.bfs.meb.sbg.web.ws.sbgfilter.FilterListResult;
import ch.bfs.meb.sbg.web.ws.sbgfilter.FilterResult;
import ch.bfs.meb.sbg.web.ws.sbgfilter.Parameter;
import ch.bfs.meb.sbg.web.ws.sbgmacroparameter.ParameterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebParameter;
import ch.bfs.meb.web.commons.util.IFilterService;
import org.springframework.util.CollectionUtils;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 364 $
 */
@Service("filterService")
public class FilterService implements IFilterService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Autowired
    private DozerBeanMapper _dozerBeanMapper;

    @Autowired
    private WebLocalizationManager localizationManager;

    protected List<WebFilter> convertToWebFilters(List<Filter> filterList) {
        List<WebFilter> filters = new ArrayList<WebFilter>();

        if (filterList != null) {
            for (Filter filter : filterList) {
                filters.add(_dozerBeanMapper.map(filter, WebFilter.class));
            }
        }

        return filters;
    }

    protected ParameterListResult convertToParameterList(ParameterListResult parameterList) {
        return _dozerBeanMapper.map(parameterList, ParameterListResult.class);
    }

    @Override
    public WebFilterListResult getFilters() {
        return convertToWebFilterListResult(_webServiceClientFactory.getFilterWebService().getFilter());
    }

    @Override
    public WebFilterResult getFilterById(Long filterId) {
        return convertToWebFilterResult(_webServiceClientFactory.getFilterWebService().getFilterById(filterId));
    }

    @Override
    public WebFilterListResult getFiltersForRefObjectAndNameDe(Long refObject, String nameDe) {
        return convertToWebFilterListResult(_webServiceClientFactory.getFilterWebService().getFiltersForRefObjectAndNameDe(refObject, nameDe));
    }

    @Override
    public WebFilterListResult getFiltersForRefObject(Long refObject) {
        return convertToWebFilterListResult(_webServiceClientFactory.getFilterWebService().getFiltersForRefObject(refObject));
    }

    @Override
    public WebFilterResult updateFilter(WebFilter filter) {
        return convertToWebFilterResult(_webServiceClientFactory.getFilterWebService().updateFilter(convertToFilter(filter)));
    }

    @Override
    public WebFilterResult insertFilter(WebFilter filter) {
        return convertToWebFilterResult(_webServiceClientFactory.getFilterWebService().insertFilter(convertToFilter(filter)));
    }

    @Override
    public WebFilterResult deleteFilter(WebFilter filter) {
        return convertToWebFilterResult(_webServiceClientFactory.getFilterWebService().deleteFilter(convertToFilter(filter)));
    }

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
