
package com.telkom.almBHZain.controller;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.telkom.almBHZain.model.Workflow;
  import com.telkom.almBHZain.model.tbPoNumber;
import com.telkom.almBHZain.model.tb_Po;
import com.telkom.almBHZain.model.tb_Po_Modification;
import com.telkom.almBHZain.repo.WorkflowRepository;
import com.telkom.almBHZain.repo.tbPoNumberRepo;
import com.telkom.almBHZain.repo.tbPoRepo;
import com.telkom.almBHZain.repo.tb_Po_ModificationRepository;
import com.telkom.almBHZain.response.BulkPoItemResult;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class BahrainController {

    private final org.apache.logging.log4j.Logger loggger = LogManager.getLogger(BahrainController.class);
    private static final Logger logger = LoggerFactory.getLogger(BahrainController.class);
 
    @Autowired
    tbPoRepo poRepo;
    @Autowired
    tbPoNumberRepo poNumberRepo;
    @Autowired
    private WorkflowRepository workflowRepository;
    @Autowired
     private tb_Po_ModificationRepository poModificationRepo;
   

//addition request of poNumber if it doesnt already exist
@PostMapping(value = "/poNumbers")
public ResponseEntity<Map<String, String>> createPONumber(@RequestBody String req) {
    loggger.info("PO NUMBER CREATE REQUEST |  " + req);
    List<String> createdPoNumbers = new ArrayList<>();
    List<String> validationErrors = new ArrayList<>();

    try {
        JSONArray jsonArray = new JSONArray(req);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String poNumber = jsonObject.getString("poNumber").trim();

            // Check if the poNumber already exists
            tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
            if (existsPoNumber != null) {
                validationErrors.add("poNumber already exists: " + poNumber);
                continue;
            }

            // Create a new tbPoNumber entry
            tbPoNumber newPoNumber = new tbPoNumber();
            newPoNumber.setPoNumber(poNumber);
            newPoNumber.setApprovalStatus("Pending Addition");

            try {
                poNumberRepo.save(newPoNumber);
                createdPoNumbers.add(poNumber);

                // Create workflow request
                Workflow workflow = new Workflow();
                workflow.setPoNumber(poNumber);
                workflow.setOriginalStatus("Pending Addition");
                workflow.setProcessId(generateProcessId());
                workflow.setInsertedBy("System");
                workflow.setInsertDate(new Date());
                workflowRepository.save(workflow);
            } catch (Exception ex) {
                loggger.info("Exception while saving | " + ex.toString());
                validationErrors.add("Failed to save: " + poNumber);
            }
        }

        if (!validationErrors.isEmpty()) {
            return response("Error", "Some errors occurred: " + String.join(", ", validationErrors));
        } else {
            return response("Success", "Created PO number: " + String.join(", ", createdPoNumbers));
        }
    } catch (JSONException exc) {
        loggger.info("JSONException | " + exc.toString());
        return response("Error", exc.getMessage());
    }
}


