/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.service;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbacanton.*;

public interface ICantonService {
    public CantonListResult getCantons(Long version, Long canton);

    public CantonResult getCantonById(Long cantonId);

    public PlausiErrorListResult getPlausiErrorsForCanton(Long cantonId);

    public CantonListResult initVersion(Long version, Long canton, boolean noSync);

    public CantonResult validateCanton(Canton canton, boolean undo);

    public CantonResult finalizeCanton(Canton canton, boolean undo);

    public CantonResult updateCanton(Canton canton, List<PlausiError> plausiErrors);

    public CantonResult insertCanton(Canton canton);

    public CantonResult deleteCanton(Canton canton);

    public Long getInitialVersion();

    public List<Long> getFilterCantonsForActUser();

    public CantonResult createPlausireport(Canton canton);

    public FileResult getLastPlausireport(Long cantonId);
}
