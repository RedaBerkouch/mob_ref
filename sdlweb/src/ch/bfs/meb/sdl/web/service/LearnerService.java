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
import ch.bfs.meb.sdl.web.ws.sdllearner.*;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.*;

@Service("learnerService")
public class LearnerService implements ILearnerService {
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
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlLearnerListResult getLearners(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton) {
        return webServiceClientFactory.getLearnerWebService().getLearners(start, buffer, convertToSortContext(sortContext),
                convertToFilterContext(filterContext), version, canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlLearnerListResult getLearnersOwnedByClasses(List<Long> classIds, WebSortContext sortContext) {
        return webServiceClientFactory.getLearnerWebService().getLearnersOwnedByClasses(classIds, convertToSortContext(sortContext));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlLearnerResult getLearnerById(Long learnerId) {
        return webServiceClientFactory.getLearnerWebService().getLearnerById(learnerId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForLearner(Long learnerId) {
        return webServiceClientFactory.getLearnerWebService().getPlausiErrorsForLearner(learnerId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlLearnerResult updateLearner(SdlLearner learner, List<PlausiError> plausiErrors, boolean noPlausi) {
        return webServiceClientFactory.getLearnerWebService().updateLearner(learner, plausiErrors, noPlausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlLearnerResult insertLearner(SdlLearner learner, boolean noPlausi) {
        return webServiceClientFactory.getLearnerWebService().insertLearner(learner, noPlausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlLearnerResult deleteLearner(SdlLearner learner, boolean noPlausi) {
        return webServiceClientFactory.getLearnerWebService().deleteLearner(learner, noPlausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlLearnerResult validateLearners(List<Long> learnerList, boolean undo) {
        return webServiceClientFactory.getLearnerWebService().validateLearners(learnerList, undo);
    }
}
