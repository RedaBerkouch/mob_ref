/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: ICantonInterventionRepository.java 834 2010-02-25 13:25:21Z dzw $
 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaCantonIntervention;
import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;

/**
 * Interface for repository for Interventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public interface ICantonInterventionRepository {
    public SbaCantonIntervention findLastPlausireport(final Long deliveryId);

    public SbaCantonIntervention getInterventionById(Long interventionId);

    public Long getLastInterventionTypeForCanton(Long cantonId);

    public boolean existsInterventionOfType(Long cantonId, Long interventionType);

    public List<CantonIntervention> getInterventionsForCanton(Long cantonId);

    public byte[] getPlausiReportFile(Long interventionId, String locale);

    public SbaCantonIntervention insertIntervention(SbaCantonIntervention intervention);

    public SbaCantonIntervention updateIntervention(SbaCantonIntervention intervention);

    public void deleteIntervention(SbaCantonIntervention intervention);
}
