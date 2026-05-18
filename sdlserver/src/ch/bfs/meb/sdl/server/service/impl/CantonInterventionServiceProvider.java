/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlCantonIntervention;
import ch.bfs.meb.sdl.server.integration.repository.ICantonInterventionRepository;
import ch.bfs.meb.server.commons.integration.dto.CantonIntervention;
import ch.bfs.meb.server.commons.service.impl.ICantonInterventionServiceProvider;

public class CantonInterventionServiceProvider implements ICantonInterventionServiceProvider {
    private ICantonInterventionRepository _interventionRepository;

    public void setCantonInterventionRepository(ICantonInterventionRepository interventionRepository) {
        _interventionRepository = interventionRepository;
    }

    @Override
    public CantonIntervention getInterventionById(Long interventionId) {
        return _interventionRepository.getInterventionById(interventionId);
    }

    @Override
    public List<CantonIntervention> getInterventionsForCanton(Long cantonId) {
        return Collections.unmodifiableList(new ArrayList<CantonIntervention>(_interventionRepository.getInterventionsForCanton(cantonId)));
    }

    @Override
    public byte[] getPlausiReportFile(Long interventionId, String locale) {
        return _interventionRepository.getPlausiReportFile(interventionId, locale);
    }

    @Override
    public CantonIntervention insertIntervention(CantonIntervention intervention) {
        return _interventionRepository.insertIntervention(new SdlCantonIntervention(intervention));
    }

    @Override
    public CantonIntervention updateIntervention(CantonIntervention intervention) {
        return _interventionRepository.updateIntervention(new SdlCantonIntervention(intervention));
    }

    @Override
    public void deleteIntervention(CantonIntervention intervention) {
        _interventionRepository.deleteIntervention(new SdlCantonIntervention(intervention));
    }
}
