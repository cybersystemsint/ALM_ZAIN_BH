package com.zain.jo.alm.pomanagement.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zain.jo.alm.pomanagement.dto.PoItemDto;
import com.zain.jo.alm.pomanagement.dto.request.BulkDeleteRequest;
import com.zain.jo.alm.pomanagement.dto.request.FilterOperator;
import com.zain.jo.alm.pomanagement.dto.request.FilterRequest;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.dto.response.BulkOperationResult;
import com.zain.jo.alm.pomanagement.dto.response.PageResult;
import com.zain.jo.alm.pomanagement.dto.response.WorkflowActionResponse;
import com.zain.jo.alm.pomanagement.service.PoItemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class POItemController {

    private static final Logger logger = LoggerFactory.getLogger(POItemController.class);

    private final PoItemService poItemService;
    private final ObjectMapper  objectMapper;

    @Autowired
    public POItemController(PoItemService poItemService) {
        this.poItemService = poItemService;
        this.objectMapper  = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ──────────────────────────────────────────────
    // Queries
    // ──────────────────────────────────────────────

    /**
     * POST /po-items
     * Paginated, filterable list of PO items.
     */
    @PostMapping(value = "/po-items",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.ALL_VALUE)
    public ResponseEntity<PageResult<PoItemDto>> fetchPOItems(
            @RequestBody(required = false) String rawBody,
            @RequestParam(value = "poNumber",     required = false) String poNumberParam,
            @RequestParam(value = "searchQuery",  required = false) String searchQueryParam,
            @RequestParam(value = "searchColumn", required = false) String searchColumnParam,
            @RequestParam(value = "page",         required = false) Integer pageParam,
            @RequestParam(value = "size",         required = false) Integer sizeParam,
            HttpServletRequest servletRequest) {

        SearchRequest request = parseSearchRequest(rawBody, servletRequest);

        if (isBlank(request.getSearchQuery())  && !isBlank(searchQueryParam))  request.setSearchQuery(searchQueryParam);
        if (isBlank(request.getSearchColumn()) && !isBlank(searchColumnParam)) request.setSearchColumn(searchColumnParam);
        if (request.getPage() == null && pageParam != null) request.setPage(pageParam);
        if (request.getSize() == null && sizeParam != null) request.setSize(sizeParam);

        String effectivePoNumber = effectiveValue(request.getPoNumber(), poNumberParam);
        injectPoNumberFilter(effectivePoNumber, request);

        return ResponseEntity.ok(poItemService.getPOItems(request));
    }

    // ──────────────────────────────────────────────
    // Single mutations
    // ──────────────────────────────────────────────

    /**
     * POST /poItems
     * Add a single PO item.
     */
    @PostMapping(value = "/poItems", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> addSinglePoItem(@RequestBody String rawJson) {
        logger.info("POST /poItems");
        Map<String, String> result = poItemService.addSinglePOItem(rawJson);
        HttpStatus status = "Error".equals(result.get("status"))
                ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * PUT /poItems/{recordNo}
     */
    @PutMapping(value = "/poItems/{recordNo}")
    public ResponseEntity<Map<String, String>> updatePoItem(
            @PathVariable long recordNo,
            @RequestBody String rawJson) {
        logger.info("PUT /poItems/{}", recordNo);
        Map<String, String> result = poItemService.updatePOItem(recordNo, rawJson);
        HttpStatus status = "Error".equals(result.get("status"))
                ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * POST /poItems/{recordNo}
     */
    @PostMapping(value = "/poItems/{recordNo}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> requestDeletePoItemPost(
            @PathVariable long recordNo,
            @RequestBody(required = false) Map<String, Object> payload) {
        logger.info("POST /poItems/{} (delete request)", recordNo);
        String requestedBy = payload != null && payload.get("requestedBy") != null
                ? payload.get("requestedBy").toString().trim() : null;
        Map<String, String> result = poItemService.requestSingleDelete(recordNo, requestedBy);
        HttpStatus status = "Error".equals(result.get("status"))
                ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    // ──────────────────────────────────────────────
    // Bulk mutations
    // ──────────────────────────────────────────────

    /**
     * POST /poItems/bulk
     */
    @PostMapping(value = "/poItems/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BulkOperationResult> addOrUpdatePoItems(@RequestBody String rawJsonArray) {
        logger.info("POST /poItems/bulk");
        BulkOperationResult result = poItemService.addOrUpdateBulk(rawJsonArray);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /poItems/delete
     */
    @PostMapping(value = "/poItems/delete",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> requestDeletePoItemsBulk(@RequestBody BulkDeleteRequest req) {
        logger.info("POST /poItems/delete | recordNos: {}",
                req != null ? req.getRecordNos() : null);
        if (req == null || req.getRecordNos() == null || req.getRecordNos().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    errorBody("recordNos must be provided as a non-empty array"));
        }
        Map<String, Object> result = poItemService.requestBulkDelete(req);
        return ResponseEntity.ok(result);
    }

    // ──────────────────────────────────────────────
    // Approval / Rejection
    // ──────────────────────────────────────────────

    /**
     * POST /poItems/approve
     */
    @PostMapping(value = "/poItems/approve")
    public ResponseEntity<WorkflowActionResponse> approvePoItems(
            @RequestBody Map<String, Object> requestBody) {
        logger.info("POST /poItems/approve");
        WorkflowActionResponse response = poItemService.processWorkflowAction(requestBody, "approve");
        int httpStatus = "PartialFailure".equals(response.getStatus()) ? 207 : 200;
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * POST /poItems/reject
     */
    @PostMapping(value = "/poItems/reject")
    public ResponseEntity<WorkflowActionResponse> rejectPoItems(
            @RequestBody Map<String, Object> requestBody) {
        logger.info("POST /poItems/reject");
        WorkflowActionResponse response = poItemService.processWorkflowAction(requestBody, "reject");
        int httpStatus = "PartialFailure".equals(response.getStatus()) ? 207 : 200;
        return ResponseEntity.status(httpStatus).body(response);
    }

    // ──────────────────────────────────────────────
    // Private utilities (controller concerns only)
    // ──────────────────────────────────────────────
    private SearchRequest parseSearchRequest(String rawBody, HttpServletRequest servletRequest) {
        try {
            if (rawBody == null || rawBody.trim().isEmpty()) return new SearchRequest();
            String ct = servletRequest.getContentType();
            if ((ct != null && ct.toLowerCase().contains("json")) || looksLikeJson(rawBody)) {
                return objectMapper.readValue(rawBody, SearchRequest.class);
            }
        } catch (IOException ex) {
            logger.warn("Failed to parse SearchRequest body, using empty: {}", ex.getMessage());
        }
        return new SearchRequest();
    }

    private boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.startsWith("{") || t.startsWith("[");
    }

    private void injectPoNumberFilter(String poNumber, SearchRequest request) {
        if (isBlank(poNumber) || request == null) return;
        if (request.getFilterBy() != null) {
            for (FilterRequest f : request.getFilterBy()) {
                if (f != null && "poNumber".equals(f.getColumn())) return;
            }
        }
        FilterRequest filter = new FilterRequest();
        filter.setColumn("poNumber");
        filter.setOperator(FilterOperator.EQUALS);
        filter.setValue(poNumber);
        List<FilterRequest> filters = request.getFilterBy() == null
                ? new ArrayList<>() : new ArrayList<>(request.getFilterBy());
        filters.add(filter);
        request.setFilterBy(filters);
    }

    private String effectiveValue(String primary, String fallback) {
        if (!isBlank(primary)) return primary.trim();
        if (!isBlank(fallback)) return fallback.trim();
        return null;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", "Error"); m.put("message", message);
        return m;
    }
}