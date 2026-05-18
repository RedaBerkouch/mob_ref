package ch.bfs.meb.sdl.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.frontend.dto.ActionRequest;
import ch.bfs.meb.sdl.web.frontend.dto.QualificationRequestBody;
import ch.bfs.meb.sdl.web.frontend.dto.SdlClassResponse;
import ch.bfs.meb.sdl.web.frontend.dto.SearchRequest;
import ch.bfs.meb.sdl.web.service.FilterService;
import ch.bfs.meb.sdl.web.service.IClassService;
import ch.bfs.meb.sdl.web.service.ISchoolService;
import ch.bfs.meb.sdl.web.service.QualificationExportService;
import ch.bfs.meb.sdl.web.utils.CsvUtils;
import ch.bfs.meb.sdl.web.utils.DateUtils;
import ch.bfs.meb.sdl.web.utils.FileUtils;
import ch.bfs.meb.sdl.web.ws.sdlclass.PlausiError;
import ch.bfs.meb.sdl.web.ws.sdlclass.PlausiErrorListResult;
import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClass;
import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClassListResult;
import ch.bfs.meb.sdl.web.ws.sdlclass.SdlClassResult;
import ch.bfs.meb.sdl.web.ws.sdlschool.SdlSchool;
import ch.bfs.meb.sdl.web.ws.sdlschool.SdlSchoolResult;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.bfs.meb.integration.dto.ResultBase.OK;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED;

@RestController
@RequestMapping("/qualifications")
@AllArgsConstructor
@Slf4j
public class QualificationRestController {

    public static final String COLUMN_PLAUSISTATUS_ID = "plausiStatus";

    private final ISchoolService personService;
    private final IClassService qualificationService;
    private final QualificationExportService qualificationExportService;
    private final WebLocalizationManager localizationManager;
    private final FilterService filterService;

    @PostMapping("/search")
    public ResponseEntity<List<SdlClassResponse>> searchQualifications(@RequestBody SearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SDL_OBJECTTYPE_CLASS);

        List<SdlClassResponse> qualifications = fetchAllQualifications(filterContext, request.getVersion(), cantonId);

