/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Canton;
import ch.bfs.meb.server.commons.integration.dto.CantonResult;
import ch.bfs.meb.server.commons.integration.dto.FileResult;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;

public interface ICantonServiceProvider {
    public List<Canton> getCantons(Long version, Long canton);

    public Canton getCantonWithConfigDeliveryAndSchoolByMaxVersion(Long version, Long canton);

    public Canton getCantonById(Long cantonId);

    public List<PlausiError> getPlausiErrorsForCanton(Long cantonId);

    public CantonResult validateCanton(Canton canton, boolean undo, String locale);

    public CantonResult finalizeCanton(Canton canton, boolean undo);

    public Canton updateCanton(Canton canton, List<PlausiError> plausiErrors);

    public Canton insertCanton(Canton canton);

    public void deleteCanton(Canton canton);

    public void initVersion(Canton canton);

    public Long getInitialVersion();

    public List<Long> getFilterCantonsForActUser();

    public CantonResult createPlausireport(Canton canton);

    public FileResult getLastPlausireport(Long cantonId, String locale);
}
