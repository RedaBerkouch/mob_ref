package ch.bfs.meb.sdl.web.frontend.controller.initialisation;

import ch.bfs.meb.sdl.web.frontend.dto.IdSearchRequest;
import ch.bfs.meb.sdl.web.frontend.dto.LastFilters;
import ch.bfs.meb.sdl.web.frontend.dto.SelectedSearchWithSyncRequest;
import ch.bfs.meb.sdl.web.frontend.dto.TunnelApiResponse;
import ch.bfs.meb.sdl.web.service.BurSchoolExportService;
import ch.bfs.meb.sdl.web.service.IBurSchoolService;
import ch.bfs.meb.sdl.web.service.WebFilterService;
import ch.bfs.meb.sdl.web.utils.CsvUtils;
import ch.bfs.meb.sdl.web.utils.FileUtils;
import ch.bfs.meb.sdl.web.ws.sdlburschool.BurSchool;
import ch.bfs.meb.sdl.web.ws.sdlburschool.BurSchoolListResult;
import ch.bfs.meb.sdl.web.ws.sdlburschool.BurSchoolResult;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebSortContext;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.bfs.meb.integration.dto.ResultBase.OK;

@RestController
@RequestMapping("/initialisations/schools")
@AllArgsConstructor
@Slf4j
public class BurSchoolRestController {

    private static final String COLUMN_CANTON_ID = "canton";

    private final IBurSchoolService burSchoolService;
    private final BurSchoolExportService burSchoolExportService;
    private final IFilterService filterService;
    private final WebFilterService webFilterService;
    private final WebLocalizationManager localizationManager;

