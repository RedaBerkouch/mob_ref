/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: ICantonInterventionService.java 429 2010-01-13 13:15:13Z jfu $

 */
package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.ws.sdlcantonintervention.CantonIntervention;
import ch.bfs.meb.sdl.web.ws.sdlcantonintervention.CantonInterventionListResult;
import ch.bfs.meb.sdl.web.ws.sdlcantonintervention.CantonInterventionResult;
import ch.bfs.meb.sdl.web.ws.sdlcantonintervention.FileResult;

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
