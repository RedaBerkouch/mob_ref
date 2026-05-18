package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.service.DeliveryInterventionExportService;
import ch.bfs.meb.sbg.web.service.IActionService;
import ch.bfs.meb.sbg.web.utils.CsvUtils;
import ch.bfs.meb.sbg.web.utils.FileUtils;
import ch.bfs.meb.sbg.web.ws.sbgaction.Action;
import ch.bfs.meb.sbg.web.ws.sbgaction.ActionList;
import ch.bfs.meb.sbg.web.ws.sbgaction.ExportResult;
import ch.bfs.meb.sbg.web.ws.sbgaction.PlausireportResult;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult.OK;

@RestController
@RequestMapping("/deliveries/interventions")
@AllArgsConstructor
@Slf4j
public class DeliveryInterventionRestController {

    private final IActionService interventionService;
    private final DeliveryInterventionExportService exportService;
    private final WebLocalizationManager localizationManager;

    @GetMapping("/{id}")
    public ResponseEntity<List<Action>> getHistoriesForDelivery(@PathVariable Long id) {
        ActionList result = interventionService.getActions(id);
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getActions());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when getting interventions: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @DeleteMapping("/{interventionId}")
    public ResponseEntity<Void> deleteIntervention(@PathVariable Long interventionId) {
        throw new NotImplementedException("delete Intervention is not implemented");
    }

    @GetMapping("/{id}/export/csv")
    public ResponseEntity<byte[]> exportInterventionsCsv(@PathVariable Long id) {
        try {
            // Générer le CSV
            byte[] csvContent = exportService.generateCsvExport(id);

            // Ajouter le BOM UTF-8 pour Excel
            byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);

            // Préparer la réponse
            HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile("interventions", csvWithBom.length);

            return new ResponseEntity<>(csvWithBom, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating interventions CSV export", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV export failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/{id}/plausi_report")
    public ResponseEntity<byte[]> showLastPlausireport(@PathVariable Long id) {
        try {
            // Générer le contenu
            PlausireportResult result = interventionService.getPlausiReport(id);

            if (result.getState() != ResultBase.OK) {
                throw new RuntimeException(result.getMessage());
            }

            byte[] content = result.getPlausireport();

            // Construire le header
            byte[] fromZip = FileUtils.extractFromZipIfNeeded(content);
            HttpHeaders headers = FileUtils.getHttpHeadersForXlsxFile("PlausiReport", fromZip);

            return new ResponseEntity<>(fromZip, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating deliveries Plausi Report", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Plausi Report failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/{id}/delivery_report")
    public ResponseEntity<byte[]> showDelivery(@PathVariable Long id) {
        try {
            // Générer le contenu
            ExportResult result = interventionService.getDeliveryfile(id);

            byte[] content = result.getExport();

            // Construire le header
            byte[] zipContent = FileUtils.createZipFileResult(content, "DeliveryFile.zip");
            HttpHeaders headers = FileUtils.getHttpHeadersForZipFile("DeliveryFile", zipContent.length);

            return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating deliveries Plausi Report", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Plausi Report failed".getBytes(StandardCharsets.UTF_8));
        }
    }

}


