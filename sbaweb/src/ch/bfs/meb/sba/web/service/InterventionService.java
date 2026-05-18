/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: InterventionService.java 429 2010-01-13 13:15:13Z jfu $
 */
package ch.bfs.meb.sba.web.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sba.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sba.web.ws.sbaintervention.FileResult;
import ch.bfs.meb.sba.web.ws.sbaintervention.Intervention;
import ch.bfs.meb.sba.web.ws.sbaintervention.InterventionListResult;
import ch.bfs.meb.sba.web.ws.sbaintervention.InterventionResult;
import ch.bfs.meb.util.SecurityConstants;

/**
 * specific intervention services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 429 $
 */
@Service("interventionService")
public class InterventionService implements IInterventionService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public InterventionListResult getInterventionsForDelivery(Long deliveryId) {
        return _webServiceClientFactory.getInterventionWebService().getInterventionsForDelivery(deliveryId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public InterventionResult getInterventionById(Long interventionId) {
        return _webServiceClientFactory.getInterventionWebService().getInterventionById(interventionId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public InterventionResult insertIntervention(Intervention intervention) {
        return _webServiceClientFactory.getInterventionWebService().insertIntervention(intervention);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public InterventionResult updateIntervention(Intervention intervention) {
        return _webServiceClientFactory.getInterventionWebService().updateIntervention(intervention);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_DL + "')")
    public InterventionResult deleteIntervention(Intervention intervention) {
        return _webServiceClientFactory.getInterventionWebService().deleteIntervention(intervention);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public FileResult getDeliveryFile(Long interventionId) {
        return _webServiceClientFactory.getInterventionWebService().getDeliveryFile(interventionId);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SBA_RO + "')")
    public FileResult getPlausiReportFile(Long interventionId) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return _webServiceClientFactory.getInterventionWebService().getPlausiReportFile(interventionId, language);
    }
}