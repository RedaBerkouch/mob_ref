/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: InterventionServiceImpl.java 429 2010-01-13 13:15:13Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.Intervention;
import ch.bfs.meb.server.commons.integration.dto.InterventionListResult;
import ch.bfs.meb.server.commons.integration.dto.InterventionResult;

/**
 * Intervention services.
 * 
 * @author $Author: dzw $
 * @version $Revision: 429 $
 */
@Service
public class InterventionServiceImpl implements IInterventionService {
    private IInterventionServiceProvider _interventionServiceProvider;

    public void setInterventionServiceProvider(IInterventionServiceProvider interventionServiceProvider) {
        _interventionServiceProvider = interventionServiceProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public InterventionListResult getInterventionsForDelivery(Long deliveryId) {
        return new InterventionListResult(_interventionServiceProvider.getInterventionsForDelivery(deliveryId));
    }

    @Override
    @Transactional(readOnly = true)
    public InterventionResult getInterventionById(Long interventionId) {
        return new InterventionResult(_interventionServiceProvider.getInterventionById(interventionId));
    }

    @Override
    @Transactional
    public InterventionResult insertIntervention(Intervention intervention) {
        intervention.setIntervention_date(new Date());
        return new InterventionResult(_interventionServiceProvider.insertIntervention(intervention));
    }

    @Override
    @Transactional
    public InterventionResult updateIntervention(Intervention intervention) {
        return new InterventionResult(_interventionServiceProvider.updateIntervention(intervention));
    }

    @Override
    @Transactional
    public InterventionResult deleteIntervention(Intervention intervention) {
        _interventionServiceProvider.deleteIntervention(intervention);
        return new InterventionResult();
    }

    @Override
    @Transactional(readOnly = true, timeout = 300)
    public FileResult getDeliveryFile(Long interventionId) {
        return new FileResult(_interventionServiceProvider.getDeliveryFile(interventionId));
    }

    @Override
    @Transactional(readOnly = true, timeout = 300)
    public FileResult getPlausiReportFile(Long interventionId, String locale) {
        byte[] plausireport = _interventionServiceProvider.getPlausiReportFile(interventionId, locale);
        if (plausireport == null) {
            return new FileResult("getlastplausireport.no.valid.report");
        }
        return new FileResult(plausireport);
    }
}