package ch.bfs.meb.ssp.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.ssp.web.service.DeliveryExportationExportService;
import ch.bfs.meb.ssp.web.service.IExportService;
import ch.bfs.meb.ssp.web.utils.CsvUtils;
import ch.bfs.meb.ssp.web.utils.FileUtils;
import ch.bfs.meb.ssp.web.ws.sspexport.Export;
import ch.bfs.meb.ssp.web.ws.sspexport.ExportListResult;
import ch.bfs.meb.ssp.web.ws.sspexport.FileResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static ch.bfs.meb.integration.dto.ResultBase.OK;

@RestController
@RequestMapping("/deliveries/exportations")
@AllArgsConstructor
@Slf4j
public class DeliveryExportationController {

    private final IExportService exportService;
    private final DeliveryExportationExportService exportGenerator;
    private final WebLocalizationManager localizationManager;

    @GetMapping()
    public ResponseEntity<List<Export>> getExports() {
        ExportListResult result = exportService.getActiveExports();
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getExports());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when getting exports: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }


    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportDeliveriesCsv() {
        try {
            // Générer le CSV
            byte[] csvContent = exportGenerator.generateCsvExport();

            // Ajouter le BOM UTF-8 pour Excel
            byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);

            // Préparer la réponse
            HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile("exports", csvWithBom.length);

            return new ResponseEntity<>(csvWithBom, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating deliveries CSV export", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV export failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    @PostMapping()
    public ResponseEntity<byte[]> runExport(@RequestBody Export export) {
        // Générer le CSV
        FileResult result = exportService.runExport(export, localizationManager.getLanguage().toLowerCase());

        if (result.getState() == WebFilterListResult.OK) {
            byte[] content = result.getBinaryFile();


            // Construire le header
            if (StringUtils.hasText(result.getFilename()) && result.getFilename().endsWith("xlsx")){
                HttpHeaders headers = FileUtils.getHttpHeadersForXlsxFile(result.getFilename(), content);
                return new ResponseEntity<>(content, headers, HttpStatus.OK);
            } else {
                byte[] zipContent = FileUtils.createZipFileResult(content, result.getFilename());
                HttpHeaders headers = FileUtils.getHttpHeadersForZipFile(result.getFilename(), zipContent.length);
                return new ResponseEntity<>(zipContent, headers, HttpStatus.OK);
            }
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when getting exports: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

}


