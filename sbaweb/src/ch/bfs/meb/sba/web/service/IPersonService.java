/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

 */
package ch.bfs.meb.sba.web.service;

import java.util.List;

import ch.bfs.meb.sba.web.ws.sbaperson.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface IPersonService {
    public SbaPersonListResult getPersons(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SbaPersonListResult getPersonsOwnedByQualifications(List<Long> qualificationIds, WebSortContext sortContext);

    public SbaPersonResult getPersonById(Long personId);

    public PlausiErrorListResult getPlausiErrorsForPerson(Long personId);

    public SbaPersonResult updatePerson(SbaPerson person, List<PlausiError> plausiErrors, boolean noPlausi);

    public SbaPersonResult insertPerson(SbaPerson person, boolean noPlausi);

    public SbaPersonResult deletePerson(SbaPerson person, boolean noPlausi);

    public SbaPersonResult validatePersons(List<Long> personList, boolean undo);
}
