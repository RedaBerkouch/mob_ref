package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.frontend.manager.DeliveryTableManager;
import ch.bfs.meb.sba.web.utils.CsvExportRow;
import ch.bfs.meb.sba.web.utils.CsvExportSheet;
import ch.bfs.meb.sba.web.ws.sbadelivery.SbaDelivery;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Service("deliveryExportService")
@Slf4j
@AllArgsConstructor
public class DeliveryExportService {

    private final DeliveryService deliveryService;
    private final WebLocalizationManager localizationManager;

    public byte[] generateCsvExport(Long version, Long cantonId, WebFilterContext filterContext) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());

            // Récupérer les données
            Stream.of(deliveryService.getDeliveries(-1, -1, new WebSortContext(), filterContext, version, cantonId))
                    .filter(Objects::nonNull)
                    .filter(result -> !CollectionUtils.isEmpty(result.getDeliveries()))
                    .flatMap(result -> result.getDeliveries().stream())
                    .forEach(delivery -> csv.add(formatDeliveryRow(delivery)));

        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("CSV generation failed", e);
        }

        return csv.getSheet().getBytes(StandardCharsets.UTF_8);
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_DELIVERYID_NAME_KEY))
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_VERSION_NAME_KEY))
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_STATUS_NAME_KEY))
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_PLAUSISTATUS_NAME_KEY))
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_DELIVERYDATE_NAME_KEY))
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_DELIVERYUSER_NAME_KEY))
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_PLAUSIPERSON_NAME_KEY))
                .add(localizationManager.getMessage(DeliveryTableManager.COLUMN_PLAUSIQUALIFICATION_NAME_KEY))
                .getRow();
    }

    private String formatDeliveryRow(SbaDelivery delivery) {
        return new CsvExportRow()
                .add(delivery.getDeliveryCode())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, delivery.getCanton()))
                .add(delivery.getVersion())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_DELIVERYSTATUS, delivery.getDeliveryStatus()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_PLAUSISTATUS, delivery.getPlausiStatus()))
                .add(delivery.getCreationDate())
                .add(delivery.getCreationUser())
                .add(localizationManager.getMessage(delivery.getNrPlausiPerson()))
                .add(localizationManager.getMessage(delivery.getNrPlausiQualification()))
                .getRow();
    }
}