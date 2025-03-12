/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.telkom.almBHZain.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.telkom.almBHZain.helper.helper;
import com.telkom.almBHZain.model.Workflow;
import com.telkom.almBHZain.model.tbPoNumber;
import com.telkom.almBHZain.model.tb_Po;
import com.telkom.almBHZain.model.tb_Po_Modification;
import com.telkom.almBHZain.repo.WorkflowRepository;
import com.telkom.almBHZain.repo.tbPoNumberRepo;
import com.telkom.almBHZain.repo.tbPoRepo;
import com.telkom.almBHZain.repo.tb_Po_ModificationRepository;
/**
 *
 * @author jgithu
 */
@RestController
public class BarhainController {

    private final org.apache.logging.log4j.Logger loggger = LogManager.getLogger(APIController.class);
    private static final Logger logger = LoggerFactory.getLogger(BarhainController.class);
    //   @Autowired
    // private JdbcTemplate jdbcTemplate;
    @Autowired
    tbPoRepo poRepo;
    @Autowired
    tbPoNumberRepo poNumberRepo;
    @Autowired
    private WorkflowRepository workflowRepository;
    @Autowired
     private tb_Po_ModificationRepository poModificationRepo;
   


   // Add PO Number
   //create po Number if it doesnt already exist
   @PostMapping(value = "/createPONumber")
   @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
   public Map<String, String> createPONumber(@RequestBody String req) {
    loggger.info("PO NUMBER CREATE REQUEST |  " + req);
    try {
        JSONArray jsonArray = new JSONArray(req);
        List<String> validationErrors = new ArrayList<>();
        String responseinfo = "Failed to save or data";
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
                 //Create workflow request
              Workflow workflow = new Workflow();
              workflow.setPoNumber(poNumber);  // Set the PO number
              workflow.setOriginalStatus("Pending Addition");  // Set initial status
              workflow.setProcessId(generateProcessId());  
              workflow.setInsertedBy("System");  
              workflow.setInsertDate(new Date()); 
              workflowRepository.save(workflow); 
                responseinfo = "Record Created Successfully";
            } catch (Exception ex) {
                loggger.info("Exception |  " + ex.toString());
                responseinfo = ex.toString();
            }
        }
        loggger.info("PO NUMBER CREATE RESPONSE |  " + responseinfo);
        loggger.info("VALIDATION RESPONSE |  " + validationErrors);

