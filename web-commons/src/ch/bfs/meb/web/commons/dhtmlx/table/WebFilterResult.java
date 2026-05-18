/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

public class WebFilterResult {
    public static final int OK = 1;
    public static final int FAILURE = 2;

    WebFilter _filter;
    String _message = "";
    int _state = OK;

    public WebFilterResult() {}

    public WebFilterResult(WebFilter filter) {
        _filter = filter;
        setState(OK);
    }

    public WebFilterResult(String message) {
        setFilter(null);
        setMessage(message);
        setState(FAILURE);
    }

    /**
     * @return Returns the filter.
     */
    public WebFilter getFilter() {
        return _filter;
    }

    /**
     * @param filter
     *            The filter to set.
     */
    public void setFilter(WebFilter filter) {
        _filter = filter;
    }

    /**
     * @return Returns the _message.
     */
    public String getMessage() {
        return _message;
    }

    /**
     * @param _message
     *            The _message to set.
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
     * @param _state
     *            The _state to set.
     */
    public void setState(int state) {
        _state = state;
    }
}
