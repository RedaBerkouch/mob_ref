/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaserver

 */
package ch.admin.bfs.sbg.webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.bfs.meb.sbg.server.integration.dto.SbgFilter;
import ch.bfs.meb.sbg.server.integration.repository.IFilterRepository;
import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.service.impl.IFilterServiceProvider;
import lombok.Setter;

public class FilterServiceProvider implements IFilterServiceProvider {
    @Setter
    private IFilterRepository filterRepository;

    @Override
    public Filter getFilterById(Long filterId) {
        return filterRepository.getFilterById(filterId);
    }

    @Override
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(new ArrayList<Filter>(filterRepository.getFilters()));
    }

    @Override
    public List<Filter> getFiltersForRefObjectAndNameDe(Long refObject, String nameDe) {
        return Collections.unmodifiableList(new ArrayList<Filter>(filterRepository.getActiveFiltersForRefObjectAndNameDe(refObject, nameDe)));
    }

    @Override
    public List<Filter> getFiltersForRefObject(Long refObject) {
        return Collections.unmodifiableList(new ArrayList<Filter>(filterRepository.getActiveFiltersForRefObject(refObject)));
    }

    @Override
    public Filter insertFilter(Filter filter) {
        return filterRepository.insertFilter(new SbgFilter(filter));
    }

    @Override
    public Filter updateFilter(Filter filter) {
        //FIXME Implement method!
        throw new UnsupportedOperationException("Not implemented yet");
        //SbgFilter updatedFilter = new SbgFilter(filter);
        //updatedFilter.setSbgParameters(filterRepository.getParameters(filter.getFilterId()));
        //return filterRepository.updateFilter(updatedFilter);
    }

    @Override
    public void deleteFilter(Filter filter) {
        filterRepository.deleteFilter(new SbgFilter(filter));
    }
}
