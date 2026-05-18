/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

  $Id: IInterventionRepository.java 834 2010-02-25 13:25:21Z dzw $
 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Intervention;
import ch.bfs.meb.ssp.server.integration.dto.SspIntervention;

/**
 * Interface for repository for Interventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public interface IInterventionRepository {
    public SspIntervention findLastUploadForDelivery(Long deliveryId);

    public SspIntervention findLastPlausireport(final Long deliveryId);

    public SspIntervention getInterventionById(Long interventionId);

    public Long getLastInterventionTypeForDelivery(Long deliveryId);

    public boolean existsInterventionOfType(Long deliveryId, Long interventionType);

    public List<Intervention> getInterventionsForDelivery(Long deliveryId);

    public byte[] getDeliveryFile(Long interventionId);

    public byte[] getPlausiReportFile(Long interventionId, String locale);

    public SspIntervention insertIntervention(SspIntervention intervention);

    public SspIntervention updateIntervention(SspIntervention intervention);

    public void deleteIntervention(SspIntervention intervention);
}
