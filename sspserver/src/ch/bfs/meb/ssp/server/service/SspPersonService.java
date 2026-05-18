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

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPersonListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspPersonResult;
import ch.bfs.meb.ssp.server.service.impl.IPersonService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspPersonWebService", name = "SspPersonWebServicePortType")
public class SspPersonService extends AbstractMebWebService<IPersonService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspPersonListResult getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getPersons(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspPersonListResult getPersonsOwnedByActivities(List<Long> activityIds, SortContext sortContext) {
        return getService().getPersonsOwnedByActivities(activityIds, sortContext);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public SspPersonResult getPersonById(Long personId) {
        return getService().getPersonById(personId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForPerson(Long personId) {
        return getService().getPlausiErrorsForPerson(personId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspPersonResult updatePerson(SspPerson person, List<PlausiError> plausiErrors, boolean noPlausi) {
        return getService().updatePerson(person, plausiErrors, noPlausi, false);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspPersonResult insertPerson(SspPerson person, boolean noPlausi) {
        return getService().insertPerson(person, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspPersonResult deletePerson(SspPerson person, boolean noPlausi) {
        return getService().deletePerson(person, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public SspPersonResult validatePersons(List<Long> personList, boolean undo) {
        return getService().validatePersons(personList, undo);
    }
}