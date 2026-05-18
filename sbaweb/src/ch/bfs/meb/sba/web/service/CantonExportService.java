package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.utils.CsvExportRow;
import ch.bfs.meb.sba.web.utils.CsvExportSheet;
import ch.bfs.meb.sba.web.ws.sbacanton.Canton;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Service("cantonExportService")
@Slf4j
@AllArgsConstructor
public class CantonExportService {

    private final ICantonService cantonService;
    private final WebLocalizationManager localizationManager;
    
    // Canton columns to export
    private static final String COLUMN_CANTON_NAME_KEY = "cantonTable.column.canton.name";
    private static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "cantonTable.column.deliveryStatus.name";
    private static final String COLUMN_PLAUSISTATUS_NAME_KEY = "cantonTable.column.plausiStatus.name";
    private static final String COLUMN_PLAUSIDATE_NAME_KEY = "cantonTable.column.plausiDate.name";
    private static final String COLUMN_PLAUSIUSER_NAME_KEY = "cantonTable.column.plausiUser.name";
    private static final String COLUMN_CREATIONDATE_NAME_KEY = "cantonTable.column.creationDate.name";
    private static final String COLUMN_CREATIONUSER_NAME_KEY = "cantonTable.column.creationUser.name";
    private static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "cantonTable.column.modificationDate.name";
    private static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "cantonTable.column.modificationUser.name";
    private static final String COLUMN_VALIDATIONDATE_NAME_KEY = "cantonTable.column.validationDate.name";
    private static final String COLUMN_VALIDATIONUSER_NAME_KEY = "cantonTable.column.validationUser.name";
    private static final String COLUMN_FINALISATIONDATE_NAME_KEY = "cantonTable.column.finalisationDate.name";
    private static final String COLUMN_FINALISATIONUSER_NAME_KEY = "cantonTable.column.finalisationUser.name";
    private static final String COLUMN_USERTEXT_NAME_KEY = "cantonTable.column.userText.name";
    
    
    public byte[] generateCsvExport(Long version, Long canton) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());

            // Récupérer les données
            Stream.of(cantonService.getCantons(version, canton))
                    .filter(Objects::nonNull)
                    .filter(resultList -> !CollectionUtils.isEmpty(resultList.getCantons()))
                    .flatMap(resultList -> resultList.getCantons().stream())
                    .forEach(cantonRow -> csv.add(formatCantonRow(cantonRow)));

        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("CSV generation failed", e);
        }

        return csv.getSheet().getBytes(StandardCharsets.UTF_8);
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DELIVERYSTATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSISTATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSIDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSIUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CREATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CREATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODIFICATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODIFICATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VALIDATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VALIDATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_FINALISATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_FINALISATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_USERTEXT_NAME_KEY))
                .getRow();
    }

    private String formatCantonRow(Canton canton) {
        return new CsvExportRow()
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, canton.getCanton()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_DELIVERYSTATUS, canton.getDeliveryStatus()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_PLAUSISTATUS, canton.getPlausiStatus()))
                .add(canton.getPlausiDate())
                .add(canton.getPlausiUser())
                .add(canton.getCreationDate())
                .add(canton.getCreationUser())
                .add(canton.getModificationUser())
                .add(canton.getModificationUser())
                .add(canton.getValidationUser())
                .add(canton.getValidationDate())
                .add(canton.getFinalisationUser())
                .add(canton.getFinalisationDate())
                .add(canton.getUserText())
                .getRow();
                
    }
}