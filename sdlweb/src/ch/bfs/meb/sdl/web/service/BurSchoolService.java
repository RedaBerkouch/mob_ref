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
import ch.bfs.meb.sdl.web.ws.sdlburschool.*;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.*;

@Service("burSchoolService")
public class BurSchoolService implements IBurSchoolService {
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
    public BurSchoolListResult getBurSchools(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton,
            boolean showBurSynch) {
        return webServiceClientFactory.getBurSchoolWebService().getBurSchools(start, buffer, convertToSortContext(sortContext),
                convertToFilterContext(filterContext), version, canton, showBurSynch);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DV + "')")
    public BurSchoolListResult getBurSchoolsOwnedByConfigDeliveries(List<Long> configDeliveryIds, WebSortContext sortContext, boolean showBurSynch) {
        return webServiceClientFactory.getBurSchoolWebService().getBurSchoolsOwnedByConfigDeliveries(configDeliveryIds, convertToSortContext(sortContext),
                showBurSynch);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DV + "')")
    public BurSchoolResult getBurSchoolById(Long burSchoolId, boolean showBurSynch, Long version) {
        return webServiceClientFactory.getBurSchoolWebService().getBurSchoolById(burSchoolId, showBurSynch, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public BurSchoolResult getBurSchoolByIdAndType(String schoolId, String schoolType, Long version) {
        return webServiceClientFactory.getBurSchoolWebService().getBurSchoolByIdAndType(schoolId, schoolType, version);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public BurSchoolListResult synchronizeSchools() {
        return webServiceClientFactory.getBurSchoolWebService().synchronizeSchools();
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public BurSchoolListResult importBurSchools(Long canton) {
        return webServiceClientFactory.getBurSchoolWebService().importBurSchools(canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public BurSchoolResult importBurSchool(BurSchool burSchool) {
        return webServiceClientFactory.getBurSchoolWebService().importBurSchool(burSchool);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public BurSchoolResult updateBurSchool(BurSchool burSchool, boolean showBurSynch) {
        return webServiceClientFactory.getBurSchoolWebService().updateBurSchool(burSchool, showBurSynch);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public BurSchoolResult insertBurSchool(BurSchool burSchool) {
        return webServiceClientFactory.getBurSchoolWebService().insertBurSchool(burSchool);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_EV + "')")
    public BurSchoolResult deleteBurSchool(BurSchool burSchool) {
        return webServiceClientFactory.getBurSchoolWebService().deleteBurSchool(burSchool);
    }
}
