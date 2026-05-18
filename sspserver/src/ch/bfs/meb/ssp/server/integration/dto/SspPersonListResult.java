/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SspPerson specific implementation for the return type of the soap web services
 *
 */
public class SspPersonListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 7666240928515314441L;

    List<SspPerson> _persons;
    Long _maxNrOfPersons;

    public SspPersonListResult() {}

    public SspPersonListResult(List<SspPerson> persons, Long maxNrOfPersons) {
        _persons = persons;
        _maxNrOfPersons = maxNrOfPersons;
        setState(OK);
    }

    public SspPersonListResult(String message) {

        setPersons(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the persons.
     */
    public List<SspPerson> getPersons() {
        return _persons;
    }

    /**
     * @param persons
     *            The persons to set.
     */
    public void setPersons(List<SspPerson> persons) {
        _persons = persons;
    }

    /**
     * @return Returns the maximum nr of persons.
     */
    public Long getMaxNrOfPersons() {
        return _maxNrOfPersons;
    }

    /**
     * @param maxNrOfPersons
     *            The persons to set.
     */
    public void setMaxNrOfPersons(Long maxNrOfPersons) {
        _maxNrOfPersons = maxNrOfPersons;
    }
}
