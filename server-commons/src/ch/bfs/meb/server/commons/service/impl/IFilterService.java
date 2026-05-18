/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IFilterService.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.FilterListResult;
import ch.bfs.meb.server.commons.integration.dto.FilterResult;

public interface IFilterService {

    public FilterListResult getFilters();

    public FilterListResult getFiltersForRefObjectAndNameDe(Long refObject, String nameDe);

    public FilterListResult getFiltersForRefObject(Long refObject);

    public FilterResult getFilterById(Long filterId);

    public FilterResult updateFilter(Filter filter);

    public FilterResult insertFilter(Filter filter);

    public FilterResult deleteFilter(Filter filter);
}
