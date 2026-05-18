package ch.bfs.meb.sdl.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.service.DeliveryInterventionExportService;
import ch.bfs.meb.sdl.web.service.IInterventionService;
import ch.bfs.meb.sdl.web.utils.CsvUtils;
import ch.bfs.meb.sdl.web.utils.FileUtils;
import ch.bfs.meb.sdl.web.ws.sdlintervention.FileResult;
import ch.bfs.meb.sdl.web.ws.sdlintervention.Intervention;
import ch.bfs.meb.sdl.web.ws.sdlintervention.InterventionListResult;
import ch.bfs.meb.sdl.web.ws.sdlintervention.InterventionResult;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static ch.bfs.meb.integration.dto.ResultBase.OK;

@RestController
@RequestMapping("/deliveries/interventions")
@AllArgsConstructor
@Slf4j
public class DeliveryInterventionRestController {

    private final IInterventionService interventionService;
    private final DeliveryInterventionExportService exportService;
    private final WebLocalizationManager localizationManager;

    @GetMapping("/{id}")
    public ResponseEntity<List<Intervention>> getHistoriesForDelivery(@PathVariable Long id) {
        InterventionListResult result = interventionService.getInterventionsForDelivery(id);
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getInterventions());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when getting interventions: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PostMapping()
    public ResponseEntity<Intervention> createIntervention(@RequestBody Intervention request) {

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        request.setInterventionUser(user.getEmail());

        InterventionResult result = interventionService.insertIntervention(request);
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getIntervention());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when creating intervention: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @DeleteMapping("/{interventionId}")
    public ResponseEntity<Void> deleteIntervention(@PathVariable Long interventionId) {
        InterventionResult resultGet = interventionService.getInterventionById(interventionId);
        if (resultGet.getState() != OK) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "intervention not found"
            );
        }
        InterventionResult result = interventionService.deleteIntervention(resultGet.getIntervention());

        if (result.getState() == OK) {
            return ResponseEntity.ok().build();
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when deleting intervention: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
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
            FileResult result = interventionService.getPlausiReportFile(id);

            if (result.getState() != OK) {
                throw new RuntimeException(result.getMessage());
            }

            byte[] content = result.getBinaryFile();

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
            FileResult result = interventionService.getDeliveryFile(id);

            byte[] content = result.getBinaryFile();

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


