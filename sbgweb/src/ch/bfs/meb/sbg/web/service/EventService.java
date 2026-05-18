/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EventWebServiceFacade.java 580 2009-05-15 09:31:51Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.sbg.web.service;

import java.util.List;

import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.bfs.meb.sbg.web.service.client.WebServiceClientFactory;
import ch.bfs.meb.sbg.web.ws.sbgevent.*;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

/**
 * TODO Describe this class
 * 
 * @author $Author: lsc $
 * @version $Revision: 580 $
 */
@Service("eventService")
public class EventService implements IEventService {
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

    public EventList getEvents(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton) {
        return _webServiceClientFactory.getEventWebService().getEvents(start, buffer, convertToSortContext(sortContext), convertToFilterContext(filterContext),
                version, canton);
    }

    public EventList getEventsOwnedByPersons(List<Long> selectedPersonIds, WebSortContext sortContext) {
        return _webServiceClientFactory.getEventWebService().getEventsOwnedByPersons(selectedPersonIds, convertToSortContext(sortContext));
    }

    public EventResult getEventById(Long id) {
        return _webServiceClientFactory.getEventWebService().getEventById(id);
    }

    public EventResult updateEvent(Event anEvent, String locale) {
        return _webServiceClientFactory.getEventWebService().updateEvent(anEvent, locale);
    }

    public EventResult insertEvent(Event anEvent, String locale) {
        return _webServiceClientFactory.getEventWebService().insertEvent(anEvent, locale);
    }
    
    public EventResult duplicateEvent(Event anEvent, String locale) {
        return _webServiceClientFactory.getEventWebService().duplicateEvent(anEvent, locale);
    }
    
    public EventResult deleteEvent(Event anEvent) {
        return _webServiceClientFactory.getEventWebService().deleteEvent(anEvent);
    }

    public KeyAspectList getKeyAspectsForSbfiCode(Long sbfiCode) {
        return _webServiceClientFactory.getEventWebService().getKeyAspectsForSbfiCode(sbfiCode);
    }
}
