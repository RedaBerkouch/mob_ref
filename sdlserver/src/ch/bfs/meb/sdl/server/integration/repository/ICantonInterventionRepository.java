/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id: ICantonInterventionRepository.java 834 2010-02-25 13:25:21Z dzw $
 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlCantonIntervention;
import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;

/**
 * Interface for repository for Interventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public interface ICantonInterventionRepository {
    public SdlCantonIntervention findLastPlausireport(final Long deliveryId);

    public SdlCantonIntervention getInterventionById(Long interventionId);

    public Long getLastInterventionTypeForCanton(Long cantonId);

    public boolean existsInterventionOfType(Long cantonId, Long interventionType);

    public List<CantonIntervention> getInterventionsForCanton(Long cantonId);

    public byte[] getPlausiReportFile(Long interventionId, String locale);

    public SdlCantonIntervention insertIntervention(SdlCantonIntervention intervention);

    public SdlCantonIntervention updateIntervention(SdlCantonIntervention intervention);

    public void deleteIntervention(SdlCantonIntervention intervention);
}
