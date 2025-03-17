
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

@RestController
public class BarhainController {

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
   

//addition request of poNumber if it doesnt already exist
 @PostMapping(value = "/poNumbers")
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


//addition request of poItem
@PostMapping(value = "/poItems")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> createPo(@RequestBody String req) throws ParseException {
        String batchfilename = "";
        LocalDateTime now = LocalDateTime.now();
        loggger.info("PO CREATE REQUEST |  " + req);
        Map<String, String> response = new HashMap<>();
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
                String modelNumber = jsonObject.getString("modelNumber").trim();
                if (poRepo.existsByModelNumber(modelNumber)) {
                    validationErrors.add("Model number '" + modelNumber + "' already exists.");
                    continue; // Skip this record
                }
                // Create new record in tb_Po
                tb_Po nwspldt = new tb_Po();
                nwspldt.setPoNumber(poNumber);
                nwspldt.setModelNumber(modelNumber);
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
                nwspldt.setApproval_Status("Pending Addition"); // Set initial status to Pending
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

//deletion request of poNumber
@DeleteMapping(value = "/poNumbers/{poNumber}")
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
private String generateProcessId() {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String randomDigit = String.valueOf((int) (Math.random() * 10));
    // Use the last 8 digits of the timestamp to ensure the processId is always unique
    return timestamp.substring(timestamp.length() - 6) + randomDigit;
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
// Update Request for poItem
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


}
