/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: ICantonInterventionService.java 429 2010-01-13 13:15:13Z jfu $

 */
package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.ws.sbacantonintervention.CantonIntervention;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.CantonInterventionListResult;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.CantonInterventionResult;
import ch.bfs.meb.sba.web.ws.sbacantonintervention.FileResult;

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

    public FileResult getPlausiReportFile(Long interventionId);
}
