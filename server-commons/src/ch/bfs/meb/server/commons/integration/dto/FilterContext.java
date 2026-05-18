/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

 */
package ch.bfs.meb.server.commons.integration.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Describe this class
 */
public class FilterContext {
    protected List<WhereFilter> _whereFilter = new ArrayList<WhereFilter>();
    protected List<Filter> _filter = new ArrayList<Filter>();
    protected String _locale;

    public List<WhereFilter> getWhereFilter() {
        return _whereFilter;
    }

    public List<Filter> getFilter() {
        return _filter;
    }

    public void setWhereFilter(List<WhereFilter> whereFilter) {
        _whereFilter = whereFilter;
    }

    public void setFilter(List<Filter> filter) {
        _filter = filter;
    }

    public String getLocale() {
        return _locale;
    }

    public void setLocale(String locale) {
        _locale = locale;
    }
}
