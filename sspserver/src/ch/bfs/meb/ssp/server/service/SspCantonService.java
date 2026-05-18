/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.ICantonService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspCantonWebService", name = "SspCantonWebServicePortType")
public class SspCantonService extends AbstractMebWebService<ICantonService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonListResult getCantons(Long version, Long canton) {
        return getService().getCantons(version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonResult getCantonById(Long cantonId) {
        return getService().getCantonById(cantonId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public PlausiErrorListResult getPlausiErrorsForCanton(Long cantonId) {
        return getService().getPlausiErrorsForCanton(cantonId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonListResult initVersion(Long version, Long canton, boolean noSync) {
        return getService().initVersion(version, canton, noSync);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonResult validateCanton(Canton canton, boolean undo, String locale) {
        return getService().validateCanton(canton, undo, locale);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonResult finalizeCanton(Canton canton, boolean undo) {
        return getService().finalizeCanton(canton, undo);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonResult updateCanton(Canton canton, List<PlausiError> plausiErrors) {
        return getService().updateCanton(canton, plausiErrors);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonResult insertCanton(Canton canton) {
        return getService().insertCanton(canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonResult deleteCanton(Canton canton) {
        return getService().deleteCanton(canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public Long getInitialVersion() {
        return getService().getInitialVersion();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public List<Long> getFilterCantonsForActUser() {
        return getService().getFilterCantonsForActUser();
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public CantonResult createPlausireport(Canton canton) {
        return getService().createPlausireport(canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DV + "')")
    public FileResult getLastPlausireport(Long cantonId, String locale) {
        return getService().getLastPlausireport(cantonId, locale);
    }
}