        if (!validationErrors.isEmpty()) {
            return response("Error", "Validation errors: " + String.join(", ", validationErrors));
        } else if (!responseinfo.contains("Success")) {
            return response("Error", responseinfo);
        } else {
            return response("Success", "Complete");
        }
    } catch (JSONException exc) {
        loggger.info("Exception |  " + exc.toString());
        return response("Error", exc.getMessage());
    }
}

   // Approve/Reject PO Number Addition
   @PutMapping(value = "/createPONumber/{poNumber}/{action}")
   @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
   public Map<String, String> processPendingAddition(@PathVariable String poNumber, @PathVariable String action) {
    logger.info("PO ACTION REQUEST | PO Number: " + poNumber + " Action: " + action);
    Map<String, String> response = new HashMap<>();
    try {
        // Fetch the PO number record with Pending Addition status
        tbPoNumber existingPoNumber = poNumberRepo.findByPoNumber(poNumber);
        if (existingPoNumber == null) {
            return response("Error", "PO Number does not exist: " + poNumber);
        }
        // Check if the approval status is "Pending Addition"
        if (!"Pending Addition".equals(existingPoNumber.getApprovalStatus())) {
            return response("Error", "PO Number is not in 'Pending Addition' status: " + poNumber);
        }
        // Determine whether to approve or reject based on the action
        if ("approve".equalsIgnoreCase(action)) {
            // Approve the PO Number
            existingPoNumber.setApprovalStatus("Approved");
            response.put("message", "PO number approved.");
        } else if ("reject".equalsIgnoreCase(action)) {
            // Reject the PO Number
            existingPoNumber.setApprovalStatus("Addition Rejected");
            response.put("message", "PO number rejected.");
        } else {
            return response("Error", "Invalid action: " + action);
        }
        // Save the updated PO number status
        poNumberRepo.save(existingPoNumber);
        // Update the workflow status accordingly
        Workflow workflow = workflowRepository.findTopByPoNumberOrderByInsertDateDesc(poNumber);
        if (workflow != null) {
            workflow.setUpdatedStatus(existingPoNumber.getApprovalStatus());
            workflow.setChangedBy("System"); // Replace with actual user if available
            workflow.setChangeDate(new Date());
            workflow.setComments("PO number status updated to '" + existingPoNumber.getApprovalStatus() + "'");
            workflowRepository.save(workflow);
            logger.info("Updated workflow: {}", workflow);
        } else {
            // Handle case where workflow entry is not found (optional)
            logger.warn("No existing workflow found for PO Number: {}", poNumber);
        }
        logger.info("PO Number: " + poNumber + " " + action + "d successfully.");
        return response("Success", "PO number " + action + "d and workflow updated.");
    } catch (Exception e) {
        logger.error("Exception | " + e.toString(), e);
        return response("Error", "Failed to " + action + " PO number: " + poNumber);
    }
}

    //ADD POItem
    @PostMapping(value = "/createPoItem")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> createPo(@RequestBody String req) throws ParseException {
        String batchfilename = "";
        LocalDateTime now = LocalDateTime.now();
        loggger.info("PO CREATE REQUEST |  " + req);
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
            JSONArray jsonArray = new JSONArray(req);
            String responseinfo = "Failed to save or data";

            List<String> validationErrors = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String poNumber = jsonObject.getString("poNumber").trim();
                // Check if the poNumber exists in tb_PONumber
                tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
                if (existsPoNumber == null) {
                    validationErrors.add("poNumber does not exist in tb_PONumber: " + poNumber);
                    continue; // Skip this record
                }
                // Create new record in tb_Po
                tb_Po nwspldt = new tb_Po();
                nwspldt.setPoNumber(poNumber);
                nwspldt.setModelNumber(jsonObject.getString("modelNumber").trim());
                nwspldt.setUom(jsonObject.getString("uom"));
                nwspldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
                nwspldt.setTotalNumberOfSites(jsonObject.getInt("totalNumberOfSites"));
                nwspldt.setTotalQty(jsonObject.getInt("totalQty"));
                nwspldt.setAccumulatedDepreciation(jsonObject.getDouble("accumulatedDepreciation"));
                nwspldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
                nwspldt.setFaCategoryNew(jsonObject.getString("faCategoryNew").trim());
                nwspldt.setL1(jsonObject.getString("l1"));
                nwspldt.setL2(jsonObject.getString("l2").trim());
                nwspldt.setL3(jsonObject.getString("l3").trim());
                nwspldt.setL4(jsonObject.getString("l4"));
                nwspldt.setOldFaCategory(jsonObject.getString("oldFaCategory").trim());
                nwspldt.setAccumulatedDepreciationCode(jsonObject.getString("accumulatedDepreciationCode"));
                nwspldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
                nwspldt.setLifeYearsNew(jsonObject.getInt("lifeYearsNew"));
                nwspldt.setVendorName(jsonObject.getString("vendorName"));
                nwspldt.setVendorNumber(jsonObject.getString("vendorNumber"));
                nwspldt.setProjectNumber(jsonObject.getString("projectNumber"));
                String datePlacedInService = jsonObject.getString("datePlacedInService");
                String poDate = jsonObject.getString("poDate");
                try {
                    java.util.Date parsedDate = dateFormat.parse(datePlacedInService);
                    java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                    java.util.Date newDate = dateFormat.parse(poDate);
                    java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
                    nwspldt.setDatePlacedInService(sqlDate);
                    nwspldt.setPoDate(sqlcreatedDate);
                    nwspldt.setRecordDateTime(new java.sql.Date(System.currentTimeMillis()));
                } catch (ParseException ex) {
                    // Correct usage of Level.SEVERE
                    // Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
                }
                nwspldt.setCurrency(jsonObject.getString("currency"));
                nwspldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
                nwspldt.setPoLine(jsonObject.getInt("poLine"));
                nwspldt.setLevel1Description(jsonObject.getString("level1Description"));
                nwspldt.setPartNumber(jsonObject.getString("partNumber"));
                nwspldt.setL3Description(jsonObject.getString("level1Description"));
                nwspldt.setApproval_Status("Pending Modification"); // Set initial status to Pending
                nwspldt.setCostCenter(jsonObject.getString("costCenter").trim());
                nwspldt.setCreatedBy(jsonObject.getString("createdBy").trim());
                try {
                    poRepo.save(nwspldt);
                    responseinfo = "Record Created Success";

                    // Create workflow request
                    Workflow workflow = new Workflow();
                    workflow.setPoNumber(nwspldt.getPoNumber());
                    workflow.setOriginalStatus(nwspldt.getApproval_Status());
                    workflow.setProcessId(generateProcessId());
                    workflow.setInsertedBy("System"); 
                    workflow.setInsertDate(new Date());
                    workflowRepository.save(workflow);
                } catch (Exception excc) {
                    loggger.info("Exception |  " + excc.toString());
                    responseinfo = excc.toString();
                }
            }
            loggger.info("PO CREATE RESPONSE |  " + responseinfo);
            loggger.info("VALIDATION RESPONSE |  " + validationErrors);
            if (!validationErrors.isEmpty()) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(responseinfo, true, batchfilename);
                return response("Error", "Validation errors: " + String.join(", ", validationErrors));
            } else if (!responseinfo.contains("Success")) {
                batchfilename = getbatchfilename("FailedUpload");
                helper.logBatchFile(responseinfo, true, batchfilename);
                return response("Error", responseinfo);
            } else {
                return response("Success", "Complete");
            }
        } catch (NumberFormatException | JSONException exc) {
            loggger.info("Exception |  " + exc.toString());
            return response("Error", exc.getMessage());
        }
    }

   // Approve/ Reject PoItem Addition
   @PutMapping(value = "/createPoItem/{poNumber}/{recordNo}/{action}")
   @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
   public Map<String, String> processPoItem(@PathVariable String poNumber, @PathVariable Long recordNo, @PathVariable String action) {
    logger.info("START: {} Addition request for PO Number: {} and Record No: {}", action, poNumber, recordNo);
    Map<String, String> response = new HashMap<>();
    try {
        // Fetch the specific PO item by poNumber and recordNo
        logger.info("Fetching PO item for PO Number: {} and Record No: {}", poNumber, recordNo);
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            logger.warn("No PO item found for PO Number: {} and Record No: {}", poNumber, recordNo);
            response.put("status", "Error");
            response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " does not exist");
            return response;
        }
        // Check if the PO item is in "Pending Modification" status
        if (!"Pending Modification".equals(poItem.getApproval_Status())) {
            logger.warn("PO item with Record No: {} is not in 'Pending Modification' status.", recordNo);
            response.put("status", "Error");
            response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " is not Pending Addition");
            return response;
        }
        // Fetch the latest workflow entry for the given PO number
        logger.info("Fetching workflow entry for PO Number: {}", poNumber);
        Workflow workflow = workflowRepository.findTopByPoNumberOrderByInsertDateDesc(poNumber);
        if (workflow == null) {
            logger.warn("No workflow entry found for PO Number: {}", poNumber);
            response.put("status", "Error");
            response.put("message", "No existing workflow entry found for PO number " + poNumber);
            return response;
        }
        // Determine the action (approve or reject)
        if ("approve".equalsIgnoreCase(action)) {
            // Approve the PO item
            if ("Pending Modification".equals(workflow.getOriginalStatus())) {
                logger.info("Updating workflow entry to 'Approved' for PO Number: {}", poNumber);
                workflow.setUpdatedStatus("Modification Approved");
                workflow.setChangedBy("System"); // Replace with actual user if available
                workflow.setChangeDate(new Date());
                workflowRepository.save(workflow);
                logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
                poItem.setApproval_Status("Approved");
                poRepo.save(poItem);
                response.put("status", "Success");
                response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " addition request approved successfully.");
            } else {
                logger.warn("Workflow for PO Number: {} is not in 'Pending Modification' status. Cannot approve addition.", poNumber);
                response.put("status", "Error");
                response.put("message", "The workflow's original status is not 'Pending Modification'. Cannot approve addition.");
                return response;
            }
        } else if ("reject".equalsIgnoreCase(action)) {
            // Reject the PO item
            if ("Pending Modification".equals(workflow.getOriginalStatus())) {
                logger.info("Updating workflow entry to 'Addition Rejected' for PO Number: {}", poNumber);
                workflow.setUpdatedStatus("Addition Rejected");
                workflow.setChangedBy("System"); // Replace with actual user if available
                workflow.setChangeDate(new Date());
                workflowRepository.save(workflow);
                logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
                // Delete the PO item if rejected
                logger.info("Deleting PO item with Record No: {}", recordNo);
                poRepo.delete(poItem);
                response.put("status", "Success");
                response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " rejected and deleted successfully.");
            } else {
                logger.warn("Workflow for PO Number: {} is not in 'Pending Addition' status. Cannot reject addition.", poNumber);
                response.put("status", "Error");
                response.put("message", "The workflow's original status is not 'Pending Addition'. Cannot reject addition.");
                return response;
            }
        } else {
            logger.warn("Invalid action: {} provided", action);
            response.put("status", "Error");
            response.put("message", "Invalid action: " + action + ". Expected 'approve' or 'reject'.");
            return response;
        }
        logger.info("SUCCESS: {} Addition request completed for PO Number: {} and Record No: {}", action, poNumber, recordNo);
    } catch (Exception ex) {
        logger.error("EXCEPTION: Error occurred while processing addition for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
        response.put("status", "Error");
        response.put("message", "Failed to process addition of PO item: " + ex.getMessage());
    }
    return response;
}

