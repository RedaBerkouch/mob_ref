/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPersonListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaPersonResult;
import ch.bfs.meb.sba.server.service.impl.IPersonService;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SbaPersonWebService", name = "SbaPersonWebServicePortType")
public class SbaPersonService extends AbstractMebWebService<IPersonService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaPersonListResult getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getPersons(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaPersonListResult getPersonsOwnedByQualifications(List<Long> qualificationIds, SortContext sortContext) {
        return getService().getPersonsOwnedByQualifications(qualificationIds, sortContext);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaPersonResult getPersonById(Long personId) {
        return getService().getPersonById(personId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForPerson(Long personId) {
        return getService().getPlausiErrorsForPerson(personId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaPersonResult updatePerson(SbaPerson person, List<PlausiError> plausiErrors, boolean noPlausi) {
        return getService().updatePerson(person, plausiErrors, noPlausi, false);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaPersonResult insertPerson(SbaPerson person, boolean noPlausi) {
        return getService().insertPerson(person, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaPersonResult deletePerson(SbaPerson person, boolean noPlausi) {
        return getService().deletePerson(person, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaPersonResult validatePersons(List<Long> personList, boolean undo) {
        return getService().validatePersons(personList, undo);
    }
}