        log.info("Search completed - {} qualifications found", qualifications.size());
        return ResponseEntity.ok(qualifications);
    }

    private List<SdlClassResponse> fetchAllQualifications(WebFilterContext filterContext, Long version, Long cantonId) {
        int buffer = 500;

        // Premier appel, nous récupérons les resultats, le nombre total (afin de savoir si il faut paginer)
        SdlClassListResult firstResult = fetchBatch(0, buffer, filterContext, version, cantonId);

        Long totalCount = firstResult.getMaxNrOfClasses();
        List<SdlClassResponse> allQualifications = new ArrayList<>(totalCount.intValue());
        allQualifications.addAll(getSbaQualificationResponses(firstResult.getClasses()));

        // Si tout est dans le premier batch
        if (totalCount <= buffer) {
            return allQualifications;
        }

        // Sinon récupérer le reste par lots
        for (int start = buffer; start < totalCount; start += buffer) {
            SdlClassListResult batchResult = fetchBatch(start, buffer, filterContext, version, cantonId);
            allQualifications.addAll(getSbaQualificationResponses(batchResult.getClasses()));
        }

        log.debug("Fetched {} qualifications in {} batches", allQualifications.size(), (totalCount / buffer) + 1);
        return allQualifications;
    }

    private List<SdlClassResponse> getSbaQualificationResponses(List<SdlClass> qualifications) {
        if (qualifications == null || qualifications.isEmpty()) {
            return Collections.emptyList();
        }
        return qualifications.stream()
                .map(person -> {
                    SdlClassResponse response = new SdlClassResponse();
                    BeanUtils.copyProperties(person, response);

                    if (response.getPlausiStatus() == MEB_PLAUSISTATUS_NOTVALID
                            || response.getPlausiStatus() == MEB_PLAUSISTATUS_CONFIRMED
                            || response.getPlausiStatus() == MEB_PLAUSISTATUS_UNDEFINED) {
                        List<PlausiError> plausiErrors = getPlausiErrors(response.getClassId());
                        response.setPlausiErrors(plausiErrors);
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    private SdlClassListResult fetchBatch(int start, int buffer, WebFilterContext filterContext, Long version, Long cantonId) {
        WebSortContext sortContext = defaultWebSortContext();

        SdlClassListResult result = qualificationService.getClasses(
                start, buffer, sortContext, filterContext, version, cantonId
        );

        if (result.getState() != OK) {
            String message = localizationManager.getMessage(result.getMessage());
            log.error("Error fetching qualifications at offset {}: {}", start, message);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }

        return result;
    }

    @GetMapping("/predefined_filters")
    public ResponseEntity<List<WebFilter>> getFilters() {
        WebFilterListResult result = filterService.getFiltersForRefObject(CodegroupUtility.SDL_OBJECTTYPE_CLASS);
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
    public ResponseEntity<List<SdlClass>> getQualificationsByPersons(@RequestParam(name = "personIds", required = false) List<Long> personIds,
                                                                     @RequestParam(name = "learnerIds", required = false) List<Long> learnerIds) {
        if (personIds != null && !personIds.isEmpty()) {
            return getQualificationsByPersons(personIds);
        }
        if (learnerIds != null && !learnerIds.isEmpty()) {
            return getQualificationsByLearners(learnerIds);
        }
        log.error("Error when getting qualifications");
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error when getting qualifications"
        );
    }

    private ResponseEntity<List<SdlClass>> getQualificationsByPersons( List<Long> personIds) {
        SdlClassListResult result = qualificationService.getClassesOwnedBySchools(personIds, defaultWebSortContext());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getClasses());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when getting qualifications: {}", message);
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
    }

    private ResponseEntity<List<SdlClass>> getQualificationsByLearners( List<Long> learnerIds) {
        SdlClassListResult result = qualificationService.getClassesOwnedByLearners(learnerIds, defaultWebSortContext());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getClasses());
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
            WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SDL_OBJECTTYPE_CLASS);

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
    public ResponseEntity<SdlClass> createPerson(@RequestBody QualificationRequestBody request) {

        SdlClass qualification = request.getQualification();
        if (request.getPersonId() != null) {
            qualification.setSchoolId(request.getPersonId());
            SdlSchoolResult personResult = personService.getSchoolById(request.getPersonId());
            SdlSchool person = personResult.getSchool();
            qualification.setCanton(person.getCanton());
            qualification.setVersion(person.getVersion());
            qualification.setDeliveryCode(person.getDeliveryCode());
            qualification.setConfigDeliveryCode(person.getConfigDeliveryCode());
        }
        Date date = new Date();
        qualification.setCreationDate(DateUtils.toXMLGregorianCalendar(date));
        qualification.setModificationDate(DateUtils.toXMLGregorianCalendar(date));
        qualification.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
        qualification.setPlausiStatus(MEB_PLAUSISTATUS_UNDEFINED);

        SdlClassResult result = qualificationService.insertClass(qualification, request.isRegisterWithoutPlausi());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getSdlClass());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when inserting qualification: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PostMapping()
    public ResponseEntity<SdlClass> updatePerson(@RequestBody QualificationRequestBody request) {
        List<PlausiError> plausiErrors = request.getQualification().getPlausiErrors();
        request.getQualification().setPlausiErrors(null);
        SdlClassResult result = qualificationService.updateClass(
                request.getQualification(),
                plausiErrors,
                request.isRegisterWithoutPlausi());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getSdlClass());
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
        SdlClassResult qualificationByIdResult = qualificationService.getClassById(qualificationId);
        if (qualificationByIdResult.getState() != ResultBase.OK) {
            String message = localizationManager.getMessage(qualificationByIdResult.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }
        SdlClassResult result = qualificationService.deleteClass(qualificationByIdResult.getSdlClass(), registerWithoutPlausi);

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

    @GetMapping("/{qualificationId}/plausi_errors")
    public ResponseEntity<List<PlausiError>> getPlausiErrorsResponse(@PathVariable Long qualificationId) {
        return ResponseEntity.ok(getPlausiErrors(qualificationId));
    }

    private List<PlausiError> getPlausiErrors(Long qualificationId) {
        PlausiErrorListResult result = qualificationService.getPlausiErrorsForClass(qualificationId);
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

    @PostMapping("/validate")
    public ResponseEntity<SdlClass> validate(@RequestBody ActionRequest actionRequest) {
        if (CollectionUtils.isEmpty(actionRequest.getPersonIds())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        SdlClassResult result = qualificationService.validateClasses(actionRequest.getPersonIds(), false);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getSdlClass());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when validating classes: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PostMapping("/undo_validate")
    public ResponseEntity<SdlClass> undoValidate(@RequestBody ActionRequest actionRequest) {
        if (CollectionUtils.isEmpty(actionRequest.getPersonIds())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        SdlClassResult result = qualificationService.validateClasses(actionRequest.getPersonIds(), true);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getSdlClass());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when validating classes: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

}


