/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb
 */
package ch.bfs.meb.ssp.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.ssp.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.ssp.web.ws.sspactivity.*;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.*;

@Service("activityService")
public class ActivityService implements IActivityService {
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
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspActivityListResult getActivities(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton) {
        return webServiceClientFactory.getActivityWebService().getActivities(start, buffer, convertToSortContext(sortContext),
                convertToFilterContext(filterContext), version, canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspActivityListResult getActivitiesOwnedByPersons(List<Long> personIds, WebSortContext sortContext) {
        return webServiceClientFactory.getActivityWebService().getActivitiesOwnedByPersons(personIds, convertToSortContext(sortContext));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspActivityResult getActivityById(Long activityId) {
        return webServiceClientFactory.getActivityWebService().getActivityById(activityId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForActivity(Long activityId) {
        return webServiceClientFactory.getActivityWebService().getPlausiErrorsForActivity(activityId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspActivityResult updateActivity(SspActivity activity, List<PlausiError> plausiErrors, boolean noPlausi) {
        return webServiceClientFactory.getActivityWebService().updateActivity(activity, plausiErrors, noPlausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspActivityResult insertActivity(SspActivity activity, boolean noPlausi) {
        return webServiceClientFactory.getActivityWebService().insertActivity(activity, noPlausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspActivityResult deleteActivity(SspActivity activity, boolean noPlausi) {
        return webServiceClientFactory.getActivityWebService().deleteActivity(activity, noPlausi);
    }
}
