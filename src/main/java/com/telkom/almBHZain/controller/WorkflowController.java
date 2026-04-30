package com.telkom.almBHZain.controller;

import com.telkom.almBHZain.dto.WorkflowDto;
import com.telkom.almBHZain.dto.Request.SearchRequest;
import com.telkom.almBHZain.dto.Response.PageResult;
import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Validated
public class WorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);

     @Autowired
    private  WorkflowService workflowService;


@PostMapping("/pendingWorkflows")
public ResponseEntity<?> getPendingWorkflows(@RequestBody SearchRequest searchRequest) {
    PageResult<WorkflowDto> result = workflowService.getWorkflows(true, searchRequest);
    return ResponseEntity.ok(result);
}

@PostMapping("/approvedApprovals")
public ResponseEntity<?> getProcessedWorkflows(@RequestBody SearchRequest searchRequest) {
    PageResult<WorkflowDto> result = workflowService.getWorkflows(false, searchRequest);
    return ResponseEntity.ok(result);
}
}