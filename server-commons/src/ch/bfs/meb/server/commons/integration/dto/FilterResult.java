/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: FilterResult.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.integration.dto;

import java.io.Serializable;

import ch.bfs.meb.integration.dto.ResultBase;

/**
 * Filter specific implementation for the return type of the soap web services
 * 
 * @author $Author: dzw $
 * @version $Revision: 228 $
 */
public class FilterResult extends ResultBase implements Serializable {
    /**
     * Generated
     */
    private static final long serialVersionUID = 2944930119071879071L;

    Filter _filter;

    public FilterResult() {}

    public FilterResult(Filter filter) {
        _filter = filter;
        filter.getParameters(); // prevents null value in transfer object
        setState(OK);
    }

    public FilterResult(String message) {

        setFilter(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the filter.
     */
    public Filter getFilter() {
        return _filter;
    }

    /**
     * @param filter
     *            The filter to set.
     */
    public void setFilter(Filter filter) {
        _filter = filter;
    }
}
