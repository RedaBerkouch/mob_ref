/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.ssp.server.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;
import ch.bfs.meb.ssp.server.integration.dto.SspPerson;
import ch.bfs.meb.ssp.server.integration.dto.SspPersonListResult;
import ch.bfs.meb.ssp.server.integration.dto.SspPersonResult;
import ch.bfs.meb.ssp.server.integration.dto.SspPlausiError;

public interface IPersonService {
    public SspPersonListResult getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SspPersonListResult getPersonsOwnedByActivities(List<Long> activityIds, SortContext sortContext);

    public SspPersonResult getPersonById(Long personId);

    public PlausiErrorListResult getPlausiErrorsForPerson(Long personId);

    public SspPersonResult updatePersonPlausierrors(Long personId, List<SspPlausiError> plausiErrors);

    public SspPersonResult updatePerson(SspPerson person, List<PlausiError> plausiErrors, boolean noPlausi, boolean businessDataChanged);

    public SspPersonResult insertPerson(SspPerson person, boolean noPlausi);

    public SspPersonResult deletePerson(SspPerson person, boolean noPlausi);

    public SspPersonResult validatePersons(List<Long> personList, boolean undo);
}