@DeleteMapping(value = "/poNumbers/{poNumber}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public ResponseEntity<Map<String, String>> requestDeletePoNum(@PathVariable String poNumber) {
    logger.info("PO NUMBER DELETE REQUEST | poNumber: {}", poNumber);
    Map<String, String> response = new HashMap<>();

    try {
        if (poNumber == null || poNumber.trim().isEmpty()) {
            return response("Error", "PO number is required");
        }

        // 1. Check if PO number exists
        tbPoNumber poNumberEntry = poNumberRepo.findByPoNumber(poNumber.trim());
        if (poNumberEntry == null) {
            return response("Error", "PO number '" + poNumber + "' does not exist");
        }

        // 2. Check if PO number is in a pending state
        String poNumberStatus = poNumberEntry.getApprovalStatus();
        if (poNumberStatus != null && (
                poNumberStatus.equalsIgnoreCase("Pending Addition") ||
                poNumberStatus.equalsIgnoreCase("Pending Deletion") ||
                poNumberStatus.equalsIgnoreCase("Pending Modification"))) {
            return response("Error", "PO number '" + poNumber + "' is in a pending state (" + 
                poNumberStatus + ") and cannot be deleted");
        }

        // 3. Fetch PO items either by repo or relationship fallback
        List<tb_Po> poItems = poRepo.findByPoNumber(poNumber);
        if (poItems == null || poItems.isEmpty()) {
            poItems = poNumberEntry.getPoItems(); // fallback
        }

        if (poItems != null && !poItems.isEmpty()) {
            for (tb_Po item : poItems) {
                String itemStatus = item.getApproval_Status();
                if (itemStatus == null || !itemStatus.equalsIgnoreCase("Approved")) {
                    return response("Error", "PO number '" + poNumber +
                        "' contains items with status '" + itemStatus +
                        "'. All items must be 'Approved' before deletion");
                }
            }
        }

        // 4. Set approval status to pending deletion
        poNumberEntry.setApprovalStatus("Pending Deletion");
        poNumberRepo.save(poNumberEntry);

        // 5. Create workflow record
        Workflow workflow = new Workflow();
        workflow.setPoNumber(poNumber);
        workflow.setOriginalStatus("Pending Deletion");
        workflow.setProcessId(generateProcessId());
        workflow.setInsertedBy("System");
        workflow.setInsertDate(new Date());
        workflowRepository.save(workflow);

        return response("Success", "PO number '" + poNumber + "' is marked for deletion");

    } catch (Exception ex) {
        logger.error("Exception while processing PO number delete request", ex);
        return response("Error", "An error occurred while processing deletion: " + ex.getMessage());
    }
}



private String generateProcessId() {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String randomDigit = String.valueOf((int) (Math.random() * 10));
    // Use the last 8 digits of the timestamp to ensure the processId is always unique
    return timestamp.substring(timestamp.length() - 6) + randomDigit;
}

//addition request of poItem
// @PostMapping(value = "/poItems")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> createPo(@RequestBody String req) throws ParseException {
//     String batchfilename = "";
//     LocalDateTime now = LocalDateTime.now();
//     logger.info("PO CREATE REQUEST |  " + req);
//     Map<String, String> response = new HashMap<>();
//     try {
//         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
//         JSONArray jsonArray = new JSONArray(req);
//         String responseinfo = "Failed to save or data";

//         List<String> validationErrors = new ArrayList<>();
//         for (int i = 0; i < jsonArray.length(); i++) {
//             JSONObject jsonObject = jsonArray.getJSONObject(i);
//             String poNumber = jsonObject.getString("poNumber").trim();
//             // Check if the poNumber exists in tb_PONumber
//             tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
//             if (existsPoNumber == null) {
//                 validationErrors.add("poNumber does not exist in tb_PONumber: " + poNumber);
//                 continue; // Skip this record
//             }

