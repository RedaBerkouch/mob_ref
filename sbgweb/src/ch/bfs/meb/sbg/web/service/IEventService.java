/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: IEventService.java 580 2009-05-15 09:31:51Z lsc $
 *
 * ------------------------------------------------------------------------- */

package ch.bfs.meb.sbg.web.service;

import java.util.List;

import ch.bfs.meb.sbg.web.ws.sbgevent.Event;
import ch.bfs.meb.sbg.web.ws.sbgevent.EventList;
import ch.bfs.meb.sbg.web.ws.sbgevent.EventResult;
import ch.bfs.meb.sbg.web.ws.sbgevent.KeyAspectList;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;

/**
 * TODO Describe this class
 * 
 * @author $Author: lsc $
 * @version $Revision: 580 $
 */
public interface IEventService {
    EventList getEventsOwnedByPersons(List<Long> selectedPersonIds, WebSortContext sortContext);

    EventList getEvents(int start, int buffer, WebSortContext sortContext, WebFilterContext filterContext, Long version, Long canton);

    EventResult getEventById(Long id);

    EventResult updateEvent(Event anEvent, String locale);

    EventResult insertEvent(Event anEvent, String locale);
    
    EventResult duplicateEvent(Event anEvent, String locale);

    EventResult deleteEvent(Event anEvent);

    KeyAspectList getKeyAspectsForSbfiCode(Long professionCode);
}
