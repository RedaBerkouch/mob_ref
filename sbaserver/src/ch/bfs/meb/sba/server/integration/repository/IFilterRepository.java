/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaFilter;
import ch.bfs.meb.sba.server.integration.dto.SbaParameter;
import ch.bfs.meb.server.commons.integration.repository.IFilterCommonsRepository;

public interface IFilterRepository extends IFilterCommonsRepository {
    public List<SbaFilter> getFilters();

    public List<SbaFilter> getActiveFiltersForRefObjectAndNameDe(Long refObject, String nameDe);

    public List<SbaFilter> getActiveFiltersForRefObject(Long refObject);

    public List<SbaParameter> getParameters(Long filterId);

    public SbaFilter getFilterById(Long filterId);

    public SbaFilter updateFilter(SbaFilter filter);

    public SbaFilter insertFilter(SbaFilter filter);

    public void deleteFilter(SbaFilter filter);
}
