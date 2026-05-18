/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

  $Id$
 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlIntervention;
import ch.bfs.meb.server.commons.integration.dto.Intervention;

/**
 * Interface for repository for Interventions.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IInterventionRepository {
    public SdlIntervention findLastUploadForDelivery(Long deliveryId);

    public SdlIntervention findLastPlausireport(Long deliveryId);

    public SdlIntervention getInterventionById(Long interventionId);

    public Long getLastInterventionTypeForDelivery(Long deliveryId);

    public boolean existsInterventionOfType(Long deliveryId, Long interventionType);

    public List<Intervention> getInterventionsForDelivery(Long deliveryId);

    public byte[] getDeliveryFile(Long interventionId);

    public byte[] getPlausiReportFile(Long interventionId, String locale);

    public SdlIntervention insertIntervention(SdlIntervention intervention);

    public SdlIntervention updateIntervention(SdlIntervention intervention);

    public void deleteIntervention(SdlIntervention intervention);
}
