/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SspPerson specific implementation for the return type of the soap web services
 *
 */
public class SspPersonResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 5041616936373998488L;

    SspPerson _person;

    public SspPersonResult() {}

    public SspPersonResult(SspPerson person) {
        _person = person;
        setState(OK);
    }

    public SspPersonResult(String message) {
        setPerson(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the person.
     */
    public SspPerson getPerson() {
        return _person;
    }

    /**
     * @param person
     *            The person to set.
     */
    public void setPerson(SspPerson person) {
        _person = person;
    }
}