private String getbatchfilename(String filetype) {
    String batchfilename = filetype + "_" + System.currentTimeMillis() + ".json";
    return batchfilename;
}
private Map<String, String> response(String status, String message) {
    Map<String, String> response = new HashMap<>();
    response.put("status", status);
    response.put("message", message);
    return response;
}

// //update poItem
// @PutMapping(value = "/updatePoItem")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> updatePoItem(@RequestBody String req) throws ParseException {
//     String batchfilename = "";
//     loggger.info("PO UPDATE REQUEST |  " + req);
//     try {
//         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
//         JSONArray jsonArray = new JSONArray(req);
//         String responseinfo = "Failed to update or data";
//         List<String> validationErrors = new ArrayList<>();
//         for (int i = 0; i < jsonArray.length(); i++) {
//             JSONObject jsonObject = jsonArray.getJSONObject(i);
//             String poNumber = jsonObject.getString("poNumber").trim();
//             long recordNo = jsonObject.getLong("recordNo");
//             // Check if the poNumber exists in tb_PONumber
//             tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
//             if (existsPoNumber == null) {
//                 validationErrors.add("poNumber does not exist in tb_PONumber: " + poNumber);
//                 continue; // Skip this record
//             }
//             // Check if the record exists in tb_Po
//             tb_Po spldt = poRepo.findByRecordNo(recordNo);
//             if (spldt == null) {
//                 validationErrors.add("Record does not exist for recordNo: " + recordNo);
//                 continue; // Skip this record
//             }
//             // Ensure the poNumber is not altered
//             if (!spldt.getPoNumber().equals(poNumber)) {
//                 validationErrors.add("Cannot alter poNumber for existing record: " + spldt.getPoNumber());
//                 continue;
//             }
//             // Update other fields (excluding poNumber)
//             spldt.setModelNumber(jsonObject.getString("modelNumber").trim());
//             spldt.setUom(jsonObject.getString("uom"));
//             spldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
//             spldt.setTotalNumberOfSites(jsonObject.getInt("totalNumberOfSites"));
//             spldt.setTotalQty(jsonObject.getInt("totalQty"));
//             spldt.setAccumulatedDepreciation(jsonObject.getDouble("accumulatedDepreciation"));
//             spldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
//             spldt.setFaCategoryNew(jsonObject.getString("faCategoryNew").trim());
//             spldt.setL1(jsonObject.getString("l1"));
//             spldt.setL2(jsonObject.getString("l2").trim());
//             spldt.setL3(jsonObject.getString("l3").trim());
//             spldt.setL4(jsonObject.getString("l4"));
//             spldt.setOldFaCategory(jsonObject.getString("oldFaCategory").trim());
//             spldt.setAccumulatedDepreciationCode(jsonObject.getString("accumulatedDepreciationCode"));
//             spldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
//             spldt.setLifeYearsNew(jsonObject.getInt("lifeYearsNew"));
//             spldt.setVendorName(jsonObject.getString("vendorName"));
//             spldt.setVendorNumber(jsonObject.getString("vendorNumber"));
//             spldt.setProjectNumber(jsonObject.getString("projectNumber"));
//             String datePlacedInService = jsonObject.getString("datePlacedInService");
//             String poDate = jsonObject.getString("poDate");
//             try {
//                 java.util.Date parsedDate = dateFormat.parse(datePlacedInService);
//                 java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
//                 java.util.Date newDate = dateFormat.parse(poDate);
//                 java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
//                 spldt.setDatePlacedInService(sqlDate);
//                 spldt.setPoDate(sqlcreatedDate);
//             } catch (ParseException ex) {
//                 // java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
//             }
//             spldt.setCurrency(jsonObject.getString("currency"));
//             spldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
//             spldt.setPoLine(jsonObject.getInt("poLine"));
//             spldt.setLevel1Description(jsonObject.getString("level1Description"));
//             spldt.setApproval_Status("Pending Modification");
//             spldt.setPartNumber(jsonObject.getString("partNumber"));
//             spldt.setL3Description(jsonObject.getString("level1Description"));
//             spldt.setCostCenter(jsonObject.getString("costCenter").trim());
//             spldt.setUpdatedBy(jsonObject.getString("updatedBy").trim());
//             try {
//                 poRepo.save(spldt);
//                 responseinfo = "Record Updated Successfully";
//             // Create workflow request
//             Workflow workflow = new Workflow();
//             workflow.setPoNumber(spldt.getPoNumber());
//             workflow.setOriginalStatus(spldt.getApproval_Status());
//             workflow.setProcessId(generateProcessId());
//             workflow.setInsertedBy("System"); 
//             workflow.setInsertDate(new Date());
//             workflowRepository.save(workflow);
//             } catch (Exception excc) {
//                 loggger.info("Exception |  " + excc.toString());
//                 responseinfo = excc.toString();
//             }
//         }
//         loggger.info("PO UPDATE RESPONSE |  " + responseinfo);
//         loggger.info("VALIDATION RESPONSE |  " + validationErrors);
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
//         loggger.info("Exception |  " + exc.toString());
//         return response("Error", exc.getMessage());
//     }
// }

