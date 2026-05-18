package ch.bfs.meb.sdl.web.frontend.controller;

import ch.bfs.meb.integration.dto.ResultBase;
import ch.bfs.meb.sdl.web.frontend.dto.DeliveryResponse;
import ch.bfs.meb.sdl.web.frontend.dto.LastFilters;
import ch.bfs.meb.sdl.web.frontend.dto.SearchRequest;
import ch.bfs.meb.sdl.web.service.DeliveryExportService;
import ch.bfs.meb.sdl.web.service.DeliveryService;
import ch.bfs.meb.sdl.web.service.FilterService;
import ch.bfs.meb.sdl.web.utils.CsvUtils;
import ch.bfs.meb.sdl.web.utils.FileUtils;
import ch.bfs.meb.sdl.web.ws.sdldelivery.FileResult;
import ch.bfs.meb.sdl.web.ws.sdldelivery.PlausiError;
import ch.bfs.meb.sdl.web.ws.sdldelivery.PlausiErrorListResult;
import ch.bfs.meb.sdl.web.ws.sdldelivery.SdlDelivery;
import ch.bfs.meb.sdl.web.ws.sdldelivery.SdlDeliveryListResult;
import ch.bfs.meb.sdl.web.ws.sdldelivery.SdlDeliveryResult;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.bfs.meb.util.CodegroupUtility.MEB_PLAUSISTATUS_NOTVALID;
import static ch.bfs.meb.web.commons.dhtmlx.table.WebFilterListResult.OK;

@RestController
@RequestMapping("/deliveries")
@AllArgsConstructor
@Slf4j
public class DeliveryRestController {

    private final DeliveryService deliveryService;
    private final DeliveryExportService deliveryExportService;
    private final FilterService filterService;
    private final WebLocalizationManager localizationManager;

    @PostMapping("/search")
    public ResponseEntity<List<DeliveryResponse>> searchDeliveries(@RequestBody SearchRequest request) {
        Long cantonId = Optional.ofNullable(request.getCanton()).orElse(0L);
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);

        List<DeliveryResponse> deliveries = fetchAllDeliveries(filterContext, request.getVersion(), cantonId);

