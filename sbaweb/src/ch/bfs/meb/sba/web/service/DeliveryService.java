/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: DeliveryService.java 957 2010-03-09 08:52:08Z msc $
 */
package ch.bfs.meb.sba.web.service;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sba.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sba.web.ws.sbadelivery.*;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.*;

/**
 * Generic delivery services.
 * 
 * @author $Author: msc $
 * @version $Revision: 957 $
 */
@Service("deliveryService")
public class DeliveryService implements IDeliveryService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

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
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaDeliveryListResult getDeliveries(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton) {
        return _webServiceClientFactory.getDeliveryWebService().getDeliveries(start, buffer, convertToSortContext(sortContext),
                convertToFilterContext(filterContext), version, canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaDeliveryResult getDeliveryById(Long deliveryId) {
        return _webServiceClientFactory.getDeliveryWebService().getDeliveryById(deliveryId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForDelivery(Long deliveryId) {
        return _webServiceClientFactory.getDeliveryWebService().getPlausiErrorsForDelivery(deliveryId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult replaceDelivery(Long deliveryId) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return _webServiceClientFactory.getDeliveryWebService().replaceDelivery(deliveryId, language);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult amendDelivery(Long deliveryId) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return _webServiceClientFactory.getDeliveryWebService().amendDelivery(deliveryId, language);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult confirmDelivery(Long deliveryId) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return _webServiceClientFactory.getDeliveryWebService().confirmDelivery(deliveryId, language);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult cancelDelivery(Long deliveryId) {
        return _webServiceClientFactory.getDeliveryWebService().cancelDelivery(deliveryId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult validateDelivery(Long deliveryId, boolean undo) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return _webServiceClientFactory.getDeliveryWebService().validateDelivery(deliveryId, undo, language);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult updateDelivery(SbaDelivery delivery, List<PlausiError> plausiErrors) {
        return _webServiceClientFactory.getDeliveryWebService().updateDelivery(delivery, plausiErrors);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult deleteDelivery(Long deliveryId) {
        return _webServiceClientFactory.getDeliveryWebService().deleteDelivery(deliveryId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaDeliveryResult createPlausireport(Long deliveryId) {
        return _webServiceClientFactory.getDeliveryWebService().createDeliveryPlausireport(deliveryId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public FileResult getLastPlausireport(Long deliveryId) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return _webServiceClientFactory.getDeliveryWebService().getLastPlausireport(deliveryId, language);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaDeliveryResult refreshStatus(SbaDelivery delivery) {
        return _webServiceClientFactory.getDeliveryWebService().refreshStatus(delivery);
    }
}
