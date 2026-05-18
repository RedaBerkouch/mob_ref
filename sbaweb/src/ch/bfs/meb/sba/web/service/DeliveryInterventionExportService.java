package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.frontend.manager.InterventionTableManager;
import ch.bfs.meb.sba.web.utils.CsvExportRow;
import ch.bfs.meb.sba.web.utils.CsvExportSheet;
import ch.bfs.meb.sba.web.ws.sbaintervention.Intervention;
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

    private final IInterventionService interventionService;
    private final WebLocalizationManager localizationManager;

    public byte[] generateCsvExport(Long id) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());

            // Récupérer les données
            Stream.of(interventionService.getInterventionsForDelivery(id))
                    .filter(Objects::nonNull)
                    .filter(result -> !CollectionUtils.isEmpty(result.getInterventions()))
                    .flatMap(result -> result.getInterventions().stream())
                    .forEach(intervention -> csv.add(formatInterventionRow(intervention)));

        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("CSV generation failed", e);
        }

        return csv.getSheet().getBytes(StandardCharsets.UTF_8);
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(InterventionTableManager.COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(InterventionTableManager.COLUMN_VERSION_NAME_KEY))
                .add(localizationManager.getMessage(InterventionTableManager.COLUMN_INTERVENTION_USER_NAME_KEY))
                .add(localizationManager.getMessage(InterventionTableManager.COLUMN_INTERVENTION_DATE_NAME_KEY))
                .add(localizationManager.getMessage(InterventionTableManager.COLUMN_TYPE_NAME_KEY))
                .add(localizationManager.getMessage(InterventionTableManager.COLUMN_REPORT_NAME_KEY))
                .getRow();
    }

    private String formatInterventionRow(Intervention intervention) {
        return new CsvExportRow()
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, intervention.getCanton()))
                .add(intervention.getVersion())
                .add(intervention.getInterventionUser())
                .add(intervention.getInterventionDate())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_INTERVENTIONTYPE, intervention.getType()))
                .add(localizationManager.getLanguage(), intervention.getReportFr(), intervention.getReportIt(), intervention.getReportDe())
                .getRow();
    }
}