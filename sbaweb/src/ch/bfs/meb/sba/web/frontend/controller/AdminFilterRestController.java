package ch.bfs.meb.sba.web.frontend.controller;

import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;
import ch.bfs.meb.web.commons.util.IFilterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller replacing the legacy AdminFilterTableManager (DHTMLX version).
 * Provides CRUD operations for filters via JSON, using the same WebFilter model.
 *
 * Base path: /api/admin/filters
 */
@RestController
@RequestMapping("/admin/filters")
public class AdminFilterRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminFilterRestController.class);

    @Autowired
    private IFilterService filterService;

    /**
     * GET /api/admin/filters
     * Retrieve all filters.
     */
    @GetMapping
    public ResponseEntity<WebFilterListResult> getAllFilters() {
        LOGGER.debug("Fetching all filters");
        WebFilterListResult result = filterService.getFilters();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/admin/filters/{id}
     * Retrieve a filter by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WebFilterResult> getFilterById(@PathVariable("id") Long id) {
        LOGGER.debug("Fetching filter by ID: {}", id);
        WebFilterResult result = filterService.getFilterById(id);
        if (result == null || result.getFilter() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/admin/filters/refObject/{refObject}
     * Retrieve all filters for a given reference object.
     */
    @GetMapping("/refObject/{refObject}")
    public ResponseEntity<WebFilterListResult> getFiltersForRefObject(@PathVariable("refObject") Long refObject) {
        LOGGER.debug("Fetching filters for refObject: {}", refObject);
        WebFilterListResult result = filterService.getFiltersForRefObject(refObject);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/admin/filters
     * Create a new filter.
     */
    @PostMapping
    public ResponseEntity<WebFilterResult> insertFilter(@RequestBody WebFilter filter) {
        LOGGER.debug("Inserting new filter: {}", filter.getNameDe());
        WebFilterResult result = filterService.insertFilter(filter);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/admin/filters/{id}
     * Update an existing filter.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WebFilterResult> updateFilter(@PathVariable("id") Long id, @RequestBody WebFilter filter) {
        LOGGER.debug("Updating filter with ID {} ({})", id, filter.getNameDe());
        filter.setFilterId(id);
        WebFilterResult result = filterService.updateFilter(filter);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/admin/filters/{id}
     * Delete a filter.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<WebFilterResult> deleteFilter(@PathVariable("id") Long id) {
        LOGGER.debug("Deleting filter with ID {}", id);
        WebFilter filter = new WebFilter();
        filter.setFilterId(id);
        WebFilterResult result = filterService.deleteFilter(filter);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/admin/filters/undo/{id}
     * Retrieve the original (unmodified) version of a filter.
     */
    @GetMapping("/undo/{id}")
    public ResponseEntity<WebFilterResult> undo(@PathVariable("id") Long id) {
        LOGGER.debug("Undo request for filter ID {}", id);
        WebFilterResult result = filterService.getFilterById(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
