/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaPerson;
import ch.bfs.meb.sba.server.integration.dto.SbaPersonListResult;
import ch.bfs.meb.sba.server.integration.dto.SbaPersonResult;
import ch.bfs.meb.sba.server.integration.dto.SbaPlausiError;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.PlausiError;
import ch.bfs.meb.server.commons.integration.dto.PlausiErrorListResult;
import ch.bfs.meb.server.commons.integration.dto.SortContext;

public interface IPersonService {
    public SbaPersonListResult getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public SbaPersonListResult getPersonsOwnedByQualifications(List<Long> qualificationIds, SortContext sortContext);

    public SbaPersonResult getPersonById(Long personId);

    public PlausiErrorListResult getPlausiErrorsForPerson(Long personId);

    public SbaPersonResult updatePersonPlausierrors(Long personId, List<SbaPlausiError> plausiErrors);

    public SbaPersonResult updatePerson(SbaPerson person, List<PlausiError> plausiErrors, boolean noPlausi, boolean businessDataChanged);

    public SbaPersonResult insertPerson(SbaPerson person, boolean noPlausi);

    public SbaPersonResult deletePerson(SbaPerson person, boolean noPlausi);

    public SbaPersonResult validatePersons(List<Long> personList, boolean undo);
}