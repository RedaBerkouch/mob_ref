/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: CantonInterventionService.java 429 2010-01-13 13:15:13Z dzw $
 */
package ch.bfs.meb.sba.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.server.commons.integration.dto.CantonInterventionListResult;
import ch.bfs.meb.server.commons.integration.dto.CantonInterventionResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.ICantonInterventionService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SbaCantonInterventionWebService", name = "SbaCantonInterventionWebServicePortType")
public class SbaCantonInterventionService extends AbstractMebWebService<ICantonInterventionService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public CantonInterventionListResult getInterventionsForCanton(Long cantonId) {
        return getService().getInterventionsForCanton(cantonId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public CantonInterventionResult getCantonInterventionById(Long interventionId) {
        return getService().getInterventionById(interventionId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public CantonInterventionResult insertCantonIntervention(CantonIntervention intervention) {
        return getService().insertIntervention(intervention);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public CantonInterventionResult updateCantonIntervention(CantonIntervention intervention) {
        return getService().updateIntervention(intervention);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public CantonInterventionResult deleteCantonIntervention(CantonIntervention intervention) {
        return getService().deleteIntervention(intervention);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public FileResult getPlausiReportFile(Long interventionId, String locale) {
        return getService().getPlausiReportFile(interventionId, locale);
    }
}