        log.info("Search completed - {} deliveries found", deliveries.size());
        return ResponseEntity.ok(deliveries);
    }

    private List<DeliveryResponse> fetchAllDeliveries(WebFilterContext filterContext, Long version, Long cantonId) {
        int buffer = 500;

        SdlDeliveryListResult firstResult = fetchBatch(0, buffer, filterContext, version, cantonId);

        Long totalCount = firstResult.getMaxNrOfDeliveries();
        List<DeliveryResponse> allDeliveries = new ArrayList<>(totalCount.intValue());
        allDeliveries.addAll(getDeliveryResponses(firstResult.getDeliveries()));

        if (totalCount <= buffer) {
            return allDeliveries;
        }

        for (int start = buffer; start < totalCount; start += buffer) {
            SdlDeliveryListResult batchResult = fetchBatch(start, buffer, filterContext, version, cantonId);
            allDeliveries.addAll(getDeliveryResponses(batchResult.getDeliveries()));
        }

        log.debug("Fetched {} deliveries in {} batches", allDeliveries.size(), (totalCount / buffer) + 1);
        return allDeliveries;
    }

    private List<DeliveryResponse> getDeliveryResponses(List<SdlDelivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return Collections.emptyList();
        }
        return deliveries.stream()
                .map(this::mapToDeliveryResponse)
                .collect(Collectors.toList());
    }

    private DeliveryResponse mapToDeliveryResponse(SdlDelivery delivery) {
        DeliveryResponse response = new DeliveryResponse();
        BeanUtils.copyProperties(delivery, response);
        if (response.getPlausiStatus() == MEB_PLAUSISTATUS_NOTVALID) {
            List<PlausiError> plausiErrors = getPlausiErrors(response.getDeliveryId());
            response.setPlausiErrors(plausiErrors);
        }
        return response;
    }

    private SdlDeliveryListResult fetchBatch(int start, int buffer, WebFilterContext filterContext, Long version, Long cantonId) {
        SdlDeliveryListResult result = deliveryService.getDeliveries(
                start, buffer, new WebSortContext(), filterContext, version, cantonId
        );

        if (result.getState() != WebFilterResult.OK) {
            String message = localizationManager.getMessage(result.getMessage());
            log.error("Error fetching deliveries at offset {}: {}", start, message);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return result;
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.getDeliveryById(deliveryId);
        if (result.getState() == OK) {
            SdlDelivery delivery = result.getDelivery();
            DeliveryResponse deliveryResponse = mapToDeliveryResponse(delivery);
            return ResponseEntity.ok(deliveryResponse);
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when getting delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @GetMapping("/{deliveryId}/refresh")
    public ResponseEntity<DeliveryResponse> refreshStatus(@PathVariable Long deliveryId) {
        SdlDelivery beforeRefresh = deliveryService.getDeliveryById(deliveryId).getDelivery();
        if (!beforeRefresh.isCreatingReport()) {
            DeliveryResponse response = mapToDeliveryResponse(beforeRefresh);
            return ResponseEntity.ok(response);
        }

        SdlDeliveryResult result = deliveryService.refreshStatus(beforeRefresh);
        if (result.getState() == OK) {
            DeliveryResponse deliveryResponse = mapToDeliveryResponse(result.getDelivery());
            return ResponseEntity.ok(deliveryResponse);
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when refreshing delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @GetMapping("/predefined_filters")
    public ResponseEntity<List<WebFilter>> getFilters() {
        WebFilterListResult result = filterService.getFiltersForRefObject(CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
        if (result.getState() == OK) {
            return ResponseEntity.ok(result.getFilters());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when retrieving filters: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/amend")
    public ResponseEntity<SdlDelivery> amend(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.amendDelivery(deliveryId);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when amending delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/replace")
    public ResponseEntity<SdlDelivery> replace(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.replaceDelivery(deliveryId);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when replacing delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/confirm")
    public ResponseEntity<SdlDelivery> confirm(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.confirmDelivery(deliveryId);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when confirming delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/cancel")
    public ResponseEntity<SdlDelivery> cancel(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.cancelDelivery(deliveryId);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when cancelling delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/validate")
    public ResponseEntity<SdlDelivery> validate(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.validateDelivery(deliveryId, false);

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
    public ResponseEntity<SdlDelivery> undoValidate(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.validateDelivery(deliveryId, true);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when unvalidating delivery: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/{deliveryId}/plausi_report")
    public ResponseEntity<SdlDelivery> createPlausiReport(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.createPlausireport(deliveryId);

        if (result.getState() == ResultBase.OK) {
            return ResponseEntity.ok(result.getDelivery());
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when creating plausi report: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<Void> delete(@PathVariable Long deliveryId) {
        SdlDeliveryResult result = deliveryService.deleteDelivery(deliveryId);

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
        WebFilterContext filterContext = filterService.filterContextFromSearchRequest(request, CodegroupUtility.SDL_OBJECTTYPE_DELIVERY);
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
            FileResult result = deliveryService.getLastPlausireport(id);

            if (result.getState() != ResultBase.OK) {
                throw new RuntimeException(result.getMessage());
            }

            byte[] content = result.getBinaryFile();
            byte[] fromZip = FileUtils.extractFromZipIfNeeded(content);
            HttpHeaders headers = FileUtils.getHttpHeadersForXlsxFile("PlausiReport", fromZip);

            return new ResponseEntity<>(fromZip, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating deliveries Plausi Report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Plausi Report failed".getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/{deliveryId}/plausi_errors")
    public ResponseEntity<List<PlausiError>> getPlausiErrorsResponse(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(getPlausiErrors(deliveryId));
    }

    private List<PlausiError> getPlausiErrors(Long deliveryId) {
        PlausiErrorListResult result = deliveryService.getPlausiErrorsForDelivery(deliveryId);
        if (result.getState() == ResultBase.OK) {
            return result.getPlausiErrors();
        }

        String message = localizationManager.getMessage(result.getMessage());
        log.error("Error when retrieving plausiErrors: {}", message);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}