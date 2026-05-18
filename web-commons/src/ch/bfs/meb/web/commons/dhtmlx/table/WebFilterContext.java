/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WebFilterContext {
    protected List<WebWhereFilter> _whereFilter = new ArrayList<WebWhereFilter>();
    protected List<WebFilter> _filter = new ArrayList<WebFilter>();
    protected String _locale;

    public WebFilterContext() {
    }

    public WebFilterContext(WebFilterContext filterContext) {
        for (WebWhereFilter whereFilter : filterContext.getWhereFilter()) {
            _whereFilter.add(whereFilter);
        }
        for (WebFilter filter : filterContext.getFilter()) {
            _filter.add(filter);
        }
        _locale = filterContext.getLocale();
    }

    public List<WebWhereFilter> getWhereFilter() {
        return _whereFilter;
    }

    public List<WebFilter> getFilter() {
        return _filter;
    }

    public void setWhereFilter(List<WebWhereFilter> whereFilter) {
        _whereFilter = whereFilter;
    }

    public void setFilter(List<WebFilter> filter) {
        _filter = filter;
    }

    public String getLocale() {
        return _locale;
    }

    public void setLocale(String locale) {
        _locale = locale;
    }


}
