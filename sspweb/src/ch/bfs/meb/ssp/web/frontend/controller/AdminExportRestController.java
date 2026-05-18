package ch.bfs.meb.ssp.web.frontend.controller;

import ch.bfs.meb.ssp.web.service.IExportService;
import ch.bfs.meb.ssp.web.ws.sspexport.Export;
import ch.bfs.meb.ssp.web.ws.sspexport.ExportListResult;
import ch.bfs.meb.ssp.web.ws.sspexport.ExportResult;
import ch.bfs.meb.ssp.web.service.IExportService;
import ch.bfs.meb.ssp.web.ws.sspexport.ExportListResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller replacing AdminExportTableManager (DHTMLX version).
 * Provides CRUD operations for Exports via JSON, using the same service layer.
 *
 * Base path: /api/admin/exports
 */
@RestController
@RequestMapping("/admin/exports")
public class AdminExportRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminExportRestController.class);

    @Autowired
    private IExportService exportService;

    /**
     * GET /api/admin/exports
     * Retrieve all exports.
     */
    @GetMapping
    public ResponseEntity<ExportListResult> getAllExports() {
        LOGGER.debug("Fetching all exports");
        ExportListResult result = exportService.getExports();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/admin/exports/{id}
     * Retrieve an export by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExportResult> getExportById(@PathVariable("id") Long id) {
        LOGGER.debug("Fetching export by ID: {}", id);
        ExportResult result = exportService.getExportById(id);
        if (result == null || result.getExport() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/admin/exports
     * Create a new export.
     */
    @PostMapping
    public ResponseEntity<ExportResult> insertExport(@RequestBody Export export) {
        LOGGER.debug("Inserting new export: {}", export.getNameDe());
        ExportResult result = exportService.insertExport(export);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/admin/exports/{id}
     * Update an existing export.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExportResult> updateExport(@PathVariable("id") Long id, @RequestBody Export export) {
        LOGGER.debug("Updating export with ID {} ({})", id, export.getNameDe());
        export.setExportId(id);
        ExportResult result = exportService.updateExport(export);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/admin/exports/{id}
     * Delete an export.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ExportResult> deleteExport(@PathVariable("id") Long id) {
        LOGGER.debug("Deleting export with ID {}", id);
        Export export = new Export();
        export.setExportId(id);
        ExportResult result = exportService.deleteExport(export);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/admin/exports/undo/{id}
     * Retrieve the original (unmodified) version of an export (like "undo" in DHTMLX).
     */
    @GetMapping("/undo/{id}")
    public ResponseEntity<ExportResult> undo(@PathVariable("id") Long id) {
        LOGGER.debug("Undo request for export ID {}", id);
        ExportResult result = exportService.getExportById(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
