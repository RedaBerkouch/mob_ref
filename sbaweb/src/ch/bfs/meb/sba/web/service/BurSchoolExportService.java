package ch.bfs.meb.sba.web.service;

import ch.bfs.meb.sba.web.utils.CsvExportRow;
import ch.bfs.meb.sba.web.utils.CsvExportSheet;
import ch.bfs.meb.sba.web.ws.sbaburschool.BurSchool;
import ch.bfs.meb.sba.web.ws.sbaburschool.BurSchoolListResult;
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

@Service("burSchoolExportService")
@Slf4j
@AllArgsConstructor
public class BurSchoolExportService {

    private final IBurSchoolService burSchoolService;
    private final WebLocalizationManager localizationManager;
    
    // BurSchool columns to export
    private static final String COLUMN_CANTON_NAME_KEY = "burSchoolTable.column.canton.name";
    private static final String COLUMN_BURNR_NAME_KEY = "burSchoolTable.column.burNr.name";
    private static final String COLUMN_LABEL_NAME_KEY = "burSchoolTable.column.label.name";
    private static final String COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY = "burSchoolTable.column.charPublFlg.name";
    private static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY = "burSchoolTable.column.charPrivSubFlg.name";
    private static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY = "burSchoolTable.column.charPrivNoSubFlg.name";
    private static final String COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY = "burSchoolTable.column.isSpecialSchool.name";
    private static final String COLUMN_DELIVERY_CODE_NAME_KEY = "burSchoolTable.column.deliveryCode.name";
    private static final String COLUMN_MUNICIPALITY_NAME_KEY = "burSchoolTable.column.municipality.name";
    private static final String COLUMN_SYNCHSTATUSBUR_NAME_KEY = "burSchoolTable.column.synchStatusBur.name";
    private static final String COLUMN_NAME_BUR_NAME_KEY = "burSchoolTable.column.nameBur.name";
    private static final String COLUMN_IS_PUBLIC_SCHOOL_BUR_NAME_KEY = "burSchoolTable.column.burCharPublFlg.name";
    private static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_BUR_NAME_KEY = "burSchoolTable.column.burCharPrivSubFlg.name";
    private static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR_NAME_KEY = "burSchoolTable.column.burCharPrivNoSubFlg.name";
    private static final String COLUMN_IS_SPECIAL_SCHOOL_BUR_NAME_KEY = "burSchoolTable.column.isSpecialSchoolBur.name";
    private static final String COLUMN_CANTON_BUR_NAME_KEY = "burSchoolTable.column.cantonBur.name";
    private static final String COLUMN_MUNICIPALITY_BUR_NAME_KEY = "burSchoolTable.column.municipalityBur.name";
    private static final String COLUMN_VALIDFROM_BUR_NAME_KEY = "burSchoolTable.column.validFromBur.name";
    private static final String COLUMN_VALIDTO_BUR_NAME_KEY = "burSchoolTable.column.validToBur.name";
    private static final String COLUMN_USERTEXT_NAME_KEY = "burSchoolTable.column.userText.name";

    public byte[] generateCsvExport(List<Long> selectedRowIds, Long version, Long canton, WebFilterContext filterContext, boolean withSynchBur) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            // En-têtes
            csv.add(getColumnHeaders());
            
            BurSchoolListResult burSchoolListResult;
            
            WebSortContext webSortContext = new WebSortContext();
            webSortContext.setAscSortOrder(true);
            webSortContext.setSortColumn("canton");
            webSortContext.setLocale("fr_CH");
            
            if (!selectedRowIds.isEmpty()) {
                burSchoolListResult = burSchoolService.getBurSchoolsOwnedByConfigDeliveries(selectedRowIds, webSortContext, withSynchBur);
            }
            else {
                burSchoolListResult = burSchoolService.getBurSchools(-1, -1, webSortContext, filterContext, version, canton, withSynchBur);
            }
            
            // Récupérer les données
            Stream.of(burSchoolListResult)
                    .filter(Objects::nonNull)
                    .filter(resultList -> !CollectionUtils.isEmpty(resultList.getSchools()))
                    .flatMap(resultList -> resultList.getSchools().stream())
                    .forEach(school -> csv.add(formatExportRow(school)));

        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("CSV generation failed", e);
        }

        return csv.getSheet().getBytes(StandardCharsets.UTF_8);
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_BURNR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_LABEL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DELIVERY_CODE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MUNICIPALITY_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_SYNCHSTATUSBUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_NAME_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PUBLIC_SCHOOL_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_SPECIAL_SCHOOL_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CANTON_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MUNICIPALITY_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VALIDFROM_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VALIDTO_BUR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_USERTEXT_NAME_KEY))
                .getRow();
    }

    private String formatExportRow(BurSchool burSchool) {
        return new CsvExportRow()
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, burSchool.getCanton()))
                .add(burSchool.getBurNr())
                .add(burSchool.getLabel())
                .add(burSchool.getCharPublFlg())
                .add(burSchool.getCharPrivSubFlg())
                .add(burSchool.getCharPrivNoSubFlg())
                .add(String.valueOf(burSchool.isIsSpecialSchool()).toUpperCase())
                .add(burSchool.getDeliveryCode())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MUNICIPALITY, burSchool.getMunicipality()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_SYNCHSTATUS, burSchool.getSynchStatusBur()))
                .add(burSchool.getBurLabel())
                .add(burSchool.getBurCharPublFlg())
                .add(burSchool.getBurCharPrivSubFlg())
                .add(burSchool.getBurCharPrivNoSubFlg())
                .add(String.valueOf(burSchool.isIsSpecialSchoolBur()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, burSchool.getCantonBur()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MUNICIPALITY, burSchool.getMunicipalityBur()))
                .add(burSchool.getBurValidFromSba())
                .add(burSchool.getBurValidToSba())
                .add(burSchool.getUserText())
                .getRow();
    }
}