/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * SdlSchool specific implementation for the return type of the soap web services
 *
 */
public class SdlPlausiErrorListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 2349780860781861564L;

    List<SdlPlausiError> _plausiErrors;

    public SdlPlausiErrorListResult() {}

    public SdlPlausiErrorListResult(List<SdlPlausiError> plausiErrors) {
        _plausiErrors = plausiErrors;
        setState(OK);
    }

    public SdlPlausiErrorListResult(String message) {
        setPlausiErrors(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the plausiErrors.
     */
    public List<SdlPlausiError> getPlausiErrors() {
        return _plausiErrors;
    }

    /**
     * @param plausiErrors
     *            The plausiErrors to set.
     */
    public void setPlausiErrors(List<SdlPlausiError> plausiErrors) {
        _plausiErrors = plausiErrors;
    }
}
