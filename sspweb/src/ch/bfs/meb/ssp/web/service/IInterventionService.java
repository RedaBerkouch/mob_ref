/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

  $Id: IInterventionService.java 429 2010-01-13 13:15:13Z jfu $

 */
package ch.bfs.meb.ssp.web.service;

import ch.bfs.meb.ssp.web.ws.sspintervention.FileResult;
import ch.bfs.meb.ssp.web.ws.sspintervention.Intervention;
import ch.bfs.meb.ssp.web.ws.sspintervention.InterventionListResult;
import ch.bfs.meb.ssp.web.ws.sspintervention.InterventionResult;

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

    public FileResult getPlausiReportFile(Long interventionId);
}
