package ch.bfs.meb.ssp.web.frontend.controller.initialisation;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.web.frontend.dto.CommonSearchRequest;
import ch.bfs.meb.ssp.web.frontend.dto.InitVersionRequest;
import ch.bfs.meb.ssp.web.frontend.dto.LastFilters;
import ch.bfs.meb.ssp.web.service.CantonExportService;
import ch.bfs.meb.ssp.web.service.ICantonService;
import ch.bfs.meb.ssp.web.utils.CsvUtils;
import ch.bfs.meb.ssp.web.utils.FileUtils;
import ch.bfs.meb.ssp.web.ws.sspcanton.Canton;
import ch.bfs.meb.ssp.web.ws.sspcanton.CantonListResult;
import ch.bfs.meb.ssp.web.ws.sspcanton.CantonResult;
import ch.bfs.meb.ssp.web.ws.sspcanton.FileResult;
import ch.bfs.meb.ssp.web.ws.sspcanton.PlausiError;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import ch.bfs.meb.web.commons.util.FilterContextUtility;
import ch.bfs.meb.web.commons.util.IFilterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult.OK;

@RestController
@RequestMapping("/initialisations/cantons")
@AllArgsConstructor
@Slf4j
public class CantonRestController {

    private final ICantonService cantonService;
    private final CantonExportService cantonExportService;
    private final IFilterService filterService;
    private final WebLocalizationManager localizationManager;

    private static final String FINALIZE = "finalize";
    private static final String VALIDATE = "validate";
    private static final String CREATE_PLAUSI_REPORT = "create_plausi_report";

    private static final String INIT_BUR_NOT_SYNCHRON_MESSAGE = "init.burnotsynchron.message";
    private static final String INIT_ALREADY_DONE_1_MESSAGE = "init.alreadydone1.message";
    private static final String INIT_ALREADY_DONE_2_MESSAGE = "init.alreadydone2.message";