//             // Create new record in tb_Po
//             tb_Po nwspldt = new tb_Po();
//             nwspldt.setPoNumber(poNumber);
//             nwspldt.setModelNumber(jsonObject.getString("modelNumber"));
//             nwspldt.setUom(jsonObject.getString("uom"));
//             nwspldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
//             nwspldt.setTotalNumberOfSites(jsonObject.getInt("totalNumberOfSites"));
//             nwspldt.setTotalQty(jsonObject.getInt("totalQty"));
//             nwspldt.setAccumulatedDepreciation(jsonObject.getDouble("accumulatedDepreciation"));
//             nwspldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
//             nwspldt.setFaCategoryNew(jsonObject.getString("faCategoryNew").trim());
//             nwspldt.setL1(jsonObject.getString("l1"));
//             nwspldt.setL2(jsonObject.getString("l2").trim());
//             nwspldt.setL3(jsonObject.getString("l3").trim());
//             nwspldt.setL4(jsonObject.getString("l4"));
//             nwspldt.setOldFaCategory(jsonObject.getString("oldFaCategory").trim());
//             nwspldt.setAccumulatedDepreciationCode(jsonObject.getString("accumulatedDepreciationCode"));
//             nwspldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
//             nwspldt.setLifeYearsNew(jsonObject.getInt("lifeYearsNew"));
//             nwspldt.setVendorName(jsonObject.getString("vendorName"));
//             nwspldt.setVendorNumber(jsonObject.getString("vendorNumber"));
//             nwspldt.setProjectNumber(jsonObject.getString("projectNumber"));
//             String datePlacedInService = jsonObject.getString("datePlacedInService");
//             String poDate = jsonObject.getString("poDate");
//             try {
//                 java.util.Date parsedDate = dateFormat.parse(datePlacedInService);
//                 java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
//                 java.util.Date newDate = dateFormat.parse(poDate);
//                 java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
//                 nwspldt.setDatePlacedInService(sqlDate);
//                 nwspldt.setPoDate(sqlcreatedDate);
//                 nwspldt.setRecordDateTime(new java.sql.Date(System.currentTimeMillis()));
//             } catch (ParseException ex) {
//                 logger.error("Error parsing date: ", ex);
//             }
//             nwspldt.setCurrency(jsonObject.getString("currency"));
//             nwspldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
//             nwspldt.setPoLine(jsonObject.getInt("poLine"));
//             nwspldt.setLevel1Description(jsonObject.getString("level1Description"));
//             nwspldt.setPartNumber(jsonObject.getString("partNumber"));
//             nwspldt.setL3Description(jsonObject.getString("level1Description"));
//             nwspldt.setApproval_Status("Pending Addition"); // Set initial status to Pending
//             nwspldt.setCostCenter(jsonObject.getString("costCenter").trim());
//             nwspldt.setCreatedBy(jsonObject.getString("createdBy").trim());

//             try {
//                 // Save the PO item
//                 poRepo.save(nwspldt);
//                 responseinfo = "Record Created Success";

//                 // Create workflow request
//                 Workflow workflow = new Workflow();
//                 workflow.setPoNumber(nwspldt.getPoNumber());
//                 workflow.setRecordNo(nwspldt.getRecordNo()); // Set the recordNo
//                 workflow.setOriginalStatus("Pending POItem Addition");
//                 workflow.setProcessId(generateProcessId());
//                 workflow.setInsertedBy("System");
//                 workflow.setInsertDate(new Date());
//                 workflowRepository.save(workflow); // Save the workflow entry
//             } catch (Exception excc) {
//                 logger.error("Exception |  " + excc.toString());
//                 responseinfo = excc.toString();
//             }
//         }
//         logger.info("PO CREATE RESPONSE |  " + responseinfo);
//         logger.info("VALIDATION RESPONSE |  " + validationErrors);
//         if (!validationErrors.isEmpty()) {
//             batchfilename = getbatchfilename("FailedUpload");
//             helper.logBatchFile(responseinfo, true, batchfilename);
//             return response("Error", "Validation errors: " + String.join(", ", validationErrors));
//         } else if (!responseinfo.contains("Success")) {
//             batchfilename = getbatchfilename("FailedUpload");
//             helper.logBatchFile(responseinfo, true, batchfilename);
//             return response("Error", responseinfo);
//         } else {
//             return response("Success", "Complete");
//         }
//     } catch (NumberFormatException | JSONException exc) {
//         logger.error("Exception |  " + exc.toString());
//         return response("Error", exc.getMessage());
//     }
// }

private String getbatchfilename(String filetype) {
    String batchfilename = filetype + "_" + System.currentTimeMillis() + ".json";
    return batchfilename;
}





