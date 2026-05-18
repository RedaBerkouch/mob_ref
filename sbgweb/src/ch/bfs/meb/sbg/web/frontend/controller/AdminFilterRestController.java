package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;
import ch.bfs.meb.web.commons.util.IFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/filters")
public class AdminFilterRestController {

    private final IFilterService filterService;

    @Autowired
    public AdminFilterRestController(IFilterService filterService) {
        this.filterService = filterService;
    }

    @GetMapping
    public ResponseEntity<WebFilterListResult> getAllFilters() {
        return ResponseEntity.ok(filterService.getFilters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebFilterResult> getFilterById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(filterService.getFilterById(id));
    }

    @PostMapping
    public ResponseEntity<WebFilterResult> createFilter(@RequestBody WebFilter filter) {
        return ResponseEntity.ok(filterService.insertFilter(filter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebFilterResult> updateFilter(@PathVariable("id") Long id,
                                                        @RequestBody WebFilter filter) {
        filter.setFilterId(id);
        return ResponseEntity.ok(filterService.updateFilter(filter));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WebFilterResult> deleteFilter(@PathVariable("id") Long id) {
        WebFilter filter = new WebFilter();
        filter.setFilterId(id);
        return ResponseEntity.ok(filterService.deleteFilter(filter));
    }
}