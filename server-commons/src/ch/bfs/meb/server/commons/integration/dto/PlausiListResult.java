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
 * Plausi specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class PlausiListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 8830340541431628306L;

    List<Plausi> _plausis;

    public PlausiListResult() {}

    public PlausiListResult(List<Plausi> plausis) {
        _plausis = plausis;
        setState(OK);
    }

    public PlausiListResult(String message) {

        setPlausis(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the plausis.
     */
    public List<Plausi> getPlausis() {
        return _plausis;
    }

    /**
     * @param plausis
     *            The plausis to set.
     */
    public void setPlausis(List<Plausi> plausis) {
        _plausis = plausis;
    }
}
