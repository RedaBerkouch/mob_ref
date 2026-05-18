/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.bfs.meb.web.commons.util;

import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;

public interface IFilterService {
    public WebFilterListResult getFilters();

    public WebFilterResult getFilterById(Long filterId);

    public WebFilterListResult getFiltersForRefObjectAndNameDe(Long refObject, String nameDe);

    public WebFilterListResult getFiltersForRefObject(Long refObject);

    public WebFilterResult updateFilter(WebFilter filter);

    public WebFilterResult insertFilter(WebFilter filter);

    public WebFilterResult deleteFilter(WebFilter filter);
}
