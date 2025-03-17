package com.telkom.almBHZain.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.model.tbPoNumber;
import com.telkom.almBHZain.model.tb_Po;
import com.telkom.almBHZain.model.tb_Po_Modification;
import com.telkom.almBHZain.repo.WorkflowRepository;
import com.telkom.almBHZain.repo.tbPoNumberRepo;
import com.telkom.almBHZain.repo.tbPoRepo;
import com.telkom.almBHZain.repo.tb_Po_ModificationRepository;

public class ApprovalWorkflowController {
    private final org.apache.logging.log4j.Logger loggger = LogManager.getLogger(APIController.class);
    private static final Logger logger = LoggerFactory.getLogger(BarhainController.class);
     @Autowired
    tbPoRepo poRepo;
    @Autowired
    tbPoNumberRepo poNumberRepo;
    @Autowired
    private WorkflowRepository workflowRepository;
    @Autowired
     private tb_Po_ModificationRepository poModificationRepo;
   
@PostMapping(value = "/poNumbers/reject")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional
public Map<String, Object> rejectPoNumber(@RequestBody Map<String, Object> requestBody) {
    logger.info("REJECT PO NUMBER REQUEST | Request Body: " + requestBody);
    return processWorkflowAction(requestBody, "reject");
}
private Map<String, Object> processWorkflowAction(Map<String, Object> requestBody, String action) {
    Map<String, Object> response = new HashMap<>();
    List<Map<String, String>> results = new ArrayList<>();

    // Extract selected rows and request type from the request body
    List<Map<String, String>> selectedRows = (List<Map<String, String>>) requestBody.get("selectedRows");
    String requestType = (String) requestBody.get("requestType");

    // Validate the request
    if (selectedRows == null || selectedRows.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "No rows selected.");
        return response;
    }
    if (requestType == null || (!"addition".equalsIgnoreCase(requestType) && !"deletion".equalsIgnoreCase(requestType))) {
        response.put("status", "Error");
        response.put("message", "Invalid request type: " + requestType);
        return response;
    }

    // Process each selected row
    for (Map<String, String> row : selectedRows) {
        String poNumber = row.get("poNumber");
        if (poNumber == null) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "Error");
            result.put("message", "PO Number is missing in the selected row.");
            results.add(result);
            continue;
        }
        Map<String, String> result = processSingleWorkflowAction(poNumber, action, requestType);
        results.add(result);
    }

    response.put("results", results);
    return response;
}
private Map<String, String> processSingleWorkflowAction(String poNumber, String action, String requestType) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    try {
        switch (requestType.toLowerCase()) {
            case "addition":
                result = processAddition(poNumber, action);
                break;
            case "deletion":
                result = processDeletion(poNumber, action);
                break;
            default:
                result.put("status", "Error");
                result.put("message", "Invalid request type: " + requestType);
                break;
        }
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to process workflow action: " + ex.getMessage());
        logger.error("Exception for PO Number: {} | Error: {}", poNumber, ex);
    }
    return result;
}

private Map<String, String> processAddition(String poNumber, String action) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    try {
        // Fetch the PO number record
        tbPoNumber existingPoNumber = poNumberRepo.findByPoNumber(poNumber);
        if (existingPoNumber == null) {
            result.put("status", "Error");
            result.put("message", "PO Number does not exist: " + poNumber);
            return result;
        }
        // Check if the approval status is "Pending Addition"
        if (!"Pending Addition".equals(existingPoNumber.getApprovalStatus())) {
            result.put("status", "Error");
            result.put("message", "PO Number is not in 'Pending Addition' status: " + poNumber);
            return result;
        }
        // Determine whether to approve or reject based on the action
        if ("approve".equalsIgnoreCase(action)) {
            existingPoNumber.setApprovalStatus("Approved");
            result.put("message", "PO number approved.");
            // Update the workflow status
            updateWorkflow(poNumber, "Pending Addition", "Addition Approved", "PO number addition approved.");
        } else if ("reject".equalsIgnoreCase(action)) {
            existingPoNumber.setApprovalStatus("Addition Rejected");
            result.put("message", "PO number rejected.");
            // Update the workflow status
            updateWorkflow(poNumber, "Pending Addition", "Addition Rejected", "PO number addition rejected.");
        }
        // Save the updated PO number status
        poNumberRepo.save(existingPoNumber);
        result.put("status", "Success");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to process addition: " + ex.getMessage());
    }
    return result;
}

