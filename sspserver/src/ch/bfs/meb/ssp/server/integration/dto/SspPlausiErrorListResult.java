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
 * SspPlausiError specific implementation for the return type of the soap web services
 *
 */
public class SspPlausiErrorListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 6554404140692615398L;

    List<SspPlausiError> _plausiErrors;

    public SspPlausiErrorListResult() {}

    public SspPlausiErrorListResult(List<SspPlausiError> plausiErrors) {
        _plausiErrors = plausiErrors;
        setState(OK);
    }

    public SspPlausiErrorListResult(String message) {
        setPlausiErrors(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the plausiErrors.
     */
    public List<SspPlausiError> getPlausiErrors() {
        return _plausiErrors;
    }

    /**
     * @param plausiErrors
     *            The plausiErrors to set.
     */
    public void setPlausiErrors(List<SspPlausiError> plausiErrors) {
        _plausiErrors = plausiErrors;
    }
}
