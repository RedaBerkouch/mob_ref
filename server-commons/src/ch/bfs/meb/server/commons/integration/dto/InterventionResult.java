/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: InterventionResult.java 228 2009-11-24 09:06:15Z jfu $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Intervention specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class InterventionResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -2368086710602012555L;

    Intervention _intervention;

    public InterventionResult() {}

    public InterventionResult(Intervention intervention) {
        _intervention = intervention;
        setState(OK);
    }

    public InterventionResult(String message) {

        setIntervention(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the intervention.
     */
    public Intervention getIntervention() {
        return _intervention;
    }

    /**
     * @param intervention
     *            The intervention to set.
     */
    public void setIntervention(Intervention intervention) {
        _intervention = intervention;
    }
}