//APPROVE UPDATE

// @PutMapping(value = "/updatePoItem/{poNumber}/{recordNo}")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> approvePutPoItem(@PathVariable String poNumber, @PathVariable Long recordNo) {
//     logger.info("START: Approving Modification request for PO Number: {} and Record No: {}", poNumber, recordNo);
//     Map<String, String> response = new HashMap<>();
//     try {
//         // Fetch the specific PO item by poNumber and recordNo
//         logger.info("Fetching PO item for PO Number: {} and Record No: {}", poNumber, recordNo);
//         tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
//         if (poItem == null) {
//             logger.warn("No PO item found for PO Number: {} and Record No: {}", poNumber, recordNo);
//             response.put("status", "Error");
//             response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " does not exist");
//             return response;
//         }
//         // Check if the PO item is pending deletion
//         if (!"Pending Modification".equals(poItem.getApproval_Status())) {
//             logger.warn("PO item with Record No: {} is not in 'Pending Modification' status.", recordNo);
//             response.put("status", "Error");
//             response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " is not Pending Modification");
//             return response;
//         }
//         // Fetch the latest workflow entry for the given PO number
//         logger.info("Fetching workflow entry for PO Number: {}", poNumber);
//         Workflow workflow = workflowRepository.findTopByPoNumberOrderByInsertDateDesc(poNumber);
//         if (workflow == null) {
//             logger.warn("No workflow entry found for PO Number: {}", poNumber);
//             response.put("status", "Error");
//             response.put("message", "No existing workflow entry found for PO number " + poNumber);
//             return response;
//         }
//         // Ensure that the workflow entry's original status is "pending deletion"
//         if ("Pending Modification".equals(workflow.getOriginalStatus())) {
//             logger.info("Updating workflow entry to 'Modification Approved' for PO Number: {}", poNumber);
//             workflow.setUpdatedStatus(" Modification Approved");
//             workflow.setChangedBy("System"); // Replace with actual user if available
//             workflow.setChangeDate(new Date());
//             workflow.setComments("Modification Approved for PO item with Record No: " + recordNo);
//             workflowRepository.save(workflow);
//             logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
//         } else {
//             logger.warn("Workflow for PO Number: {} is not in 'Pending Modification' status. Cannot approve Modification.", poNumber);
//             response.put("status", "Error");
//             response.put("message", "The workflow's original status is not 'Pending Modification'. Cannot approve Modification.");
//             return response;
//         }
//         poItem.setApproval_Status("Approved");
//         poRepo.save(poItem);
//         logger.info("approved Modification for PO item with Record No: {}", recordNo);
//         logger.info("SUCCESS: Approval of Modification request completed for PO Number: {} and Record No: {}", poNumber, recordNo);
//         response.put("status", "Success");
//         response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " Modification request approved successfully.");
//     } catch (Exception ex) {
//         logger.error("EXCEPTION: Error occurred while approving Modification for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
//         response.put("status", "Error");
//         response.put("message", "Failed to approve Modification of PO item: " + ex.getMessage());
//     }
//     return response;
// }


