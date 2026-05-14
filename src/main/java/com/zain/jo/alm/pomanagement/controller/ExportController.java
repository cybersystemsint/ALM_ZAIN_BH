package com.zain.jo.alm.pomanagement.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zain.jo.alm.pomanagement.dto.request.FilterOperator;
import com.zain.jo.alm.pomanagement.dto.request.FilterRequest;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.service.ExportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@RestController
public class ExportController {

    private final ExportService exportService;
    private final ObjectMapper  objectMapper;

    @Autowired
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
        this.objectMapper  = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ──────────────────────────────────────────────
    // Export endpoints
    // ──────────────────────────────────────────────

    /**
     * POST /purchase-orders/export
     */
    @PostMapping(value = "/purchase-orders/export",
            produces = "application/octet-stream",
            consumes = MediaType.ALL_VALUE)
    public StreamingResponseBody exportPurchaseOrders(
            @RequestBody(required = false) String rawBody,
            @RequestParam(required = false) String format,
            HttpServletRequest servletRequest,
            HttpServletResponse response) throws IOException {

        SearchRequest request     = parseBody(rawBody, servletRequest);
        String        chosenFormat = resolveFormat(format, request, "excel");

        setResponseHeaders(response, "purchase_orders", chosenFormat);
        return out -> exportService.exportPurchaseOrders(request, out, chosenFormat);
    }

    /**
     * POST /po-items/export
     */
    @PostMapping(value = "/po-items/export",
            produces = "application/octet-stream",
            consumes = MediaType.ALL_VALUE)
    public StreamingResponseBody exportPOItems(
            @RequestParam(value = "format",   required = false) String format,
            @RequestParam(value = "poNumber", required = false) String poNumberParam,
            @RequestBody(required = false) String rawBody,
            HttpServletRequest servletRequest,
            HttpServletResponse response) throws IOException {

        SearchRequest request = parseBody(rawBody, servletRequest);

        String effectivePoNumber = effectiveValue(request.getPoNumber(), poNumberParam);
        injectPoNumberFilter(effectivePoNumber, request);

        String chosenFormat = resolveFormat(format, request, "excel");
        String baseName = "po_items" + (effectivePoNumber != null ? "_" + effectivePoNumber : "");

        setResponseHeaders(response, baseName, chosenFormat);
        return out -> exportService.exportPOItems(request, out, chosenFormat);
    }

    /**
     * POST /pendingWorkflows/export
     */
    @PostMapping(value = "/pendingWorkflows/export",
            produces = "application/octet-stream",
            consumes = MediaType.ALL_VALUE)
    public StreamingResponseBody exportPendingWorkflows(
            @RequestBody(required = false) String rawBody,
            @RequestParam(required = false) String format,
            HttpServletRequest servletRequest,
            HttpServletResponse response) throws IOException {

        SearchRequest request = parseBody(rawBody, servletRequest);
        injectUpdatedStatusFilter(request, true);

        String chosenFormat = resolveFormat(format, request, "excel");
        setResponseHeaders(response, "PO_Pending_Workflow", chosenFormat);
        return out -> exportService.exportWorkflows(request, out, chosenFormat);
    }

    /**
     * POST /approvedApprovals/export
     */
    @PostMapping(value = "/approvedApprovals/export",
            produces = "application/octet-stream",
            consumes = MediaType.ALL_VALUE)
    public StreamingResponseBody exportUpdatedWorkflows(
            @RequestBody(required = false) String rawBody,
            @RequestParam(required = false) String format,
            HttpServletRequest servletRequest,
            HttpServletResponse response) throws IOException {

        SearchRequest request = parseBody(rawBody, servletRequest);
        injectUpdatedStatusFilter(request, false);

        String chosenFormat = resolveFormat(format, request, "excel");
        setResponseHeaders(response, "PO_Workflow_History", chosenFormat);
        return out -> exportService.exportWorkflows(request, out, chosenFormat);
    }

    // ──────────────────────────────────────────────
    // Private utilities (controller concerns only)
    // ──────────────────────────────────────────────

    private void setResponseHeaders(HttpServletResponse response, String baseName, String format) {
        boolean isCsv = "csv".equalsIgnoreCase(format);
        String ext  = isCsv ? "csv" : "xlsx";
        String mime = isCsv ? "text/csv"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        response.setContentType(mime);
        response.setHeader("Content-Disposition", "attachment; filename=" + baseName + "_" + ts + "." + ext);
        response.setHeader("Cache-Control", "no-cache");
    }

    private SearchRequest parseBody(String rawBody, HttpServletRequest servletRequest) {
        if (rawBody == null || rawBody.trim().isEmpty()) return new SearchRequest();
        try {
            String ct = servletRequest.getContentType();
            if ((ct != null && ct.toLowerCase().contains("json")) || looksLikeJson(rawBody)) {
                return objectMapper.readValue(rawBody, SearchRequest.class);
            }
        } catch (IOException ignored) { /* fall through */ }
        return new SearchRequest();
    }

    private boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.startsWith("{") || t.startsWith("[");
    }

    private String resolveFormat(String queryParam, SearchRequest request, String defaultFormat) {
        if (!isBlank(queryParam))                    return queryParam.trim().toLowerCase();
        if (request != null)                         return request.getFormatOrDefault(defaultFormat);
        return defaultFormat;
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

    /**
     * Injects an UPDATED_STATUS IS_EMPTY (pending) or IS_NOT_EMPTY (processed) filter,
     * but only if one is not already present.
     */
    private void injectUpdatedStatusFilter(SearchRequest request, boolean pending) {
        if (request == null) return;
        final String normalizedTarget = normalizeName("UPDATED_STATUS");

        if (request.getFilterBy() != null) {
            for (FilterRequest f : request.getFilterBy()) {
                if (f == null || f.getColumn() == null) continue;
                if (normalizeName(f.getColumn()).equals(normalizedTarget)) return;
            }
        }
        FilterRequest fr = new FilterRequest();
        fr.setColumn("UPDATED_STATUS");
        fr.setOperator(pending ? FilterOperator.IS_EMPTY : FilterOperator.IS_NOT_EMPTY);

        List<FilterRequest> filters = request.getFilterBy() == null
                ? new ArrayList<>() : new ArrayList<>(request.getFilterBy());
        filters.add(fr);
        request.setFilterBy(filters);
    }

    private String effectiveValue(String primary, String fallback) {
        if (!isBlank(primary)) return primary.trim();
        if (!isBlank(fallback)) return fallback.trim();
        return null;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private String normalizeName(String s) {
        return s == null ? "" : s.replaceAll("[_\\s]", "").toLowerCase(Locale.ROOT);
    }
}