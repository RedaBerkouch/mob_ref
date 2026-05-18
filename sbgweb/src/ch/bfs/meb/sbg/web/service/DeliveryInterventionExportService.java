package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.utils.CsvExportRow;
import ch.bfs.meb.sbg.web.utils.CsvExportSheet;
import ch.bfs.meb.sbg.web.ws.sbgaction.Action;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Service("deliveryInterventionExportService")
@Slf4j
@AllArgsConstructor
public class DeliveryInterventionExportService {

    private static final String COLUMN_CANTON_NAME_KEY = "actionTable.column.canton.name";
    private static final String COLUMN_VERSION_NAME_KEY = "actionTable.column.version.name";
    private static final String COLUMN_ACTIONUSER_NAME_KEY = "actionTable.column.actionuser.name";
    private static final String COLUMN_EXECUTIONDATE_NAME_KEY = "actionTable.column.executiondate.name";
    private static final String COLUMN_TYPE_NAME_KEY = "actionTable.column.type.name";
    private static final String COLUMN_PLAUSIREPORT_NAME_KEY = "actionTable.column.plausireport.name";
    private static final String COLUMN_VALIDATION_NAME_KEY = "actionTable.column.validation.name";

    private final IActionService interventionService;
    private final WebLocalizationManager localizationManager;

    public byte[] generateCsvExport(Long id) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());

            // Récupérer les données
            Stream.of(interventionService.getActions(id))
                    .filter(Objects::nonNull)
                    .filter(result -> !CollectionUtils.isEmpty(result.getActions()))
                    .flatMap(result -> result.getActions().stream())
                    .forEach(intervention -> csv.add(formatInterventionRow(intervention)));

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
                .add(localizationManager.getMessage(COLUMN_ACTIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EXECUTIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_TYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSIREPORT_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VALIDATION_NAME_KEY))
                .getRow();
    }

    private String formatInterventionRow(Action intervention) {
        return new CsvExportRow()
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, intervention.getCanton()))
                .add(intervention.getVersion())
                .add(intervention.getActionuser())
                .add(intervention.getExecutiondate())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.SBG_ACTIONTYPE, intervention.getType()))
                .add(intervention.getPlausireportname())
                .add(localizationManager.getLanguage(), intervention.getValidationreportFr(), intervention.getValidationreportFr(), intervention.getValidationreportDe())
                .getRow();
    }
}