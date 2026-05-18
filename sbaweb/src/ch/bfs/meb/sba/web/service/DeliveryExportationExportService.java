package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.frontend.manager.ExportTableManager;
import ch.bfs.meb.sba.web.utils.CsvExportRow;
import ch.bfs.meb.sba.web.utils.CsvExportSheet;
import ch.bfs.meb.sba.web.ws.sbaexport.Export;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Service("deliveryExportationExportService")
@Slf4j
@AllArgsConstructor
public class DeliveryExportationExportService {

    private final IExportService exportService;
    private final WebLocalizationManager localizationManager;

    public byte[] generateCsvExport() {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());

            // Récupérer les données
            Stream.of(exportService.getActiveExports())
                    .filter(Objects::nonNull)
                    .filter(exportListResult -> !CollectionUtils.isEmpty(exportListResult.getExports()))
                    .flatMap(exportListResult -> exportListResult.getExports().stream())
                    .forEach(export -> csv.add(formatExportRow(export)));

        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("CSV generation failed", e);
        }

        return csv.getSheet().getBytes(StandardCharsets.UTF_8);
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(ExportTableManager.COLUMN_NAME_NAME_KEY))
                .add(localizationManager.getMessage(ExportTableManager.COLUMN_DESCRIPTION_NAME_KEY))
                .add(localizationManager.getMessage(ExportTableManager.COLUMN_PARAMETER_NAME_KEY))
                .getRow();
    }

    private String formatExportRow(Export export) {
        return new CsvExportRow()
                .add(localizationManager.getLanguage(), export.getNameFr(), export.getNameIt(), export.getNameDe())
                .add(localizationManager.getLanguage(), export.getDescriptionFr(), export.getDescriptionIt(), export.getDescriptionDe())
                .add(localizationManager.getLanguage(), export.getParameters())
                .getRow();
    }
}