/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id: InterventionService.java 429 2010-01-13 13:15:13Z jfu $
 */
package ch.bfs.meb.ssp.web.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ch.bfs.meb.ssp.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.CantonIntervention;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.CantonInterventionListResult;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.CantonInterventionResult;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.FileResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * specific canton intervention services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 429 $
 */
@Service("cantonInterventionService")
public class CantonInterventionService implements ICantonInterventionService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Autowired
    private IWebLocalizationManager _localizationManager;

    protected CantonInterventionResult updateData(CantonInterventionResult result) {
        if (result.getIntervention() != null && result.getIntervention().getType() == CodegroupUtility.SSP_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT) {
            result.getIntervention().setText(_localizationManager.getMessage("intervention.plausi"));
        }
        return result;
    }

    protected CantonInterventionListResult updateData(CantonInterventionListResult result) {
        for (CantonIntervention intervention : result.getInterventions()) {
            if (intervention.getType() == CodegroupUtility.SSP_CANTONINTERVENTIONTYPE_CREATE_PLAUSIREPORT) {
                intervention.setText(_localizationManager.getMessage("intervention.plausi"));
            }
        }
        return result;
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public CantonInterventionListResult getInterventionsForCanton(Long cantonId) {
        return updateData(_webServiceClientFactory.getCantonInterventionWebService().getInterventionsForCanton(cantonId));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public CantonInterventionResult getInterventionById(Long interventionId) {
        return updateData(_webServiceClientFactory.getCantonInterventionWebService().getCantonInterventionById(interventionId));
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public CantonInterventionResult insertIntervention(CantonIntervention intervention) {
        return _webServiceClientFactory.getCantonInterventionWebService().insertCantonIntervention(intervention);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public CantonInterventionResult updateIntervention(CantonIntervention intervention) {
        return _webServiceClientFactory.getCantonInterventionWebService().updateCantonIntervention(intervention);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_DL + "')")
    public CantonInterventionResult deleteIntervention(CantonIntervention intervention) {
        return _webServiceClientFactory.getCantonInterventionWebService().deleteCantonIntervention(intervention);
    }

    @Override
    @PreAuthorize("hasAuthority('" + SecurityConstants.ROLE_SSP_RO + "')")
    public FileResult getPlausiReportFile(Long interventionId) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage().substring(0, 2);
        return _webServiceClientFactory.getCantonInterventionWebService().getPlausiReportFile(interventionId, language);
    }
}