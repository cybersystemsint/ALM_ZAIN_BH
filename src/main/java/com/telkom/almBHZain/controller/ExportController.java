package com.telkom.almBHZain.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telkom.almBHZain.dto.Request.FilterOperator;
import com.telkom.almBHZain.dto.Request.FilterRequest;
import com.telkom.almBHZain.dto.Request.SearchRequest;
import com.telkom.almBHZain.service.ExportService;
import com.telkom.almBHZain.service.POItemService;

@RestController
public class ExportController {

    private final POItemService service;
    private final ExportService exportService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ExportController(POItemService service, ExportService exportService) {
        this.service = service;
        this.exportService = exportService;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

private void setResponseHeaders(HttpServletResponse response, String baseName, String chosenFormat) {
    String ext = "csv".equalsIgnoreCase(chosenFormat) ? "csv" : "xlsx";
    String mime = "csv".equalsIgnoreCase(chosenFormat)
            ? "text/csv"
            : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

    response.setContentType(mime);
    response.setHeader("Content-Disposition",
            "attachment; filename=" + baseName + "_" + timestamp + "." + ext);
    response.setHeader("Cache-Control", "no-cache");
}

    // Purchase orders export
    @PostMapping(value = "/purchase-orders/export", produces = "application/octet-stream", consumes = MediaType.ALL_VALUE)
    public StreamingResponseBody exportPurchaseOrders(
            @RequestBody(required = false) String rawBody,
            @RequestParam(required = false) String format,
            HttpServletRequest servletRequest,
            HttpServletResponse response) throws IOException {

        SearchRequest parsed = parseRequestBodyIfJson(rawBody, servletRequest);
        final SearchRequest request = (parsed != null) ? parsed : new SearchRequest();

        String chosenFormat = (format != null && !format.trim().isEmpty())
                ? format.toLowerCase()
                : request.getFormatOrDefault("excel");

        setResponseHeaders(response, "purchase_orders", chosenFormat);
        return out -> exportService.exportPurchaseOrders(request, out, chosenFormat);
    }

@PostMapping(value = "/po-items/export", produces = "application/octet-stream", consumes = MediaType.ALL_VALUE)
public StreamingResponseBody exportPOItems(
        @RequestParam(value = "format", required = false) String format,
        @RequestParam(value = "poNumber", required = false) String poNumberParam, // optional fallback
        @RequestBody(required = false) String rawBody,
        HttpServletRequest servletRequest,
        HttpServletResponse response) throws IOException {

    SearchRequest parsed = parseRequestBodyIfJson(rawBody, servletRequest);
    final SearchRequest request = (parsed != null) ? parsed : new SearchRequest();

    // prefer poNumber in body; otherwise fallback to query param
    String effectivePoNumber = (request.getPoNumber() != null && !request.getPoNumber().trim().isEmpty())
            ? request.getPoNumber().trim()
            : (poNumberParam != null && !poNumberParam.trim().isEmpty() ? poNumberParam.trim() : null);

    injectPoNumberFilterIfNeeded(effectivePoNumber, request);

    String chosenFormat = (format != null && !format.trim().isEmpty())
            ? format.toLowerCase()
            : request.getFormatOrDefault("excel");

    setResponseHeaders(response, "po_items" + (effectivePoNumber != null ? ("_" + effectivePoNumber) : ""), chosenFormat);

    return out -> exportService.exportPOItems(request, out, chosenFormat);
}

@PostMapping(value = "/pendingWorkflows/export", produces = "application/octet-stream", consumes = MediaType.ALL_VALUE)
public StreamingResponseBody exportPendingWorkflows(@RequestBody(required = false) String rawBody,
                                                    @RequestParam(required = false) String format,
                                                    HttpServletRequest servletRequest,
                                                    HttpServletResponse response) throws IOException {
    SearchRequest parsed = parseRequestBodyIfJson(rawBody, servletRequest);
    final SearchRequest request = (parsed != null) ? parsed : new SearchRequest();

    injectUpdatedStatusFilter(request, true);

    String chosenFormat = (format != null && !format.trim().isEmpty()) ? format.toLowerCase()
            : request.getFormatOrDefault("excel");
    setResponseHeaders(response, "PO_Pending_Workflow", chosenFormat);
    return out -> exportService.exportWorkflows(request, out, chosenFormat);
}

@PostMapping(value = "/approvedApprovals/export", produces = "application/octet-stream", consumes = MediaType.ALL_VALUE)
public StreamingResponseBody exportUpdatedWorkflows(@RequestBody(required = false) String rawBody,
                                                    @RequestParam(required = false) String format,
                                                    HttpServletRequest servletRequest,
                                                    HttpServletResponse response) throws IOException {
    SearchRequest parsed = parseRequestBodyIfJson(rawBody, servletRequest);
    final SearchRequest request = (parsed != null) ? parsed : new SearchRequest();
    injectUpdatedStatusFilter(request, false);

    String chosenFormat = (format != null && !format.trim().isEmpty()) ? format.toLowerCase()
            : request.getFormatOrDefault("excel");
    setResponseHeaders(response, "PO_Workflow_History", chosenFormat);
    return out -> exportService.exportWorkflows(request, out, chosenFormat);
}

// Helper
private void injectUpdatedStatusFilter(SearchRequest request, boolean pending) {
    if (request == null) return;
    final String normalizedTarget = normalizeName("UPDATED_STATUS");

    List<FilterRequest> existing = request.getFilterBy();
    if (existing != null) {
        for (FilterRequest f : existing) {
            if (f == null || f.getColumn() == null) continue;
            if (normalizeName(f.getColumn()).equals(normalizedTarget)) {
                return;
            }
        }
    }

    FilterRequest fr = new FilterRequest();
    fr.setColumn("UPDATED_STATUS");
    fr.setOperator(pending ? FilterOperator.IS_EMPTY : FilterOperator.IS_NOT_EMPTY);

    List<FilterRequest> newFilters = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
    newFilters.add(fr);
    request.setFilterBy(newFilters);
}

// normalizer (same approach used in ExportService)
private String normalizeName(String s) {
    if (s == null) return "";
    return s.replaceAll("[_\\s]", "").toLowerCase(Locale.ROOT);
}

    
    // Helper
    private SearchRequest parseRequestBodyIfJson(String rawBody, HttpServletRequest servletRequest) {
        try {
            String contentType = servletRequest.getContentType();
            if (rawBody == null || rawBody.trim().isEmpty()) {
                return null;
            }
            if ((contentType != null && contentType.toLowerCase().contains("json")) || looksLikeJson(rawBody)) {
                return objectMapper.readValue(rawBody, SearchRequest.class);
            } else {
                return null;
            }
        } catch (IOException ex) {
            return null;
        }
    }

    private boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.startsWith("{") || t.startsWith("[");
    }

    // Reusable injection of poNumber filter (keeps existing filters and avoids duplicates)
    private void injectPoNumberFilterIfNeeded(String poNumber, SearchRequest request) {
        if (poNumber == null || poNumber.trim().isEmpty() || request == null) return;
        if (request.getFilterBy() != null) {
            for (FilterRequest f : request.getFilterBy()) {
                if (f != null && "poNumber".equals(f.getColumn())) return; 
            }
        }
        FilterRequest poFilter = new FilterRequest();
        poFilter.setColumn("poNumber");
        poFilter.setOperator(com.telkom.almBHZain.dto.Request.FilterOperator.EQUALS);
        poFilter.setValue(poNumber);
        List<FilterRequest> filters = request.getFilterBy() == null ? new ArrayList<>() : new ArrayList<>(request.getFilterBy());
        filters.add(poFilter);
        request.setFilterBy(filters);
    }
}