package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.utils.CsvExportRow;
import ch.bfs.meb.sdl.web.utils.CsvExportSheet;
import ch.bfs.meb.sdl.web.ws.sdlcantonintervention.CantonIntervention;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Service("cantonInterventionExportService")
@Slf4j
@AllArgsConstructor
public class CantonInterventionExportService {

    private final ICantonInterventionService cantonInterventionService;
    private final WebLocalizationManager localizationManager;

    // CantonIntervention columns to export
    private static final String COLUMN_CANTON_NAME_KEY = "interventionTable.column.canton.name";
    private static final String COLUMN_VERSION_NAME_KEY = "interventionTable.column.version.name";
    private static final String COLUMN_INTERVENTION_USER_NAME_KEY = "interventionTable.column.interventionuser.name";
    private static final String COLUMN_INTERVENTION_DATE_NAME_KEY = "interventionTable.column.interventiondate.name";
    private static final String COLUMN_TYPE_NAME_KEY = "interventionTable.column.type.name";
    private static final String COLUMN_TEXT_NAME_KEY = "interventionTable.column.text.name";

    public byte[] generateCsvExport(Long cantonId) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());

            // Récupérer les données
            Stream.of(cantonInterventionService.getInterventionsForCanton(cantonId))
                    .filter(Objects::nonNull)
                    .filter(resultList -> !CollectionUtils.isEmpty(resultList.getInterventions()))
                    .flatMap(resultList -> resultList.getInterventions().stream())
                    .forEach(cantonIntervention -> csv.add(formatExportRow(cantonIntervention)));

        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("CSV generation failed", e);
        }

        return csv.getSheet().getBytes(StandardCharsets.UTF_8);
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VERSION_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_INTERVENTION_USER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_INTERVENTION_DATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_TYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_TEXT_NAME_KEY))
                .getRow();
    }

    private String formatExportRow(CantonIntervention cantonIntervention) {
        return new CsvExportRow()
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, cantonIntervention.getCanton()))
                .add(cantonIntervention.getVersion())
                .add(cantonIntervention.getInterventionUser())
                .add(cantonIntervention.getInterventionDate())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.SDL_CANTONINTERVENTIONTYPE, cantonIntervention.getType()))
                .add(cantonIntervention.getText())
                .getRow();
    }
}