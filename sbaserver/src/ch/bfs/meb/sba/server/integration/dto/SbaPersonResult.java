/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SbaPerson specific implementation for the return type of the soap web services
 *
 */
public class SbaPersonResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 5041616936373998488L;

    SbaPerson _person;

    public SbaPersonResult() {}

    public SbaPersonResult(SbaPerson person) {
        _person = person;
        setState(OK);
    }

    public SbaPersonResult(String message) {
        setPerson(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the person.
     */
    public SbaPerson getPerson() {
        return _person;
    }

    /**
     * @param person
     *            The person to set.
     */
    public void setPerson(SbaPerson person) {
        _person = person;
    }
}
