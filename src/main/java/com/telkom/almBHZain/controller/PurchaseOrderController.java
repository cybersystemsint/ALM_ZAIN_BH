package com.telkom.almBHZain.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.telkom.almBHZain.dto.BulkDeleteRequest;
import com.telkom.almBHZain.dto.PurchaseOrderDto;
import com.telkom.almBHZain.dto.Request.SearchRequest;
import com.telkom.almBHZain.dto.Response.PageResult;
import com.telkom.almBHZain.model.PurchaseOrder;
import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.repository.POItemRepository;
import com.telkom.almBHZain.repository.PurchaseOrderRepository;
import com.telkom.almBHZain.repository.WorkflowRepository;
import com.telkom.almBHZain.repository.tb_Po_ModificationRepository;
import com.telkom.almBHZain.service.PurchaseOrderService;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class PurchaseOrderController {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);

    @Autowired
    private PurchaseOrderService service;
    @Autowired
    private POItemRepository poRepo;
    @Autowired
    private PurchaseOrderRepository poNumberRepo;
    @Autowired
    private WorkflowRepository workflowRepository;
    @Autowired
    private tb_Po_ModificationRepository poModificationRepo;

    @PostMapping(value = "/purchase-orders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResult<PurchaseOrderDto> getPurchaseOrders(@RequestBody SearchRequest request) {
        return service.getPurchaseOrders(request);
    }

    // addition request of poNumber if it doesnt already exist
    @PostMapping(value = "/poNumbers")
    public ResponseEntity<Map<String, String>> createPONumber(@RequestBody String req) {
        logger.info("PO NUMBER CREATE REQUEST |  {}", req);
        List<String> createdPoNumbers = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(req);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (!jsonObject.has("poNumber")) {
                    validationErrors.add("poNumber is required in item index " + i);
                    continue;
                }
                String poNumber = jsonObject.getString("poNumber").trim();
                if (poNumber.isEmpty()) {
                    validationErrors.add("poNumber cannot be empty at index " + i);
                    continue;
                }

                String createdBy = jsonObject.optString("createdBy", "System").trim();
                if (createdBy.isEmpty()) createdBy = "System";

                PurchaseOrder existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
                if (existsPoNumber != null) {
                    validationErrors.add("poNumber already exists: " + poNumber);
                    continue;
                }

                PurchaseOrder newPoNumber = new PurchaseOrder();
                newPoNumber.setPoNumber(poNumber);
                newPoNumber.setApprovalStatus("Pending Addition");
                newPoNumber.setCreatedBy(createdBy);

                try {
                    poNumberRepo.save(newPoNumber);
                    createdPoNumbers.add(poNumber);

                    Workflow workflow = new Workflow();
                    workflow.setPoNumber(poNumber);
                    workflow.setOriginalStatus("Pending Addition");
                    workflow.setProcessId(generateProcessId());
                    workflow.setInsertedBy(createdBy);
                    workflow.setInsertDate(new Date());
                    workflowRepository.save(workflow);
                } catch (Exception ex) {
                    logger.info("Exception while saving | {}", ex.toString());
                    validationErrors.add("Failed to save: " + poNumber);
                }
            }

            if (!validationErrors.isEmpty()) {
                return response("Error", "Some errors occurred: " + String.join(", ", validationErrors));
            } else {
                return response("Success", "Created PO number(s): " + String.join(", ", createdPoNumbers));
            }
        } catch (JSONException exc) {
            logger.info("JSONException | {}", exc.toString());
            return response("Error", exc.getMessage());
        }
    }

    // update endpoint for PO Number
    @PutMapping(value = "/poNumbers/{poNumber}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> updatePONumber(@PathVariable String poNumber, @RequestBody Map<String, Object> payload) {
        logger.info("PO NUMBER UPDATE REQUEST | poNumber: {} | payload: {}", poNumber, payload);
        try {
            if (poNumber == null || poNumber.trim().isEmpty()) {
                return response("Error", "PO number path variable is required");
            }

            PurchaseOrder existing = poNumberRepo.findByPoNumber(poNumber.trim());
            if (existing == null) {
                return response("Error", "PO number '" + poNumber + "' does not exist");
            }

            String updatedBy = payload.containsKey("updatedBy") && payload.get("updatedBy") != null
                    ? payload.get("updatedBy").toString().trim()
                    : null;
            if (updatedBy == null || updatedBy.isEmpty()) {
                return response("Error", "updatedBy is required in payload");
            }

            if (payload.containsKey("approvalStatus") && payload.get("approvalStatus") != null) {
                existing.setApprovalStatus(payload.get("approvalStatus").toString());
            }
            existing.setUpdatedBy(updatedBy);
            poNumberRepo.save(existing);

            Workflow workflow = new Workflow();
            workflow.setPoNumber(poNumber);
            workflow.setOriginalStatus(existing.getApprovalStatus());
            workflow.setProcessId(generateProcessId());
            workflow.setInsertedBy(updatedBy);
            workflow.setInsertDate(new Date());
            workflowRepository.save(workflow);

            return response("Success", "PO number '" + poNumber + "' updated");
        } catch (Exception ex) {
            logger.error("Exception while updating PO number", ex);
            return response("Error", "An error occurred while updating: " + ex.getMessage());
        }
    }

