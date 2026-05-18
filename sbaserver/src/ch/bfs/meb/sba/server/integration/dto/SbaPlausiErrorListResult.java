/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SdlSchool specific implementation for the return type of the soap web services
 *
 */
public class SbaPlausiErrorListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 2546421087623425916L;

    List<SbaPlausiError> _plausiErrors;

    public SbaPlausiErrorListResult() {}

    public SbaPlausiErrorListResult(List<SbaPlausiError> plausiErrors) {
        _plausiErrors = plausiErrors;
        setState(OK);
    }

    public SbaPlausiErrorListResult(String message) {
        setPlausiErrors(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the plausiErrors.
     */
    public List<SbaPlausiError> getPlausiErrors() {
        return _plausiErrors;
    }

    /**
     * @param plausiErrors
     *            The plausiErrors to set.
     */
    public void setPlausiErrors(List<SbaPlausiError> plausiErrors) {
        _plausiErrors = plausiErrors;
    }
}