@PostMapping(value = "/poItems", produces = "application/json")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public ResponseEntity<Map<String, String>> addSinglePoItem(@RequestBody String req) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    try {
        JSONObject jsonObject = new JSONObject(req);
        String poNumber = jsonObject.getString("poNumber").trim();
        int poLine = jsonObject.getInt("poLine");

        // Check if PO number exists
        tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
        if (existsPoNumber == null) {
            return response("Error", "PO Number " + poNumber + " does not exist, provide an existing PO Number");
        }

        // Check if PO item already exists
        tb_Po existing = poRepo.findByPoNumberAndPoLine(poNumber, poLine);
        if (existing != null) {
            String status = existing.getApproval_Status();
            if (status != null && status.startsWith("Pending")) {
                return response("Error", "PO item already exists for poNumber " + poNumber + ", poLine " + poLine + " and is currently in pending state: " + status + ".");
            } else {
                return response("Error", "PO item already exists for poNumber " + poNumber + ", poLine " + poLine + ".");
            }
        }

        // Create and save new PO item
        tb_Po newPoItem = mapToPo(jsonObject, dateFormat);
        poRepo.save(newPoItem);

        // Create workflow entry
        workflowRepository.save(buildWorkflow(newPoItem.getPoNumber(), newPoItem.getRecordNo(), ""));

        return response("Success", "PO Item for PO Number " + poNumber + ", PO Line " + poLine + " added successfully.");
    } catch (JSONException e) {
        return response("Error", "Invalid JSON input: " + e.getMessage());
    } catch (ParseException e) {
        return response("Error", "Date parsing error: " + e.getMessage());
    } catch (Exception e) {
        return response("Error", "Unexpected error: " + e.getMessage());
    }
}

