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

import ch.bfs.meb.sba.server.integration.dto.SbaQualification;
import ch.bfs.meb.sba.server.integration.dto.SbaQualificationListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaQualificationResult;
import ch.bfs.meb.sba.server.service.impl.IQualificationService;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SbaQualificationWebService", name = "SbaQualificationWebServicePortType")
public class SbaQualificationService extends AbstractMebWebService<IQualificationService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaQualificationListResult getQualifications(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version,
            Long canton) {
        return getService().getQualifications(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaQualificationListResult getQualificationsOwnedByPersons(List<Long> personIds, SortContext sortContext) {
        return getService().getQualificationsOwnedByPersons(personIds, sortContext);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public SbaQualificationResult getQualificationById(Long qualificationId) {
        return getService().getQualificationById(qualificationId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public PlausiErrorListResult getPlausiErrorsForQualification(Long qualificationId) {
        return getService().getPlausiErrorsForQualification(qualificationId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaQualificationResult updateQualification(SbaQualification qualification, List<PlausiError> plausiErrors, boolean noPlausi) {
        return getService().updateQualification(qualification, plausiErrors, noPlausi, false);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaQualificationResult insertQualification(SbaQualification qualification, boolean noPlausi) {
        return getService().insertQualification(qualification, noPlausi);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public SbaQualificationResult deleteQualification(SbaQualification qualification, boolean noPlausi) {
        return getService().deleteQualification(qualification, noPlausi);
    }
}