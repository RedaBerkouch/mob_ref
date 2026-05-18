package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.utils.CsvExportRow;
import ch.bfs.meb.sbg.web.utils.CsvExportSheet;
import ch.bfs.meb.sbg.web.ws.sbgperson.Person;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonList;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static ch.bfs.meb.sbg.web.frontend.manager.PersonTableManager.COLUMN_PLAUSISTATUS_ID;

@Service("personExportService")
@Slf4j
@AllArgsConstructor
public class PersonExportService {

    public static final String COLUMN_CANTON_NAME_KEY = "personTable.column.canton.name";
    public static final String COLUMN_VERSION_NAME_KEY = "personTable.column.version.name";
    public static final String COLUMN_IDTYPE_NAME_KEY = "personTable.column.idType.name";
    public static final String COLUMN_SEX_NAME_KEY = "personTable.column.sex.name";
    public static final String COLUMN_BIRTHDATE_NAME_KEY = "personTable.column.birthDate.name";
    public static final String COLUMN_NEWBIRTHDATE_NAME_KEY = "personTable.column.newBirthDate.name";
    public static final String COLUMN_USERCOMMENT_NAME_KEY = "personTable.column.userComment.name";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "personTable.column.plausistatus.name";
    public static final String COLUMN_STATUS_NAME_KEY = "personTable.column.status.name";
    public static final String COLUMN_MODUSER_NAME_KEY = "personTable.column.modified.name";
    public static final String COLUMN_MODDATE_NAME_KEY = "personTable.column.modDate.name";
    public static final String COLUMN_VALIDUSER_NAME_KEY = "personTable.column.validUser.name";
    public static final String COLUMN_VALIDDATE_NAME_KEY = "personTable.column.validDate.name";
    public static final String COLUMN_ORIGINTEXT_NAME_KEY = "personTable.column.originText.name";

    private final PersonService personService;
    private final WebLocalizationManager localizationManager;

    /**
     * Génère un export CSV des personnes avec filtres (version, canton, filtres web)
     */
    public byte[] generateCsvExport(Long version, Long cantonId, WebFilterContext filterContext) {
        PersonList result = personService.getPersons(
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
        PersonList result = personService.getPersonsOwnedByEvents(
                qualificationIds,
                defaultWebSortContext()
        );
        return generateCsvFromResult(result);
    }

    /**
     * Génère le CSV à partir d'un résultat de liste de personnes
     */
    private byte[] generateCsvFromResult(PersonList result) {
        try {
            CsvExportSheet csv = new CsvExportSheet();

            // En-têtes
            csv.add(getColumnHeaders());

            // Données : stream les personnes et formatter chaque ligne
            if (result != null && result.getPersons() != null && !result.getPersons().isEmpty()) {
                result.getPersons()
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
                .add(localizationManager.getMessage(COLUMN_CANTON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VERSION_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IDTYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_IDTYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_SEX_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_SEX_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_BIRTHDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_NEWBIRTHDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_USERCOMMENT_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSISTATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_STATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VALIDUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_VALIDDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_ORIGINTEXT_NAME_KEY))
                .getRow();
    }

    private String formatPersonRow(Person person) {
        return new CsvExportRow()
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.CANTON, person.getCanton()))
                .add(person.getVersion())
                .add(person.getIdType())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.SEX, person.getSex()))
                .add(person.getBirthDate())
                .add(person.getNewBirthDate())
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_PLAUSISTATUS, person.getPlausiStatus()))
                .add(localizationManager.getCodeGroupValueById(CodegroupUtility.MEB_DELIVERYSTATUS, person.getStatus()))
                .add(person.getModUser())
                .add(person.getModDate())
                .add(person.getValidationUser())
                .add(person.getValidationDate())
                .add(person.getDeliveryText())
                .getRow();
    }
}