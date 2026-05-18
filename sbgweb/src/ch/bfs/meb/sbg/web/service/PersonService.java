/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: PersonWebServiceFacade.java 580 2009-05-15 09:31:51Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import java.util.List;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sbg.web.ws.sbgperson.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

/**
 * TODO Describe this class
 * 
 * @author $Author: lsc $
 * @version $Revision: 580 $
 */
@Service("personService")
public class PersonService implements IPersonService {
    @Autowired
    private WebServiceClientFactory _webServiceClientFactory;

    @Autowired
    private DozerBeanMapper _dozerBeanMapper;

    protected SortContext convertToSortContext(WebSortContext sortContext) {
        if (sortContext == null) {
            return null;
        }

        return _dozerBeanMapper.map(sortContext, SortContext.class);
    }

    protected FilterContext convertToFilterContext(WebFilterContext filterContext) {
        if (filterContext == null) {
            return null;
        }

        return _dozerBeanMapper.map(filterContext, FilterContext.class);
    }

    public PersonList getPersons(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton) {
        return _webServiceClientFactory.getPersonWebService().getPersons(start, buffer, convertToSortContext(sortContext),
                convertToFilterContext(filterContext), version, canton);
    }

    public PersonList getPersonsOwnedByEvents(List<Long> selectedEventIds, WebSortContext sortContext) {
        return _webServiceClientFactory.getPersonWebService().getPersonsOwnedByEvents(selectedEventIds, convertToSortContext(sortContext));
    }

    public PersonResult getPersonById(Long id) {
        return _webServiceClientFactory.getPersonWebService().getPersonById(id);
    }

    public PersonResult updatePerson(Person person, String locale) {
        return _webServiceClientFactory.getPersonWebService().updatePerson(person, locale);
    }

    public PersonResult insertPerson(Person person, String locale) {
        return _webServiceClientFactory.getPersonWebService().insertPerson(person, locale);
    }

    public PersonResult deletePerson(Person person) {
        return _webServiceClientFactory.getPersonWebService().deletePerson(person);
    }

    public PersonResult validatePersons(List<Long> selectedPersonIds, String locale) {
        return _webServiceClientFactory.getPersonWebService().validatePersons(selectedPersonIds, locale);
    }
}
