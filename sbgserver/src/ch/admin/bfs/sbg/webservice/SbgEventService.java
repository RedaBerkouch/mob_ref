/*
  EventService.java

  This file was auto-generated from WSDL
  by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package ch.admin.bfs.sbg.webservice;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import ch.admin.bfs.sbg.transfer.*;
import ch.bfs.meb.server.commons.service.AbstractMebWebService;

@WebService(serviceName = "SbgEventWebService", name = "SbgEventWebServicePortType")
public class SbgEventService extends AbstractMebWebService<IEventService> {
    @WebMethod
    public EventResult getEventById(Long id) {
        return getService().getEventById(id);
    }

    @WebMethod
    public EventList getEvents(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton) {
        return getService().getEvents(start, buffer, sortContext, filterContext, version, canton);
    }

    @WebMethod
    public EventList getEventsOwnedByPersons(List<Long> selectedPersonIds, SortContext sortContext) {
        return getService().getEventsOwnedByPersons(selectedPersonIds, sortContext);
    }

    @WebMethod
    public EventResult insertEvent(Event anEvent, String locale) {
        return getService().insertEvent(anEvent, locale);
    }
    
    @WebMethod
    public EventResult duplicateEvent(Event anEvent, String locale) {
        return getService().duplicateEvent(anEvent, locale);
    }

    @WebMethod
    public EventResult updateEvent(Event anEvent, String locale) {
        return getService().updateEvent(anEvent, locale);
    }

    @WebMethod
    public EventResult deleteEvent(Event anEvent) {
        // Needed in case of modification on person (plausi status)
        return getService().deleteEvent(anEvent);
    }

    @WebMethod
    public KeyAspectList getKeyAspectsForSbfiCode(Long sbfiCode) {
        return getService().getKeyAspectsForSbfiCode(sbfiCode);
    }
}
