/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbg-webservice

  $Id: IActionService.java 1162 2010-03-26 12:39:56Z msc $
 */
package ch.admin.bfs.sbg.webservice;

import java.util.List;

import ch.admin.bfs.sbg.transfer.*;

/**
 * Interface for generic person services.
 * 
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IPersonService {
    public PersonResult getPersonById(Long id);

    public PersonList getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    public PersonList getPersonsOwnedByEvents(List<Long> selectedPersonIds, SortContext sortContext);

    public PersonResult updatePerson(Person person, String locale);

    public PersonResult insertPerson(Person person, String locale);

    public PersonResult deletePerson(Person person);

    public PersonResult validatePersons(List<Long> selectedPersonIds, String locale);
}
