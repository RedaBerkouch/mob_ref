package ch.bfs.meb.sbg.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sbg.web.frontend.dto.DeliveryResponse;
import ch.bfs.meb.sbg.web.frontend.dto.LastFilters;
import ch.bfs.meb.sbg.web.frontend.dto.SearchRequest;
import ch.bfs.meb.sbg.web.service.DeliveryExportService;
import ch.bfs.meb.sbg.web.service.DeliveryService;
import ch.bfs.meb.sbg.web.service.FilterService;
import ch.bfs.meb.sbg.web.utils.CsvUtils;
import ch.bfs.meb.sbg.web.utils.FileUtils;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.DeliveryResult;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.PlausireportResult;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDelivery;
import ch.bfs.meb.sbg.web.ws.sbgdelivery.SbgDeliveryListResult;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterResult;
import ch.bfs.meb.web.commons.dhtmlx.table.WebWhereFilter;
import ch.bfs.meb.web.commons.i18n.WebLocalizationManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult.OK;

@RestController
@RequestMapping("/deliveries")
@AllArgsConstructor
@Slf4j
public class DeliveryRestController {

    public static final String COLUMN_VERSION_ID = "version";
    public static final String COLUMN_CANTON_ID = "canton";
    private static final String COLUMN_DELIVERYID = "deliveryid";

    private final DeliveryService deliveryService;
    private final DeliveryExportService deliveryExportService;
    private final FilterService filterService;
    private final WebLocalizationManager localizationManager;

    @PostMapping("/search")
    public ResponseEntity<List<DeliveryResponse>> searchDeliveries(@RequestBody SearchRequest request) {
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBG_OBJECTTYPE_DELIVERY);

        List<WebWhereFilter> filters = filterContext.getWhereFilter();
        if (!filters.isEmpty()) filters.get(filters.size() - 1).setRelation("AND");
        if (request.getVersion() != null) filters.add(buildFilter(COLUMN_VERSION_ID, request.getVersion().toString()));
        if (request.getCanton() != null && request.getCanton() > 0L) filters.add(buildFilter(COLUMN_CANTON_ID, request.getCanton().toString()));

