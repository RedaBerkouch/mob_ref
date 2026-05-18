package ch.bfs.meb.sbg.web.service;

import ch.bfs.meb.sbg.web.utils.CsvExportRow;
import ch.bfs.meb.sbg.web.utils.CsvExportSheet;
import ch.bfs.meb.sbg.web.ws.sbgevent.Event;
import ch.bfs.meb.sbg.web.ws.sbgevent.EventList;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static ch.bfs.meb.sbg.web.frontend.manager.PersonTableManager.COLUMN_PLAUSISTATUS_ID;

@Service("qualificationExportService")
@Slf4j
@AllArgsConstructor
public class QualificationExportService {

    public static final String COLUMN_CANTON_NAME_KEY = "eventTable.column.canton.name";
    public static final String COLUMN_VERSION_NAME_KEY = "eventTable.column.version.name";
    public static final String COLUMN_TYPE_NAME_KEY = "eventTable.column.type.name";
    public static final String COLUMN_SBFICODE_NAME_KEY = "eventTable.column.sbficode.name";
    public static final String COLUMN_CONTRACTNO_NAME_KEY = "eventTable.column.contractno.name";
    public static final String COLUMN_PROFESSIONCODE_NAME_KEY = "eventTable.column.professioncode.name";
    public static final String COLUMN_KEYASPECT_NAME_KEY = "eventTable.column.keyaspect.name";
    public static final String COLUMN_EDUCATIONYEAR_NAME_KEY = "eventTable.column.educationyear.name";
    public static final String COLUMN_CONTRACTTYPE_NAME_KEY = "eventTable.column.contracttype.name";
    public static final String COLUMN_CONTRACTDATE_NAME_KEY = "eventTable.column.contractdate.name";
    public static final String COLUMN_EXAMTYPE_NAME_KEY = "eventTable.column.examtype.name";
    public static final String COLUMN_EXAMNR_NAME_KEY = "eventTable.column.examnr.name";
    public static final String COLUMN_EXAMREP_NAME_KEY = "eventTable.column.examrep.name";
    public static final String COLUMN_EXAMRESULT_NAME_KEY = "eventTable.column.examresult.name";
    public static final String COLUMN_CANCELREASON_NAME_KEY = "eventTable.column.cancelreason.name";
    public static final String COLUMN_CANCELDATE_NAME_KEY = "eventTable.column.canceldate.name";
    public static final String COLUMN_BURNR_NAME_KEY = "eventTable.column.burnr.name";
    public static final String COLUMN_PLAUSISTATUS_NAME_KEY = "eventTable.column.plausistatus.name";
    public static final String COLUMN_MODUSER_NAME_KEY = "eventTable.column.modified.name";
    public static final String COLUMN_MODDATE_NAME_KEY = "eventTable.column.modDate.name";
    public static final String COLUMN_USERCOMMENT_NAME_KEY = "eventTable.column.usercomment.name";
    public static final String COLUMN_KANTLBCODE_NAME_KEY = "eventTable.column.kantlbcode.name";
    public static final String COLUMN_FIRSTNAME_NAME_KEY = "eventTable.column.firstname.name";
    public static final String COLUMN_FIRMSTREET_NAME_KEY = "eventTable.column.firmstreet.name";
    public static final String COLUMN_FIRMSTREETNO_NAME_KEY = "eventTable.column.firmstreetno.name";
    public static final String COLUMN_FIRMPLZ_NAME_KEY = "eventTable.column.firmplz.name";
    public static final String COLUMN_FIRMMUNICIPAL_NAME_KEY = "eventTable.column.firmmunicipal.name";
    public static final String COLUMN_FLAGLBV_NAME_KEY = "eventTable.checkbox.flaglbv.name";

    private final IEventService qualificationService;
    private final WebLocalizationManager localizationManager;

    /**
     * Génère un export CSV des qualifications avec filtres (version, canton, filtres web)
     */
    public byte[] generateCsvExport(Long version, Long cantonId, WebFilterContext filterContext) {
        EventList result = qualificationService.getEvents(
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
        EventList result = qualificationService.getEventsOwnedByPersons(
                personIds,
                defaultWebSortContext()
        );
        return generateCsvFromResult(result);
    }

    /**
     * Génère le CSV à partir d'un résultat de liste de qualifications
     */
    private byte[] generateCsvFromResult(EventList result) {
        CsvExportSheet csv = new CsvExportSheet();

        try {
            csv.add(getColumnHeaders());

            if (result != null && !CollectionUtils.isEmpty(result.getEvents())) {
                result.getEvents().stream()
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
                .add(localizationManager.getMessage(COLUMN_TYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_SBFICODE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CONTRACTNO_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PROFESSIONCODE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_KEYASPECT_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EDUCATIONYEAR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CONTRACTTYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CONTRACTDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EXAMTYPE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EXAMNR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EXAMREP_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_EXAMRESULT_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CANCELREASON_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_CANCELDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_BURNR_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_PLAUSISTATUS_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODUSER_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_MODDATE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_USERCOMMENT_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_KANTLBCODE_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_FIRSTNAME_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_FIRMSTREET_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_FIRMSTREETNO_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_FIRMPLZ_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_FIRMMUNICIPAL_NAME_KEY))
                .add(localizationManager.getMessage(COLUMN_FLAGLBV_NAME_KEY))
                .getRow();
    }

    private String formatQualificationRow(Event qualification) {
        return new CsvExportRow()
                .getRow();
    }
}