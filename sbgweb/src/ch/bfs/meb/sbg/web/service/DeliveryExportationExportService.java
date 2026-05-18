package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.frontend.manager.ExportTableManager;
import ch.bfs.meb.sbg.web.utils.CsvExportRow;
import ch.bfs.meb.sbg.web.utils.CsvExportSheet;
//import ch.bfs.meb.sbg.web.ws.sbaexport.Export;
import ch.bfs.meb.sbg.web.ws.sbgmacro.Macro;
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
    private static final String COLUMN_NAME_NAME_KEY = "exportTable.column.name.name";
    private static final String COLUMN_DESCRIPTION_NAME_KEY = "exportTable.column.description.name";
    private static final String COLUMN_PARAMETER_NAME_KEY = "exportTable.column.parameter.name";

    private final IMacroService exportService;
    private final WebLocalizationManager localizationManager;

    public byte[] generateCsvExport() {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());

            // Récupérer les données
            Stream.of(exportService.getExportMacros())
                    .filter(Objects::nonNull)
                    .filter(exportListResult -> !CollectionUtils.isEmpty(exportListResult.getMacros()))
                    .flatMap(exportListResult -> exportListResult.getMacros().stream())
                    .forEach(export -> csv.add(formatExportRow(export)));

        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("CSV generation failed", e);
        }

        return csv.getSheet().getBytes(StandardCharsets.UTF_8);
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(COLUMN_NAME_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DESCRIPTION_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PARAMETER_NAME_KEY))
                .getRow();
    }

    private String formatExportRow(Macro export) {
        return new CsvExportRow()
                .add(localizationManager.getLanguage(), export.getNameFr(), null, export.getNameDe())
                .add(localizationManager.getLanguage(), export.getDescriptionFr(), null, export.getDescriptionDe())
                .add(localizationManager.getLanguage(), export.getParameters())
                .getRow();
    }
}