package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.utils.CsvExportRow;
import ch.bfs.meb.sbg.web.utils.CsvExportSheet;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDelivery;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebWhereFilter;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static ch.bfs.meb.sbg.web.frontend.controller.DeliveryRestController.COLUMN_CANTON_ID;
import static ch.bfs.meb.sbg.web.frontend.controller.DeliveryRestController.COLUMN_VERSION_ID;

@Service("deliveryExportService")
@Slf4j
@AllArgsConstructor
public class DeliveryExportService {

    public static final String COLUMN_CANTON_NAME_KEY = "deliveryTable.column.canton.name";
    public static final String COLUMN_VERSION_NAME_KEY = "deliveryTable.column.version.name";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "deliveryTable.column.plausistatus.name";
    public static final String COLUMN_STATUS_NAME_KEY = "deliveryTable.column.status.name";
    public static final String COLUMN_DELIVERYDATE_NAME_KEY = "deliveryTable.column.deliverydate.name";
    public static final String COLUMN_DELIVERYUSER_NAME_KEY = "deliveryTable.column.deliveryuser.name";
    public static final String COLUMN_PLAUSIPERSON_NAME_KEY = "deliveryTable.column.plausiperson.name";
    public static final String COLUMN_PLAUSIEVENT_NAME_KEY = "deliveryTable.column.plausievent.name";
    public static final String COLUMN_NOTVALID_NAME_KEY = "deliveryTable.column.notvalid.name";

    private final DeliveryService deliveryService;
    private final WebLocalizationManager localizationManager;

    public byte[] generateCsvExport(Long version, Long cantonId, WebFilterContext filterContext) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());

            // Récupérer les données
            List<WebWhereFilter> filters = filterContext.getWhereFilter();
            if (!filters.isEmpty()) filters.get(filters.size() - 1).setRelation("AND");
            if (version != null) filters.add(buildFilter(COLUMN_VERSION_ID, version.toString()));
            if (cantonId != null && cantonId > 0L) filters.add(buildFilter(COLUMN_CANTON_ID, cantonId.toString()));

            Stream.of(deliveryService.getFilteredDeliveries(filterContext))
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

    private WebWhereFilter buildFilter(String attribute, String value) {
        WebWhereFilter filter = new WebWhereFilter();
        filter.setAttribute(attribute);
        filter.setOperator("=");
        filter.setRelation("AND");
        filter.setValue(value);
        return filter;
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VERSION_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_STATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSISTATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DELIVERYDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DELIVERYUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSIPERSON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSIEVENT_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_NOTVALID_NAME_KEY))
                .getRow();
    }

    private String formatDeliveryRow(SbgDelivery delivery) {
        return new CsvExportRow()
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, delivery.getCanton()))
                .add(delivery.getVersion())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.SBG_DELIVERYSTATUS, delivery.getStatus()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.SBG_PLAUSISTATUS, delivery.getPlausistatus()))
                .add(delivery.getDeliverydate())
                .add(delivery.getDeliveryuser())
                .add(delivery.getNrplausiperson())
                .add(delivery.getNrplausievent())
                .add(delivery.getNotvalid())
                .getRow();
    }
}