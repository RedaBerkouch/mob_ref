package ch.bfs.meb.ssp.web.frontend.controller.initialisation;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.ssp.web.frontend.dto.LastFilters;
import ch.bfs.meb.ssp.web.frontend.dto.SelectedSearchRequest;
import ch.bfs.meb.ssp.web.service.ConfigDeliveryExportService;
import ch.bfs.meb.ssp.web.service.IConfigDeliveryService;
import ch.bfs.meb.ssp.web.service.WebFilterService;
import ch.bfs.meb.ssp.web.utils.CsvUtils;
import ch.bfs.meb.ssp.web.utils.FileUtils;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.ConfigDelivery;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.ConfigDeliveryListResult;
import ch.bfs.meb.ssp.web.ws.sspconfigdelivery.ConfigDeliveryResult;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult.OK;

@RestController
@RequestMapping("/initialisations/config-deliveries")
@AllArgsConstructor
@Slf4j
public class ConfigDeliveryRestController {

    private static final String COLUMN_CANTON_ID = "canton";

    private final IConfigDeliveryService configDeliveryService;
    private final ConfigDeliveryExportService configDeliveryExportService;
    private final WebFilterService webFilterService;
    private final IFilterService filterService;
    private final WebLocalizationManager localizationManager;
    
    /* ******
     * CRUD *
     * ******/
    /**
     * Get a configDelivery based on its id.
     *
     * @param id the configDelivery id to get.
     * @return a CantonIntervention object
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConfigDelivery> getById(@PathVariable Long id) {

        ConfigDeliveryResult configDeliveryResult = configDeliveryService.getConfigDeliveryById(id);

        if (configDeliveryResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(configDeliveryResult.getConfigDelivery());
        }

        String message = localizationManager.getMessage(configDeliveryResult.getMessage());
        log.error("Error when retrieving configDelivery: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }
    
    
    /**
     * Get all configuration deliveries, filtered on a version (year) and optionally a canton or on selected schools.
     *
     * @param request request containing the parameters of the ConfigDelivery to filter on
     * @return List of all ConfigDelivery objects, filtered by the given parameters.
     */
    @PostMapping("/search")
    public ResponseEntity<List<ConfigDelivery>> getAll(@RequestBody SelectedSearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(-1L);
        List<Long> schoolList = Optional.ofNullable(request.getSelectedIds()).orElse(new ArrayList<>());

        WebFilterContext filterContext =
                webFilterService.filterContextFromSearchRequest(request, CodegroupUtility.SSP_OBJECTTYPE_CONFIGDELIVERY);

        ConfigDeliveryListResult configDeliveryResults;

        if (schoolList.isEmpty()) {
            configDeliveryResults =
                    configDeliveryService.getConfigDeliveries(-1, -1, defaultWebSortContext(), filterContext, request.getVersion(), cantonId);
        } else {
            configDeliveryResults =
                    configDeliveryService.getConfigDeliveriesOwnedBySchools(schoolList, defaultWebSortContext(), request.getVersion());
        }

        if (configDeliveryResults.getState() == OK) {
            MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            boolean isPrivileged = user.isInRole(SecurityConstants.ROLE_SSP_EA)
                    || user.isInRole(SecurityConstants.ROLE_SSP_EV);

            List<ConfigDelivery> configDeliveries = configDeliveryResults.getConfigDeliveries();

            if (!isPrivileged) {
                List<Long> allowedCantons = user.getCantons();
                String userEmail = user.getEmail();

                configDeliveries = configDeliveries.stream()
                        .filter(config ->
                                isAllowedCanton(config, allowedCantons)
                                        && containsUserEmail(config.getDlUsers(), userEmail))
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(configDeliveries);
        }

        String message = localizationManager.getMessage(configDeliveryResults.getMessage());
        log.error("Error when getting all config deliveries {}: {}",
                schoolList.isEmpty() ? "" : "owned by school", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    private boolean isAllowedCanton(ConfigDelivery config, List<Long> allowedCantons) {
        return config.getCanton() != null
                && allowedCantons != null
                && allowedCantons.contains(config.getCanton());
    }

    private boolean containsUserEmail(String dlUsers, String userEmail) {
        if (dlUsers == null || dlUsers.trim().isEmpty() || userEmail == null || userEmail.trim().isEmpty()) {
            return false;
        }

        return Arrays.stream(dlUsers.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(email -> email.equalsIgnoreCase(userEmail));
    }

    private WebSortContext defaultWebSortContext() {
        WebSortContext sortContext = new WebSortContext();
        sortContext.setSortColumn(COLUMN_CANTON_ID);
        sortContext.setLocale(localizationManager.getLocale().toString());
        sortContext.setAscSortOrder(true);
        return sortContext;
    }

    /**
     * Create a ConfigDelivery
     *
     * @param configDelivery ConfigDelivery to create
     * @return The new created ConfigDelivery
     */
    @PutMapping
    public ResponseEntity<ConfigDelivery> create(@RequestBody ConfigDelivery configDelivery) {
        ConfigDeliveryResult configDeliveryResult = configDeliveryService.insertConfigDelivery(configDelivery);

        if (configDeliveryResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(configDeliveryResult.getConfigDelivery());
        }

        String message = localizationManager.getMessage(configDeliveryResult.getMessage());
        log.error("Error while creating configDelivery: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Update an existing ConfigDelivery.
     *
     * @param configDelivery The ConfigDelivery to update
     * @return The updated ConfigDelivery
     */
    @PostMapping
    public ResponseEntity<ConfigDelivery> update(@RequestBody ConfigDelivery configDelivery) {
        ConfigDeliveryResult configDeliveryResult = configDeliveryService.updateConfigDelivery(configDelivery);

        if (configDeliveryResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok(configDeliveryResult.getConfigDelivery());
        }

        String message = localizationManager.getMessage(configDeliveryResult.getMessage());
        log.error("Error while updating ConfigDelivery: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /**
     * Delete a ConfigDelivery
     *
     * @param id ConfigDelivery's id to create
     * @return {@code true} if deleted successfully, else {@code false}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        ConfigDelivery configDeliveryToDelete = configDeliveryService.getConfigDeliveryById(id).getConfigDelivery();

        ConfigDeliveryResult configDeliveryResult = configDeliveryService.deleteConfigDelivery(configDeliveryToDelete);

        if (configDeliveryResult.getState() == ResultBase.OK) {
            return ResponseEntity.ok().build();
        }

        String message = localizationManager.getMessage(configDeliveryResult.getMessage());
        log.error("Error while deleting Canton: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }

    /* ******* *
     * Actions *
     * ******* */
    @PostMapping("/export_csv")
    public ResponseEntity<byte[]> exportCsvAction(@RequestBody SelectedSearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        WebFilterContext filterContext =
                webFilterService.filterContextFromSearchRequest(request, CodegroupUtility.SSP_OBJECTTYPE_CONFIGDELIVERY);
        try {
            // Générer le CSV
            byte[] csvContent = configDeliveryExportService.generateCsvExport(
                    request.getSelectedIds(), request.getVersion(), cantonId, filterContext);

            // Ajouter le BOM UTF-8 pour Excel
            byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);

            // Construire le header
            HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile("ConfigDeliveries", csvWithBom.length);

            return new ResponseEntity<>(csvWithBom, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating cantons CSV export", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV export failed".getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /* ******* *
     * Filters *
     * ******* */
    @GetMapping("/predefined_filters")
    public ResponseEntity<List<WebFilter>> getFilters() {
        WebFilterListResult result =
                filterService.getFiltersForRefObject(CodegroupUtility.SSP_OBJECTTYPE_CONFIGDELIVERY);
        
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getFilters());
        }
        
        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when retrieving filters for ConfigDelivery: {}", message);
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
