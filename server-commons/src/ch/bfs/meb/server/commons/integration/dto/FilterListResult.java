/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: FilterListResult.java 228 2009-11-24 09:06:15Z jfu $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;
import java.util.List;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Filter specific implementation for the return type of the soap web services
 * 
 * @author $Author: jfu $
 * @version $Revision: 228 $
 */
public class FilterListResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 5343089536179055050L;

    List<Filter> _filters;

    public FilterListResult() {}

    public FilterListResult(List<Filter> filters) {
        _filters = filters;
        setState(OK);
    }

    public FilterListResult(String message) {
        setFilters(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the filters.
     */
    public List<Filter> getFilters() {
        return _filters;
    }

    /**
     * @param filters
     *            The filters to set.
     */
    public void setFilters(List<Filter> filters) {
        _filters = filters;
    }
}
