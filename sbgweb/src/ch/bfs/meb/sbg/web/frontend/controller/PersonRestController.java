package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.frontend.dto.ActionRequest;
import ch.bfs.meb.sbg.web.frontend.dto.LastFilters;
import ch.bfs.meb.sbg.web.frontend.dto.PersonRequestBody;
import ch.bfs.meb.sbg.web.frontend.dto.SearchRequest;
import ch.bfs.meb.sbg.web.service.FilterService;
import ch.bfs.meb.sbg.web.service.IDeliveryService;
import ch.bfs.meb.sbg.web.service.IPersonService;
import ch.bfs.meb.sbg.web.service.PersonExportService;
import ch.bfs.meb.sbg.web.utils.CsvUtils;
import ch.bfs.meb.sbg.web.utils.FileUtils;
import ch.bfs.meb.sbg.web.ws.sbgperson.Person;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonList;
import ch.bfs.meb.sbg.web.ws.sbgperson.PersonResult;
import ch.bfs.meb.security.MebUser;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
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
import java.util.List;
import java.util.Optional;

import static ch.bfs.meb.sbg.web.frontend.manager.PersonTableManager.COLUMN_PLAUSISTATUS_ID;
import static ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult.OK;

@RestController
@RequestMapping("/persons")
@AllArgsConstructor
@Slf4j
public class PersonRestController {

    private final IPersonService personService;
    private final PersonExportService personExportService;
    private final WebLocalizationManager localizationManager;
    private final FilterService filterService;
    private IDeliveryService deliveryService;

    @PostMapping("/search")
    public ResponseEntity<List<Person>> searchPersons(@RequestBody SearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBG_OBJECTTYPE_PERSON);

        List<Person> persons = fetchAllPersons(filterContext, request.getVersion(), cantonId);

        log.info("Search completed - {} persons found", persons.size());
        return ResponseEntity.ok(persons);
    }

    private List<Person> fetchAllPersons(WebFilterContext filterContext, Long version, Long cantonId) {
        int buffer = 500;

        // Premier appel, nous récupérons les resultats, le nombre total (afin de savoir si il faut paginer)
        PersonList firstResult = fetchBatch(0, buffer, filterContext, version, cantonId);

        Long totalCount = firstResult.getResultSize();
        List<Person> allPersons = new ArrayList<>(totalCount.intValue());
        allPersons.addAll(firstResult.getPersons());

        // Si tout est dans le premier batch
        if (totalCount <= buffer) {
            return allPersons;
        }

        // Sinon récupérer le reste par lots
        for (int start = buffer; start < totalCount; start += buffer) {
            PersonList batchResult = fetchBatch(start, buffer, filterContext, version, cantonId);
            allPersons.addAll(batchResult.getPersons());
        }

        log.debug("Fetched {} persons in {} batches", allPersons.size(), (totalCount / buffer) + 1);
        return allPersons;
    }

    private PersonList fetchBatch(int start, int buffer, WebFilterContext filterContext, Long version, Long cantonId) {
        WebSortContext sortContext = defaultWebSortContext();

        PersonList result = personService.getPersons(
                start, buffer, sortContext, filterContext, version, cantonId
        );

        if (result.getState() != WebFilterResult.OK) {
            String message = localizationManager.getMessage(result.getMessage());
            log.error("Error fetching persons at offset {}: {}", start, message);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }

        return result;
    }

    private WebSortContext defaultWebSortContext() {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(COLUMN_PLAUSISTATUS_ID);
        sortContext.setLocale(localizationManager.getLocale().toString());
        sortContext.setAscSortOrder(true);
        return sortContext;
    }

    @GetMapping("/predefined_filters")
    public ResponseEntity<List<WebFilter>> getFilters() {
        WebFilterListResult result = filterService.getFiltersForRefObject(CodegroupUtility.SBG_OBJECTTYPE_PERSON);
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

    @GetMapping("/lastFilters")
    public LastFilters getLastFilters() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long lastFilterCanton = Optional.ofNullable(user.getLastFilterCanton())
                .filter(canton -> canton > 0)
                .orElseGet(this::getDefaultCanton);

        return new LastFilters(user.getLastFilterVersion(), lastFilterCanton);
    }

    private Long getDefaultCanton() {
        return deliveryService.getFilterCantonsForActUser(localizationManager).stream()
                .filter(canton -> canton > 0L)
                .findFirst()
                .orElse(null);
    }

    @GetMapping()
    public ResponseEntity<List<Person>> getPersonsByQualifications(@RequestParam List<Long> qualificationIds) {
        PersonList result = personService.getPersonsOwnedByEvents(qualificationIds, defaultWebSortContext());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getPersons());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when getting persons: {}", message);
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
    }

    /**
     * Exporte les personnes en CSV avec filtres (version, canton, filtres web)
     */
    @PostMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsvWithFilters(@RequestBody SearchRequest request) {
        try {
            Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
            WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBG_OBJECTTYPE_PERSON);

            byte[] csvContent = personExportService.generateCsvExport(
                    request.getVersion(),
                    cantonId,
                    filterContext
            );

            return buildCsvResponse(csvContent, "Persons");

        } catch (Exception e) {
            return handleExportError("Error generating persons CSV export with filters", e);
        }
    }

    /**
     * Exporte les personnes en CSV par liste d'IDs de qualifications
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsvByQualificationIds(@RequestParam List<Long> qualificationIds) {
        try {
            byte[] csvContent = personExportService.generateCsvExport(qualificationIds);
            return buildCsvResponse(csvContent, "Persons");

        } catch (Exception e) {
            return handleExportError("Error generating persons CSV export by qualification IDs", e);
        }
    }

    /**
     * Construit la réponse HTTP pour un export CSV avec BOM UTF-8 et headers appropriés
     */
    private ResponseEntity<byte[]> buildCsvResponse(byte[] csvContent, String filename) {
        byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);
        HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile(filename, csvWithBom.length);
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

    @PostMapping("/validate")
    public ResponseEntity<Person> validate(@RequestBody ActionRequest actionRequest) {
        if (CollectionUtils.isEmpty(actionRequest.getPersonIds())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        PersonResult result = personService.validatePersons(actionRequest.getPersonIds(), localizationManager.getLocale().toString());

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getPerson());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when validating persons: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PostMapping("/undo_validate")
    public ResponseEntity<Person> undoValidate(@RequestBody ActionRequest actionRequest) {
        if (CollectionUtils.isEmpty(actionRequest.getPersonIds())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        PersonResult result = personService.validatePersons(actionRequest.getPersonIds(), localizationManager.getLocale().toString());

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getPerson());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when validating persons: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PutMapping()
    public ResponseEntity<Person> createPerson(@RequestBody PersonRequestBody request) {

        PersonResult result = personService.insertPerson(request.getPerson(), localizationManager.getLocale().toString());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getPerson());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when inserting person: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PostMapping()
    public ResponseEntity<Person> updatePerson(@RequestBody PersonRequestBody request) {
        PersonResult result = personService.updatePerson(request.getPerson(), localizationManager.getLanguage());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getPerson());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when updating person: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @DeleteMapping("/{personId}/{registerWithoutPlausi}")
    public ResponseEntity<Void> delete(@PathVariable Long personId, @PathVariable boolean registerWithoutPlausi) {
        PersonResult personByIdResult = personService.getPersonById(personId);
        if (personByIdResult.getState() != ResultBase.OK) {
            String message = localizationManager.getMessage(personByIdResult.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }
        PersonResult result = personService.deletePerson(personByIdResult.getPerson());

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok().build();
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when deleting person: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

}


