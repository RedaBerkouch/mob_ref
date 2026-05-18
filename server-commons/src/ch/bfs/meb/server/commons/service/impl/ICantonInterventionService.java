/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IInterventionService.java 429 2010-01-13 13:15:13Z jfu $
 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.server.commons.integration.dto.CantonInterventionListResult;
import ch.bfs.meb.server.commons.integration.dto.CantonInterventionResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;

/**
 * Interface for specific intervention services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 429 $
 */
public interface ICantonInterventionService {
    public CantonInterventionListResult getInterventionsForCanton(Long cantonId);

    public CantonInterventionResult getInterventionById(Long interventionId);

    public CantonInterventionResult insertIntervention(CantonIntervention intervention);

    public CantonInterventionResult updateIntervention(CantonIntervention intervention);

    public CantonInterventionResult deleteIntervention(CantonIntervention intervention);

    public FileResult getPlausiReportFile(Long interventionId, String locale);
}
