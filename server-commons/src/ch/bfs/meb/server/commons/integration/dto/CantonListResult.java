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
 * Canton specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class CantonListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 9084725801843173045L;

    List<Canton> _cantons;

    public CantonListResult() {}

    public CantonListResult(List<Canton> cantons) {
        _cantons = cantons;
        setState(OK);
    }

    public CantonListResult(String message) {
        setCantons(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the cantons.
     */
    public List<Canton> getCantons() {
        return _cantons;
    }

    /**
     * @param cantons
     *            The cantons to set.
     */
    public void setCantons(List<Canton> cantons) {
        _cantons = cantons;
    }
}