//DELETE PO NUMBER

// Request deletion of Po Number
@PostMapping(value = "/deletePoNumber/{poNumber}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> requestDeletePoNum(@PathVariable String poNumber) {
    logger.info("PO NUMBER DELETE REQUEST | poNumber: " + poNumber);
    Map<String, String> response = new HashMap<>();
    try {
        // Check if the PO number exists in the tb_PoNumber table
        tbPoNumber poNumberEntry = poNumberRepo.findByPoNumber(poNumber);
        if (poNumberEntry == null) {
            response.put("status", "Error");
            response.put("message", "PO number " + poNumber + " does not exist");
            return response;
        }
        // Update the approval status of the PO number to "Pending Deletion"
        poNumberEntry.setApprovalStatus("Pending Deletion");
        poNumberRepo.save(poNumberEntry);
        // Create a new Workflow entry for the deletion request
        Workflow workflow = new Workflow();
        workflow.setPoNumber(poNumber);  
        workflow.setOriginalStatus("Pending Deletion");  
        workflow.setProcessId(generateProcessId());  
        workflow.setInsertedBy("System");  
        workflow.setInsertDate(new Date()); 
        workflowRepository.save(workflow);
        response.put("status", "Success");
        response.put("message", "PO number " + poNumber + " is pending deletion");
    } catch (Exception ex) {
        logger.error("Exception | ", ex);
        response.put("status", "Error");
        response.put("message", "Failed to request deletion of PO number: " + ex.getMessage());
    }
    return response;
}

