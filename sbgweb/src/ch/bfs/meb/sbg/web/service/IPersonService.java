/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: IPersonService.java 580 2009-05-15 09:31:51Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import java.util.List;

import ch.bfs.meb.sbg.web.ws.sbgperson.Person;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonList;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

/**
 * TODO Describe this class
 * 
 * @author $Author: lsc $
 * @version $Revision: 580 $
 */
public interface IPersonService {
    public PersonList getPersons(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    public PersonList getPersonsOwnedByEvents(List<Long> selectedPersonIds, WebSortContext sortContext);

    public PersonResult getPersonById(Long id);

    public PersonResult updatePerson(Person person, String locale);

    public PersonResult insertPerson(Person person, String locale);

    public PersonResult deletePerson(Person person);

    public PersonResult validatePersons(List<Long> selectedPersonIds, String locale);
}