    /* ******
     * CRUD *
     * ******/
    /**
     * Get a canton based on its id.
     *
     * @param id the id of the canton to get.
     * @return a Canton object
     */
    @GetMapping("/{id}")
    public ResponseEntity<Canton> getById(@PathVariable Long id) {

        CantonResult cantonResult = cantonService.getCantonById(id);

        if (cantonResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(cantonResult.getCanton());
        }

        String message = localizationManager.getMessage(cantonResult.getMessage());
        log.error("Error when retrieving canton: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Get all cantons, filtered by a version (year) and optionally a canton
     *
     * @param request request containing version and optionally a canton to filter on
     * @return List of all Canton objects filtered on the version and optionally a canton.
     */
    @PostMapping("/search")
    public ResponseEntity<List<Canton>> getAll(@RequestBody CommonSearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);

        CantonListResult cantonResult = cantonService.getCantons(request.getVersion(), cantonId);

        if (cantonResult.getState() == OK) {
            return ResponseEntity.ok(cantonResult.getCantons());
        }

        String message = localizationManager.getMessage(cantonResult.getMessage());
        log.error("Error when getting all Cantons: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Create a Canton
     *
     * @param canton Canton to create
     * @return The new created Canton
     */
    @PutMapping
    public ResponseEntity<Canton> create(@RequestBody Canton canton) {
        CantonResult cantonResult = cantonService.insertCanton(canton);

        if (cantonResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(cantonResult.getCanton());
        }

        String message = localizationManager.getMessage(cantonResult.getMessage());
        log.error("Error while creating Canton: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Update an existing Canton.
     *
     * @param canton The Canton object to update
     * @return The updated Canton
     */
    @PostMapping
    public ResponseEntity<Canton> update(@RequestBody Canton canton) {
        // TODO loadPlausiError
        List<PlausiError> plausiErrors = new ArrayList<>();

        CantonResult cantonResult = cantonService.updateCanton(canton,plausiErrors);

        if (cantonResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(cantonResult.getCanton());
        }

        String message = localizationManager.getMessage(cantonResult.getMessage());
        log.error("Error while updating Canton: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Delete a Canton
     *
     * @param id Canton's id to delete
     * @return {@code true} if deleted successfully, else {@code false}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        CantonResult cantonToDelete = cantonService.getCantonById(id);
        String msgKey;

        if (cantonToDelete.getState() == ResultBase.OK) {
            CantonResult result = cantonService.deleteCanton(cantonToDelete.getCanton());

            if (result.getState() == ResultBase.OK) {
                return ResponseEntity.ok().build();
            }
            else {
                msgKey = result.getMessage();
            }
        }
        else {
            msgKey = cantonToDelete.getMessage();
        }

        String message = localizationManager.getMessage(msgKey);
        log.error("Error while deleting Canton: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    @GetMapping("missing_cantons")
    public ResponseEntity<List<Long>> missingCanton(@RequestParam Long version) {
        List<Long> userCantons = cantonService.getFilterCantonsForActUser();

        List<Long> existingCantonIds =
                cantonService.getCantons(version, -1L).getCantons().stream()
                        .map(Canton::getCanton)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(userCantons.stream()
                .filter(canton -> !existingCantonIds.contains(canton))
                .sorted()
                .collect(Collectors.toList()));
    }

    /* ************* *
     * Hidden action *
     * ************* */
    @PostMapping("init_version")
    public ResponseEntity<String> initVersion(@RequestBody InitVersionRequest initVersionRequest) {
        // vérifier qu'un init n'est pas déjà en train de tourner
        CantonListResult initResult =
                cantonService.initVersion(initVersionRequest.getVersion(), initVersionRequest.getCanton(), initVersionRequest.isNoSync());
        //boolean sync_bur = false;

        try {
            if (initResult.getState() == ResultBase.OK) {
                if (!initVersionRequest.isNoSync()) {
                    //sync_bur = true;
                }
            } else {
                List<String> possibleErrorMessages = Arrays.asList(INIT_BUR_NOT_SYNCHRON_MESSAGE, INIT_ALREADY_DONE_1_MESSAGE, INIT_ALREADY_DONE_2_MESSAGE);
                if (!initVersionRequest.isNoSync() && possibleErrorMessages.contains(initResult.getMessage())) {
                    //sync_bur = true;
                }
            }

            // TODO retourner paramètre sync_bur en plus
            return ResponseEntity.ok(localizationManager.getMessage(initResult.getMessage()));
        }
        catch (RuntimeException e) {
            log.error("Error while initializing version", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while initializing version");
        }

    }

    /* ******* *
     * Actions *
     * ******* */
    @PostMapping("/export_csv")
    public ResponseEntity<byte[]> exportCsvAction(@RequestBody CommonSearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);

        try {
            // Générer le CSV
            byte[] csvContent = cantonExportService.generateCsvExport(request.getVersion(), cantonId);

            // Ajouter le BOM UTF-8 pour Excel
            byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);

            // Construire le header
            HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile("Cantons", csvWithBom.length);

            return new ResponseEntity<>(csvWithBom, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating cantons CSV export", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV export failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    @PostMapping("/finalize/{id}")
    public ResponseEntity<Canton> finalizeAction(@PathVariable Long id) {
        return doAction(id, FINALIZE, false);
    }

    @PostMapping("/undo_finalize/{id}")
    public ResponseEntity<Canton> undoFinalizeAction(@PathVariable Long id) {
        return doAction(id, FINALIZE, true);
    }

    @PostMapping("/validate/{id}")
    public ResponseEntity<Canton> validateAction(@PathVariable Long id) {
        return doAction(id, VALIDATE, false);
    }

    @PostMapping("/undo_validate/{id}")
    public ResponseEntity<Canton> undoValidateAction(@PathVariable Long id) {
        return doAction(id, VALIDATE, true);
    }

    @PostMapping("/create_plausi_report/{id}")
    public ResponseEntity<Canton> createPlausiReportAction(@PathVariable Long id) {
        return doAction(id, CREATE_PLAUSI_REPORT, false); // undo parameter is usless
    }

    private ResponseEntity<Canton> doAction(Long id, String actionName, boolean undo) {
        CantonResult canton = cantonService.getCantonById(id);
        String msgKey;

        if (canton.getState() == ResultBase.OK) {
            CantonResult result;

            if (FINALIZE.equals(actionName)) {
                result = cantonService.finalizeCanton(canton.getCanton(), undo);
            }
            else if (VALIDATE.equals(actionName)) {
                result = cantonService.validateCanton(canton.getCanton(), undo);
            }
            else {
                result = cantonService.createPlausireport(canton.getCanton());
            }

            if (result.getState() == ResultBase.OK) {
                return ResponseEntity.ok(result.getCanton());
            }
            else {
                msgKey = result.getMessage();
            }
        }
        else {
            msgKey = canton.getMessage();
        }

        String message = localizationManager.getMessage(msgKey);
        log.error("Error while {} action '{}': {}", (undo ? "un" : "") + "doing", actionName, message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    @GetMapping("/show_last_plausi_report/{id}")
    public ResponseEntity<byte[]> showLastPlausiReportAction(@PathVariable Long id) {
        try {
            // Générer le contenu
            FileResult result = cantonService.getLastPlausireport(id);

            if (result.getState() != ResultBase.OK) {
                throw new RuntimeException(result.getMessage());
            }

            byte[] content = result.getBinaryFile();

            // Construire le header
            byte[] fromZip = FileUtils.extractFromZipIfNeeded(content);
            HttpHeaders headers = FileUtils.getHttpHeadersForXlsxFile("PlausiReport", fromZip);

            return new ResponseEntity<>(fromZip, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error while generating cantons Plausi Report", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Plausi Report failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    /* ******* *
     * Filters *
     * ******* */
    @GetMapping("/predefined_filters")
    public ResponseEntity<List<WebFilter>> getFilters() {
        WebFilterListResult result = filterService.getFiltersForRefObject(CodegroupUtility.SSP_OBJECTTYPE_CANTON);
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getFilters());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when retrieving filters for Canton: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    @GetMapping("/lastFilters")
    public LastFilters getLastFilters() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long canton = user.getLastFilterCanton();

        if (canton == null) {
            canton = cantonService.getFilterCantonsForActUser().get(0);
        }

        Long version = FilterContextUtility.getInitVersion(filterService, CodegroupUtility.SSP_OBJECTTYPE_CONFIGURATION);

        return new LastFilters(version, canton);
    }

    @PutMapping("/lastFilters")
    public void getLastFilters(LastFilters lastFilters) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        user.setLastFilterVersion(lastFilters.getVersion());
        user.setLastFilterCanton(lastFilters.getCanton());
    }
}