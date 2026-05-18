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

import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.server.commons.integration.dto.CantonInterventionListResult;
import ch.bfs.meb.server.commons.integration.dto.CantonInterventionResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;

/**
 * Intervention services.
 * 
 * @author $Author: dzw $
 * @version $Revision: 429 $
 */
@Service
public class CantonInterventionServiceImpl implements ICantonInterventionService {
    private ICantonInterventionServiceProvider _interventionServiceProvider;

    public void setCantonInterventionServiceProvider(ICantonInterventionServiceProvider interventionServiceProvider) {
        _interventionServiceProvider = interventionServiceProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public CantonInterventionListResult getInterventionsForCanton(Long cantonId) {
        return new CantonInterventionListResult(_interventionServiceProvider.getInterventionsForCanton(cantonId));
    }

    @Override
    @Transactional(readOnly = true)
    public CantonInterventionResult getInterventionById(Long interventionId) {
        return new CantonInterventionResult(_interventionServiceProvider.getInterventionById(interventionId));
    }

    @Override
    @Transactional
    public CantonInterventionResult insertIntervention(CantonIntervention intervention) {
        intervention.setIntervention_date(new Date());
        return new CantonInterventionResult(_interventionServiceProvider.insertIntervention(intervention));
    }

    @Override
    @Transactional
    public CantonInterventionResult updateIntervention(CantonIntervention intervention) {
        return new CantonInterventionResult(_interventionServiceProvider.updateIntervention(intervention));
    }

    @Override
    @Transactional
    public CantonInterventionResult deleteIntervention(CantonIntervention intervention) {
        _interventionServiceProvider.deleteIntervention(intervention);
        return new CantonInterventionResult();
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