private Map<String, String> processDeletion(String poNumber, String action) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    try {
        // Fetch the PO number record
        tbPoNumber existingPoNumber = poNumberRepo.findByPoNumber(poNumber);
        if (existingPoNumber == null) {
            result.put("status", "Error");
            result.put("message", "PO Number does not exist: " + poNumber);
            return result;
        }
        // Check if the approval status is "Pending Deletion"
        if (!"Pending Deletion".equals(existingPoNumber.getApprovalStatus())) {
            result.put("status", "Error");
            result.put("message", "PO Number is not in 'Pending Deletion' status: " + poNumber);
            return result;
        }
        // Determine whether to approve or reject based on the action
        if ("approve".equalsIgnoreCase(action)) {
            existingPoNumber.setApprovalStatus("Deletion Approved");
            result.put("message", "PO number deletion approved.");
            // Update the workflow status
            updateWorkflow(poNumber, "Pending Deletion", "Deletion Approved", "PO number deletion approved.");
        } else if ("reject".equalsIgnoreCase(action)) {
            existingPoNumber.setApprovalStatus("Deletion Rejected");
            result.put("message", "PO number deletion rejected.");
            // Update the workflow status
            updateWorkflow(poNumber, "Pending Deletion", "Deletion Rejected", "PO number deletion rejected.");
        }
        // Save the updated PO number status
        poNumberRepo.save(existingPoNumber);
        result.put("status", "Success");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to process deletion: " + ex.getMessage());
    }
    return result;
}


//Approve Approvals Add, Put, Delete for poItems
@PostMapping(value = "/poItems/approve")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional
public Map<String, Object> approvePoItems(@RequestBody Map<String, Object> requestBody) {
    logger.info("APPROVE PO ITEMS REQUEST | Request Body: " + requestBody);
    Map<String, Object> response = new HashMap<>();
    List<Map<String, String>> results = new ArrayList<>();
    // Extract selected rows and request type from the request body
    List<Map<String, Object>> selectedRows = (List<Map<String, Object>>) requestBody.get("selectedRows");
    String requestType = (String) requestBody.get("requestType");
    // Validate the request
    if (selectedRows == null || selectedRows.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "No rows selected.");
        return response;
    }
    if (!"addition".equalsIgnoreCase(requestType) && !"modification".equalsIgnoreCase(requestType) && !"deletion".equalsIgnoreCase(requestType)) {
        response.put("status", "Error");
        response.put("message", "Invalid request type: " + requestType);
        return response;
    }
    // Process each selected row
    for (Map<String, Object> row : selectedRows) {
        String poNumber = (String) row.get("poNumber");
        Long recordNo = row.get("recordNo") != null ? Long.parseLong(row.get("recordNo").toString()) : null;
        Map<String, String> result = processSinglePoItemApprove(poNumber, recordNo, requestType);
        results.add(result);
    }
    response.put("results", results);
    return response;
}

