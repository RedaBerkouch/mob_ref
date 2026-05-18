/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons

  $Id: IFilterService.java 228 2009-11-24 09:06:15Z dzw $
 */
package ch.bfs.meb.server.commons.service.impl;

import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Filter;

public interface IFilterServiceProvider {
    public List<Filter> getFilters();

    public List<Filter> getFiltersForRefObjectAndNameDe(Long refObject, String nameDe);

    public List<Filter> getFiltersForRefObject(Long refObject);

    public Filter getFilterById(Long filterId);

    public Filter updateFilter(Filter filter);

    public Filter insertFilter(Filter filter);

    public void deleteFilter(Filter filter);
}
