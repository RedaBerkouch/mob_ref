/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: FilterServiceImpl.java 564 2008-11-28 12:57:40Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import ch.admin.bfs.sbg.db.dao.MacroParameterDAO;
import ch.bfs.meb.sbg.server.integration.dto.SbgFilter;
import ch.bfs.meb.sbg.server.integration.repository.IFilterRepository;
import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.FilterListResult;
import ch.bfs.meb.server.commons.integration.dto.FilterResult;
import ch.bfs.meb.server.commons.service.impl.IFilterService;
import lombok.Setter;

/**
 * Service implementation of {@link ch.bfs.meb.server.commons.service.impl.IFilterService}.
 *
 * @author $Author: lsc $
 * @version $Revision: 564 $
 */
@Service
public class FilterServiceImpl implements IFilterService, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterServiceImpl.class);

    private TransactionTemplate txTemplate;

    @Setter
    private MacroParameterDAO macroParameterDAO;

    @Setter
    private IFilterRepository filterRepository;

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        txTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    @Transactional(readOnly = true)
    public FilterListResult getFilters() {
        List<Filter> filters = new ArrayList<Filter>(filterRepository.getActiveFilters());
        return new FilterListResult(filters);
    }

    @Override
    public FilterListResult getFiltersForRefObjectAndNameDe(Long refObject, String nameDe) {
        List<SbgFilter> guiSbgFilters = Collections
                .unmodifiableList(new ArrayList<SbgFilter>(filterRepository.getActiveFiltersForRefObjectAndNameDe(refObject, nameDe)));
        List<Filter> guiFilters = new ArrayList<Filter>(guiSbgFilters);
        return new FilterListResult(guiFilters);
    }

    @Override
    public FilterListResult getFiltersForRefObject(Long refObject) {
        List<SbgFilter> guiSbgFilters = Collections.unmodifiableList(new ArrayList<SbgFilter>(filterRepository.getActiveFiltersForRefObject(refObject)));
        List<Filter> guiFilters = new ArrayList<Filter>(guiSbgFilters);
        return new FilterListResult(guiFilters);
    }

    @Override
    public FilterResult getFilterById(Long filterId) {
        SbgFilter filter = filterRepository.getFilterById(filterId);

        if (filter == null) {
            return new FilterResult("Could not find filter with id: " + filterId);
        } else {
            return new FilterResult(filter);
        }
    }

    @Override
    @Transactional
    public FilterResult updateFilter(Filter filter) {
        SbgFilter updatedFilter;
        FilterResult result;

        updatedFilter = filterRepository.updateFilter(new SbgFilter(filter));

        result = new FilterResult(updatedFilter);

        return result;
    }

    @Override
    @Transactional
    public FilterResult insertFilter(Filter filter) {
        SbgFilter newFilter;
        //XXX: Really modify input parameter?
        filter.setIsActive(true);
        newFilter = filterRepository.updateFilter(new SbgFilter(filter));

        return new FilterResult(newFilter);
    }

    @Override
    @Transactional
    public FilterResult deleteFilter(Filter filter) {
        filterRepository.deleteFilter(filterRepository.getFilterById(filter.getFilterId()));
        return new FilterResult();
    }
}