package ch.bfs.meb.sba.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sba.web.frontend.dto.QualificationRequestBody;
import ch.bfs.meb.sba.web.frontend.dto.SbaQualificationResponse;
import ch.bfs.meb.sba.web.frontend.dto.SearchRequest;
import ch.bfs.meb.sba.web.service.FilterService;
import ch.bfs.meb.sba.web.service.IPersonService;
import ch.bfs.meb.sba.web.service.IQualificationService;
import ch.bfs.meb.sba.web.service.QualificationExportService;
import ch.bfs.meb.sba.web.utils.CsvUtils;
import ch.bfs.meb.sba.web.utils.DateUtils;
import ch.bfs.meb.sba.web.utils.FileUtils;
import ch.bfs.meb.sba.web.ws.sbaperson.SbaPerson;
import ch.bfs.meb.sba.web.ws.sbaperson.SbaPersonResult;
import ch.bfs.meb.sba.web.ws.sbaqualification.PlausiError;
import ch.bfs.meb.sba.web.ws.sbaqualification.PlausiErrorListResult;
import ch.bfs.meb.sba.web.ws.sbaqualification.SbaQualification;
import ch.bfs.meb.sba.web.ws.sbaqualification.SbaQualificationListResult;
import ch.bfs.meb.sba.web.ws.sbaqualification.SbaQualificationResult;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.bfs.meb.sba.web.frontend.manager.PersonTableManager.COLUMN_PLAUSISTATUS_ID;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_CONFIRMED;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED;
import static ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult.OK;

@RestController
@RequestMapping("/qualifications")
@AllArgsConstructor
@Slf4j
public class QualificationRestController {

    private final IQualificationService qualificationService;
    private final IPersonService personService;
    private final QualificationExportService qualificationExportService;
    private final WebLocalizationManager localizationManager;
    private final FilterService filterService;

    @PostMapping("/search")
    public ResponseEntity<List<SbaQualificationResponse>> searchQualifications(@RequestBody SearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION);

        List<SbaQualificationResponse> qualifications = fetchAllQualifications(filterContext, request.getVersion(), cantonId);

        log.info("Search completed - {} qualifications found", qualifications.size());
        return ResponseEntity.ok(qualifications);
    }

    private List<SbaQualificationResponse> fetchAllQualifications(WebFilterContext filterContext, Long version, Long cantonId) {
        int buffer = 500;

        // Premier appel, nous récupérons les resultats, le nombre total (afin de savoir si il faut paginer)
        SbaQualificationListResult firstResult = fetchBatch(0, buffer, filterContext, version, cantonId);

        Long totalCount = firstResult.getMaxNrOfQualifications();
        List<SbaQualificationResponse> allQualifications = new ArrayList<>(totalCount.intValue());
        allQualifications.addAll(getSbaQualificationResponses(firstResult.getQualifications()));

        // Si tout est dans le premier batch
        if (totalCount <= buffer) {
            return allQualifications;
        }

        // Sinon récupérer le reste par lots
        for (int start = buffer; start < totalCount; start += buffer) {
            SbaQualificationListResult batchResult = fetchBatch(start, buffer, filterContext, version, cantonId);
            allQualifications.addAll(getSbaQualificationResponses(batchResult.getQualifications()));
        }

        log.debug("Fetched {} qualifications in {} batches", allQualifications.size(), (totalCount / buffer) + 1);
        return allQualifications;
    }

    private List<SbaQualificationResponse> getSbaQualificationResponses(List<SbaQualification> qualifications) {
        if (qualifications == null || qualifications.isEmpty()) {
            return Collections.emptyList();
        }
        return qualifications.stream()
                .map(person -> {
                    SbaQualificationResponse response = new SbaQualificationResponse();
                    BeanUtils.copyProperties(person, response);

                    if (response.getPlausiStatus() == MEB_PLAUSISTATUS_NOTVALID
                            || response.getPlausiStatus() == MEB_PLAUSISTATUS_CONFIRMED
                            || response.getPlausiStatus() == MEB_PLAUSISTATUS_UNDEFINED) {
                        List<PlausiError> plausiErrors = getPlausiErrors(response.getQualificationId());
                        response.setPlausiErrors(plausiErrors);
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    private SbaQualificationListResult fetchBatch(int start, int buffer, WebFilterContext filterContext, Long version, Long cantonId) {
        WebSortContext sortContext = defaultWebSortContext();

        SbaQualificationListResult result = qualificationService.getQualifications(
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
        WebFilterListResult result = filterService.getFiltersForRefObject(CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION);
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
    public ResponseEntity<List<SbaQualification>> getQualificationsByPersons(@RequestParam List<Long> personIds) {
        SbaQualificationListResult result = qualificationService.getQualificationsOwnedByPersons(personIds, defaultWebSortContext());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getQualifications());
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
            WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBA_OBJECTTYPE_QUALIFICATION);

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
    public ResponseEntity<SbaQualification> createPerson(@RequestBody QualificationRequestBody request) {

        SbaQualification qualification = request.getQualification();
        if (request.getPersonId() != null) {
            qualification.setPersonId(request.getPersonId());
            SbaPersonResult personResult = personService.getPersonById(request.getPersonId());
            SbaPerson person = personResult.getPerson();
            qualification.setCanton(person.getCanton());
            qualification.setVersion(person.getVersion());
            qualification.setDeliveryCode(person.getDeliveryCode());
            qualification.setConfigDeliveryCode(person.getConfigDeliveryCode());
        }
        Date date = new Date();
        qualification.setCreationDate(DateUtils.toXMLGregorianCalendar(date));
        qualification.setModificationDate(DateUtils.toXMLGregorianCalendar(date));
        qualification.setDeliveryStatus(CodegroupUtility.MEB_DATASTATUS_DELIVERED);
        qualification.setPlausiStatus(CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED);

        SbaQualificationResult result = qualificationService.insertQualification(qualification, request.isRegisterWithoutPlausi());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getQualification());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when inserting qualification: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PostMapping()
    public ResponseEntity<SbaQualification> updatePerson(@RequestBody QualificationRequestBody request) {
        List<PlausiError> plausiErrors = request.getQualification().getPlausiErrors();
        request.getQualification().setPlausiErrors(null);
        SbaQualificationResult result = qualificationService.updateQualification(
                request.getQualification(),
                plausiErrors,
                request.isRegisterWithoutPlausi());
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getQualification());
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
        SbaQualificationResult qualificationByIdResult = qualificationService.getQualificationById(qualificationId);
        if (qualificationByIdResult.getState() != ResultBase.OK) {
            String message = localizationManager.getMessage(qualificationByIdResult.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }
        SbaQualificationResult result = qualificationService.deleteQualification(qualificationByIdResult.getQualification(), registerWithoutPlausi);

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
        PlausiErrorListResult result = qualificationService.getPlausiErrorsForQualification(qualificationId);
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


