package com.telkom.almBHZain.service;

import com.telkom.almBHZain.dto.PurchaseOrderDto;
import com.telkom.almBHZain.dto.request.BulkDeleteRequest;
import com.telkom.almBHZain.dto.request.SearchRequest;
import com.telkom.almBHZain.dto.response.PageResult;
import com.telkom.almBHZain.dto.response.WorkflowActionResponse;

import java.util.Map;

/**
 * Contract for all Purchase Order business operations.
 */
public interface PurchaseOrderService {

    // ──────────────────────────────────────────────
    // Queries
    // ──────────────────────────────────────────────

    /**
     * Returns a paginated, filtered list of purchase orders.
     */
    PageResult<PurchaseOrderDto> getPurchaseOrders(SearchRequest request);

    // ──────────────────────────────────────────────
    // Mutations – single record
    // ──────────────────────────────────────────────

    /**
     * Creates one or more PO numbers from a JSON array payload.
     * Returns a summary map with {@code status} and {@code message}.
     */
    Map<String, String> createPONumbers(String rawJsonArray);

    /**
     * Updates metadata (e.g. approvalStatus, updatedBy) for an existing PO number.
     */
    Map<String, String> updatePONumber(String poNumber, Map<String, Object> payload);

    /**
     * Marks a single PO number as Pending Deletion (after validating all items are Approved).
     */
    Map<String, String> requestSingleDelete(String poNumber, Map<String, Object> payload);

    // ──────────────────────────────────────────────
    // Mutations – bulk
    // ──────────────────────────────────────────────

    /**
     * Bulk-marks multiple purchase orders as Pending Deletion.
     */
    Map<String, Object> processBulkDelete(BulkDeleteRequest request);

    // ──────────────────────────────────────────────
    // Approval / Rejection
    // ──────────────────────────────────────────────

    /**
     * Processes approve or reject for a list of PO number workflow rows.
     *
     * @param requestBody  must contain {@code changedBy} and {@code selectedRows}
     *                     (each row must have {@code poNumber} and {@code requestType})
     * @param action       {@code "approve"} or {@code "reject"}
     */
    WorkflowActionResponse processWorkflowAction(Map<String, Object> requestBody, String action);
}