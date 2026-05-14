package com.zain.jo.alm.pomanagement.controller;

import com.zain.jo.alm.pomanagement.dto.WorkflowDto;
import com.zain.jo.alm.pomanagement.dto.request.SearchRequest;
import com.zain.jo.alm.pomanagement.dto.response.PageResult;
import com.zain.jo.alm.pomanagement.service.WorkflowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Thin REST controller for Workflow read endpoints.
 *
 * Responsibilities:
 *   - Accept HTTP requests
 *   - Delegate to {@link WorkflowService}
 *   - Return {@link ResponseEntity} responses
 *
 * No business logic belongs here.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Validated
public class WorkflowController {

    private final WorkflowService workflowService;

    @Autowired
    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * POST /pendingWorkflows
     * Returns workflows where updatedStatus IS NULL (pending approval).
     */
    @PostMapping("/pendingWorkflows")
    public ResponseEntity<PageResult<WorkflowDto>> getPendingWorkflows(
            @RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(workflowService.getWorkflows(true, searchRequest));
    }

    /**
     * POST /approvedApprovals
     * Returns workflows where updatedStatus IS NOT NULL (already processed).
     */
    @PostMapping("/approvedApprovals")
    public ResponseEntity<PageResult<WorkflowDto>> getProcessedWorkflows(
            @RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(workflowService.getWorkflows(false, searchRequest));
    }
}