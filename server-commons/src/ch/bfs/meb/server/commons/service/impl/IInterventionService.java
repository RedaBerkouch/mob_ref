/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IInterventionService.java 429 2010-01-13 13:15:13Z jfu $
 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.Intervention;
import ch.bfs.meb.server.commons.integration.dto.InterventionListResult;
import ch.bfs.meb.server.commons.integration.dto.InterventionResult;

/**
 * Interface for specific intervention services.
 * 
 * @author $Author: jfu $
 * @version $Revision: 429 $
 */
public interface IInterventionService {
    public InterventionListResult getInterventionsForDelivery(Long deliveryId);

    public InterventionResult getInterventionById(Long interventionId);

    public InterventionResult insertIntervention(Intervention intervention);

    public InterventionResult updateIntervention(Intervention intervention);

    public InterventionResult deleteIntervention(Intervention intervention);

    public FileResult getDeliveryFile(Long interventionId);

    public FileResult getPlausiReportFile(Long interventionId, String locale);
}
