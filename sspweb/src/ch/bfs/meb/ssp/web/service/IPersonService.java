/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspweb

 */
package ch.bfs.meb.ssp.web.service;

import java.util.List;

import ch.bfs.meb.ssp.web.ws.sspperson.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

public interface IPersonService {
    public SspPersonListResult getPersons(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public SspPersonListResult getPersonsOwnedByActivities(List<Long> activityIds, WebSortContext sortContext);

    public SspPersonResult getPersonById(Long personId);

    public PlausiErrorListResult getPlausiErrorsForPerson(Long personId);

    public SspPersonResult updatePerson(SspPerson person, List<PlausiError> plausiErrors, boolean noPlausi);

    public SspPersonResult insertPerson(SspPerson person, boolean noPlausi);

    public SspPersonResult deletePerson(SspPerson person, boolean noPlausi);

    public SspPersonResult validatePersons(List<Long> personList, boolean undo);
}
