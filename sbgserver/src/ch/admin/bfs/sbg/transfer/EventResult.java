/* ----------------------------------------------------------------------------
 *
 * SBG-Projekt
 *
 * Copyright (c) 2006 GLANCE AG, Switzerland
 *
 * $Id: EventResult.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

import java.io.Serializable;

import ch.bfs.meb.sbg.server.integration.dto.SbgEvent;

/**
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
public class EventResult extends ResultBase implements Serializable {

    private static final long serialVersionUID = -6513696769789555648L;

    Event _event;

    public EventResult() {}

    public EventResult(SbgEvent anEvent) {
        _event = new Event(anEvent);
        setState(super.OK);
    }

    public EventResult(String message) {
        setEvent(new Event());
        setMessage(message);
        setState(super.FAILURE);
    }

    /**
     * @return Returns the person.
     */
    public Event getEvent() {
        return _event;
    }

    /**
     * @param anEvent The event to set.
     */
    public void setEvent(Event anEvent) {
        this._event = anEvent;
    }

}
