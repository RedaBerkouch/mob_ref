/*
  PersonService.java

  This file was auto-generated from WSDL
  by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package ch.admin.bfs.sbg.webservice;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ch.admin.bfs.sbg.transfer.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

@WebService(serviceName = "SbgPersonWebService", name = "SbgPersonWebServicePortType")
public class SbgPersonService extends AbstractMebWebService<IPersonService> {
    @WebMethod
    public PersonResult getPersonById(Long id) {
        return getService().getPersonById(id);
    }

    @WebMethod
    public PersonList getPersons(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getPersons(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    public PersonList getPersonsOwnedByEvents(List<Long> selectedEventIds, SortContext sortContext) {
        return getService().getPersonsOwnedByEvents(selectedEventIds, sortContext);
    }

    @WebMethod
    public PersonResult updatePerson(Person person, String locale) {
        return getService().updatePerson(person, locale);
    }

    @WebMethod
    public PersonResult insertPerson(Person person, String locale) {
        return getService().insertPerson(person, locale);
    }

    @WebMethod
    public PersonResult deletePerson(Person person) {
        // Needed in case of modification on person (plausi status)
        return getService().deletePerson(person);
    }

    @WebMethod
    public PersonResult validatePersons(List<Long> selectedPersonIds, String locale) {
        return getService().validatePersons(selectedPersonIds, locale);
    }
}
