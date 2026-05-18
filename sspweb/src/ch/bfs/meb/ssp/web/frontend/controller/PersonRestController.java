package ch.bfs.meb.ssp.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.web.frontend.dto.ActionRequest;
import ch.bfs.meb.ssp.web.frontend.dto.LastFilters;
import ch.bfs.meb.ssp.web.frontend.dto.PersonRequestBody;
import ch.bfs.meb.ssp.web.frontend.dto.SearchRequest;
import ch.bfs.meb.ssp.web.frontend.dto.SspPersonResponse;
import ch.bfs.meb.ssp.web.service.FilterService;
import ch.bfs.meb.ssp.web.service.ICantonService;
import ch.bfs.meb.ssp.web.service.IPersonService;
import ch.bfs.meb.ssp.web.service.PersonExportService;
import ch.bfs.meb.ssp.web.utils.CsvUtils;
import ch.bfs.meb.ssp.web.utils.FileUtils;
import ch.bfs.meb.ssp.web.ws.sspperson.PlausiError;
import ch.bfs.meb.ssp.web.ws.sspperson.PlausiErrorListResult;
import ch.bfs.meb.ssp.web.ws.sspperson.SspPerson;
import ch.bfs.meb.ssp.web.ws.sspperson.SspPersonListResult;
import ch.bfs.meb.ssp.web.ws.sspperson.SspPersonResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.bfs.meb.integration.dto.ResultBase.OK;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED;

@RestController
@RequestMapping("/persons")
@AllArgsConstructor
@Slf4j
public class PersonRestController {
    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";

    private final IPersonService personService;
    private final PersonExportService personExportService;
    private final WebLocalizationManager localizationManager;
    private final FilterService filterService;
    private final ICantonService cantonService;

    @PostMapping("/search")
    public ResponseEntity<List<SspPersonResponse>> searchPersons(@RequestBody SearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBA_OBJECTTYPE_PERSON);

        List<SspPersonResponse> persons = fetchAllPersons(filterContext, request.getVersion(), cantonId);

        log.info("Search completed - {} persons found", persons.size());
        return ResponseEntity.ok(persons);
    }

    private List<SspPersonResponse> fetchAllPersons(WebFilterContext filterContext, Long version, Long cantonId) {
        int buffer = 500;

        // Premier appel, nous récupérons les resultats, le nombre total (afin de savoir si il faut paginer)
        SspPersonListResult firstResult = fetchBatch(0, buffer, filterContext, version, cantonId);

        Long totalCount = firstResult.getMaxNrOfPersons();
        List<SspPersonResponse> allPersons = new ArrayList<>(totalCount.intValue());
        allPersons.addAll(getSbaPersonResponses(firstResult.getPersons()));

        // Si tout est dans le premier batch
        if (totalCount <= buffer) {
            return allPersons;
        }

        // Sinon récupérer le reste par lots
        for (int start = buffer; start < totalCount; start += buffer) {
            SspPersonListResult batchResult = fetchBatch(start, buffer, filterContext, version, cantonId);
            allPersons.addAll(getSbaPersonResponses(batchResult.getPersons()));
        }

        log.debug("Fetched {} persons in {} batches", allPersons.size(), (totalCount / buffer) + 1);
        return allPersons;
    }

    private List<SspPersonResponse> getSbaPersonResponses(List<SspPerson> persons) {
        if (persons == null || persons.isEmpty()) {
            return Collections.emptyList();
        }
        return persons.stream()
                .map(person -> {
                    SspPersonResponse response = new SspPersonResponse();
                    BeanUtils.copyProperties(person, response);

                    if (response.getPlausiStatus() == MEB_PLAUSISTATUS_NOTVALID
                            || response.getPlausiStatus() == MEB_PLAUSISTATUS_CONFIRMED
                            || response.getPlausiStatus() == MEB_PLAUSISTATUS_UNDEFINED) {
                        List<PlausiError> plausiErrors = getPlausiErrors(response.getPersonId());
                        response.setPlausiErrors(plausiErrors);
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    private SspPersonListResult fetchBatch(int start, int buffer, WebFilterContext filterContext, Long version, Long cantonId) {
        WebSortContext sortContext = defaultWebSortContext();

        SspPersonListResult result = personService.getPersons(
                start, buffer, sortContext, filterContext, version, cantonId
        );

        if (result.getState() != OK) {
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
        WebFilterListResult result = filterService.getFiltersForRefObject(CodegroupUtility.SBA_OBJECTTYPE_PERSON);
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
        return cantonService.getFilterCantonsForActUser().stream()
                .filter(canton -> canton > 0L)
                .findFirst()
                .orElse(null);
    }

    @GetMapping()
    public ResponseEntity<List<SspPerson>> getPersonsByQualifications(@RequestParam List<Long> qualificationIds) {
        SspPersonListResult result = personService.getPersonsOwnedByActivities(qualificationIds, defaultWebSortContext());
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
            WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBA_OBJECTTYPE_PERSON);

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
    public ResponseEntity<SspPerson> validate(@RequestBody ActionRequest actionRequest) {
        if (CollectionUtils.isEmpty(actionRequest.getPersonIds())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        SspPersonResult result = personService.validatePersons(actionRequest.getPersonIds(), false);

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
    public ResponseEntity<SspPerson> undoValidate(@RequestBody ActionRequest actionRequest) {
        if (CollectionUtils.isEmpty(actionRequest.getPersonIds())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        SspPersonResult result = personService.validatePersons(actionRequest.getPersonIds(), true);

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
    public ResponseEntity<SspPerson> createPerson(@RequestBody PersonRequestBody request) {

        SspPersonResult result = personService.insertPerson(request.getPerson(), request.isRegisterWithoutPlausi());
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
    public ResponseEntity<SspPerson> updatePerson(@RequestBody PersonRequestBody request) {
        List<PlausiError> plausiErrors = request.getPerson().getPlausiErrors();
        request.getPerson().setPlausiErrors(null);
        SspPersonResult result = personService.updatePerson(request.getPerson(), plausiErrors, request.isRegisterWithoutPlausi());
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
        SspPersonResult personByIdResult = personService.getPersonById(personId);
        if (personByIdResult.getState() != ResultBase.OK) {
            String message = localizationManager.getMessage(personByIdResult.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }
        SspPersonResult result = personService.deletePerson(personByIdResult.getPerson(), registerWithoutPlausi);

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

    @GetMapping("/{personId}/plausi_errors")
    public ResponseEntity<List<PlausiError>> getPlausiErrorsResponse(@PathVariable Long personId) {
        return ResponseEntity.ok(getPlausiErrors(personId));
    }

    private List<PlausiError> getPlausiErrors(Long personId) {
        PlausiErrorListResult result = personService.getPlausiErrorsForPerson(personId);
        if (result.getState() == ResultBase.OK) {
            return result.getPlausiErrors();
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when retrieving plausiErrors: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

}


