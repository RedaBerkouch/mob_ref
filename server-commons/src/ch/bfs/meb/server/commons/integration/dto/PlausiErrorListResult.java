/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: PlausiListResult 228 2009-11-24 09:06:15Z jfu $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * PlausiError specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class PlausiErrorListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 3294119514985716346L;

    List<PlausiError> _plausiErrors;

    public PlausiErrorListResult() {}

    public PlausiErrorListResult(List<PlausiError> plausiErrors) {
        _plausiErrors = plausiErrors;
        setState(OK);
    }

    public PlausiErrorListResult(String message) {

        setPlausiErrors(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the plausiErrors.
     */
    public List<PlausiError> getPlausiErrors() {
        return _plausiErrors;
    }

    /**
     * @param plausiErrors
     *            The plausiErrors to set.
     */
    public void setPlausiErrors(List<PlausiError> plausiErrors) {
        _plausiErrors = plausiErrors;
    }
}
