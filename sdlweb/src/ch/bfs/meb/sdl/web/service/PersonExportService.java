package ch.bfs.meb.sdl.web.service;

import ch.bfs.meb.sdl.web.utils.CsvExportRow;
import ch.bfs.meb.sdl.web.utils.CsvExportSheet;
import ch.bfs.meb.sdl.web.ws.sdlschool.SdlSchool;
import ch.bfs.meb.sdl.web.ws.sdlschool.SdlSchoolListResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service("personExportService")
@Slf4j
@AllArgsConstructor
public class PersonExportService {
    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";
    public static final String COLUMN_VERSION_NAME_KEY = "personTable.column.version.name";
    public static final String COLUMN_CANTON_NAME_KEY = "personTable.column.canton.name";
    public static final String COLUMN_DELIVERYCODE_NAME_KEY = "personTable.column.deliveryCode.name";
    public static final String COLUMN_IDTYPE_NAME_KEY = "personTable.column.idtype.name";
    public static final String COLUMN_ID_NAME_KEY = "personTable.column.personId.name";
    public static final String COLUMN_DELIVERYSTATUS_NAME_KEY = "personTable.column.deliveryStatus.name";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "personTable.column.plausiStatus.name";
    public static final String COLUMN_CREATIONUSER_NAME_KEY = "personTable.column.creationUser.name";
    public static final String COLUMN_CREATIONDATE_NAME_KEY = "personTable.column.creationDate.name";
    public static final String COLUMN_MODIFICATIONUSER_NAME_KEY = "personTable.column.modificationUser.name";
    public static final String COLUMN_MODIFICATIONDATE_NAME_KEY = "personTable.column.modificationDate.name";
    public static final String COLUMN_PREVELATIONUSER_NAME_KEY = "personTable.column.prevalidationUser.name";
    public static final String COLUMN_PREVELATIONDATE_NAME_KEY = "personTable.column.prevalidationDate.name";
    public static final String COLUMN_USERTEXT_NAME_KEY = "personTable.column.userText.name";

    private final ISchoolService personService;
    private final WebLocalizationManager localizationManager;

    /**
     * Génère un export CSV des personnes avec filtres (version, canton, filtres web)
     */
    public byte[] generateCsvExport(Long version, Long cantonId, WebFilterContext filterContext) {
        SdlSchoolListResult result = personService.getSchools(
                -1, -1,
                defaultWebSortContext(),
                filterContext,
                version,
                cantonId
        );
        return generateCsvFromResult(result);
    }

    /**
     * Génère un export CSV des personnes par liste d'IDs de qualifications
     */
    public byte[] generateCsvExport(List<Long> qualificationIds) {
        SdlSchoolListResult result = personService.getSchoolsOwnedByClasses(
                qualificationIds,
                defaultWebSortContext()
        );
        return generateCsvFromResult(result);
    }

    /**
     * Génère le CSV à partir d'un résultat de liste de personnes
     */
    private byte[] generateCsvFromResult(SdlSchoolListResult result) {
        try {
            CsvExportSheet csv = new CsvExportSheet();

            // En-têtes
            csv.add(getColumnHeaders());

            // Données : stream les personnes et formatter chaque ligne
            if (result != null && result.getSchools() != null && !result.getSchools().isEmpty()) {
                result.getSchools()
                        .forEach(person -> csv.add(formatPersonRow(person)));
            }

            return csv.getSheet().getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error generating CSV from persons result", e);
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
                .add(localizationManager.getMessage(COLUMN_DELIVERYCODE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IDTYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_ID_NAME_KEY))
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

    private String formatPersonRow(SdlSchool person) {
        return new CsvExportRow()
                .add(person.getVersion())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, person.getCanton()))
                .add(person.getDeliveryCode())
                .add(person.getIdType())
                .add(person.getId())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_DELIVERYSTATUS, person.getDeliveryStatus()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_PLAUSISTATUS, person.getPlausiStatus()))
                .add(person.getCreationUser())
                .add(person.getCreationDate())
                .add(person.getModificationUser())
                .add(person.getModificationDate())
                .add(person.getPrevalidationUser())
                .add(person.getPrevalidationDate())
                .add(person.getUserText())
                .getRow();
    }
}