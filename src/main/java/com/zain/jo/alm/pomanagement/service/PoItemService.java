package com.zain.jo.alm.pomanagement.service;

import com.zain.jo.alm.pomanagement.dto.PoItemDto;
import com.zain.jo.alm.pomanagement.dto.request.BulkDeleteRequest;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.dto.response.BulkOperationResult;
import com.zain.jo.alm.pomanagement.dto.response.PageResult;
import com.zain.jo.alm.pomanagement.dto.response.WorkflowActionResponse;

import java.util.List;
import java.util.Map;

/**
 * Contract for all PO Item business operations.
 * Controllers depend solely on this interface; implementation details are hidden.
 */
public interface PoItemService {

    // ──────────────────────────────────────────────
    // Queries
    // ──────────────────────────────────────────────

    /**
     * Returns a paginated, filtered list of PO items.
     */
    PageResult<PoItemDto> getPOItems(SearchRequest request);

    // ──────────────────────────────────────────────
    // Mutations – single record
    // ──────────────────────────────────────────────

    /**
     * Creates a new PO item and opens a Pending Addition workflow.
     */
    Map<String, String> addSinglePOItem(String rawJson);

    /**
     * Requests an update to an existing PO item (creates a modification record +
     * Pending Modification workflow).
     */
    Map<String, String> updatePOItem(long recordNo, String rawJson);

    /**
     * Marks a single PO item as Pending Deletion and opens the corresponding workflow.
     */
    Map<String, String> requestSingleDelete(long recordNo, String requestedBy);

    // ──────────────────────────────────────────────
    // Mutations – bulk
    // ──────────────────────────────────────────────

    /**
     * Bulk add-or-modify PO items from a JSON array payload.
     */
    BulkOperationResult addOrUpdateBulk(String rawJsonArray);

    /**
     * Marks multiple PO items as Pending Deletion in a single transaction.
     */
    Map<String, Object> requestBulkDelete(BulkDeleteRequest request);

    // ──────────────────────────────────────────────
    // Approval / Rejection
    // ──────────────────────────────────────────────

    /**
     * Processes approve or reject for a list of PO item workflow rows.
     *
     * @param requestBody  must contain {@code changedBy} (String) and
     *                     {@code selectedRows} (List of maps with poNumber, recordNo, requestType)
     * @param action       {@code "approve"} or {@code "reject"}
     */
    WorkflowActionResponse processWorkflowAction(Map<String, Object> requestBody, String action);
}