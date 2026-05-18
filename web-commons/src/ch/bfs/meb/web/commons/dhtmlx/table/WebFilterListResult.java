/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.List;

public class WebFilterListResult {
    public static final int OK = 1;
    public static final int FAILURE = 2;

    List<WebFilter> _filters;
    String _message = "";
    int _state = OK;

    public WebFilterListResult() {}

    public WebFilterListResult(List<WebFilter> filters) {
        _filters = filters;
        setState(OK);
    }

    public WebFilterListResult(String message) {
        setFilters(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the filters.
     */
    public List<WebFilter> getFilters() {
        return _filters;
    }

    /**
     * @param filters The filters to set.
     */
    public void setFilters(List<WebFilter> filters) {
        _filters = filters;
    }

    /**
     * @return Returns the _message.
     */
    public String getMessage() {
        return _message;
    }

    /**
     * @param message The _message to set.
     */
    public void setMessage(String message) {
        _message = message;
    }

    /**
     * @return Returns the _state.
     */
    public int getState() {
        return _state;
    }

    /**
     * @param state The _state to set.
     */
    public void setState(int state) {
        _state = state;
    }
}
