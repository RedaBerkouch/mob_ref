package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.frontend.dto.QualificationRequestBody;
import ch.bfs.meb.sbg.web.frontend.dto.SearchRequest;
import ch.bfs.meb.sbg.web.service.FilterService;
import ch.bfs.meb.sbg.web.service.IEventService;
import ch.bfs.meb.sbg.web.service.IPersonService;
import ch.bfs.meb.sbg.web.service.QualificationExportService;
import ch.bfs.meb.sbg.web.utils.CsvUtils;
import ch.bfs.meb.sbg.web.utils.DateUtils;
import ch.bfs.meb.sbg.web.utils.FileUtils;
import ch.bfs.meb.sbg.web.ws.sbgevent.Event;
import ch.bfs.meb.sbg.web.ws.sbgevent.EventList;
import ch.bfs.meb.sbg.web.ws.sbgevent.EventResult;
import ch.bfs.meb.sbg.web.ws.sbgperson.Person;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static ch.bfs.meb.sbg.web.frontend.manager.PersonTableManager.COLUMN_PLAUSISTATUS_ID;
import static ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult.OK;

@RestController
@RequestMapping("/qualifications")
@AllArgsConstructor
@Slf4j
public class QualificationRestController {

    private final IEventService qualificationService;
    private final IPersonService personService;
    private final QualificationExportService qualificationExportService;
    private final WebLocalizationManager localizationManager;
    private final FilterService filterService;

    @PostMapping("/search")
    public ResponseEntity<List<Event>> searchQualifications(@RequestBody SearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBG_OBJECTTYPE_EVENT);

        List<Event> qualifications = fetchAllQualifications(filterContext, request.getVersion(), cantonId);

        log.info("Search completed - {} qualifications found", qualifications.size());
        return ResponseEntity.ok(qualifications);
    }

    private List<Event> fetchAllQualifications(WebFilterContext filterContext, Long version, Long cantonId) {
        int buffer = 500;

        // Premier appel, nous récupérons les resultats, le nombre total (afin de savoir si il faut paginer)
        EventList firstResult = fetchBatch(0, buffer, filterContext, version, cantonId);

        Long totalCount = firstResult.getResultSize();
        List<Event> allQualifications = new ArrayList<>(totalCount.intValue());
        allQualifications.addAll(firstResult.getEvents());

        // Si tout est dans le premier batch
        if (totalCount <= buffer) {
            return allQualifications;
        }

        // Sinon récupérer le reste par lots
        for (int start = buffer; start < totalCount; start += buffer) {
            EventList batchResult = fetchBatch(start, buffer, filterContext, version, cantonId);
            allQualifications.addAll(batchResult.getEvents());
        }

        log.debug("Fetched {} qualifications in {} batches", allQualifications.size(), (totalCount / buffer) + 1);
        return allQualifications;
    }

    private EventList fetchBatch(int start, int buffer, WebFilterContext filterContext, Long version, Long cantonId) {
        WebSortContext sortContext = defaultWebSortContext();

        EventList result = qualificationService.getEvents(
                start, buffer, sortContext, filterContext, version, cantonId
        );

        if (result.getState() != WebFilterResult.OK) {
            String message = localizationManager.getMessage(result.getMessage());
            log.error("Error fetching qualifications at offset {}: {}", start, message);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }

        return result;
    }

    @GetMapping("/predefined_filters")
    public ResponseEntity<List<WebFilter>> getFilters() {
        WebFilterListResult result = filterService.getFiltersForRefObject(CodegroupUtility.SBG_OBJECTTYPE_EVENT);
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getFilters());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when retrieving filters: {}", message);
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
    }

    @GetMapping()
    public ResponseEntity<List<Event>> getQualificationsByPersons(@RequestParam List<Long> personIds) {
        EventList result = qualificationService.getEventsOwnedByPersons(personIds, defaultWebSortContext());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getEvents());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when getting qualifications: {}", message);
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
    }

    private WebSortContext defaultWebSortContext() {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(COLUMN_PLAUSISTATUS_ID);
        sortContext.setLocale(localizationManager.getLocale().toString());
        sortContext.setAscSortOrder(true);
        return sortContext;
    }

    /**
     * Exporte les qualifications en CSV avec filtres (version, canton, filtres web)
     */
    @PostMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsvWithFilters(@RequestBody SearchRequest request) {
        try {
            Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
            WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBG_OBJECTTYPE_EVENT);

            byte[] csvContent = qualificationExportService.generateCsvExport(
                    request.getVersion(),
                    cantonId,
                    filterContext
            );

            return buildCsvResponse(csvContent);

        } catch (Exception e) {
            return handleExportError("Error generating qualifications CSV export with filters", e);
        }
    }

    /**
     * Exporte les qualifications en CSV par liste d'IDs de personnes
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsvByPersonIds(@RequestParam List<Long> personIds) {
        try {
            byte[] csvContent = qualificationExportService.generateCsvExport(personIds);
            return buildCsvResponse(csvContent);

        } catch (Exception e) {
            return handleExportError("Error generating qualifications CSV export by person IDs", e);
        }
    }

    /**
     * Construit la réponse HTTP pour un export CSV avec BOM UTF-8 et headers appropriés
     */
    private ResponseEntity<byte[]> buildCsvResponse(byte[] csvContent) {
        byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);
        HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile("Qualification", csvWithBom.length);
        return new ResponseEntity<>(csvWithBom, headers, HttpStatus.OK);
    }

    /**
     * Gère les erreurs d'export et retourne une réponse 500 avec message d'erreur
     */
    private ResponseEntity<byte[]> handleExportError(String logMessage, Exception e) {
        log.error(logMessage, e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("CSV export failed".getBytes(StandardCharsets.UTF_8));
    }

    @PutMapping()
    public ResponseEntity<Event> createPerson(@RequestBody QualificationRequestBody request) {

        Event qualification = request.getQualification();
        if (request.getPersonId() != null) {
            qualification.setEventid(request.getPersonId());
            PersonResult personResult = personService.getPersonById(request.getPersonId());
            Person person = personResult.getPerson();
            qualification.setCanton(person.getCanton());
            qualification.setVersion(person.getVersion());
        }
        Date date = new Date();
        qualification.setContractDate(DateUtils.toXMLGregorianCalendar(date));
        qualification.setModDate(DateUtils.toXMLGregorianCalendar(date));
        qualification.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);

        EventResult result = qualificationService.insertEvent(qualification, localizationManager.getLocale().toString());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getEvent());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when inserting qualification: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PostMapping()
    public ResponseEntity<Event> updatePerson(@RequestBody QualificationRequestBody request) {
        EventResult result = qualificationService.updateEvent(
                request.getQualification(), localizationManager.getLocale().toString());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getEvent());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when updating qualification: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @DeleteMapping("/{qualificationId}/{registerWithoutPlausi}")
    public ResponseEntity<Void> delete(@PathVariable Long qualificationId, @PathVariable boolean registerWithoutPlausi) {
        EventResult qualificationByIdResult = qualificationService.getEventById(qualificationId);
        if (qualificationByIdResult.getState() != ResultBase.OK) {
            String message = localizationManager.getMessage(qualificationByIdResult.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }
        EventResult result = qualificationService.deleteEvent(qualificationByIdResult.getEvent());

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok().build();
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when deleting qualification: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

}


