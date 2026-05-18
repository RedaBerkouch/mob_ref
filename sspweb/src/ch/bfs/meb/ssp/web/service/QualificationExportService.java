package ch.bfs.meb.ssp.web.service;

import ch.bfs.meb.ssp.web.utils.CsvExportRow;
import ch.bfs.meb.ssp.web.utils.CsvExportSheet;
import ch.bfs.meb.ssp.web.ws.sspactivity.SspActivity;
import ch.bfs.meb.ssp.web.ws.sspactivity.SspActivityListResult;
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

@Service("qualificationExportService")
@Slf4j
@AllArgsConstructor
public class QualificationExportService {

    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_VERSION_NAME_KEY = "qualificationTable.column.version.name";
    public static final String COLUMN_CANTON_NAME_KEY = "qualificationTable.column.canton.name";
    public static final String COLUMN_SCHOOLIDTYPE_NAME_KEY = "qualificationTable.column.schoolIdType.name";
    public static final String COLUMN_SCHOOLID_NAME_KEY = "qualificationTable.column.schoolId.name";
    public static final String COLUMN_NAMEBURSCHOOL_NAME_KEY = "qualificationTable.column.nameBurSchool.name";
    public static final String COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY = "qualificationTable.column.charPublFlg.name";
    public static final String COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY = "qualificationTable.column.charPrivSubFlg.name";
    public static final String COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY = "qualificationTable.column.charPrivNoSubFlg.name";
    public static final String COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY = "qualificationTable.column.isSpecialSchool.name";
    public static final String COLUMN_EDUCATIONTYPE_NAME_KEY = "qualificationTable.column.educationType.name";
    public static final String COLUMN_EXAMTYPE_NAME_KEY = "qualificationTable.column.examType.name";
    public static final String COLUMN_EXAMDATE_NAME_KEY = "qualificationTable.column.examDate.name";
    public static final String COLUMN_EXAMNR_NAME_KEY = "qualificationTable.column.examNr.name";
    public static final String COLUMN_RESULT_NAME_KEY = "qualificationTable.column.result.name";
    public static final String COLUMN_MATURITY_LANGUAGES_NAME_KEY = "qualificationTable.column.maturityLanguages.name";
    public static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "qualificationTable.column.deliveryStatus.name";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "qualificationTable.column.plausiStatus.name";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "qualificationTable.column.creationUser.name";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "qualificationTable.column.creationDate.name";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "qualificationTable.column.modificationUser.name";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "qualificationTable.column.modificationDate.name";
    public static final String COLUMN_PREVELATIONUSER_NAME_KEY = "qualificationTable.column.prevalidationUser.name";
    public static final String COLUMN_PREVELATIONDATE_NAME_KEY = "qualificationTable.column.prevalidationDate.name";
    public static final String COLUMN_USERTEXT_NAME_KEY = "qualificationTable.column.userText.name";

    private final ActivityService qualificationService;
    private final WebLocalizationManager localizationManager;

    /**
     * Génère un export CSV des qualifications avec filtres (version, canton, filtres web)
     */
    public byte[] generateCsvExport(Long version, Long cantonId, WebFilterContext filterContext) {
        SspActivityListResult result = qualificationService.getActivities(
                -1, -1,
                defaultWebSortContext(),
                filterContext,
                version,
                cantonId
        );
        return generateCsvFromResult(result);
    }

    /**
     * Génère un export CSV des qualifications par liste d'IDs de personnes
     */
    public byte[] generateCsvExport(List<Long> personIds) {
        SspActivityListResult result = qualificationService.getActivitiesOwnedByPersons(
                personIds,
                defaultWebSortContext()
        );
        return generateCsvFromResult(result);
    }

    /**
     * Génère le CSV à partir d'un résultat de liste de qualifications
     */
    private byte[] generateCsvFromResult(SspActivityListResult result) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            csv.add(getColumnHeaders());

            if (result != null && !CollectionUtils.isEmpty(result.getActivities())) {
                result.getActivities().stream()
                        .forEach(qualification -> csv.add(formatQualificationRow(qualification)));
            }

            return csv.getSheet().getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error generating CSV from qualifications result", e);
            throw new RuntimeException("CSV generation failed", e);
        }
    }

    private WebSortContext defaultWebSortContext() {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(COLUMN_PLAUSISTATUS_ID);
        sortContext.setLocale(localizationManager.getLocale().toString());
        sortContext.setAscSortOrder(true);
        return sortContext;
    }

    private String getColumnHeaders() {
        return new CsvExportRow()
                .add(localizationManager.getMessage(COLUMN_VERSION_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_SCHOOLIDTYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_SCHOOLID_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_NAMEBURSCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PUBLIC_SCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PRIVATE_SUBSIDISED_SCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_PRIVATE_NOT_SUBSIDISED_SCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IS_SPECIAL_SCHOOL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EDUCATIONTYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EXAMTYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EXAMDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EXAMNR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_RESULT_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MATURITY_LANGUAGES_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_DELIVERYSTATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSISTATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CREATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CREATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODIFICATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODIFICATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PREVELATIONUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PREVELATIONDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_USERTEXT_NAME_KEY))
                .getRow();
    }

    private String formatQualificationRow(SspActivity qualification) {
        return new CsvExportRow()
                .add(qualification.getVersion())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, qualification.getCanton()))
                .add(qualification.getSchoolIdType())
                .add(qualification.getSchoolId())
                .add(qualification.getNameBurSchool())
                .add(qualification.getCharPublFlg())
                .add(qualification.getCharPrivSubFlg())
                .add(qualification.getCharPrivNoSubFlg())
                .add(qualification.isIsSpecialSchool())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.PERS_CATEGORY, qualification.getPersCategory()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CONTRACTTYPE, qualification.getContractType()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_DATASTATUS, qualification.getDeliveryStatus()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_PLAUSISTATUS, qualification.getPlausiStatus()))
                .add(qualification.getCreationUser())
                .add(qualification.getCreationDate())
                .add(qualification.getModificationUser())
                .add(qualification.getModificationDate())
                .add(qualification.getPrevalidationUser())
                .add(qualification.getPrevalidationDate())
                .add(qualification.getUserText())
                .getRow();
    }
}