@PostMapping(value = "/poNumbers/{poNumber}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public ResponseEntity<Map<String, String>> requestDeletePoNum(
        @PathVariable String poNumber,
        @RequestBody(required = false) Map<String, Object> payload) {

    logger.info("PO NUMBER DELETE REQUEST | poNumber: {} | payload: {}", poNumber, payload);
    try {
        if (poNumber == null || poNumber.trim().isEmpty()) {
            return response("Error", "PO number is required");
        }
        String poNum = poNumber.trim();

        PurchaseOrder poNumberEntry = poNumberRepo.findByPoNumber(poNum);
        if (poNumberEntry == null) {
            return response("Error", "PO number '" + poNum + "' does not exist");
        }

        // 1) Ensure no POItems are unapproved (fast count)
        long notApprovedCount = poRepo.countNotApprovedItems(poNum);
        if (notApprovedCount > 0) {
            return response("Error", "PO number '" + poNum + "' contains " + notApprovedCount +
                    " item(s) that are not 'Approved'. All items must be 'Approved' before deletion");
        }

        // read updatedBy from payload (payload should contain only updatedBy)
        String payloadUpdatedBy = null;
        if (payload != null && payload.get("updatedBy") != null) {
            payloadUpdatedBy = payload.get("updatedBy").toString().trim();
            if (payloadUpdatedBy.isEmpty()) payloadUpdatedBy = null;
        }

        // 2) Attempt an atomic update: set PO -> Pending Deletion only if not already pending
        int updatedRows = poNumberRepo.markPendingDeletionIfNotPending(poNum, "Pending Deletion", payloadUpdatedBy);
        if (updatedRows == 0) {
            // no row updated -> PO must already be in a pending state (or changed concurrently)
            return response("Error", "PO number '" + poNum + "' is already in a pending state and cannot be deleted");
        }

        // At this point the PO is marked Pending Deletion (and updatedBy saved if provided).
        // Use the preferred insertedBy for the workflow (payload.updatedBy -> po.updatedBy -> createdBy -> System).
        String insertedBy = (payloadUpdatedBy != null)
                ? payloadUpdatedBy
                : (poNumberEntry.getUpdatedBy() != null && !poNumberEntry.getUpdatedBy().trim().isEmpty()
                    ? poNumberEntry.getUpdatedBy()
                    : (poNumberEntry.getCreatedBy() != null && !poNumberEntry.getCreatedBy().trim().isEmpty()
                        ? poNumberEntry.getCreatedBy()
                        : "System"));

        // 3) Create workflow entry
        Workflow workflow = new Workflow();
        workflow.setPoNumber(poNum);
        workflow.setOriginalStatus("Pending Deletion");
        workflow.setProcessId(generateProcessId());
        workflow.setInsertedBy(insertedBy);
        workflow.setInsertDate(new Date());
        workflowRepository.save(workflow);

        return response("Success", "PO number '" + poNum + "' is marked for deletion");
    } catch (Exception ex) {
        logger.error("Exception while processing PO number delete request", ex);
        return response("Error", "An error occurred while processing deletion: " + ex.getMessage());
    }
}
    private String generateProcessId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomDigit = String.valueOf((int) (Math.random() * 10));
        return timestamp.substring(Math.max(0, timestamp.length() - 6)) + randomDigit;
    }

   //bulk delete
    @PostMapping(value = "/poNumbers/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> bulkDeletePoNumbers(@RequestBody BulkDeleteRequest payload) {
        logger.info("BULK PO DELETE REQUEST | payload: {}", payload);
        try {
            Map<String, Object> result = service.processBulkDelete(payload);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            logger.error("Exception processing bulk delete", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "Error");
            error.put("message", ex.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping(value = "/poNumbers/approve")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    @Transactional
    public Map<String, Object> approvePoNumber(@RequestBody Map<String, Object> requestBody) {
        logger.info("Approve PO Number request | Request Body: {}", requestBody);
        try {
            return processWorkflowAction(requestBody, "approve");
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request in approvePoNumber: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "Error");
            error.put("message", e.getMessage());
            error.put("errorType", "IllegalArgumentException");
            return error;
        } catch (Exception e) {
            logger.error("Unexpected error in approvePoNumber", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "Error");
            error.put("message", "Internal server error: " + e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return error;
        }
    }

    @PostMapping(value = "/poNumbers/reject")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    @Transactional
    public Map<String, Object> rejectPoNumber(@RequestBody Map<String, Object> requestBody) {
        logger.info("Reject PO Number Request | Request Body: {}", requestBody);
        try {
            return processWorkflowAction(requestBody, "reject");
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request in rejectPoNumber: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "Error");
            error.put("message", e.getMessage());
            error.put("errorType", "IllegalArgumentException");
            return error;
        } catch (Exception e) {
            logger.error("Unexpected error in rejectPoNumber", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "Error");
            error.put("message", "Internal server error: " + e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return error;
        }
    }

  @SuppressWarnings("unchecked")
private Map<String, Object> processWorkflowAction(Map<String, Object> requestBody, String action) {
    Map<String, Object> response = new HashMap<>();
    List<Map<String, String>> results = new ArrayList<>();

    String changedBy = requestBody.containsKey("changedBy") && requestBody.get("changedBy") != null
            ? requestBody.get("changedBy").toString().trim()
            : null;
    if (changedBy == null || changedBy.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "changedBy is required in request payload");
        return response;
    }

    // Fix cast: Object, not String — Spring deserializes map values as Object
    List<Map<String, Object>> selectedRows =
            (List<Map<String, Object>>) requestBody.get("selectedRows");

    if (selectedRows == null || selectedRows.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "No rows selected.");
        return response;
    }

    // requestType is now read per row — no top-level requestType needed
    boolean hasErrors = false;
    for (Map<String, Object> row : selectedRows) {
        Map<String, String> result = new HashMap<>();
        try {
            Object poNumberObj = row.get("poNumber");
            if (poNumberObj == null || poNumberObj.toString().trim().isEmpty()) {
                result.put("status", "Error");
                result.put("message", "poNumber is missing in row.");
                results.add(result);
                hasErrors = true;
                continue;
            }
            String poNumber = poNumberObj.toString().trim();
            result.put("poNumber", poNumber);

            // Read requestType from each individual row
            Object rtObj = row.get("requestType");
            if (rtObj == null || rtObj.toString().trim().isEmpty()) {
                result.put("status", "Error");
                result.put("message", "requestType is missing in row for poNumber: " + poNumber);
                results.add(result);
                hasErrors = true;
                continue;
            }
            String rowRequestType = rtObj.toString().trim();
            if (!"addition".equalsIgnoreCase(rowRequestType)
                    && !"deletion".equalsIgnoreCase(rowRequestType)) {
                result.put("status", "Error");
                result.put("message", "Invalid requestType '" + rowRequestType
                        + "' for poNumber: " + poNumber + ". Must be 'addition' or 'deletion'.");
                results.add(result);
                hasErrors = true;
                continue;
            }

            Map<String, String> actionResult =
                    processSingleWorkflowAction(poNumber, action, rowRequestType, changedBy);
            result.putAll(actionResult);

        } catch (Exception ex) {
            hasErrors = true;
            result.put("status", "Error");
            result.put("message", "Unexpected error processing row: " + ex.getMessage());
            logger.error("Exception processing row in processWorkflowAction | action={} | row={}", action, row, ex);
        }
        results.add(result);
    }

    response.put("results", results);
    response.put("status", hasErrors ? "PartialFailure" : "Success");
    return response;
}


    private Map<String, String> processSingleWorkflowAction(String poNumber, String action, String requestType, String changedBy) {
        Map<String, String> result = new HashMap<>();
        result.put("poNumber", poNumber);
        try {
            switch (requestType.toLowerCase()) {
                case "addition":
                    return processAddition(poNumber, action, changedBy);
                case "deletion":
                    return processDeletion(poNumber, action, changedBy);
                default:
                    result.put("status", "Error");
                    result.put("message", "Invalid request type: " + requestType);
                    return result;
            }
        } catch (Exception ex) {
            result.put("status", "Error");
            result.put("message", "Failed to process workflow action: " + ex.getMessage());
            logger.error("Exception for PO Number: {} | Error: {}", poNumber, ex);
            return result;
        }
    }

    private Map<String, String> processAddition(String poNumber, String action, String changedBy) {
        Map<String, String> result = new HashMap<>();
        result.put("poNumber", poNumber);
        try {
            PurchaseOrder existingPoNumber = poNumberRepo.findByPoNumber(poNumber);
            if (existingPoNumber == null) {
                result.put("status", "Error");
                result.put("message", "PO Number does not exist: " + poNumber);
                return result;
            }

            if (!"Pending Addition".equals(existingPoNumber.getApprovalStatus())) {
                result.put("status", "Error");
                result.put("message", "PO Number is not in 'Pending Addition' status: " + poNumber);
                return result;
            }

            if ("approve".equalsIgnoreCase(action)) {
                existingPoNumber.setApprovalStatus("Approved");
                poNumberRepo.save(existingPoNumber);
                updateWorkflow(poNumber, "Pending Addition", "Addition Approved", "PO number addition approved.", changedBy);
                result.put("message", "PO number approved.");
            } else if ("reject".equalsIgnoreCase(action)) {
                poNumberRepo.delete(existingPoNumber);
                updateWorkflow(poNumber, "Pending Addition", "Addition Rejected", "PO number addition rejected and deleted.", changedBy);
                result.put("message", "PO number rejected and deleted from the database.");
            }

            result.put("status", "Success");
        } catch (Exception ex) {
            result.put("status", "Error");
            result.put("message", "Failed to process addition: " + ex.getMessage());
            logger.error("Error processing addition for PO Number: " + poNumber, ex);
        }
        return result;
    }

    private Map<String, String> processDeletion(String poNumber, String action, String changedBy) {
        Map<String, String> result = new HashMap<>();
        result.put("poNumber", poNumber);

        try {
            PurchaseOrder existingPoNumber = poNumberRepo.findByPoNumber(poNumber);
            if (existingPoNumber == null) {
                result.put("status", "Error");
                result.put("message", "PO Number does not exist: " + poNumber);
                return result;
            }

            if (!"Pending Deletion".equals(existingPoNumber.getApprovalStatus())) {
                result.put("status", "Error");
                result.put("message", "PO Number is not in 'Pending Deletion' status: " + poNumber);
                return result;
            }

            if ("approve".equalsIgnoreCase(action)) {
                poNumberRepo.delete(existingPoNumber);
                updateWorkflow(poNumber, "Pending Deletion", "Deletion Approved", "PO number deletion approved and removed.", changedBy);
                result.put("message", "PO number deletion approved and removed from database.");
            } else if ("reject".equalsIgnoreCase(action)) {
                existingPoNumber.setApprovalStatus("Approved");
                poNumberRepo.save(existingPoNumber);
                updateWorkflow(poNumber, "Pending Deletion", "Deletion Rejected", "PO number deletion rejected.", changedBy);
                result.put("message", "PO number deletion rejected.");
            } else {
                result.put("status", "Error");
                result.put("message", "Invalid action: " + action);
                return result;
            }

            result.put("status", "Success");
        } catch (Exception ex) {
            result.put("status", "Error");
            result.put("message", "Failed to process deletion: " + ex.getMessage());
            logger.error("Exception while processing deletion for PO Number: {} | Error: {}", poNumber, ex);
        }

        return result;
    }

    private void updateWorkflow(String poNumber, String originalStatus, String updatedStatus, String comments, String changedBy) {
        List<Workflow> workflows = workflowRepository.findByPoNumberAndOriginalStatus(poNumber, originalStatus);
        if (!workflows.isEmpty()) {
            for (Workflow workflow : workflows) {
                workflow.setUpdatedStatus(updatedStatus);
                workflow.setChangedBy(changedBy != null ? changedBy : "System");
                workflow.setChangeDate(new Date());
                workflow.setComments(comments);
                workflowRepository.save(workflow);
            }
            logger.info("Updated workflow for PO Number: {} with original status: {}", poNumber, originalStatus);
        } else {
            logger.warn("No existing workflow found for PO Number: {} with original status: {}", poNumber, originalStatus);
            Workflow wf = new Workflow();
            wf.setPoNumber(poNumber);
            wf.setOriginalStatus(originalStatus);
            wf.setUpdatedStatus(updatedStatus);
            wf.setProcessId(generateProcessId());
            wf.setInsertedBy("System");
            wf.setInsertDate(new Date());
            wf.setChangedBy(changedBy != null ? changedBy : "System");
            wf.setChangeDate(new Date());
            wf.setComments(comments);
            workflowRepository.save(wf);
        }
    }

    private ResponseEntity<Map<String, String>> response(String status, String message) {
        Map<String, String> res = new HashMap<>();
        res.put("status", status);
        res.put("message", message);
        return ResponseEntity.ok(res);
    }
}