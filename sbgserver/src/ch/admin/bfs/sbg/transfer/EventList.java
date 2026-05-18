/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: EventList.java 348 2007-09-14 13:33:08Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

import java.util.ArrayList;
import java.util.List;

import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;

/**
 * @author $Author: dzw $
 * @version $Revision: 348 $
 */
public class EventList extends ResultBase {
    private static final long serialVersionUID = 1681866881597244345L;

    private Event[] _events;
    private Long _resultSize;

    public EventList() {
        _resultSize = 0L;
    }

    public EventList(List<SbgEvent> sbgEvents) {
        List<Event> events = new ArrayList<Event>();
        for (SbgEvent sbgEvent : sbgEvents) {
            events.add(new Event(sbgEvent));
        }
        Event[] eventArr = new Event[events.size()];
        _events = events.toArray(eventArr);
        _resultSize = new Long(events.size());
    }

    public EventList(Event[] events) {
        _events = events;
        _resultSize = 0L;
    }

    public Event[] getEvents() {
        return _events;
    }

    public void setEvents(Event[] events) {
        _events = events;
    }

    public Long getResultSize() {
        return _resultSize;
    }

    public void setResultSize(Long resultSize) {
        _resultSize = resultSize;
    }
}