private Map<String, String> processSinglePoItemApprove(String poNumber, Long recordNo, String requestType) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    if (recordNo != null) {
        result.put("recordNo", String.valueOf(recordNo));
    }
    try {
        switch (requestType.toLowerCase()) {
            case "addition":
                result = approveAddition(poNumber, recordNo);
                break;
            case "modification":
                result = approveModification(poNumber, recordNo);
                break;
            case "deletion":
                result = approveDeletion(poNumber, recordNo);
                break;
            default:
                result.put("status", "Error");
                result.put("message", "Invalid request type: " + requestType);
                break;
        }
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to process approval: " + ex.getMessage());
        logger.error("Exception for PO Number: {} | Record No: {} | Error: {}", poNumber, recordNo, ex);
    }
    return result;
}
private Map<String, String> approveAddition(String poNumber, Long recordNo) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    result.put("recordNo", String.valueOf(recordNo));
    try {
        // Fetch the PO item by poNumber and recordNo
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            result.put("status", "Error");
            result.put("message", "PO item with PO Number: " + poNumber + " and Record No: " + recordNo + " does not exist");
            return result;
        }
        // Check if the PO item is in "Pending Addition" status
        if (!"Pending Addition".equals(poItem.getApproval_Status())) {
            result.put("status", "Error");
            result.put("message", "PO item with PO Number: " + poNumber + " and Record No: " + recordNo + " is not 'Pending Addition'");
            return result;
        }
        // Approve the PO item
        poItem.setApproval_Status("Approved");
        poRepo.save(poItem);
        // Update the workflow status
        updateWorkflow(poNumber, "Pending Addition", "Addition Approved", "PO item addition approved.");
        result.put("status", "Success");
        result.put("message", "PO item addition approved.");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to approve addition: " + ex.getMessage());
    }
    return result;
}
private Map<String, String> approveModification(String poNumber, Long recordNo) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    result.put("recordNo", String.valueOf(recordNo));
    try {
        // Fetch the modification request
        tb_Po_Modification modification = poModificationRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (modification == null) {
            result.put("status", "Error");
            result.put("message", "No modification request found for poNumber " + poNumber + " and recordNo " + recordNo);
            return result;
        }
        // Fetch the corresponding PO item
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            result.put("status", "Error");
            result.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " does not exist");
            return result;
        }
        // Ensure modification is pending
        if (!"Pending Modification".equals(modification.getApproval_Status())) {
            result.put("status", "Error");
            result.put("message", "Modification request is not pending approval");
            return result;
        }
        // Apply all modifications to the main PO record
        poItem.setRecordNo(modification.getRecordNo());
        poItem.setPoNumber(modification.getPoNumber());
        poItem.setModelNumber(modification.getModelNumber());
        poItem.setUom(modification.getUom());
        poItem.setQtyPerSite(modification.getQtyPerSite());
        poItem.setTotalNumberOfSites(modification.getTotalNumberOfSites());
        poItem.setTotalQty(modification.getTotalQty());
        poItem.setAccumulatedDepreciation(modification.getAccumulatedDepreciation());
        poItem.setSalvageValue(modification.getSalvageValue());
        poItem.setFaCategoryNew(modification.getFaCategoryNew());
        poItem.setL1(modification.getL1());
        poItem.setL2(modification.getL2());
        poItem.setL3(modification.getL3());
        poItem.setL4(modification.getL4());
        poItem.setOldFaCategory(modification.getOldFaCategory());
        poItem.setAccumulatedDepreciationCode(modification.getAccumulatedDepreciationCode());
        poItem.setDepreciationCode(modification.getDepreciationCode());
        poItem.setLifeYearsNew(modification.getLifeYearsNew());
        poItem.setVendorName(modification.getVendorName());
        poItem.setVendorNumber(modification.getVendorNumber());
        poItem.setProjectNumber(modification.getProjectNumber());
        poItem.setCurrency(modification.getCurrency());
        poItem.setUnitPrice(modification.getUnitPrice());
        poItem.setPoLine(modification.getPoLine());
        poItem.setLevel1Description(modification.getLevel1Description());
        poItem.setPartNumber(modification.getPartNumber());
        poItem.setCostCenter(modification.getCostCenter());
        poItem.setUpdatedBy(modification.getUpdatedBy());
        poItem.setApproval_Status("Approved");
        // Save the updated PO item
        poRepo.save(poItem);
        // Update the workflow status
        updateWorkflow(poNumber, "Pending Modification", "Modification Approved", "PO item modification approved.");
        // Delete the modification request
        poModificationRepo.delete(modification);
        result.put("status", "Success");
        result.put("message", "PO item modification approved.");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to approve modification: " + ex.getMessage());
    }
    return result;
}
private Map<String, String> approveDeletion(String poNumber, Long recordNo) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    result.put("recordNo", String.valueOf(recordNo));
    try {
        // Fetch the PO item by poNumber and recordNo
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            result.put("status", "Error");
            result.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " does not exist");
            return result;
        }
        // Check if the PO item is in "Pending Deletion" status
        if (!"Pending Deletion".equals(poItem.getApproval_Status())) {
            result.put("status", "Error");
            result.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " is not 'Pending Deletion'");
            return result;
        }
        // Approve the deletion
        poRepo.delete(poItem);
        // Update the workflow status
        updateWorkflow(poNumber, "Pending Deletion", "Deletion Approved", "PO item deletion approved.");
        result.put("status", "Success");
        result.put("message", "PO item deletion approved.");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to approve deletion: " + ex.getMessage());
    }
    return result;
}

// rejection Approval for Add, Put, Delete for poItems
@PostMapping(value = "/poItems/reject")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional
public Map<String, Object> rejectPoItems(@RequestBody Map<String, Object> requestBody) {
    logger.info("REJECT PO ITEMS REQUEST | Request Body: " + requestBody);
    Map<String, Object> response = new HashMap<>();
    List<Map<String, String>> results = new ArrayList<>();

    // Extract selected rows and request type from the request body
    List<Map<String, Object>> selectedRows = (List<Map<String, Object>>) requestBody.get("selectedRows");
    String requestType = (String) requestBody.get("requestType");

    // Validate the request
    if (selectedRows == null || selectedRows.isEmpty()) {
        response.put("status", "Error");
        response.put("message", "No rows selected.");
        return response;
    }
    if (!"addition".equalsIgnoreCase(requestType) && !"modification".equalsIgnoreCase(requestType) && !"deletion".equalsIgnoreCase(requestType)) {
        response.put("status", "Error");
        response.put("message", "Invalid request type: " + requestType);
        return response;
    }

    // Process each selected row
    for (Map<String, Object> row : selectedRows) {
        String poNumber = (String) row.get("poNumber");
        Long recordNo = row.get("recordNo") != null ? Long.parseLong(row.get("recordNo").toString()) : null;

        Map<String, String> result = processSinglePoItemReject(poNumber, recordNo, requestType);
        results.add(result);
    }

    response.put("results", results);
    return response;
}

