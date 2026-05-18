/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb
 */
package ch.bfs.meb.sdl.web.service;

import java.util.List;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sdl.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sdl.web.ws.sdlschool.*;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

@Service("schoolService")
public class SchoolService implements ISchoolService {
    @Autowired
    private WebServiceClientFactory webServiceClientFactory;

    @Autowired
    private DozerBeanMapper _dozerBeanMapper;

    //	private Parameter convertToParameter(WebParameter webParameter)
    //	{
    //		Parameter parameter = new Parameter();
    //		parameter.setUniqueName(webParameter.getUniqueName());
    //		parameter.setDefaultValue(webParameter.getDefaultValue());
    //		return parameter;
    //	}
    //
    //	private Filter convertToFilter(WebFilter webFilter)
    //	{
    //		Filter filter = new Filter();
    //		filter.setFilterId(webFilter.getFilterId());
    //		filter.setDescriptionDe(webFilter.getDescriptionDe());
    //		filter.setDescriptionFr(webFilter.getDescriptionFr());
    //		filter.setDescriptionIt(webFilter.getDescriptionIt());
    //		filter.setNameDe(webFilter.getNameDe());
    //		filter.setNameFr(webFilter.getNameFr());
    //		filter.setNameIt(webFilter.getNameIt());
    //		filter.setRefObject(webFilter.getRefObject());
    //		filter.setSource(webFilter.getSource());
    //		filter.setAuthorisationLevel(webFilter.getAuthorisationLevel());
    //		filter.setIsActive(webFilter.getIsActive());
    //		filter.setIsDefault(webFilter.getIsDefault());
    //		filter.setFilterOrder(webFilter.getFilterOrder());
    //		for(WebParameter webParameter : webFilter.getParameters())
    //		{
    //			filter.getParameters().add(convertToParameter(webParameter));
    //		}
    //		return filter;
    //	}
    //
    //	private WhereFilter convertToWhereFilter(WebWhereFilter webWhereFilter)
    //	{
    //		WhereFilter whereFilter = new WhereFilter();
    //		whereFilter.setAttribute(webWhereFilter.getAttribute());
    //		whereFilter.setOperator(webWhereFilter.getOperator());
    //		whereFilter.setRelation(webWhereFilter.getRelation());
    //		whereFilter.setValue(webWhereFilter.getValue());
    //		whereFilter.setId(webWhereFilter.getId());
    //		return whereFilter;
    //	}
    //
    //	private FilterContext convertToFilterContext(WebFilterContext webFilterContext)
    //	{
    //		if(webFilterContext == null)
    //		{
    //			return null;
    //		}
    //		
    //		FilterContext filterContext = new FilterContext();
    //		for(WebFilter webFilter : webFilterContext.getFilter())
    //		{
    //			filterContext.getFilter().add(convertToFilter(webFilter));
    //		}
    //		for(WebWhereFilter webWhereFilter : webFilterContext.getWhereFilter())
    //		{
    //			filterContext.getWhereFilter().add(convertToWhereFilter(webWhereFilter));
    //		}
    //		filterContext.setLocale(webFilterContext.getLocale());
    //		return filterContext;
    //	}
    //
    //	private SortContext convertToSortContext(WebSortContext webSortContext)
    //	{
    //		SortContext sortContext = new SortContext();
    //		sortContext.setSortColumn(webSortContext.getSortColumn());
    //		sortContext.setAscSortOrder(webSortContext.getAscSortOrder());
    //		sortContext.setLocale(webSortContext.getLocale());
    //		return sortContext;
    //	}

    private FilterContext convertToFilterContext(WebFilterContext webFilterContext) {
        if (webFilterContext == null) {
            return null;
        }

        return _dozerBeanMapper.map(webFilterContext, FilterContext.class);
    }

    private SortContext convertToSortContext(WebSortContext webSortContext) {
        if (webSortContext == null) {
            return null;
        }

        return _dozerBeanMapper.map(webSortContext, SortContext.class);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlSchoolListResult getSchools(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton) {
        return webServiceClientFactory.getSchoolWebService().getSchools(start, buffer, convertToSortContext(sortContext), convertToFilterContext(filterContext),
                version, canton);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlSchoolListResult getSchoolsOwnedByClasses(List<Long> classIds, WebSortContext sortContext) {
        return webServiceClientFactory.getSchoolWebService().getSchoolsOwnedByClasses(classIds, convertToSortContext(sortContext));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public SdlSchoolResult getSchoolById(Long schoolId) {
        return webServiceClientFactory.getSchoolWebService().getSchoolById(schoolId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForSchool(Long schoolId) {
        return webServiceClientFactory.getSchoolWebService().getPlausiErrorsForSchool(schoolId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlSchoolResult updateSchool(SdlSchool school, List<PlausiError> plausiErrors, boolean noPlausi) {
        return webServiceClientFactory.getSchoolWebService().updateSchool(school, plausiErrors, noPlausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlSchoolResult insertSchool(SdlSchool school, boolean noPlausi) {
        return webServiceClientFactory.getSchoolWebService().insertSchool(school, noPlausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlSchoolResult deleteSchool(SdlSchool school, boolean noPlausi) {
        return webServiceClientFactory.getSchoolWebService().deleteSchool(school, noPlausi);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SDL_DL + "')")
    public SdlSchoolResult validateSchools(List<Long> schoolList, boolean undo) {
        return webServiceClientFactory.getSchoolWebService().validateSchools(schoolList, undo);
    }
}
