/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb
 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sdl.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sdl.web.ws.sdlconfigdelivery.*;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.*;

@Service("configDeliveryService")
public class ConfigDeliveryService implements IConfigDeliveryService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    private Parameter convertToParameter(WebParameter webParameter) {
        Parameter parameter = new Parameter();
        parameter.setUniqueName(webParameter.getUniqueName());
        parameter.setDefaultValue(webParameter.getDefaultValue());
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

    private WhereFilter convertToWhereFilter(WebWhereFilter webWhereFilter) {
        WhereFilter whereFilter = new WhereFilter();
        whereFilter.setAttribute(webWhereFilter.getAttribute());
        whereFilter.setOperator(webWhereFilter.getOperator());
        whereFilter.setRelation(webWhereFilter.getRelation());
        whereFilter.setValue(webWhereFilter.getValue());
        whereFilter.setId(webWhereFilter.getId());
        return whereFilter;
    }

    private FilterContext convertToFilterContext(WebFilterContext webFilterContext) {
        if (webFilterContext == null) {
            return null;
        }

        FilterContext filterContext = new FilterContext();
        for (WebFilter webFilter : webFilterContext.getFilter()) {
            filterContext.getFilter().add(convertToFilter(webFilter));
        }
        for (WebWhereFilter webWhereFilter : webFilterContext.getWhereFilter()) {
            filterContext.getWhereFilter().add(convertToWhereFilter(webWhereFilter));
        }
        filterContext.setLocale(webFilterContext.getLocale());
        return filterContext;
    }

    private SortContext convertToSortContext(WebSortContext webSortContext) {
        SortContext sortContext = new SortContext();
        sortContext.setSortColumn(webSortContext.getSortColumn());
        sortContext.setAscSortOrder(webSortContext.getAscSortOrder());
        sortContext.setLocale(webSortContext.getLocale());
        return sortContext;
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DV + "')")
    public ConfigDeliveryListResult getConfigDeliveries(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version,
            Long canton) {
        return webServiceClientFactory.getConfigDeliveryWebService().getConfigDeliveries(start, buffer, convertToSortContext(sortContext),
                convertToFilterContext(filterContext), version, canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DV + "')")
    public ConfigDeliveryListResult getConfigDeliveriesOwnedBySchools(List<Long> schoolIds, WebSortContext sortContext, Long version) {
        return webServiceClientFactory.getConfigDeliveryWebService().getConfigDeliveriesOwnedBySchools(schoolIds, convertToSortContext(sortContext), version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DV + "')")
    public ConfigDeliveryResult getConfigDeliveryById(Long deliveryId) {
        return webServiceClientFactory.getConfigDeliveryWebService().getConfigDeliveryById(deliveryId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public ConfigDeliveryResult updateConfigDelivery(ConfigDelivery delivery) {
        return webServiceClientFactory.getConfigDeliveryWebService().updateConfigDelivery(delivery);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public ConfigDeliveryResult insertConfigDelivery(ConfigDelivery delivery) {
        return webServiceClientFactory.getConfigDeliveryWebService().insertConfigDelivery(delivery);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public ConfigDeliveryResult deleteConfigDelivery(ConfigDelivery delivery) {
        return webServiceClientFactory.getConfigDeliveryWebService().deleteConfigDelivery(delivery);
    }
}
