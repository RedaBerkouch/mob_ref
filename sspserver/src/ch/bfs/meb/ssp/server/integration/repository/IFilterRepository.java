/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.ssp.server.integration.repository;

import java.util.List;

import ch.bfs.meb.server.commons.integration.repository.IFilterCommonsRepository;
import ch.bfs.meb.ssp.server.integration.dto.SspFilter;
import ch.bfs.meb.ssp.server.integration.dto.SspParameter;

public interface IFilterRepository extends IFilterCommonsRepository {
    public List<SspFilter> getFilters();

    public List<SspFilter> getActiveFiltersForRefObjectAndNameDe(Long refObject, String nameDe);

    public List<SspFilter> getActiveFiltersForRefObject(Long refObject);

    public List<SspParameter> getParameters(Long filterId);

    public SspFilter getFilterById(Long filterId);

    public SspFilter updateFilter(SspFilter filter);

    public SspFilter insertFilter(SspFilter filter);

    public void deleteFilter(SspFilter filter);
}