private Map<String, String> processSinglePoItemReject(String poNumber, Long recordNo, String requestType) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    if (recordNo != null) {
        result.put("recordNo", String.valueOf(recordNo));
    }
    try {
        switch (requestType.toLowerCase()) {
            case "addition":
                result = rejectAddition(poNumber, recordNo);
                break;
            case "modification":
                result = rejectModification(poNumber, recordNo);
                break;
            case "deletion":
                result = rejectDeletion(poNumber, recordNo);
                break;
            default:
                result.put("status", "Error");
                result.put("message", "Invalid request type: " + requestType);
                break;
        }
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to process rejection: " + ex.getMessage());
        logger.error("Exception for PO Number: {} | Record No: {} | Error: {}", poNumber, recordNo, ex);
    }
    return result;
}

private Map<String, String> rejectAddition(String poNumber, Long recordNo) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    result.put("recordNo", String.valueOf(recordNo));
    try {
        // Fetch the PO item by poNumber and recordNo
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            result.put("status", "Error");
            result.put("message", "PO item with PO Number: " + poNumber + " and Record No: " + recordNo + " does not exist");
            return result;
        }
        // Check if the PO item is in "Pending Addition" status
        if (!"Pending Addition".equals(poItem.getApproval_Status())) {
            result.put("status", "Error");
            result.put("message", "PO item with PO Number: " + poNumber + " and Record No: " + recordNo + " is not 'Pending Addition'");
            return result;
        }
        // Reject the PO item
        poItem.setApproval_Status("Addition Rejected");
        poRepo.save(poItem);
        // Update the workflow status
        updateWorkflow(poNumber, "Pending Addition", "Addition Rejected", "PO item addition rejected.");
        result.put("status", "Success");
        result.put("message", "PO item addition rejected.");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to reject addition: " + ex.getMessage());
    }
    return result;
}
private Map<String, String> rejectModification(String poNumber, Long recordNo) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    result.put("recordNo", String.valueOf(recordNo));
    try {
        // Fetch the modification request
        tb_Po_Modification modification = poModificationRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (modification == null) {
            result.put("status", "Error");
            result.put("message", "No modification request found for poNumber " + poNumber + " and recordNo " + recordNo);
            return result;
        }
        // Fetch the corresponding PO item
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            result.put("status", "Error");
            result.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " does not exist");
            return result;
        }
        // Ensure modification is pending
        if (!"Pending Modification".equals(modification.getApproval_Status())) {
            result.put("status", "Error");
            result.put("message", "Modification request is not pending approval");
            return result;
        }
        // Reject the modification
        poItem.setApproval_Status("Approved");
        poRepo.save(poItem);
        // Update the workflow status
        updateWorkflow(poNumber, "Pending Modification", "Modification Rejected", "PO item modification rejected.");
        // Delete the modification request
        poModificationRepo.delete(modification);
        result.put("status", "Success");
        result.put("message", "PO item modification rejected.");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to reject modification: " + ex.getMessage());
    }
    return result;
}
private Map<String, String> rejectDeletion(String poNumber, Long recordNo) {
    Map<String, String> result = new HashMap<>();
    result.put("poNumber", poNumber);
    result.put("recordNo", String.valueOf(recordNo));
    try {
        // Fetch the PO item by poNumber and recordNo
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            result.put("status", "Error");
            result.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " does not exist");
            return result;
        }
        // Check if the PO item is in "Pending Deletion" status
        if (!"Pending Deletion".equals(poItem.getApproval_Status())) {
            result.put("status", "Error");
            result.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " is not 'Pending Deletion'");
            return result;
        }
        // Reject the deletion
        poItem.setApproval_Status("Approved");
        poRepo.save(poItem);
        // Update the workflow status
        updateWorkflow(poNumber, "Pending Deletion", "Deletion Rejected", "PO item deletion rejected.");
        result.put("status", "Success");
        result.put("message", "PO item deletion rejected.");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to reject deletion: " + ex.getMessage());
    }
    return result;
}
private void updateWorkflow(String poNumber, String originalStatus, String updatedStatus, String comments) {
    // Fetch all workflow entries for the given PO number and original status
    List<Workflow> workflows = workflowRepository.findByPoNumberAndOriginalStatus(poNumber, originalStatus);
    if (!workflows.isEmpty()) {
        for (Workflow workflow : workflows) {
            workflow.setUpdatedStatus(updatedStatus);
            workflow.setChangedBy("System"); // Replace with actual user if available
            workflow.setChangeDate(new Date());
            workflow.setComments(comments);
            workflowRepository.save(workflow);
        }
        logger.info("Updated workflow for PO Number: {} with original status: {}", poNumber, originalStatus);
    } else {
        logger.warn("No existing workflow found for PO Number: {} with original status: {}", poNumber, originalStatus);
    }
}



}
