/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: ICantonInterventionServiceProvider.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;

public interface ICantonInterventionServiceProvider {
    public CantonIntervention getInterventionById(Long interventionId);

    public List<CantonIntervention> getInterventionsForCanton(Long deliveryId);

    public byte[] getPlausiReportFile(Long interventionId, String locale);

    public CantonIntervention insertIntervention(CantonIntervention intervention);

    public CantonIntervention updateIntervention(CantonIntervention intervention);

    public void deleteIntervention(CantonIntervention intervention);
}
