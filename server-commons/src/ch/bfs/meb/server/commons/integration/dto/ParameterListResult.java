/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: ParameterListResult 228 2009-11-24 09:06:15Z jfu $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Parameter specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class ParameterListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = -1869187257512891165L;

    List<Parameter> _parameters;

    public ParameterListResult() {}

    public ParameterListResult(List<Parameter> parameters) {
        _parameters = parameters;
        setState(OK);
    }

    public ParameterListResult(String message) {

        setParameters(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the parameters.
     */
    public List<Parameter> getParameters() {
        return _parameters;
    }

    /**
     * @param parameters
     *            The parameters to set.
     */
    public void setParameters(List<Parameter> parameters) {
        _parameters = parameters;
    }
}
