/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: ICantonInterventionRepository.java 834 2010-02-25 13:25:21Z dzw $
 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.ssp.server.integration.dto.SspCantonIntervention;

/**
 * Interface for repository for Interventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public interface ICantonInterventionRepository {
    public SspCantonIntervention findLastPlausireport(final Long deliveryId);

    public SspCantonIntervention getInterventionById(Long interventionId);

    public Long getLastInterventionTypeForCanton(Long cantonId);

    public boolean existsInterventionOfType(Long cantonId, Long interventionType);

    public List<CantonIntervention> getInterventionsForCanton(Long cantonId);

    public byte[] getPlausiReportFile(Long interventionId, String locale);

    public SspCantonIntervention insertIntervention(SspCantonIntervention intervention);

    public SspCantonIntervention updateIntervention(SspCantonIntervention intervention);

    public void deleteIntervention(SspCantonIntervention intervention);
}
