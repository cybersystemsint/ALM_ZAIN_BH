package com.telkom.almBHZain.controller;

import com.telkom.almBHZain.dto.PurchaseOrderDto;
import com.telkom.almBHZain.dto.request.BulkDeleteRequest;
import com.telkom.almBHZain.dto.request.SearchRequest;
import com.telkom.almBHZain.dto.response.PageResult;
import com.telkom.almBHZain.dto.response.WorkflowActionResponse;
import com.telkom.almBHZain.service.PurchaseOrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin REST controller for Purchase Order endpoints.
 *
 * Responsibilities:
 *   - Accept and validate HTTP input
 *   - Delegate ALL business logic to {@link PurchaseOrderService}
 *   - Return appropriate {@link ResponseEntity} responses
 *
 * No business logic, repository access, or transaction management belongs here.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class PurchaseOrderController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final PurchaseOrderService purchaseOrderService;

    @Autowired
    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    // ──────────────────────────────────────────────
    // Queries
    // ──────────────────────────────────────────────

    /**
     * POST /purchase-orders
     * Paginated, filterable list of purchase orders.
     */
    @PostMapping(value = "/purchase-orders",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResult<PurchaseOrderDto>> getPurchaseOrders(
            @RequestBody SearchRequest request) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrders(request));
    }

    // ──────────────────────────────────────────────
    // Single mutations
    // ──────────────────────────────────────────────

    /**
     * POST /poNumbers
     * Create one or more PO numbers from a JSON array.
     */
    @PostMapping(value = "/poNumbers")
    public ResponseEntity<Map<String, String>> createPONumber(@RequestBody String rawJson) {
        logger.info("POST /poNumbers");
        Map<String, String> result = purchaseOrderService.createPONumbers(rawJson);
        HttpStatus status = "Error".equals(result.get("status"))
                ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * PUT /poNumbers/{poNumber}
     * Update metadata for an existing PO number.
     */
    @PutMapping(value = "/poNumbers/{poNumber}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> updatePONumber(
            @PathVariable String poNumber,
            @RequestBody Map<String, Object> payload) {
        logger.info("PUT /poNumbers/{}", poNumber);
        Map<String, String> result = purchaseOrderService.updatePONumber(poNumber, payload);
        HttpStatus status = "Error".equals(result.get("status"))
                ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * POST /poNumbers/{poNumber}
     * Request deletion of a single PO number.
     */
    @PostMapping(value = "/poNumbers/{poNumber}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> requestDeletePoNumber(
            @PathVariable String poNumber,
            @RequestBody(required = false) Map<String, Object> payload) {
        logger.info("POST /poNumbers/{} (delete request)", poNumber);
        Map<String, String> result = purchaseOrderService.requestSingleDelete(poNumber, payload);
        HttpStatus status = "Error".equals(result.get("status"))
                ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    // ──────────────────────────────────────────────
    // Bulk mutations
    // ──────────────────────────────────────────────

    /**
     * POST /poNumbers/delete
     * Bulk-request deletion for multiple PO numbers.
     */
    @PostMapping(value = "/poNumbers/delete",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> bulkDeletePoNumbers(@RequestBody BulkDeleteRequest payload) {
        logger.info("POST /poNumbers/delete");
        try {
            Map<String, Object> result = purchaseOrderService.processBulkDelete(payload);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            logger.error("Exception processing bulk PO delete", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody(ex.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // Approval / Rejection
    // ──────────────────────────────────────────────

    /**
     * POST /poNumbers/approve
     * Approve selected PO number workflow requests.
     */
    @PostMapping(value = "/poNumbers/approve")
    public ResponseEntity<WorkflowActionResponse> approvePoNumber(
            @RequestBody Map<String, Object> requestBody) {
        logger.info("POST /poNumbers/approve");
        WorkflowActionResponse response = purchaseOrderService.processWorkflowAction(requestBody, "approve");
        int httpStatus = "PartialFailure".equals(response.getStatus()) ? 207 : 200;
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * POST /poNumbers/reject
     * Reject selected PO number workflow requests.
     */
    @PostMapping(value = "/poNumbers/reject")
    public ResponseEntity<WorkflowActionResponse> rejectPoNumber(
            @RequestBody Map<String, Object> requestBody) {
        logger.info("POST /poNumbers/reject");
        WorkflowActionResponse response = purchaseOrderService.processWorkflowAction(requestBody, "reject");
        int httpStatus = "PartialFailure".equals(response.getStatus()) ? 207 : 200;
        return ResponseEntity.status(httpStatus).body(response);
    }

    // ──────────────────────────────────────────────
    // Utilities
    // ──────────────────────────────────────────────

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("status",  "Error");
        m.put("message", message);
        return m;
    }
}