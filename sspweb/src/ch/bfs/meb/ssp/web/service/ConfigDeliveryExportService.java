package ch.bfs.meb.ssp.web.service;

import ch.bfs.meb.ssp.web.utils.CsvExportRow;
import ch.bfs.meb.ssp.web.utils.CsvExportSheet;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.ConfigDelivery;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.ConfigDeliveryListResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service("configDeliveryExportService")
@Slf4j
@AllArgsConstructor
public class ConfigDeliveryExportService {

    private final IConfigDeliveryService configDeliveryService;
    private final WebLocalizationManager localizationManager;
    
    // ConfigDelivery columns to export
    private static final String COLUMN_CANTON_NAME_KEY = "configDeliveryTable.column.canton.name";
    private static final String COLUMN_DELIVERYCODE_NAME_KEY = "configDeliveryTable.column.deliveryCode.name";
    private static final String COLUMN_DEFAULT_NAME_KEY = "configDeliveryTable.column.default.name";
    private static final String COLUMN_DLUSERS_NAME_KEY = "configDeliveryTable.column.dlUsers.name";
    //private static final String COLUMN_DLUSERS_PARAMNAME_KEY = "configDeliveryTable.column.dlUsers.param.name";
    private static final String COLUMN_ROUSERS_NAME_KEY = "configDeliveryTable.column.roUsers.name";
    //private static final String COLUMN_ROUSERS_PARAMNAME_KEY = "configDeliveryTable.column.roUsers.param.name";
    private static final String COLUMN_DUEDATE_NAME_KEY = "configDeliveryTable.column.dueDate.name";
    private static final String COLUMN_REFERENCEDATE_NAME_KEY = "configDeliveryTable.column.referenceDate.name";
    private static final String COLUMN_CREATIONDATE_NAME_KEY = "configDeliveryTable.column.creationDate.name";
    private static final String COLUMN_CREATIONUSER_NAME_KEY = "configDeliveryTable.column.creationUser.name";
    private static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "configDeliveryTable.column.modificationDate.name";
    private static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "configDeliveryTable.column.modificationUser.name";
    private static final String COLUMN_USERTEXT_NAME_KEY = "configDeliveryTable.column.userText.name";
    
    public byte[] generateCsvExport(List<Long> selectedIds, Long version, Long canton, WebFilterContext filterContext) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // Header
            csv.add(getColumnHeaders());

            ConfigDeliveryListResult configDeliveryListResult;

            WebSortContext webSortContext = new WebSortContext();
            webSortContext.setAscSortOrder(true);
            webSortContext.setSortColumn("canton");
            webSortContext.setLocale("fr_CH");

            if (!selectedIds.isEmpty()) {
                configDeliveryListResult = configDeliveryService
                        .getConfigDeliveriesOwnedBySchools(selectedIds, webSortContext, version);
            }
            else  {
                configDeliveryListResult = configDeliveryService
                        .getConfigDeliveries(-1, -1, webSortContext, filterContext, version, canton);
            }

            // Data
            Stream.of(configDeliveryListResult)
                    .filter(Objects::nonNull)
                    .filter(resultList -> !CollectionUtils.isEmpty(resultList.getConfigDeliveries()))
                    .flatMap(resultList -> resultList.getConfigDeliveries().stream())
                    .forEach(configDelivery -> csv.add(formatExportRow(configDelivery)));

        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("CSV generation failed", e);
        }

        return csv.getSheet().getBytes(StandardCharsets.UTF_8);
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DELIVERYCODE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DEFAULT_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DLUSERS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_ROUSERS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DUEDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_REFERENCEDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CREATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CREATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODIFICATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODIFICATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_USERTEXT_NAME_KEY))
                .getRow();
    }

    private String formatExportRow(ConfigDelivery configDelivery) {
        return new CsvExportRow()
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, configDelivery.getCanton()))
                .add(configDelivery.getDeliveryCode())
                .add(String.valueOf(configDelivery.isIsDefault()).toUpperCase())
                .add(configDelivery.getDlUsers())
                .add(configDelivery.getRoUsers())
                .add(configDelivery.getDueDate())
                .add(configDelivery.getReferenceDate())
                .add(configDelivery.getCreationDate())
                .add(configDelivery.getCreationUser())
                .add(configDelivery.getModificationUser())
                .add(configDelivery.getModificationUser())
                .add(configDelivery.getUserText())
                .getRow();
    }
}