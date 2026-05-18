/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.bfs.meb.sba.server.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bfs.meb.sba.server.integration.dto.SbaFilter;
import ch.bfs.meb.sba.server.integration.repository.IFilterRepository;
import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.service.impl.IFilterServiceProvider;

public class FilterServiceProvider implements IFilterServiceProvider {
    private IFilterRepository _filterRepository;

    public void setFilterRepository(IFilterRepository filterRepository) {
        _filterRepository = filterRepository;
    }

    @Override
    public Filter getFilterById(Long filterId) {
        return _filterRepository.getFilterById(filterId);
    }

    @Override
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(new ArrayList<Filter>(_filterRepository.getFilters()));
    }

    @Override
    public List<Filter> getFiltersForRefObjectAndNameDe(Long refObject, String nameDe) {
        return Collections.unmodifiableList(new ArrayList<Filter>(_filterRepository.getActiveFiltersForRefObjectAndNameDe(refObject, nameDe)));
    }

    @Override
    public List<Filter> getFiltersForRefObject(Long refObject) {
        return Collections.unmodifiableList(new ArrayList<Filter>(_filterRepository.getActiveFiltersForRefObject(refObject)));
    }

    @Override
    public Filter insertFilter(Filter filter) {
        return _filterRepository.insertFilter(new SbaFilter(filter));
    }

    @Override
    public Filter updateFilter(Filter filter) {
        SbaFilter updatedFilter = new SbaFilter(filter);
        updatedFilter.setSbaParameters(_filterRepository.getParameters(filter.getFilterId()));
        return _filterRepository.updateFilter(updatedFilter);
    }

    @Override
    public void deleteFilter(Filter filter) {
        _filterRepository.deleteFilter(new SbaFilter(filter));
    }
}