    /* ******
     * CRUD *
     * ******/
    /**
     * Get a canton based on its id and version.
     *
     * @param request the request containing the parameters of the BurSchool
     * @return a BurSchool object
     */
    @GetMapping
    public ResponseEntity<BurSchool> getById(@RequestBody IdSearchRequest request) {

        BurSchoolResult burSchoolResult = burSchoolService.getBurSchoolById(request.getId(), false, request.getVersion());

        if (burSchoolResult.getState() == OK) {
            return ResponseEntity.ok(burSchoolResult.getSchool());
        }

        String message = localizationManager.getMessage(burSchoolResult.getMessage());
        log.error("Error when retrieving BurSchool by id: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Get all BurSchools, either filtered on a version (year) and optionally a canton or on selected configuration
     * deliveries.
     *
     * @param request the request containing the parameters of the BurSchool to filter on
     * @return List of all BurSchool objects
     */
    @PostMapping("/search")
    public ResponseEntity<List<BurSchool>> getAll(@RequestBody SelectedSearchWithSyncRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(-1L);
        List<Long> configList = Optional.ofNullable(request.getSelectedIds()).orElse(new ArrayList<Long>());
        boolean withSync = Optional.ofNullable(request.getWithSync()).orElse(false);

        List<BurSchool> burSchools = new ArrayList<>();

        if (configList.isEmpty()) {
            WebFilterContext filterContext =
                    webFilterService.filterContextFromSearchRequest(request, CodegroupUtility.SDL_OBJECTTYPE_BURSCHOOL);

            burSchools = fetchAllBurSchool(filterContext, request.getVersion(), cantonId, withSync);
            log.info("Search completed - {} BurSchool found", burSchools.size());
        }
        else {
            BurSchoolListResult burSchoolListResult =
                    burSchoolService.getBurSchoolsOwnedByConfigDeliveries(configList, defaultWebSortContext(), withSync);

            if (burSchoolListResult.getState() == OK) {
                burSchools = burSchoolListResult.getSchools();
            }
        }

        if (!burSchools.isEmpty()) {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            boolean isPrivileged = user.isInRole(SecurityConstants.ROLE_SDL_EA)
                    || user.isInRole(SecurityConstants.ROLE_SDL_EV);

            List<Long> allowedCantons = isPrivileged ? null : user.getCantons();

            List<BurSchool> result = burSchools
                    .stream()
                    .filter(school -> allowedCantons == null
                            || (school.getCanton() != null && allowedCantons.contains(school.getCanton())))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        }

        return ResponseEntity.ok(burSchools);
    }

    private List<BurSchool> fetchAllBurSchool(WebFilterContext filterContext, Long version, Long cantonId,
                                            boolean withSync) {
        int buffer = 500;

        BurSchoolListResult firstResult = fetchBatch(0, buffer, filterContext, version, cantonId, withSync);

        Long totalCount = firstResult.getMaxNrOfSchools();
        List<BurSchool> allBurSchools = new ArrayList<>(totalCount.intValue());
        allBurSchools.addAll(firstResult.getSchools());

        if (totalCount <= buffer) {
            return allBurSchools;
        }

        for (int start = buffer; start < totalCount; start += buffer) {
            BurSchoolListResult batchResult = fetchBatch(start, buffer, filterContext, version, cantonId, withSync);
            allBurSchools.addAll(batchResult.getSchools());
        }

        log.debug("Fetched {} BurSchools in {} batches", allBurSchools.size(), (totalCount / buffer) + 1);
        return allBurSchools;
    }

    private BurSchoolListResult fetchBatch(int start, int buffer, WebFilterContext filterContext, Long version,
                                           Long cantonId, boolean withSynch) {
        WebSortContext sortContext = defaultWebSortContext();

        BurSchoolListResult result = burSchoolService.getBurSchools(
                start, buffer, sortContext, filterContext, version, cantonId, withSynch);

        if (result.getState() != WebFilterListResult.OK) {
            String message = localizationManager.getMessage(result.getMessage());
            log.error("Error fetching persons at offset {}: {}", start, message);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }

        return result;
    }

    private WebSortContext defaultWebSortContext() {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(COLUMN_CANTON_ID);
        sortContext.setLocale(localizationManager.getLocale().toString());
        sortContext.setAscSortOrder(true);
        return sortContext;
    }

    /**
     * Create a BurSchool
     *
     * @param burSchool BurSchool to create
     * @return The new created BurSchool
     */
    @PutMapping
    public ResponseEntity<BurSchool> create(@RequestBody BurSchool burSchool) {
        BurSchoolResult burSchoolResult = burSchoolService.insertBurSchool(burSchool);

        if (burSchoolResult.getState() == OK) {
            return ResponseEntity.ok(burSchoolResult.getSchool());
        }

        String message = localizationManager.getMessage(burSchoolResult.getMessage());
        log.error("Error while creating BurSchool: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Update an existing BurSchool.
     *
     * @param burSchool The BurSchool to update
     * @return The updated BurSchool
     */
    @PostMapping
    public ResponseEntity<BurSchool> update(@RequestBody BurSchool burSchool) {
        BurSchoolResult burSchoolResult = burSchoolService.updateBurSchool(burSchool, false);

        if (burSchoolResult.getState() == OK) {
            return ResponseEntity.ok(burSchoolResult.getSchool());
        }

        String message = localizationManager.getMessage(burSchoolResult.getMessage());
        log.error("Error while updating BurSchool: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Delete a BurSchool by id (path variable)
     *
     * @param id The id of the BurSchool to delete
     * @return 200 OK if deleted successfully
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {

        BurSchoolResult findResult = burSchoolService.getBurSchoolById(id, false, null);
        if (findResult.getState() != OK || findResult.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BurSchool not found: " + id);
        }

        BurSchoolResult burSchoolResult = burSchoolService.deleteBurSchool(findResult.getSchool());

        if (burSchoolResult.getState() == OK) {
            return ResponseEntity.ok().build();
        }

        String message = localizationManager.getMessage(burSchoolResult.getMessage());
        log.error("Error while deleting BurSchool: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Delete a BurSchool
     *
     * @param request The request containing the parameters of the BurSchool to delete
     * @return {@code true} if deleted successfully, else {@code false}
     */
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody IdSearchRequest request) {

        BurSchool burSchoolToDelete =
                burSchoolService.getBurSchoolById(request.getId(), false, request.getVersion()).getSchool();

        BurSchoolResult burSchoolResult = burSchoolService.deleteBurSchool(burSchoolToDelete);

        if (burSchoolResult.getState() == OK) {
            return ResponseEntity.ok().build();
        }

        String message = localizationManager.getMessage(burSchoolResult.getMessage());
        log.error("Error while deleting BurSchool: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /* ******* *
     * Actions *
     * ******* */
    @PostMapping("/export_csv")
    public ResponseEntity<byte[]> exportCsvAction(@RequestBody SelectedSearchWithSyncRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        boolean withSync = Optional.ofNullable(request.getWithSync()).orElse(false);

        WebFilterContext filterContext =
                webFilterService.filterContextFromSearchRequest(request, CodegroupUtility.SDL_OBJECTTYPE_BURSCHOOL);

        try {
            // Générer le CSV
            byte[] csvContent = burSchoolExportService.generateCsvExport(
                    request.getSelectedIds(), request.getVersion(), cantonId, filterContext, withSync);

            // Ajouter le BOM UTF-8 pour Excel
            byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);

            // Construire le header
            HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile("BurSchools", csvWithBom.length);

            return new ResponseEntity<>(csvWithBom, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating cantons CSV export", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV export failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    @PostMapping("/get_bur")
    public ResponseEntity<TunnelApiResponse<BurSchool>> getBurAction(@RequestBody BurSchool burSchool) {
        BurSchoolResult result = burSchoolService.importBurSchool(burSchool);
        boolean success = result.getState() == OK;
        BurSchool school = success ? result.getSchool() : null;
        return ResponseEntity.ok(new TunnelApiResponse<>(200, localizeMessage(result.getMessage()), school, success));
    }

    @PostMapping("/sync_bur")
    public ResponseEntity<TunnelApiResponse<List<BurSchool>>> sync_burAction() {
        BurSchoolListResult burSchoolListResult = burSchoolService.synchronizeSchools();
        boolean success = burSchoolListResult.getState() == OK;
        List<BurSchool> schools = success ? burSchoolListResult.getSchools() : null;
        return ResponseEntity.ok(new TunnelApiResponse<>(200, localizeMessage(burSchoolListResult.getMessage()), schools, success));
    }

    @PostMapping("/get_all_bur/{canton}")
    public ResponseEntity<TunnelApiResponse<Boolean>> getAllBurAction(@PathVariable Long canton) {
        BurSchoolListResult burSchoolListResult = burSchoolService.importBurSchools(canton);
        boolean success = burSchoolListResult.getState() == OK;
        return ResponseEntity.ok(new TunnelApiResponse<>(200, localizeMessage(burSchoolListResult.getMessage()), success, success));
    }

    private String localizeMessage(String messageKey) {
        return (messageKey != null && !messageKey.isEmpty()) ? localizationManager.getMessage(messageKey) : null;
    }

    /* ******* *
     * Filters *
     * ******* */
    @GetMapping("/predefined_filters")
    public ResponseEntity<List<WebFilter>> getFilters() {
        WebFilterListResult result =
                filterService.getFiltersForRefObject(CodegroupUtility.SDL_OBJECTTYPE_BURSCHOOL);

        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getFilters());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when retrieving filters for BurSchool: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    @GetMapping("/lastFilters")
    public LastFilters getLastFilters() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new LastFilters(user.getLastFilterVersion(), user.getLastFilterCanton());
    }

    @PutMapping("/lastFilters")
    public void getLastFilters(LastFilters lastFilters) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.setLastFilterVersion(lastFilters.getVersion());
        user.setLastFilterCanton(lastFilters.getCanton());
    }
}