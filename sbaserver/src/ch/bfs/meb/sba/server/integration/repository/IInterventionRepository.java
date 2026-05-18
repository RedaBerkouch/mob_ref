/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

  $Id: IInterventionRepository.java 834 2010-02-25 13:25:21Z dzw $
 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaIntervention;
import ch.bfs.meb.server.commons.integration.dto.Intervention;

/**
 * Interface for repository for Interventions.
 * 
 * @author $Author: dzw $
 * @version $Revision: 834 $
 */
public interface IInterventionRepository {
    public SbaIntervention findLastUploadForDelivery(Long deliveryId);

    public SbaIntervention findLastPlausireport(final Long deliveryId);

    public SbaIntervention getInterventionById(Long interventionId);

    public Long getLastInterventionTypeForDelivery(Long deliveryId);

    public boolean existsInterventionOfType(Long deliveryId, Long interventionType);

    public List<Intervention> getInterventionsForDelivery(Long deliveryId);

    public byte[] getDeliveryFile(Long interventionId);

    public byte[] getPlausiReportFile(Long interventionId, String locale);

    public SbaIntervention insertIntervention(SbaIntervention intervention);

    public SbaIntervention updateIntervention(SbaIntervention intervention);

    public void deleteIntervention(SbaIntervention intervention);
}