        SbgDeliveryListResult result = deliveryService.getFilteredDeliveries(filterContext);
        if (result.getState() != WebFilterResult.OK) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, localizationManager.getMessage(result.getMessage()));
        }

        List<DeliveryResponse> deliveries = result.getDeliveries() == null ? Collections.emptyList() :
                result.getDeliveries().stream().map(d -> {
                    DeliveryResponse r = new DeliveryResponse();
                    BeanUtils.copyProperties(d, r);
                    return r;
                }).collect(Collectors.toList());

        log.info("Search completed - {} deliveries found", deliveries.size());
        return ResponseEntity.ok(deliveries);
    }

    private WebWhereFilter buildFilter(String attribute, String value) {
        WebWhereFilter filter = new WebWhereFilter();
        filter.setAttribute(attribute);
        filter.setOperator("=");
        filter.setRelation("AND");
        filter.setValue(value);
        return filter;
    }

    @GetMapping("/predefined_filters")
    public ResponseEntity<List<WebFilter>> getFilters() {
        WebFilterListResult result = filterService.getFiltersForRefObject(CodegroupUtility.SBG_OBJECTTYPE_DELIVERY);
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getFilters());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when retrieving filters: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private SbgDelivery getDelivery(Long deliveryId) {
        WebFilterContext fullFilterContext = new WebFilterContext();

        WebWhereFilter filterVersion = new WebWhereFilter();
        filterVersion.setAttribute(COLUMN_DELIVERYID);
        filterVersion.setOperator("=");
        filterVersion.setRelation("AND");
        filterVersion.setValue(deliveryId.toString());
        fullFilterContext.getWhereFilter().add(filterVersion);

        SbgDeliveryListResult delivery = deliveryService.getFilteredDeliveries(fullFilterContext);
        return delivery.getDeliveries()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{deliveryId}/amend")
    public ResponseEntity<SbgDelivery> amend(@PathVariable Long deliveryId) {
        SbgDelivery delivery = getDelivery(deliveryId);
        DeliveryResult result = deliveryService.amendDelivery(delivery, localizationManager.getLanguage());

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when amending delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/replace")
    public ResponseEntity<SbgDelivery> replace(@PathVariable Long deliveryId) {
        SbgDelivery delivery = getDelivery(deliveryId);
        DeliveryResult result = deliveryService.replaceDelivery(delivery, localizationManager.getLanguage());

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when replacing delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/confirm")
    public ResponseEntity<SbgDelivery> confirm(@PathVariable Long deliveryId) {
        SbgDelivery delivery = getDelivery(deliveryId);
        DeliveryResult result = deliveryService.confirmDelivery(delivery, localizationManager.getLanguage());

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when confirming delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/cancel")
    public ResponseEntity<SbgDelivery> cancel(@PathVariable Long deliveryId) {
        SbgDelivery delivery = getDelivery(deliveryId);
        DeliveryResult result = deliveryService.cancelDelivery(delivery);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when cancelling delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/validate")
    public ResponseEntity<SbgDelivery> validate(@PathVariable Long deliveryId) {
        SbgDelivery delivery = getDelivery(deliveryId);
        DeliveryResult result = deliveryService.validateDelivery(delivery);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when validating delivery: {}", message);
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @PostMapping("/{deliveryId}/undo_validate")
    public ResponseEntity<SbgDelivery> undoValidate(@PathVariable Long deliveryId) {
        SbgDelivery delivery = getDelivery(deliveryId);
        DeliveryResult result = deliveryService.unvalidateDelivery(delivery);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when unvalidating delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/plausi_report")
    public ResponseEntity<SbgDelivery> createPlausiReport(@PathVariable Long deliveryId) {
        SbgDelivery delivery = getDelivery(deliveryId);
        DeliveryResult result = deliveryService.createPlausiReport(delivery);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when creating plausi report: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<Void> delete(@PathVariable Long deliveryId) {
        SbgDelivery delivery = getDelivery(deliveryId);
        DeliveryResult result = deliveryService.deleteDelivery(delivery);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok().build();
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when deleting delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @GetMapping("/lastFilters")
    public LastFilters getLastFilters() {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new LastFilters(user.getLastFilterVersion(), user.getLastFilterCanton());
    }

    @PutMapping("/lastFilters")
    public void setLastFilters(@RequestBody LastFilters lastFilters) {
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.setLastFilterVersion(lastFilters.getVersion());
        user.setLastFilterCanton(lastFilters.getCanton());
    }

    @PostMapping("/export/csv")
    public ResponseEntity<byte[]> exportDeliveriesCsv(@RequestBody SearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SBG_OBJECTTYPE_DELIVERY);
        try {
            byte[] csvContent = deliveryExportService.generateCsvExport(request.getVersion(), cantonId, filterContext);
            byte[] csvWithBom = CsvUtils.addUtf8Bom(csvContent);
            HttpHeaders headers = FileUtils.getHttpHeadersForCsvFile("deliveries", csvWithBom.length);
            return new ResponseEntity<>(csvWithBom, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating deliveries CSV export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSV export failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/{id}/plausi_report")
    public ResponseEntity<byte[]> showLastPlausireport(@PathVariable Long id) {
        try {
            PlausireportResult result = deliveryService.getLastPlausiReport(id, localizationManager.getLanguage().toLowerCase());

            if (result.getState() != ResultBase.OK) {
                throw new RuntimeException(result.getMessage());
            }

            byte[] content = result.getPlausireport();
            byte[] fromZip = FileUtils.extractFromZipIfNeeded(content);
            HttpHeaders headers = FileUtils.getHttpHeadersForXlsxFile("PlausiReport", fromZip);

            return new ResponseEntity<>(fromZip, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating deliveries Plausi Report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Plausi Report failed".getBytes(StandardCharsets.UTF_8));
        }
    }
}