/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Plausi specific implementation for the return type of the soap web services
 *
 */
public class PlausiResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 1695998943038753759L;

    Plausi _plausi;

    public PlausiResult() {}

    public PlausiResult(Plausi plausi) {
        _plausi = plausi;
        plausi.getParameters(); // prevents null value in transfer object
        setState(OK);
    }

    public PlausiResult(String message) {
        setPlausi(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the plausi.
     */
    public Plausi getPlausi() {
        return _plausi;
    }

    /**
     * @param plausi
     *            The plausi to set.
     */
    public void setPlausi(Plausi plausi) {
        _plausi = plausi;
    }
}
