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
 * Parameter specific implementation for the return type of the soap web services
 *
 */
public class ParameterResult extends ResultBase implements Serializable {
    private static final long serialVersionUID = -4242343238118358952L;

    Parameter _parameter;

    public ParameterResult() {}

    public ParameterResult(Parameter parameter) {

        _parameter = parameter;
        setState(OK);
    }

    public ParameterResult(String message) {
        setParameter(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the filter.
     */
    public Parameter getParameter() {
        return _parameter;
    }

    /**
     * @param filter
     *            The filter to set.
     */
    public void setParameter(Parameter parameter) {
        _parameter = parameter;
    }
}
