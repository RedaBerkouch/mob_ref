/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: InterventionService.java 429 2010-01-13 13:15:13Z dzw $
 */
package ch.bfs.meb.ssp.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.springframework.security.access.prepost.PreAuthorize;

import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.Intervention;
import ch.bfs.meb.server.commons.integration.dto.InterventionListResult;
import ch.bfs.meb.server.commons.integration.dto.InterventionResult;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;
import ch.bfs.meb.server.commons.service.impl.IInterventionService;
import ch.bfs.meb.util.SecurityConstants;

@WebService(serviceName = "SspInterventionWebService", name = "SspInterventionWebServicePortType")
public class SspInterventionService extends AbstractMebWebService<IInterventionService> {
    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public InterventionListResult getInterventionsForDelivery(Long deliveryId) {
        return getService().getInterventionsForDelivery(deliveryId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public InterventionResult getInterventionById(Long interventionId) {
        return getService().getInterventionById(interventionId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public InterventionResult insertIntervention(Intervention intervention) {
        return getService().insertIntervention(intervention);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public InterventionResult updateIntervention(Intervention intervention) {
        return getService().updateIntervention(intervention);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public InterventionResult deleteIntervention(Intervention intervention) {
        return getService().deleteIntervention(intervention);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public FileResult getDeliveryFile(Long interventionId) {
        return getService().getDeliveryFile(interventionId);
    }

    @WebMethod
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public FileResult getPlausiReportFile(Long interventionId, String locale) {
        return getService().getPlausiReportFile(interventionId, locale);
    }
}