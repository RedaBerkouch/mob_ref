/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sspserver

 */
package ch.bfs.meb.sbg.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sbg.server.integration.dto.SbgFilter;
import ch.bfs.meb.sbg.server.integration.dto.SbgParameter;
import ch.bfs.meb.server.commons.integration.repository.IFilterCommonsRepository;

public interface IFilterRepository extends IFilterCommonsRepository {
    public List<SbgFilter> getFilters();

    public List<SbgFilter> getActiveFiltersForRefObjectAndNameDe(Long refObject, String nameDe);

    public List<SbgFilter> getActiveFiltersForRefObject(Long refObject);

    public List<SbgFilter> getActiveFilters();

    public List<SbgParameter> getParameters(Long filterId);

    public SbgFilter getFilterById(Long filterId);

    public List<SbgFilter> getDefaultPersonFilters();

    public SbgFilter updateFilter(SbgFilter filter);

    public SbgFilter insertFilter(SbgFilter filter);

    public void deleteFilter(SbgFilter filter);
}
