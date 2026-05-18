/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: SspFilterService.java 590 2010-02-02 12:33:53Z jfu $
 */
package ch.bfs.meb.ssp.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.FilterListResult;
import ch.bfs.meb.server.commons.integration.dto.FilterResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IFilterService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspFilterWebService", name = "SspFilterWebServicePortType")
public class SspFilterService extends AbstractMebWebService<IFilterService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public FilterListResult getFilters() {
        return getService().getFilters();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public FilterListResult getFiltersForRefObjectAndNameDe(Long refObject, String nameDe) {
        return getService().getFiltersForRefObjectAndNameDe(refObject, nameDe);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public FilterListResult getFiltersForRefObject(Long refObject) {
        return getService().getFiltersForRefObject(refObject);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public FilterResult getFilterById(Long filterId) {
        return getService().getFilterById(filterId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EA + "')")
    public FilterResult updateFilter(Filter filter) {
        return getService().updateFilter(filter);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EA + "')")
    public FilterResult insertFilter(Filter filter) {
        return getService().insertFilter(filter);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_EA + "')")
    public FilterResult deleteFilter(Filter filter) {
        return getService().deleteFilter(filter);
    }
}