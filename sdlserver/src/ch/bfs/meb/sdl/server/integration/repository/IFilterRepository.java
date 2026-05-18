/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlserver

 */
package ch.bfs.meb.sdl.server.integration.repository;

import java.util.List;

import ch.bfs.meb.sdl.server.integration.dto.SdlFilter;
import ch.bfs.meb.sdl.server.integration.dto.SdlParameter;
import ch.bfs.meb.server.commons.integration.repository.IFilterCommonsRepository;

public interface IFilterRepository extends IFilterCommonsRepository {
    public List<SdlFilter> getFilters();

    public List<SdlFilter> getActiveFiltersForRefObjectAndNameDe(Long refObject, String nameDe);

    public List<SdlFilter> getActiveFiltersForRefObject(Long refObject);

    public List<SdlParameter> getParameters(Long filterId);

    public SdlFilter getFilterById(Long filterId);

    public SdlFilter updateFilter(SdlFilter filter);

    public SdlFilter insertFilter(SdlFilter filter);

    public void deleteFilter(SdlFilter filter);
}