//Approve/reject ponumber deletion
@PostMapping(value = "/deletePoNumber/{poNumber}/{action}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional
public Map<String, String> deletePoItem(@PathVariable String poNumber, @PathVariable String action) {
    logger.info("START: Deletion request for PO Number: {} with action: {}", poNumber, action);
    Map<String, String> response = new HashMap<>();
    try {
        // Fetch the PO number entry
        logger.info("Fetching PO Number entry for PO Number: {}", poNumber);
        tbPoNumber poNumberEntry = poNumberRepo.findByPoNumber(poNumber);
        if (poNumberEntry == null) {
            logger.warn("No PO Number entry found for PO Number: {}", poNumber);
            response.put("status", "Error");
            response.put("message", "PO Number " + poNumber + " does not exist");
            return response;
        }
        // Check if the PO number is in 'Pending Deletion' status
        if (!"Pending Deletion".equals(poNumberEntry.getApprovalStatus())) {
            logger.warn("PO Number: {} is not in 'Pending Deletion' status.", poNumber);
            response.put("status", "Error");
            response.put("message", "PO Number " + poNumber + " is not pending deletion");
            return response;
        }
        // Fetch the latest workflow entry for the given PO number
        logger.info("Fetching workflow entry for PO Number: {}", poNumber);
        Workflow workflow = workflowRepository.findTopByPoNumberOrderByInsertDateDesc(poNumber);
        if (workflow == null) {
            logger.warn("No workflow entry found for PO Number: {}", poNumber);
            response.put("status", "Error");
            response.put("message", "No existing workflow entry found for PO number " + poNumber);
            return response;
        }
        // Handle approval or rejection based on the action parameter
        if ("approve".equalsIgnoreCase(action)) {
            // Approve deletion
            logger.info("Approving deletion for PO Number: {}", poNumber);
            // Update the workflow status to "Deletion Approved"
            workflow.setUpdatedStatus("Deletion Approved");
            workflow.setChangedBy("System");  // Replace with actual user if available
            workflow.setChangeDate(new Date());
            workflow.setComments("PO Number " + poNumber + " approved for deletion.");
            workflowRepository.save(workflow);
            // Delete the PO number entry and associated PO items
            List<tb_Po> poItems = poRepo.findAllByPoNumber(poNumber);
            if (!poItems.isEmpty()) {
                logger.info("Deleting " + poItems.size() + " PO items associated with PO Number: " + poNumber);
                poRepo.deleteByPoNumber(poNumber);  // Delete all PO items
            }
            poNumberRepo.delete(poNumberEntry);  // Delete the PO number entry
            response.put("status", "Success");
            response.put("message", "PO Number " + poNumber + " and all associated PO items deleted successfully.");
        } else if ("reject".equalsIgnoreCase(action)) {
            // Reject deletion
            logger.info("Rejecting deletion for PO Number: {}", poNumber);
            // Update the workflow status to "Deletion Rejection"
            workflow.setUpdatedStatus("Deletion Rejection");
            workflow.setChangedBy("System");  // Replace with actual user if available
            workflow.setChangeDate(new Date());
            workflow.setComments("Deletion Rejected for PO Number: " + poNumber);
            workflowRepository.save(workflow);
            // Update the approval status of the PO number entry to "Approved"
            logger.info("Reverting approval status for PO Number: {}", poNumber);
            poNumberEntry.setApprovalStatus("Approved");
            poNumberRepo.save(poNumberEntry);
            response.put("status", "Success");
            response.put("message", "PO Number " + poNumber + " deletion request rejected successfully.");
        } else {
            response.put("status", "Error");
            response.put("message", "Invalid action. Use 'approve' or 'reject'.");
        }
        logger.info("SUCCESS: Deletion request completed for PO Number: {}", poNumber);
    } catch (Exception ex) {
        logger.error("EXCEPTION: Error occurred while processing the deletion request for PO Number: {}. Error: {}", poNumber, ex.getMessage(), ex);
        response.put("status", "Error");
        response.put("message", "Failed to process deletion request for PO Number: " + ex.getMessage());
    }
    return response;
}


private String generateProcessId() {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String randomDigit = String.valueOf((int) (Math.random() * 10));
    // Use the last 8 digits of the timestamp to ensure the processId is always unique
    return timestamp.substring(timestamp.length() - 6) + randomDigit;
}

//DELETE PO ITEM 

// Request deletion of po item
@PostMapping(value = "/deletePoItem/{recordNo}")
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
        // Update the approval status to "pending deletion"
        poItem.setApproval_Status("Pending Deletion");
        poRepo.save(poItem);
        // Create a new Workflow entry
        Workflow workflow = new Workflow();
        workflow.setPoNumber(poItem.getPoNumber());
        workflow.setOriginalStatus(poItem.getApproval_Status());
        workflow.setProcessId(generateProcessId()); 
        workflow.setInsertedBy("System"); // or get the current user
        workflow.setInsertDate(new Date());
        workflowRepository.save(workflow);
        response.put("status", "Success");
        response.put("message", "PO item with recordNo " + recordNo + " is pending deletion");
    } catch (Exception ex) {
        logger.error("Exception | ", ex);
        response.put("status", "Error");
        response.put("message", "Failed to request deletion of PO item: " + ex.getMessage());
    }
    return response;
}

