package ch.bfs.meb.ssp.web.frontend.controller.initialisation;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.ssp.web.service.CantonInterventionExportService;
import ch.bfs.meb.ssp.web.service.ICantonInterventionService;
import ch.bfs.meb.ssp.web.utils.CsvUtils;
import ch.bfs.meb.ssp.web.utils.FileUtils;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.CantonIntervention;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.CantonInterventionListResult;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.CantonInterventionResult;
import ch.bfs.meb.ssp.web.ws.sspcantonintervention.FileResult;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/initialisations/canton-interventions")
@AllArgsConstructor
@Slf4j
public class CantonInterventionRestController {
    
    private final ICantonInterventionService cantonInterventionService;
    private final CantonInterventionExportService cantonInterventionExportService;
    private final WebLocalizationManager localizationManager;

    /* ******
     * CRUD *
     * ******/
    /**
     * Get a cantonIntervention based on its id.
     *
     * @param id the cantonIntervention id to get.
     * @return a CantonIntervention object
     */
    @GetMapping("/{id}")
    public ResponseEntity<CantonIntervention> getById(@PathVariable Long id) {

        CantonInterventionResult cantonInterventionResult = cantonInterventionService.getInterventionById(id);

        if (cantonInterventionResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(cantonInterventionResult.getIntervention());
        }

        String message = localizationManager.getMessage(cantonInterventionResult.getMessage());
        log.error("Error when retrieving cantonIntervention: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Get all cantonInterventions, filtered on the selected cantonId provided in parameter
     *
     * @param cantonId the canton id to filter the cantonIntervention on
     * @return List of all CantonIntervention objects, filtered on the selected cantonId
     */
    @PostMapping("/search")
    public ResponseEntity<List<CantonIntervention>> getAll(@RequestBody Long cantonId) {

        CantonInterventionListResult results = null;

        if (cantonId != null) {
            results = cantonInterventionService.getInterventionsForCanton(cantonId);
        }

        List<CantonIntervention> cantonInterventions =
                results != null ? results.getInterventions() : new ArrayList<>();

        return ResponseEntity.ok(cantonInterventions);
    }

    /**
     * Create a CantonIntervention.
     *
     * @param cantonIntervention CantonIntervention to create
     * @return The new created CantonIntervention
     */
    @PutMapping
    public ResponseEntity<CantonIntervention> create(@RequestBody CantonIntervention cantonIntervention) {
        CantonInterventionResult cantonInterventionResult
                = cantonInterventionService.insertIntervention(cantonIntervention);

        if (cantonInterventionResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(cantonInterventionResult.getIntervention());
        }

        String message = localizationManager.getMessage(cantonInterventionResult.getMessage());
        log.error("Error while creating cantonIntervention: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Update an existing CantonIntervention.
     *
     * @param cantonIntervention  Updated CantonIntervention object
     * @return The updated CantonIntervention
     */
    @PostMapping
    public ResponseEntity<CantonIntervention> update(@RequestBody CantonIntervention cantonIntervention) {
        CantonInterventionResult cantonInterventionResult = cantonInterventionService.updateIntervention(cantonIntervention);

        if (cantonInterventionResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(cantonInterventionResult.getIntervention());
        }
        String message = localizationManager.getMessage(cantonInterventionResult.getMessage());
        log.error("Error while updating cantonIntervention: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Delete a CantonIntervention
     *
     * @param id CantonIntervention's id to delete
     * @return {@code true} if deleted successfully, else {@code false}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        CantonInterventionResult interventionToDelete = cantonInterventionService.getInterventionById(id);

        if (interventionToDelete.getState() != ResultBase.OK) {
            String message = localizationManager.getMessage(interventionToDelete.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }

        CantonInterventionResult cantonInterventionResult =
                cantonInterventionService.deleteIntervention(interventionToDelete.getIntervention());

        if (cantonInterventionResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok().build();
        }

        String message = localizationManager.getMessage(cantonInterventionResult.getMessage());
        log.error("Error while deleting CantonIntervention: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /* ******* *
     * Actions *
     * ******* */
    @GetMapping("/export_csv/{id}")
    public ResponseEntity<byte[]> exportCsvAction(@PathVariable Long id) {
        Long cantonId = Optional.ofNullable(id).orElse(0L);

        try {
            // Générer le CSV
            byte[] csvContent = cantonInterventionExportService.generateCsvExport(cantonId);

            // Ajouter le BOM UTF-8 pour Excel
            byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);

            // Construire le header
            HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile("Interventions", csvWithBom.length);

            return new ResponseEntity<>(csvWithBom, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating interventions CSV export", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV export failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/show_plausi_report/{id}")
    public ResponseEntity<byte[]> showPlausiReportAction(@PathVariable Long id) {
        try {
            // Générer le contenu
            FileResult result = cantonInterventionService.getPlausiReportFile(id);

            if (result.getState() != ResultBase.OK) {
                throw new RuntimeException(result.getMessage());
            }

            byte[] content = result.getBinaryFile();

            // Construire le header
            byte[] fromZip = FileUtils.extractFromZipIfNeeded(content);
            HttpHeaders headers = FileUtils.getHttpHeadersForXlsxFile("PlausiReport", fromZip);

            return new ResponseEntity<>(fromZip, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating cantons Plausi Report", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Plausi Report failed".getBytes(StandardCharsets.UTF_8));
        }
    }
}