//bulk add or modify poitems
@PostMapping(value = "/poItems/bulk", produces = "application/json")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public ResponseEntity<BulkPoItemResult> addOrUpdatePoItems(@RequestBody String req) {
    BulkPoItemResult result = new BulkPoItemResult();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    try {
        JSONArray jsonArray = new JSONArray(req);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            try {
                String poNumber = jsonObject.getString("poNumber").trim();
                int poLine = jsonObject.getInt("poLine");

                tb_Po existing = poRepo.findByPoNumberAndPoLine(poNumber, poLine);
                if (existing != null) {
                    // Check approval status
                    String status = existing.getApproval_Status();
                    if (status == null || !status.startsWith("Pending")) {
                        tb_Po_Modification mod = mapToModification(existing.getRecordNo(), jsonObject);
                        poModificationRepo.save(mod);

                        existing.setApproval_Status("Pending Modification");
                        poRepo.save(existing);

                        workflowRepository.save(buildWorkflow(existing.getPoNumber(), existing.getRecordNo(), ""));
                        result.getModifiedRows().add(mod);
                    } else {
                        result.getModificationErrors().add("PO Number" + poNumber + " and PO Line " + poLine + " is already in pending state: " + status + ".");
                    }
                } else {
                    // Add new item
                    tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
                    if (existsPoNumber == null) {
                        result.getAdditionErrors().add("PO Number " + poNumber + " does not exist, Provide an existing PO Number ");
                        continue;
                    }

                    tb_Po newItem = mapToPo(jsonObject, dateFormat);
                    poRepo.save(newItem);

                    workflowRepository.save(buildWorkflow(newItem.getPoNumber(), newItem.getRecordNo(), "Pending POItem Addition"));
                    result.getAddedRows().add(newItem);
                }
            } catch (Exception e) {
                result.getAdditionErrors().add("Processing error for item: " + e.getMessage());
            }
        }

        // Set summary counts
        result.setAddedCount(result.getAddedRows().size());
        result.setModifiedCount(result.getModifiedRows().size());
        result.setFailedAdditionCount(result.getAdditionErrors().size());
        result.setFailedModificationCount(result.getModificationErrors().size());

        return ResponseEntity.ok(result);
    } catch (JSONException e) {
        result.getAdditionErrors().add("Invalid JSON format: " + e.getMessage());
        result.setFailedAdditionCount(result.getAdditionErrors().size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}


//deletion request of po item
@DeleteMapping(value = "/poItems/{recordNo}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> requestDeletePoItem(@PathVariable long recordNo) {
    logger.info("PO DELETE REQUEST | recordNo: " + recordNo);
    Map<String, String> response = new HashMap<>();
    try {
        // Check if the PO item exists
        tb_Po poItem = poRepo.findByRecordNo(recordNo);
        if (poItem == null) {
            response.put("status", "Error");
            response.put("message", "PO item with recordNo " + recordNo + " does not exist");
            return response;
        }

        // Check if the PO item is in a pending state
        String currentStatus = poItem.getApproval_Status();
        if (currentStatus.equals("Pending Addition") || currentStatus.equals("Pending Deletion") || currentStatus.equals("Pending Modification")) {
            response.put("status", "Error");
            response.put("message", "PO item with recordNo " + recordNo + " is already in a pending state (" + currentStatus + ") and cannot be requested for deletion");
            return response;
        }

        // Update the approval status to "Pending Deletion"
        poItem.setApproval_Status("Pending Deletion");
        poRepo.save(poItem);

        // Create a new Workflow entry
        Workflow workflow = new Workflow();
        workflow.setPoNumber(poItem.getPoNumber());
        workflow.setRecordNo(poItem.getRecordNo()); // Set the recordNo
        workflow.setOriginalStatus("Pending POItem Deletion");
        workflow.setProcessId(generateProcessId());
        workflow.setInsertedBy("System"); // or get the current user
        workflow.setInsertDate(new Date());
        workflowRepository.save(workflow); // Save the workflow entry

        // Success response in JSON format
        response.put("status", "Success");
        response.put("message", "PO item with recordNo " + recordNo + " is pending deletion");
    } catch (Exception ex) {
        logger.error("Exception | ", ex);
        response.put("status", "Error");
        response.put("message", "Failed to request deletion of PO item: " + ex.getMessage());
    }
    return response;
}

@PutMapping(value = "/poItems/{recordNo}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> updatePoItem(@PathVariable long recordNo, @RequestBody String req) {
    logger.info("PO UPDATE REQUEST | Record No: {} | Payload: {}", recordNo, req);
    Map<String, String> response = new HashMap<>();
    try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JSONObject jsonObject = new JSONObject(req);
        // Fetch the existing PO record
        tb_Po existingPo = poRepo.findByRecordNo(recordNo);
        if (existingPo == null) {
            response.put("status", "Error");
            response.put("message", "Record does not exist for recordNo: " + recordNo);
            return response;
        }
        // Check if the PO item is in a pending state
        String currentStatus = existingPo.getApproval_Status();
        if (currentStatus.equals("Pending Addition") || currentStatus.equals("Pending Deletion") || currentStatus.equals("Pending Modification")) {
            response.put("status", "Error");
            response.put("message", "PO item with recordNo " + recordNo + " is already in a pending state (" + currentStatus + ") and cannot be updated");
            return response;
        }
        // Ensure the poNumber is not altered
        String poNumber = jsonObject.getString("poNumber").trim();
        if (!existingPo.getPoNumber().equals(poNumber)) {
            response.put("status", "Error");
            response.put("message", "Cannot alter poNumber for recordNo: " + recordNo);
            return response;
        }
        // Create a new tb_Po_Modification entity and populate it with the request data
        tb_Po_Modification modification = new tb_Po_Modification();
        modification.setRecordNo(recordNo);
        modification.setPoNumber(poNumber);
        modification.setModelNumber(jsonObject.optString("modelNumber", null));
        modification.setUom(jsonObject.optString("uom", null));
        modification.setQtyPerSite(jsonObject.optInt("qtyPerSite", 0));
        modification.setTotalNumberOfSites(jsonObject.optInt("totalNumberOfSites", 0));
        modification.setTotalQty(jsonObject.optInt("totalQty", 0));
        modification.setAccumulatedDepreciation(jsonObject.optDouble("accumulatedDepreciation", 0.0));
        modification.setSalvageValue(jsonObject.optDouble("salvageValue", 0.0));
        modification.setFaCategoryNew(jsonObject.optString("faCategoryNew", null));
        modification.setL1(jsonObject.optString("l1", null));
        modification.setL2(jsonObject.optString("l2", null));
        modification.setL3(jsonObject.optString("l3", null));
        modification.setL4(jsonObject.optString("l4", null));
        modification.setOldFaCategory(jsonObject.optString("oldFaCategory", null));
        modification.setAccumulatedDepreciationCode(jsonObject.optString("accumulatedDepreciationCode", null));
        modification.setDepreciationCode(jsonObject.optString("depreciationCode", null));
        modification.setLifeYearsNew(jsonObject.optInt("lifeYearsNew", 0));
        modification.setVendorName(jsonObject.optString("vendorName", null));
        modification.setVendorNumber(jsonObject.optString("vendorNumber", null));
        modification.setProjectNumber(jsonObject.optString("projectNumber", null));
        modification.setCurrency(jsonObject.optString("currency", null));
        modification.setUnitPrice(jsonObject.optDouble("unitPrice", 0.0));
        modification.setPoLine(jsonObject.optInt("poLine", 0));
        modification.setLevel1Description(jsonObject.optString("level1Description", null));
        modification.setPartNumber(jsonObject.optString("partNumber", null));
        modification.setCostCenter(jsonObject.optString("costCenter", null));
        modification.setUpdatedBy(jsonObject.optString("updatedBy", null));
        modification.setApproval_Status("Pending Modification");
        modification.setRecordDateTime(new Date());
        modification.setCreatedBy("System");
        try {
            // Save the modification request
            poModificationRepo.save(modification);
            // Update the existing PO record's approval status
            existingPo.setApproval_Status("Pending Modification");
            poRepo.save(existingPo);
            // Insert into Workflow Table
            try {
                logger.info("Creating workflow entry for PO Number: {}", existingPo.getPoNumber());
                Workflow workflow = new Workflow();
                workflow.setPoNumber(modification.getPoNumber());
                workflow.setRecordNo(modification.getRecordNo()); // Set the recordNo
                workflow.setOriginalStatus("Pending POItem Modification");
                workflow.setProcessId(generateProcessId());
                workflow.setInsertedBy("System");
                workflow.setInsertDate(new Date());
                workflowRepository.save(workflow); // Save the workflow entry

                logger.info("Workflow entry saved successfully for PO Number: {}", existingPo.getPoNumber());
            } catch (Exception e) {
                logger.error("Error saving workflow entry: {}", e.getMessage(), e);
                response.put("status", "Error");
                response.put("message", "Failed to save workflow entry for PO Number: " + existingPo.getPoNumber());
                return response;
            }
            logger.info("Modification request saved for PO Number: {}, Record No: {}", poNumber, recordNo);
            response.put("status", "Success");
            response.put("message", "Modification request submitted successfully for approval.");
            return response;
        } catch (Exception e) {
            logger.error("Error saving modification request: {}", e.getMessage(), e);
            response.put("status", "Error");
            response.put("message", "Failed to save modification for recordNo: " + recordNo);
            return response;
        }
    } catch (Exception e) {
        logger.error("Exception | {}", e.toString(), e);
        response.put("status", "Error");
        response.put("message", "Failed to process update request: " + e.getMessage());
        return response;
    }
}

 
     @PostMapping(value = "/poNumbers/approve")
     @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
     @Transactional
     public Map<String, Object> approvePoNumber(@RequestBody Map<String, Object> requestBody) {
         logger.info("APPROVE PO NUMBER REQUEST | Request Body: " + requestBody);
         return processWorkflowAction(requestBody, "approve");
     }

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
            // Approve the PO number
            existingPoNumber.setApprovalStatus("Approved");
            result.put("message", "PO number approved.");
            // Update the workflow status
            updateWorkflow(poNumber, "Pending Addition", "Addition Approved", "PO number addition approved.");
            // Save the updated PO number status
            poNumberRepo.save(existingPoNumber);
        } else if ("reject".equalsIgnoreCase(action)) {
            // Reject the PO number and delete it from the database
            poNumberRepo.delete(existingPoNumber); // Delete the PO number
            result.put("message", "PO number rejected and deleted from the database.");
            // Update the workflow status
            updateWorkflow(poNumber, "Pending Addition", "Addition Rejected", "PO number addition rejected and deleted.");
        }

        result.put("status", "Success");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to process addition: " + ex.getMessage());
        logger.error("Error processing addition for PO Number: " + poNumber, ex);
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
            // Delete the PO number and all associated PO items (cascading delete)
            poNumberRepo.delete(existingPoNumber); // Cascading delete will handle associated tb_Po items
            result.put("message", "PO number deletion approved and removed from database.");

            // Update the workflow status
            updateWorkflow(poNumber, "Pending Deletion", "Deletion Approved", "PO number deletion approved and removed.");
        } else if ("reject".equalsIgnoreCase(action)) {
            // Reject the deletion request
            existingPoNumber.setApprovalStatus("Approved");
            poNumberRepo.save(existingPoNumber);
            result.put("message", "PO number deletion rejected.");

            // Update the workflow status
            updateWorkflow(poNumber, "Pending Deletion", "Deletion Rejected", "PO number deletion rejected.");
        } else {
            // Invalid action
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
        updateWorkflow(poNumber, recordNo, "Pending POItem Addition", "Addition Approved", "PO item addition approved.");
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
        updateWorkflow(poNumber, recordNo, "Pending POItem Modification", "Modification Approved", "PO item modification approved.");
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
        updateWorkflow(poNumber, recordNo, "Pending POItem Deletion", "Deletion Approved", "PO item deletion approved.");
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
        poRepo.delete(poItem);
        // Update the workflow status
        updateWorkflow(poNumber, recordNo, "Pending POItem Addition", "Addition Rejected", "PO item addition rejected.");
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
        updateWorkflow(poNumber, recordNo, "Pending POItem Modification", "Modification Rejected", "PO item modification rejected.");
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
        updateWorkflow(poNumber, recordNo, "Pending POItem Deletion", "Deletion Rejected", "PO item deletion rejected.");
        result.put("status", "Success");
        result.put("message", "PO item deletion rejected.");
    } catch (Exception ex) {
        result.put("status", "Error");
        result.put("message", "Failed to reject deletion: " + ex.getMessage());
    }
    return result;
}

