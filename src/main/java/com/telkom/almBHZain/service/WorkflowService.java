package com.telkom.almBHZain.service;

import com.telkom.almBHZain.dto.WorkflowDto;
import com.telkom.almBHZain.dto.request.SearchRequest;
import com.telkom.almBHZain.dto.response.PageResult;

/**
 * Contract for workflow read operations.
 * Write operations (creating / updating workflow entries) are handled
 * internally by POItemService and PurchaseOrderService implementations.
 */
public interface WorkflowService {

    /**
     * Returns a paginated list of workflows filtered by their {@code updatedStatus}.
     *
     * @param pendingOnly  {@code true}  → return records where updatedStatus IS NULL  (pending)
     *                     {@code false} → return records where updatedStatus IS NOT NULL (processed)
     * @param request      search / filter / pagination parameters
     */
    PageResult<WorkflowDto> getWorkflows(boolean pendingOnly, SearchRequest request);
}