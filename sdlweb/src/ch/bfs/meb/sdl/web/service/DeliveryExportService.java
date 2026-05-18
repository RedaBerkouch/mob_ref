package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.frontend.manager.DeliveryTableManager;
import ch.bfs.meb.sdl.web.utils.CsvExportRow;
import ch.bfs.meb.sdl.web.utils.CsvExportSheet;
import ch.bfs.meb.sdl.web.ws.sdldelivery.SdlDelivery;
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

    private static final String COLUMN_DELIVERYID_NAME_KEY = "deliveryTable.column.deliveryid.name";
    public static final String COLUMN_CANTON_NAME_KEY = "deliveryTable.column.canton.name";
    public static final String COLUMN_VERSION_NAME_KEY = "deliveryTable.column.version.name";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "deliveryTable.column.plausistatus.name";
    public static final String COLUMN_STATUS_NAME_KEY = "deliveryTable.column.status.name";
    public static final String COLUMN_DELIVERYDATE_NAME_KEY = "deliveryTable.column.deliverydate.name";
    public static final String COLUMN_DELIVERYUSER_NAME_KEY = "deliveryTable.column.deliveryuser.name";
    public static final String COLUMN_PLAUSISCHOOL_NAME_KEY = "deliveryTable.column.plausischool.name";
    public static final String COLUMN_PLAUSICLASS_NAME_KEY = "deliveryTable.column.plausiclass.name";
    public static final String COLUMN_PLAUSILEARNER_NAME_KEY = "deliveryTable.column.plausilearner.name";

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
                .add(localizationManager.getMessage(COLUMN_DELIVERYID_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VERSION_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_STATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSISTATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DELIVERYDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DELIVERYUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSISCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSICLASS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSILEARNER_NAME_KEY))
                .getRow();
    }

    private String formatDeliveryRow(SdlDelivery delivery) {
        return new CsvExportRow()
                .add(delivery.getDeliveryCode())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, delivery.getCanton()))
                .add(delivery.getVersion())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_DELIVERYSTATUS, delivery.getDeliveryStatus()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_PLAUSISTATUS, delivery.getPlausiStatus()))
                .add(delivery.getCreationDate())
                .add(delivery.getCreationUser())
                .add(localizationManager.getMessage(delivery.getNrPlausiSchool()))
                .add(localizationManager.getMessage(delivery.getNrPlausiClass()))
                .add(localizationManager.getMessage(delivery.getNrPlausiLearner()))
                .getRow();
    }
}