private void updateWorkflow(String poNumber, Long recordNo, String originalStatus, String updatedStatus, String comments) {
    try {
        // Fetch the existing workflow entry
        Workflow workflow = workflowRepository.findByPoNumberAndRecordNoAndOriginalStatus(poNumber, recordNo, originalStatus);

        if (workflow == null) {
            // If no existing entry is found, create a new one
            workflow = new Workflow();
            workflow.setPoNumber(poNumber);
            workflow.setRecordNo(recordNo);
            workflow.setOriginalStatus(originalStatus);
            workflow.setProcessId(generateProcessId());
            workflow.setInsertedBy("System");
            workflow.setInsertDate(new Date());
        }

        // Update the workflow entry
        workflow.setUpdatedStatus(updatedStatus);
        workflow.setComments(comments);
        workflow.setChangedBy("System");
        workflow.setChangeDate(new Date());
        workflowRepository.save(workflow); // Save or update the workflow entry

    } catch (Exception ex) {
        logger.error("Failed to update workflow for PO Number: {} and Record No: {}", poNumber, recordNo, ex);
    }
}



//Mapping Helpers

private tb_Po mapToPo(JSONObject obj, SimpleDateFormat dateFormat) throws ParseException {
    tb_Po po = new tb_Po();
    po.setPoNumber(obj.getString("poNumber").trim());
    po.setModelNumber(obj.optString("modelNumber"));
    po.setUom(obj.optString("uom"));
    po.setQtyPerSite(obj.optInt("qtyPerSite", 0));
    po.setTotalNumberOfSites(obj.optInt("totalNumberOfSites", 0));
    po.setTotalQty(obj.optInt("totalQty", 0));
    po.setAccumulatedDepreciation(obj.optDouble("accumulatedDepreciation", 0.0));
    po.setSalvageValue(obj.optDouble("salvageValue", 0.0));
    po.setFaCategoryNew(obj.optString("faCategoryNew"));
    po.setL1(obj.optString("l1"));
    po.setL2(obj.optString("l2"));
    po.setL3(obj.optString("l3"));
    po.setL4(obj.optString("l4"));
    po.setOldFaCategory(obj.optString("oldFaCategory"));
    po.setAccumulatedDepreciationCode(obj.optString("accumulatedDepreciationCode"));
    po.setDepreciationCode(obj.optString("depreciationCode"));
    po.setLifeYearsNew(obj.optInt("lifeYearsNew", 0));
    po.setVendorName(obj.optString("vendorName"));
    po.setVendorNumber(obj.optString("vendorNumber"));
    po.setProjectNumber(obj.optString("projectNumber"));
    po.setCurrency(obj.optString("currency"));
    po.setUnitPrice(obj.optDouble("unitPrice", 0.0));
    po.setPoLine(obj.optInt("poLine", 0));
    po.setLevel1Description(obj.optString("level1Description"));
    po.setPartNumber(obj.optString("partNumber"));
    po.setCostCenter(obj.optString("costCenter"));
    po.setCreatedBy(obj.optString("createdBy", "System"));
    po.setApproval_Status("Pending Addition");
    po.setPoDate(new java.sql.Date(dateFormat.parse(obj.optString("poDate")).getTime()));
    po.setDatePlacedInService(new java.sql.Date(dateFormat.parse(obj.optString("datePlacedInService")).getTime()));
    po.setRecordDateTime(new java.sql.Date(System.currentTimeMillis()));
    return po;
}