//Reject/Approve deletion of poitem
@PostMapping(value = "/deletePoItem/{poNumber}/{recordNo}/{action}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> deletePoItem(@PathVariable String poNumber, @PathVariable Long recordNo, @PathVariable String action) {
    logger.info("START: {} deletion request for PO Number: {} and Record No: {}", action, poNumber, recordNo);
    Map<String, String> response = new HashMap<>();
    try {
        // Fetch the specific PO item by poNumber and recordNo
        logger.info("Fetching PO item for PO Number: {} and Record No: {}", poNumber, recordNo);
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            logger.warn("No PO item found for PO Number: {} and Record No: {}", poNumber, recordNo);
            response.put("status", "Error");
            response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " does not exist");
            return response;
        }
        // Check if the PO item is pending deletion
        if (!"Pending Deletion".equals(poItem.getApproval_Status())) {
            logger.warn("PO item with Record No: {} is not in 'Pending Deletion' status.", recordNo);
            response.put("status", "Error");
            response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " is not pending deletion");
            return response;
        }
        // Fetch the latest workflow entry for the given PO number
        logger.info("Fetching workflow entry for PO Number: {}", poNumber);
        Workflow workflow = workflowRepository.findTopByPoNumberOrderByInsertDateDesc(poNumber);
        if (workflow == null) {
            logger.warn("No workflow entry found for PO Number: {}", poNumber);
            response.put("status", "Error");
            response.put("message", "No existing workflow entry found for PO number " + poNumber);
            return response;
        }
        // Ensure that the workflow entry's original status is "pending deletion"
        if ("Pending Deletion".equals(workflow.getOriginalStatus())) {
            if ("approve".equalsIgnoreCase(action)) {
                // Approve Deletion
                logger.info("Approving deletion for PO Number: {} and Record No: {}", poNumber, recordNo);
                // Update the workflow status to "Approved Deletion"
                workflow.setUpdatedStatus("Approved Deletion");
                workflow.setChangedBy("System");  // Replace with actual user if available
                workflow.setChangeDate(new Date());
                workflow.setComments("Deletion approved for PO item with Record No: " + recordNo);
                workflowRepository.save(workflow);
                logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
                // Delete the specific PO item
                logger.info("Deleting PO item with Record No: {}", recordNo);
                poRepo.delete(poItem);
                logger.info("Successfully deleted PO item with Record No: {}", recordNo);
                response.put("status", "Success");
                response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " deleted successfully.");
            } else if ("reject".equalsIgnoreCase(action)) {
                // Reject Deletion
                logger.info("Rejecting deletion for PO Number: {} and Record No: {}", poNumber, recordNo); 
                // Update the workflow status to "Deletion Rejection"
                workflow.setUpdatedStatus("Deletion Rejection");
                workflow.setChangedBy("System");  // Replace with actual user if available
                workflow.setChangeDate(new Date());
                workflow.setComments("Deletion Rejected for PO item with Record No: " + recordNo);
                workflowRepository.save(workflow);
                logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
                // Update the approval status of the specific PO item to "Approved"
                logger.info("Reverting approval status for PO item with Record No: {}", recordNo);
                poItem.setApproval_Status("Approved");
                poRepo.save(poItem);
                logger.info("Rejected deletion for PO item with Record No: {}", recordNo);

                response.put("status", "Success");
                response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " deletion request rejected successfully.");
            } else {
                response.put("status", "Error");
                response.put("message", "Invalid action. Use 'approve' or 'reject'.");
            }
        } else {
            logger.warn("Workflow for PO Number: {} is not in 'pending deletion' status. Cannot process deletion.", poNumber);
            response.put("status", "Error");
            response.put("message", "The workflow's original status is not 'pending deletion'. Cannot process deletion.");
        }
    } catch (Exception ex) {
        logger.error("EXCEPTION: Error occurred while processing deletion for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
        response.put("status", "Error");
        response.put("message", "Failed to process deletion for PO item: " + ex.getMessage());
    }
    return response;
}


//UPDATE

// Update Request
@PutMapping(value = "/updatePoItem/{recordNo}")
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
            return response("Error", "Record does not exist for recordNo: " + recordNo);
        }

        // Ensure the poNumber is not altered
        String poNumber = jsonObject.getString("poNumber").trim();
        if (!existingPo.getPoNumber().equals(poNumber)) {
            return response("Error", "Cannot alter poNumber for recordNo: " + recordNo);
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
                workflow.setOriginalStatus(modification.getApproval_Status());
                workflow.setProcessId(generateProcessId());
                workflow.setInsertedBy("System"); 
                workflow.setInsertDate(new Date());
                workflowRepository.save(workflow);

                logger.info("Workflow entry saved successfully for PO Number: {}", existingPo.getPoNumber());
            } catch (Exception e) {
                logger.error("Error saving workflow entry: {}", e.getMessage(), e);
                return response("Error", "Failed to save workflow entry for PO Number: " + existingPo.getPoNumber());
            }

            logger.info("Modification request saved for PO Number: {}, Record No: {}", poNumber, recordNo);
            return response("Success", "Modification request submitted successfully for approval.");
        } catch (Exception e) {
            logger.error("Error saving modification request: {}", e.getMessage(), e);
            return response("Error", "Failed to save modification for recordNo: " + recordNo);
        }
    } catch (Exception e) {
        logger.error("Exception | {}", e.toString(), e);
        return response("Error", "Failed to process update request: " + e.getMessage());
    }
}

