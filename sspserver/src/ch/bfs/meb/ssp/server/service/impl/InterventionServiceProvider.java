/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Intervention;
import ch.bfs.meb.server.commons.service.impl.IInterventionServiceProvider;
import ch.bfs.meb.ssp.server.integration.dto.SspIntervention;
import ch.bfs.meb.ssp.server.integration.repository.IInterventionRepository;

public class InterventionServiceProvider implements IInterventionServiceProvider {
    private IInterventionRepository _interventionRepository;

    public void setInterventionRepository(IInterventionRepository interventionRepository) {
        _interventionRepository = interventionRepository;
    }

    @Override
    public Intervention getInterventionById(Long interventionId) {
        return _interventionRepository.getInterventionById(interventionId);
    }

    @Override
    public List<Intervention> getInterventionsForDelivery(Long deliveryId) {
        return Collections.unmodifiableList(new ArrayList<Intervention>(_interventionRepository.getInterventionsForDelivery(deliveryId)));
    }

    @Override
    public byte[] getDeliveryFile(Long interventionId) {
        return _interventionRepository.getDeliveryFile(interventionId);
    }

    @Override
    public byte[] getPlausiReportFile(Long interventionId, String locale) {
        return _interventionRepository.getPlausiReportFile(interventionId, locale);
    }

    @Override
    public Intervention insertIntervention(Intervention intervention) {
        return _interventionRepository.insertIntervention(new SspIntervention(intervention));
    }

    @Override
    public Intervention updateIntervention(Intervention intervention) {
        return _interventionRepository.updateIntervention(new SspIntervention(intervention));
    }

    @Override
    public void deleteIntervention(Intervention intervention) {
        _interventionRepository.deleteIntervention(new SspIntervention(intervention));
    }
}
