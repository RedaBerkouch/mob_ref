/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IInterventionServiceProvider.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Intervention;

public interface IInterventionServiceProvider {
    public Intervention getInterventionById(Long interventionId);

    public List<Intervention> getInterventionsForDelivery(Long deliveryId);

    public byte[] getDeliveryFile(Long interventionId);

    public byte[] getPlausiReportFile(Long interventionId, String locale);

    public Intervention insertIntervention(Intervention intervention);

    public Intervention updateIntervention(Intervention intervention);

    public void deleteIntervention(Intervention intervention);
}