private tb_Po_Modification mapToModification(long recordNo, JSONObject obj) {
    tb_Po_Modification mod = new tb_Po_Modification();
    mod.setRecordNo(recordNo);
    mod.setPoNumber(obj.optString("poNumber"));
    mod.setModelNumber(obj.optString("modelNumber"));
    mod.setUom(obj.optString("uom"));
    mod.setQtyPerSite(obj.optInt("qtyPerSite", 0));
    mod.setTotalNumberOfSites(obj.optInt("totalNumberOfSites", 0));
    mod.setTotalQty(obj.optInt("totalQty", 0));
    mod.setAccumulatedDepreciation(obj.optDouble("accumulatedDepreciation", 0.0));
    mod.setSalvageValue(obj.optDouble("salvageValue", 0.0));
    mod.setFaCategoryNew(obj.optString("faCategoryNew"));
    mod.setL1(obj.optString("l1"));
    mod.setL2(obj.optString("l2"));
    mod.setL3(obj.optString("l3"));
    mod.setL4(obj.optString("l4"));
    mod.setOldFaCategory(obj.optString("oldFaCategory"));
    mod.setAccumulatedDepreciationCode(obj.optString("accumulatedDepreciationCode"));
    mod.setDepreciationCode(obj.optString("depreciationCode"));
    mod.setLifeYearsNew(obj.optInt("lifeYearsNew", 0));
    mod.setVendorName(obj.optString("vendorName"));
    mod.setVendorNumber(obj.optString("vendorNumber"));
    mod.setProjectNumber(obj.optString("projectNumber"));
    mod.setCurrency(obj.optString("currency"));
    mod.setUnitPrice(obj.optDouble("unitPrice", 0.0));
    mod.setPoLine(obj.optInt("poLine", 0));
    mod.setLevel1Description(obj.optString("level1Description"));
    mod.setPartNumber(obj.optString("partNumber"));
    mod.setCostCenter(obj.optString("costCenter"));
    mod.setUpdatedBy(obj.optString("updatedBy", "System"));
    mod.setApproval_Status("Pending Modification");
    mod.setRecordDateTime(new Date());
    mod.setCreatedBy("System");
    return mod;
}

private Workflow buildWorkflow(String poNumber, long recordNo, String status) {
    Workflow wf = new Workflow();
    wf.setPoNumber(poNumber);
    wf.setRecordNo(recordNo);
    wf.setOriginalStatus(status);
    wf.setProcessId(generateProcessId());
    wf.setInsertedBy("System");
    wf.setInsertDate(new Date());
    return wf;
}

private ResponseEntity<Map<String, String>> response(String status, String message) {
    Map<String, String> res = new HashMap<>();
    res.put("status", status);
    res.put("message", message);
    return ResponseEntity.ok(res);
}

}