//Approval Update
@PutMapping(value = "/approvePoItem/{poNumber}/{recordNo}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional 
public Map<String, String> approvePoItem(@PathVariable String poNumber, @PathVariable Long recordNo) {
    logger.info("START: Approving Modification request for PO Number: {} and Record No: {}", poNumber, recordNo);
    Map<String, String> response = new HashMap<>();
    try {
        // Fetch the modification request
        tb_Po_Modification modification = poModificationRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (modification == null) {
            logger.warn("No modification request found for PO Number: {} and Record No: {}", poNumber, recordNo);
            response.put("status", "Error");
            response.put("message", "No modification request found for poNumber " + poNumber + " and recordNo " + recordNo);
            return response;
        }
        logger.info("Fetched modification request: {}", modification);
        // Fetch the corresponding PO item
        tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
        if (poItem == null) {
            logger.warn("No PO item found for PO Number: {} and Record No: {}", poNumber, recordNo);
            response.put("status", "Error");
            response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " does not exist");
            return response;
        }
        logger.info("Fetched PO item: {}", poItem);
        // Ensure modification is pending
        if (!"Pending Modification".equals(modification.getApproval_Status())) {
            logger.warn("Modification request for PO Number: {} is not in 'Pending Modification' status.", poNumber);
            response.put("status", "Error");
            response.put("message", "Modification request is not pending approval");
            return response;
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
        logger.info("Updated PO Item: {}", poItem);
        // Update workflow status
        Workflow workflow = workflowRepository.findTopByPoNumberOrderByInsertDateDesc(poNumber);
        if (workflow != null) {
            workflow.setUpdatedStatus("Modification Approved");
            workflow.setChangedBy("System"); // Replace with actual user if available
            workflow.setChangeDate(new Date());
            workflow.setComments("Modification approved for PO item with Record No: " + recordNo);
            workflowRepository.save(workflow);
            logger.info("Updated workflow: {}", workflow);
        }
        // Delete the modification request
        poModificationRepo.delete(modification);
        logger.info("Deleted modification request for PO Number: {} and Record No: {}", poNumber, recordNo);

        logger.info("SUCCESS: Modification request approved and applied for PO Number: {} and Record No: {}", poNumber, recordNo);
        response.put("status", "Success");
        response.put("message", "Modification request approved and applied successfully.");
    } catch (Exception ex) {
        logger.error("EXCEPTION: Error occurred while approving modification for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
        response.put("status", "Error");
        response.put("message", "Failed to approve modification: " + ex.getMessage());
    }
    return response;
}

// Reject Update
@DeleteMapping(value = "/rejectPoModification/{recordNo}/{workflowId}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional
public Map<String, String> rejectPoModification(@PathVariable long recordNo, @PathVariable Long workflowId) {
    logger.info("PO MODIFICATION REJECTION REQUEST | Record No: {}, Workflow ID: {}", recordNo, workflowId);
    Map<String, String> response = new HashMap<>();
    try {
        // Fetch the modification request
        tb_Po_Modification modification = poModificationRepo.findByRecordNo(recordNo);
        if (modification == null) {
            logger.error("Modification request not found for Record No: {}", recordNo);
            return response("Error", "Modification request not found for Record No: " + recordNo);
        }
        // Fetch the original PO record
        tb_Po originalPo = poRepo.findByRecordNo(recordNo);
        if (originalPo == null) {
            logger.error("Original PO record not found for Record No: {}", recordNo);
            return response("Error", "Original PO record not found for Record No: " + recordNo);
        }
        // Fetch the specific workflow entry by ID
        Workflow workflow = workflowRepository.findById(workflowId).orElse(null);
        if (workflow == null) {
            logger.error("Workflow entry not found for Workflow ID: {}", workflowId);
            return response("Error", "Workflow entry not found for Workflow ID: " + workflowId);
        }
        // Delete the modification request
        poModificationRepo.delete(modification);
        logger.info("Modification request deleted for Record No: {}", recordNo);
        // Update the original PO's approval status to "Modification Rejected"
        originalPo.setApproval_Status("Modification Rejected");
        poRepo.save(originalPo);
        logger.info("Original PO status updated to 'Modification Rejected' for Record No: {}", recordNo);
        // Update the workflow entry directly
        workflow.setUpdatedStatus("Rejected Modification");
        workflow.setChangedBy("System");
        workflow.setChangeDate(new Date());
        workflow.setComments("Modification request rejected for Record No: " + recordNo);
        workflowRepository.save(workflow);
        logger.info("Workflow updated successfully for Workflow ID: {}", workflowId);
        return response("Success", "Modification request rejected successfully.");
    } catch (Exception e) {
        logger.error("Exception occurred: {}", e.getMessage(), e);
        return response("Error", "Failed to reject modification request: " + e.getMessage());
    }
}
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}



}
