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
 * Interface for generic event services.
 * 
 * @author $Author: msc $
 * @version $Revision: 1162 $
 */
public interface IEventService {
    EventResult getEventById(Long id);

    EventList getEvents(int start, int buffer, SortContext sortContext, FilterContext filterContext, Long version, Long canton);

    EventList getEventsOwnedByPersons(List<Long> selectedPersonIds, SortContext sortContext);

    EventResult insertEvent(Event anEvent, String locale);
    
    EventResult duplicateEvent(Event anEvent, String locale);

    EventResult updateEvent(Event anEvent, String locale);

    EventResult deleteEvent(Event anEvent);

    KeyAspectList getKeyAspectsForSbfiCode(Long sbfiCode);
}
