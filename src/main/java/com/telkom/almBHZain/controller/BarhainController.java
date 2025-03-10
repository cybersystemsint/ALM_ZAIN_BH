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

import javax.transaction.Transactional;

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
   

    //=================================NEW PO END POINT FOR BARHAIN ====
//     @PostMapping(value = "/createPo")
//     @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
//     public Map<String, String> createPo(@RequestBody String req) throws ParseException, ParseException, ParseException {
//         String batchfilename = "";
//         LocalDateTime now = LocalDateTime.now();
//         long recordNo = 0;
//         loggger.info("PO CREATE REQUEST |  " + req);
//         try {
//             SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
//             JSONArray jsonArray = new JSONArray(req);
//             String responseinfo = "Failed to save or data";

//             List<String> validationErrors = new ArrayList<>();
//             for (int i = 0; i < jsonArray.length(); i++) {
//                 JSONObject jsonObject = jsonArray.getJSONObject(i);
//                 recordNo = Integer.parseInt(jsonObject.getString("recordNo"));

//                 tb_Po spldt = poRepo.findByRecordNo(recordNo);
//                 if (spldt != null) {

//                     spldt.setPoNumber(jsonObject.getString("poNumber").trim());
//                     spldt.setModelNumber(jsonObject.getString("modelNumber").trim());
//                     spldt.setUom(jsonObject.getString("uom"));
//                     spldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
//                     spldt.setTotalNumberOfSites(jsonObject.getInt("totalNumberOfSites"));
//                     spldt.setTotalQty(jsonObject.getInt("totalQty"));
//                     spldt.setAccumulatedDepreciation(jsonObject.getDouble("accumulatedDepreciation"));
//                     spldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
//                     spldt.setFaCategoryNew(jsonObject.getString("faCategoryNew").trim());
//                     spldt.setL1(jsonObject.getString("l1"));
//                     spldt.setL2(jsonObject.getString("l2").trim());
//                     spldt.setL3(jsonObject.getString("l3").trim());
//                     spldt.setL4(jsonObject.getString("l4"));
//                     spldt.setOldFaCategory(jsonObject.getString("oldFaCategory").trim());
//                     spldt.setAccumulatedDepreciationCode(jsonObject.getString("accumulatedDepreciationCode"));
//                     spldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
//                     spldt.setLifeYearsNew(jsonObject.getInt("lifeYearsNew"));
//                     spldt.setVendorName(jsonObject.getString("vendorName"));
//                     spldt.setVendorNumber(jsonObject.getString("vendorNumber"));
//                     spldt.setProjectNumber(jsonObject.getString("projectNumber"));
//                     String datePlacedInService = jsonObject.getString("datePlacedInService");
//                     String poDate = jsonObject.getString("poDate");
//                     try {
//                         java.util.Date parsedDate = dateFormat.parse(datePlacedInService);
//                         java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
// //                        java.util.Date today = dateFormat.parse(now.toString());
// //                        java.sql.Date createdDate = new java.sql.Date(today.getTime());
//                         java.util.Date newDate = dateFormat.parse(poDate);
//                         java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
//                         spldt.setDatePlacedInService(sqlDate);
//                         spldt.setPoDate(sqlcreatedDate);
//                     } catch (ParseException ex) {
//                         java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
//                     }
//                     spldt.setCurrency(jsonObject.getString("currency"));
//                     spldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
//                     spldt.setPoLine(jsonObject.getInt("poLine"));
//                     spldt.setLevel1Description(jsonObject.getString("level1Description"));
//                     spldt.setPartNumber(jsonObject.getString("partNumber"));
//                     spldt.setL3Description(jsonObject.getString("level1Description"));
//                     spldt.setCostCenter(jsonObject.getString("costCenter").trim());
//                     spldt.setUpdatedBy(jsonObject.getString("updatedBy").trim());

//                     try {
//                         poRepo.save(spldt);
//                         responseinfo = "Record Updated Successfully";
//                     } catch (Exception excc) {
//                         loggger.info("Exception |  " + excc.toString());
//                         responseinfo = excc.toString();
//                     }
//                 } else {

//                     tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(jsonObject.getString("poNumber"));
//                     String createdpoNum = existsPoNumber != null ? String.valueOf(existsPoNumber.getPoNumber()) : "";

//                     tb_Po topRecord = poRepo.findTopByPoNumberAndPoLine(jsonObject.getString("poNumber"), jsonObject.getInt("poLine"));
//                     String poNum = topRecord != null ? String.valueOf(topRecord.getPoNumber()) : "";

//                     if (createdpoNum.length() < 1) {
//                         tbPoNumber newpo = new tbPoNumber();
//                         newpo.setPoNumber(jsonObject.getString("poNumber"));
//                         poNumberRepo.save(newpo);
//                     }

//                     if (poNum.length() < 1) {

//                         tb_Po nwspldt = new tb_Po();

//                         nwspldt.setPoNumber(jsonObject.getString("poNumber").trim());
//                         nwspldt.setModelNumber(jsonObject.getString("modelNumber").trim());
//                         nwspldt.setUom(jsonObject.getString("uom"));
//                         nwspldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
//                         nwspldt.setTotalNumberOfSites(jsonObject.getInt("totalNumberOfSites"));
//                         nwspldt.setTotalQty(jsonObject.getInt("totalQty"));
//                         nwspldt.setAccumulatedDepreciation(jsonObject.getDouble("accumulatedDepreciation"));
//                         nwspldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
//                         nwspldt.setFaCategoryNew(jsonObject.getString("faCategoryNew").trim());
//                         nwspldt.setL1(jsonObject.getString("l1"));
//                         nwspldt.setL2(jsonObject.getString("l2").trim());
//                         nwspldt.setL3(jsonObject.getString("l3").trim());
//                         nwspldt.setL4(jsonObject.getString("l4"));
//                         nwspldt.setOldFaCategory(jsonObject.getString("oldFaCategory").trim());
//                         nwspldt.setAccumulatedDepreciationCode(jsonObject.getString("accumulatedDepreciationCode"));
//                         nwspldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
//                         nwspldt.setLifeYearsNew(jsonObject.getInt("lifeYearsNew"));
//                         nwspldt.setVendorName(jsonObject.getString("vendorName"));
//                         nwspldt.setVendorNumber(jsonObject.getString("vendorNumber"));
//                         nwspldt.setProjectNumber(jsonObject.getString("projectNumber"));
//                         String datePlacedInService = jsonObject.getString("datePlacedInService");
//                         String poDate = jsonObject.getString("poDate");
//                         try {
//                             java.util.Date parsedDate = dateFormat.parse(datePlacedInService);
//                             java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
//                             java.util.Date today = dateFormat.parse(now.toString());
//                             java.sql.Date createdDate = new java.sql.Date(today.getTime());
//                             java.util.Date newDate = dateFormat.parse(poDate);
//                             java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
//                             nwspldt.setDatePlacedInService(sqlDate);
//                             nwspldt.setPoDate(sqlcreatedDate);
//                             nwspldt.setRecordDateTime(createdDate);
//                         } catch (ParseException ex) {
//                             java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
//                         }
//                         nwspldt.setCurrency(jsonObject.getString("currency"));
//                         nwspldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
//                         nwspldt.setPoLine(jsonObject.getInt("poLine"));
//                         nwspldt.setLevel1Description(jsonObject.getString("level1Description"));
//                         nwspldt.setPartNumber(jsonObject.getString("partNumber"));
//                         nwspldt.setL3Description(jsonObject.getString("level1Description"));
//                         nwspldt.setCostCenter(jsonObject.getString("costCenter").trim());
//                         nwspldt.setCreatedBy(jsonObject.getString("createdBy").trim());

//                         try {
//                             poRepo.save(nwspldt);
//                             responseinfo = "Record Created Success";

//                         } catch (JSONException excc) {

//                             loggger.info("Exception |  " + excc.toString());

//                             responseinfo = excc.toString();
//                         }

//                         // }
//                     } else {
//                         validationErrors.add(jsonObject.getString("poNumber") + " " + jsonObject.getInt("lineNumber"));
//                     }

//                 }

//             }
//             loggger.info("PO CREATE RESPONSE |  " + responseinfo);
//             loggger.info("VALIDATION RESPONSE |  " + validationErrors);
//             if (!validationErrors.isEmpty()) {
//                 batchfilename = getbatchfilename("FailedUpload");
//                 helper.logBatchFile(responseinfo, true, batchfilename);
//                 return response("Error", "PO numbers and Line Items: " + String.join(", ", validationErrors) + " are already uploaded. Duplicates not allowed");
//             } else if (!responseinfo.contains("Success")) {
//                 batchfilename = getbatchfilename("FailedUpload");
//                 helper.logBatchFile(responseinfo, true, batchfilename);
//                 return response("Error", responseinfo);
//             } else {
//                 return response("Success", "Complete");
//             }

//         } catch (NumberFormatException | JSONException exc) {
//             loggger.info("Exception |  " + exc.toString());
//             return response("Error", exc.getMessage());
//         }
//     }

//     private String getbatchfilename(String filetype) {
//         String batchfilename = filetype + "_" + System.currentTimeMillis() + ".json";
//         return batchfilename;
//     }


// create po Number if it doesnt already exist
// @PostMapping(value = "/createPONumber")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> createPONumber(@RequestBody String req) {
//     loggger.info("PO NUMBER CREATE REQUEST |  " + req);
//     try {
//         JSONArray jsonArray = new JSONArray(req);
//         List<String> validationErrors = new ArrayList<>();
//         String responseinfo = "Failed to save or data";

//         for (int i = 0; i < jsonArray.length(); i++) {
//             JSONObject jsonObject = jsonArray.getJSONObject(i);
//             String poNumber = jsonObject.getString("poNumber").trim();

//             // Check if the poNumber already exists
//             tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
//             if (existsPoNumber != null) {
//                 validationErrors.add("poNumber already exists: " + poNumber);
//                 continue;
//             }

//             // Create a new tbPoNumber entry
//             tbPoNumber newPoNumber = new tbPoNumber();
//             newPoNumber.setPoNumber(poNumber);

//             try {
//                 poNumberRepo.save(newPoNumber);
//                 responseinfo = "Record Created Successfully";
//             } catch (Exception ex) {
//                 loggger.info("Exception |  " + ex.toString());
//                 responseinfo = ex.toString();
//             }
//         }

//         loggger.info("PO NUMBER CREATE RESPONSE |  " + responseinfo);
//         loggger.info("VALIDATION RESPONSE |  " + validationErrors);

//         if (!validationErrors.isEmpty()) {
//             return response("Error", "Validation errors: " + String.join(", ", validationErrors));
//         } else if (!responseinfo.contains("Success")) {
//             return response("Error", responseinfo);
//         } else {
//             return response("Success", "Complete");
//         }
//     } catch (JSONException exc) {
//         loggger.info("Exception |  " + exc.toString());
//         return response("Error", exc.getMessage());
//     }
// }



// // create po item if po num already exist if not terminate
// @PostMapping(value = "/createPoItem")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> createPo(@RequestBody String req) throws ParseException {
//     String batchfilename = "";
//     LocalDateTime now = LocalDateTime.now();
//     loggger.info("PO CREATE REQUEST |  " + req);
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
//             nwspldt.setModelNumber(jsonObject.getString("modelNumber").trim());
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
//                 java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
//             }
//             nwspldt.setCurrency(jsonObject.getString("currency"));
//             nwspldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
//             nwspldt.setPoLine(jsonObject.getInt("poLine"));
//             nwspldt.setLevel1Description(jsonObject.getString("level1Description"));
//             nwspldt.setPartNumber(jsonObject.getString("partNumber"));
//             nwspldt.setL3Description(jsonObject.getString("level1Description"));
//             nwspldt.setApproval_Status(jsonObject.getString("Approval_Status"));
//             nwspldt.setCostCenter(jsonObject.getString("costCenter").trim());
//             nwspldt.setCreatedBy(jsonObject.getString("createdBy").trim());

//             try {
//                 poRepo.save(nwspldt);
//                 responseinfo = "Record Created Success";
//             } catch (Exception excc) {
//                 loggger.info("Exception |  " + excc.toString());
//                 responseinfo = excc.toString();
//             }
//         }

//         loggger.info("PO CREATE RESPONSE |  " + responseinfo);
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



    @PutMapping(value = "/createPoItem/{poNumber}/{recordNo}")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> approveAddPoItem(@PathVariable String poNumber, @PathVariable Long recordNo) {
        logger.info("START: Approving Addition request for PO Number: {} and Record No: {}", poNumber, recordNo);
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
            if (!"Pending Addition".equals(poItem.getApproval_Status())) {
                logger.warn("PO item with Record No: {} is not in 'Pending Addition' status.", recordNo);
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
            // Ensure that the workflow entry's original status is "pending deletion"
            if ("Pending Addition".equals(workflow.getOriginalStatus())) {
                logger.info("Updating workflow entry to 'Addition Approved' for PO Number: {}", poNumber);
                workflow.setUpdatedStatus("Addition Approved");
                workflow.setChangedBy("System"); // Replace with actual user if available
                workflow.setChangeDate(new Date());
                workflow.setComments("Addition Approved for PO item with Record No: " + recordNo);
                workflowRepository.save(workflow);
                logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
            } else {
                logger.warn("Workflow for PO Number: {} is not in 'Pending Addition' status. Cannot reject deletion.", poNumber);
                response.put("status", "Error");
                response.put("message", "The workflow's original status is not 'Pending Addition'. Cannot approve addition.");
                return response;
            }
           
            poItem.setApproval_Status("Approved");
            poRepo.save(poItem);
            logger.info("Rejected deletion for PO item with Record No: {}", recordNo);
    
            logger.info("SUCCESS: Approval of addition request completed for PO Number: {} and Record No: {}", poNumber, recordNo);
            response.put("status", "Success");
            response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " addition request approved successfully.");
        } catch (Exception ex) {
            logger.error("EXCEPTION: Error occurred while approving addition for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
            response.put("status", "Error");
            response.put("message", "Failed to approve addition of PO item: " + ex.getMessage());
        }
        return response;
    }

    @DeleteMapping(value = "/createPoItem/{poNumber}/{recordNo}")
    @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
    public Map<String, String> approveAddingPoItem(@PathVariable String poNumber, @PathVariable Long recordNo) {
        logger.info("START: Rejection Addition request for PO Number: {} and Record No: {}", poNumber, recordNo);
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
            if (!"Pending Addition".equals(poItem.getApproval_Status())) {
                logger.warn("PO item with Record No: {} is not in 'Pending Addition' status.", recordNo);
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
            // Ensure that the workflow entry's original status is "pending deletion"
            if ("Pending Addition".equals(workflow.getOriginalStatus())) {
                logger.info("Updating workflow entry to 'Addition Rejected' for PO Number: {}", poNumber);
                workflow.setUpdatedStatus("Addition Rejected");
                workflow.setChangedBy("System"); // Replace with actual user if available
                workflow.setChangeDate(new Date());
                workflow.setComments("Addition Rejected for PO item with Record No: " + recordNo);
                workflowRepository.save(workflow);
                logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
            } else {
                logger.warn("Workflow for PO Number: {} is not in 'Pending Addition' status. Cannot approve addition.", poNumber);
                response.put("status", "Error");
                response.put("message", "The workflow's original status is not 'Pending Addition'. Cannot approve addition.");
                return response;
            }
            // Delete the specific PO item
            logger.info("Deleting PO item with Record No: {}", recordNo);
            poRepo.delete(poItem);
            logger.info("Successfully Rejected adition of PO item with Record No: {}", recordNo);
            logger.info("SUCCESS: Rejection of Addition request completed for PO Number: {} and Record No: {}", poNumber, recordNo);
            response.put("status", "Success");
            response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " rejected successfully.");
        } catch (Exception ex) {
            logger.error("EXCEPTION: Error occurred while rejecting addition for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
            response.put("status", "Error");
            response.put("message", "Failed to reject deletion of PO item: " + ex.getMessage());
        }
        return response;
    }

// UPDATE








// pending addition workflow in tb po modification
// @PostMapping(value = "/createPoItem")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> createPo(@RequestBody String req) throws ParseException {
//     String batchfilename = "";
//     LocalDateTime now = LocalDateTime.now();
//     loggger.info("PO CREATE REQUEST |  " + req);
//     try {
//         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
//             // Create new record in tb_po_modification
//             tb_Po_Modification nwspldt = new tb_Po_Modification();
//             nwspldt.setPoNumber(poNumber);
//             nwspldt.setModelNumber(jsonObject.getString("modelNumber").trim());
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
//             } catch (ParseException ex) {
//                 // Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
//             }
//             nwspldt.setCurrency(jsonObject.getString("currency"));
//             nwspldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
//             nwspldt.setPoLine(jsonObject.getInt("poLine"));
//             nwspldt.setLevel1Description(jsonObject.getString("level1Description"));
//             nwspldt.setPartNumber(jsonObject.getString("partNumber"));
//             nwspldt.setL3Description(jsonObject.getString("level1Description"));
//             nwspldt.setApproval_Status("Pending Addition");
//             nwspldt.setCostCenter(jsonObject.getString("costCenter").trim());
//             nwspldt.setCreatedBy(jsonObject.getString("createdBy").trim());
//             try {
//                 poModificationRepo.save(nwspldt);
//                 responseinfo = "Record Created Success";
//                      //Create workflow request
//                      Workflow workflow = new Workflow();
//                      workflow.setPoNumber(nwspldt.getPoNumber());
//                      workflow.setOriginalStatus(nwspldt.getApproval_Status());
//                      workflow.setProcessId(generateProcessId());
//                      workflow.setInsertedBy("System"); 
//                      workflow.setInsertDate(new Date());
//                      workflowRepository.save(workflow);
//             } catch (Exception excc) {
//                 loggger.info("Exception |  " + excc.toString());
//                 responseinfo = excc.toString();
//             }
//         }
//         loggger.info("PO CREATE RESPONSE |  " + responseinfo);
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

//update poItem
@PutMapping(value = "/updatePoItem")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> updatePoItem(@RequestBody String req) throws ParseException {
    String batchfilename = "";
    loggger.info("PO UPDATE REQUEST |  " + req);
    try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
        JSONArray jsonArray = new JSONArray(req);
        String responseinfo = "Failed to update or data";
        List<String> validationErrors = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String poNumber = jsonObject.getString("poNumber").trim();
            long recordNo = jsonObject.getLong("recordNo");
            // Check if the poNumber exists in tb_PONumber
            tbPoNumber existsPoNumber = poNumberRepo.findByPoNumber(poNumber);
            if (existsPoNumber == null) {
                validationErrors.add("poNumber does not exist in tb_PONumber: " + poNumber);
                continue; // Skip this record
            }
            // Check if the record exists in tb_Po
            tb_Po spldt = poRepo.findByRecordNo(recordNo);
            if (spldt == null) {
                validationErrors.add("Record does not exist for recordNo: " + recordNo);
                continue; // Skip this record
            }
            // Ensure the poNumber is not altered
            if (!spldt.getPoNumber().equals(poNumber)) {
                validationErrors.add("Cannot alter poNumber for existing record: " + spldt.getPoNumber());
                continue;
            }
            // Update other fields (excluding poNumber)
            spldt.setModelNumber(jsonObject.getString("modelNumber").trim());
            spldt.setUom(jsonObject.getString("uom"));
            spldt.setQtyPerSite(jsonObject.getInt("qtyPerSite"));
            spldt.setTotalNumberOfSites(jsonObject.getInt("totalNumberOfSites"));
            spldt.setTotalQty(jsonObject.getInt("totalQty"));
            spldt.setAccumulatedDepreciation(jsonObject.getDouble("accumulatedDepreciation"));
            spldt.setSalvageValue(jsonObject.getDouble("salvageValue"));
            spldt.setFaCategoryNew(jsonObject.getString("faCategoryNew").trim());
            spldt.setL1(jsonObject.getString("l1"));
            spldt.setL2(jsonObject.getString("l2").trim());
            spldt.setL3(jsonObject.getString("l3").trim());
            spldt.setL4(jsonObject.getString("l4"));
            spldt.setOldFaCategory(jsonObject.getString("oldFaCategory").trim());
            spldt.setAccumulatedDepreciationCode(jsonObject.getString("accumulatedDepreciationCode"));
            spldt.setDepreciationCode(jsonObject.getString("depreciationCode"));
            spldt.setLifeYearsNew(jsonObject.getInt("lifeYearsNew"));
            spldt.setVendorName(jsonObject.getString("vendorName"));
            spldt.setVendorNumber(jsonObject.getString("vendorNumber"));
            spldt.setProjectNumber(jsonObject.getString("projectNumber"));
            String datePlacedInService = jsonObject.getString("datePlacedInService");
            String poDate = jsonObject.getString("poDate");
            try {
                java.util.Date parsedDate = dateFormat.parse(datePlacedInService);
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                java.util.Date newDate = dateFormat.parse(poDate);
                java.sql.Date sqlcreatedDate = new java.sql.Date(newDate.getTime());
                spldt.setDatePlacedInService(sqlDate);
                spldt.setPoDate(sqlcreatedDate);
            } catch (ParseException ex) {
                // java.util.logging.Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
            }
            spldt.setCurrency(jsonObject.getString("currency"));
            spldt.setUnitPrice(jsonObject.getDouble("unitPrice"));
            spldt.setPoLine(jsonObject.getInt("poLine"));
            spldt.setLevel1Description(jsonObject.getString("level1Description"));
            spldt.setApproval_Status("Pending Modification");
            spldt.setPartNumber(jsonObject.getString("partNumber"));
            spldt.setL3Description(jsonObject.getString("level1Description"));
            spldt.setCostCenter(jsonObject.getString("costCenter").trim());
            spldt.setUpdatedBy(jsonObject.getString("updatedBy").trim());
            try {
                poRepo.save(spldt);
                responseinfo = "Record Updated Successfully";
            // Create workflow request
            Workflow workflow = new Workflow();
            workflow.setPoNumber(spldt.getPoNumber());
            workflow.setOriginalStatus(spldt.getApproval_Status());
            workflow.setProcessId(generateProcessId());
            workflow.setInsertedBy("System"); 
            workflow.setInsertDate(new Date());
            workflowRepository.save(workflow);

            } catch (Exception excc) {
                loggger.info("Exception |  " + excc.toString());
                responseinfo = excc.toString();
            }
        }
        loggger.info("PO UPDATE RESPONSE |  " + responseinfo);
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

//APPROVE UPDATE

@PutMapping(value = "/updatePoItem/{poNumber}/{recordNo}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> approvePutPoItem(@PathVariable String poNumber, @PathVariable Long recordNo) {
    logger.info("START: Approving Modification request for PO Number: {} and Record No: {}", poNumber, recordNo);
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
        if (!"Pending Modification".equals(poItem.getApproval_Status())) {
            logger.warn("PO item with Record No: {} is not in 'Pending Modification' status.", recordNo);
            response.put("status", "Error");
            response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " is not Pending Modification");
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
        if ("Pending Modification".equals(workflow.getOriginalStatus())) {
            logger.info("Updating workflow entry to 'Modification Approved' for PO Number: {}", poNumber);
            workflow.setUpdatedStatus(" Approved");
            workflow.setChangedBy("System"); // Replace with actual user if available
            workflow.setChangeDate(new Date());
            workflow.setComments("Modification Approved for PO item with Record No: " + recordNo);
            workflowRepository.save(workflow);
            logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
        } else {
            logger.warn("Workflow for PO Number: {} is not in 'Pending Modification' status. Cannot approve Modification.", poNumber);
            response.put("status", "Error");
            response.put("message", "The workflow's original status is not 'Pending Modification'. Cannot approve Modification.");
            return response;
        }
       
        poItem.setApproval_Status("Approved");
        poRepo.save(poItem);
        logger.info("approved Modification for PO item with Record No: {}", recordNo);

        logger.info("SUCCESS: Approval of Modification request completed for PO Number: {} and Record No: {}", poNumber, recordNo);
        response.put("status", "Success");
        response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " Modification request approved successfully.");
    } catch (Exception ex) {
        logger.error("EXCEPTION: Error occurred while approving Modification for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
        response.put("status", "Error");
        response.put("message", "Failed to approve Modification of PO item: " + ex.getMessage());
    }
    return response;
}

//REJECT UPDATE
// @Transactional
// @PutMapping(value = "/updatePoItem/{poNumber}/{recordNo}")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> rejectPutPoItem(@PathVariable String poNumber, @PathVariable Long recordNo) {
//     logger.info("START: Rejecting Modification request for PO Number: {} and Record No: {}", poNumber, recordNo);
//     Map<String, String> response = new HashMap<>();

//     try {
//         // Fetch the latest workflow entry for rollback purposes
//         Workflow workflow = workflowRepository.findTopByPoNumberOrderByInsertDateDesc(poNumber);
//         if (workflow == null) {
//             logger.warn("No workflow entry found for PO Number: {}", poNumber);
//             response.put("status", "Error");
//             response.put("message", "No workflow history found for PO number " + poNumber);
//             return response;
//         }

//         // Fetch the latest APPROVED PO item before modification
//         tb_Po previousPoItem = poRepo.findByPoNumberAndApprovalStatus(poNumber, "Approved");
//         if (previousPoItem == null) {
//             logger.warn("No previously approved PO found for rollback.");
//             response.put("status", "Error");
//             response.put("message", "No previously approved PO item found for rollback.");
//             return response;
//         }

//         // Fetch the current PO item
//         tb_Po poItem = poRepo.findByPoNumberAndRecordNo(poNumber, recordNo);
//         if (poItem == null) {
//             logger.warn("No PO item found for rollback.");
//             response.put("status", "Error");
//             response.put("message", "PO item not found.");
//             return response;
//         }

//         // Restore previous values
//         poItem.setModelNumber(previousPoItem.getModelNumber());
//         poItem.setUom(previousPoItem.getUom());
//         poItem.setQtyPerSite(previousPoItem.getQtyPerSite());
//         poItem.setTotalNumberOfSites(previousPoItem.getTotalNumberOfSites());
//         poItem.setTotalQty(previousPoItem.getTotalQty());
//         poItem.setAccumulatedDepreciation(previousPoItem.getAccumulatedDepreciation());
//         poItem.setSalvageValue(previousPoItem.getSalvageValue());
//         poItem.setFaCategoryNew(previousPoItem.getFaCategoryNew());
//         poItem.setL1(previousPoItem.getL1());
//         poItem.setL2(previousPoItem.getL2());
//         poItem.setL3(previousPoItem.getL3());
//         poItem.setL4(previousPoItem.getL4());
//         poItem.setOldFaCategory(previousPoItem.getOldFaCategory());
//         poItem.setAccumulatedDepreciationCode(previousPoItem.getAccumulatedDepreciationCode());
//         poItem.setDepreciationCode(previousPoItem.getDepreciationCode());
//         poItem.setLifeYearsNew(previousPoItem.getLifeYearsNew());
//         poItem.setVendorName(previousPoItem.getVendorName());
//         poItem.setVendorNumber(previousPoItem.getVendorNumber());
//         poItem.setProjectNumber(previousPoItem.getProjectNumber());
//         poItem.setDatePlacedInService(previousPoItem.getDatePlacedInService());
//         poItem.setPoDate(previousPoItem.getPoDate());
//         poItem.setCurrency(previousPoItem.getCurrency());
//         poItem.setUnitPrice(previousPoItem.getUnitPrice());
//         poItem.setPoLine(previousPoItem.getPoLine());
//         poItem.setLevel1Description(previousPoItem.getLevel1Description());
//         poItem.setPartNumber(previousPoItem.getPartNumber());
//         poItem.setL3Description(previousPoItem.getL3Description());
//         poItem.setCostCenter(previousPoItem.getCostCenter());
//         poItem.setUpdatedBy(previousPoItem.getUpdatedBy());
//         poItem.setApproval_Status("Approved"); // Restore approval status

//         // Save rollback changes
//         poRepo.save(poItem);

//         // Update workflow
//         workflow.setUpdatedStatus("Modification Rejected");
//         workflow.setChangedBy("System");
//         workflow.setChangeDate(new Date());
//         workflow.setComments("Modification Rejected, PO item restored to last approved version.");
//         workflowRepository.save(workflow);

//         logger.info("SUCCESS: PO item rollback completed for PO Number: {} and Record No: {}", poNumber, recordNo);
//         response.put("status", "Success");
//         response.put("message", "PO item rollback successful. Modification rejected.");
//     } catch (Exception ex) {
//         logger.error("EXCEPTION: Error occurred while rolling back Modification for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
//         TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); // Rollback transaction
//         response.put("status", "Error");
//         response.put("message", "Failed to rollback PO item: " + ex.getMessage());
//     }

//     return response;
// }







// delete po item
// @DeleteMapping(value = "/deletePoItem/{recordNo}")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> deletePoItem(@PathVariable long recordNo) {
//     loggger.info("PO DELETE REQUEST | recordNo: " + recordNo);
//     Map<String, String> response = new HashMap<>();
//     try {
//         // Check if the PO item exists
//         tb_Po poItem = poRepo.findByRecordNo(recordNo);
//         if (poItem == null) {
//             response.put("status", "Error");
//             response.put("message", "PO item with recordNo " + recordNo + " does not exist");
//             return response;
//         }
//         // Delete the PO item
//         poRepo.deleteById(recordNo);
//         response.put("status", "Success");
//         response.put("message", "PO item with recordNo " + recordNo + " deleted successfully");
//     } catch (Exception ex) {
//         loggger.info("Exception |  " + ex.toString());
//         response.put("status", "Error");
//         response.put("message", "Failed to delete PO item: " + ex.getMessage());
//     }
//     return response;
// }


//delete po number
@DeleteMapping(value = "/deletePONumber/{poNumber}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Transactional // Ensure the operation is performed within a transaction
public Map<String, String> deletePONumber(@PathVariable String poNumber) {
    loggger.info("PO NUMBER DELETE REQUEST | poNumber: " + poNumber);
    Map<String, String> response = new HashMap<>();
    try {
        // Check if the poNumber exists
        tbPoNumber poNumberEntry = poNumberRepo.findByPoNumber(poNumber);
        if (poNumberEntry == null) {
            response.put("status", "Error");
            response.put("message", "poNumber " + poNumber + " does not exist");
            return response;
        }
        // Delete all PO items associated with the poNumber
        List<tb_Po> referencedPoItems = poRepo.findAllByPoNumber(poNumber);
        if (!referencedPoItems.isEmpty()) {
            loggger.info("Deleting " + referencedPoItems.size() + " PO items associated with poNumber " + poNumber);
            poRepo.deleteByPoNumber(poNumber); // Delete all PO items
        }
        // Delete the poNumber entry
        poNumberRepo.delete(poNumberEntry);
        response.put("status", "Success");
        response.put("message", "poNumber " + poNumber + " and all associated PO items deleted successfully");
    } catch (Exception ex) {
        loggger.info("Exception |  " + ex.toString());
        response.put("status", "Error");
        response.put("message", "Failed to delete poNumber: " + ex.getMessage());
    }
    return response;
}



private String generateProcessId() {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String randomDigit = String.valueOf((int) (Math.random() * 10));
    // Use the last 8 digits of the timestamp to ensure the processId is always unique
    return timestamp.substring(timestamp.length() - 6) + randomDigit;
}


@PostMapping(value = "/requestDeletePoItem/{recordNo}")
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
        poItem.setApproval_Status("pending deletion");
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


@DeleteMapping(value = "/approveDeletePoItem/{poNumber}/{recordNo}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> approveDeletePoItem(@PathVariable String poNumber, @PathVariable Long recordNo) {
    logger.info("START: Approving deletion request for PO Number: {} and Record No: {}", poNumber, recordNo);
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
        if (!"pending deletion".equals(poItem.getApproval_Status())) {
            logger.warn("PO item with Record No: {} is not in 'pending deletion' status.", recordNo);
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
        if ("pending deletion".equals(workflow.getOriginalStatus())) {
            logger.info("Updating workflow entry to 'Deletion Approved' for PO Number: {}", poNumber);
            workflow.setUpdatedStatus("Deletion Approved");
            workflow.setChangedBy("System"); // Replace with actual user if available
            workflow.setChangeDate(new Date());
            workflow.setComments("Deletion approved for PO item with Record No: " + recordNo);
            workflowRepository.save(workflow);
            logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
        } else {
            logger.warn("Workflow for PO Number: {} is not in 'pending deletion' status. Cannot approve deletion.", poNumber);
            response.put("status", "Error");
            response.put("message", "The workflow's original status is not 'pending deletion'. Cannot approve deletion.");
            return response;
        }
        // Delete the specific PO item
        logger.info("Deleting PO item with Record No: {}", recordNo);
        poRepo.delete(poItem);
        logger.info("Successfully deleted PO item with Record No: {}", recordNo);
        logger.info("SUCCESS: Approval of deletion request completed for PO Number: {} and Record No: {}", poNumber, recordNo);
        response.put("status", "Success");
        response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " deleted successfully.");
    } catch (Exception ex) {
        logger.error("EXCEPTION: Error occurred while approving deletion for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
        response.put("status", "Error");
        response.put("message", "Failed to approve deletion of PO item: " + ex.getMessage());
    }
    return response;
}



@PostMapping(value = "/rejectDeletePoItem/{poNumber}/{recordNo}")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public Map<String, String> rejectDeletePoItem(@PathVariable String poNumber, @PathVariable Long recordNo) {
    logger.info("START: Rejecting deletion request for PO Number: {} and Record No: {}", poNumber, recordNo);
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
        if (!"pending deletion".equals(poItem.getApproval_Status())) {
            logger.warn("PO item with Record No: {} is not in 'pending deletion' status.", recordNo);
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
        if ("pending deletion".equals(workflow.getOriginalStatus())) {
            logger.info("Updating workflow entry to 'Deletion Rejection' for PO Number: {}", poNumber);
            workflow.setUpdatedStatus("Deletion Rejection");
            workflow.setChangedBy("System"); // Replace with actual user if available
            workflow.setChangeDate(new Date());
            workflow.setComments("Deletion Rejected for PO item with Record No: " + recordNo);
            workflowRepository.save(workflow);
            logger.info("Workflow entry updated successfully for PO Number: {}", poNumber);
        } else {
            logger.warn("Workflow for PO Number: {} is not in 'pending deletion' status. Cannot reject deletion.", poNumber);
            response.put("status", "Error");
            response.put("message", "The workflow's original status is not 'pending deletion'. Cannot reject deletion.");
            return response;
        }
        // Update the approval status of the specific PO item to "Approved"
        logger.info("Reverting approval status for PO item with Record No: {}", recordNo);
        poItem.setApproval_Status("Approved");
        poRepo.save(poItem);
        logger.info("Rejected deletion for PO item with Record No: {}", recordNo);

        logger.info("SUCCESS: Rejection of deletion request completed for PO Number: {} and Record No: {}", poNumber, recordNo);
        response.put("status", "Success");
        response.put("message", "PO item with poNumber " + poNumber + " and recordNo " + recordNo + " deletion request rejected successfully.");
    } catch (Exception ex) {
        logger.error("EXCEPTION: Error occurred while rejecting deletion for PO Number: {} and Record No: {}. Error: {}", poNumber, recordNo, ex.getMessage(), ex);
        response.put("status", "Error");
        response.put("message", "Failed to reject deletion of PO item: " + ex.getMessage());
    }
    return response;
}
// @PutMapping(value = "/updatePoItem/{recordNo}")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> updatePoItem(@PathVariable long recordNo, @RequestBody String req) {
//     logger.info("PO UPDATE REQUEST | Record No: " + recordNo + " | Payload: " + req);
//     Map<String, String> response = new HashMap<>();
//     try {
//         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//         JSONObject jsonObject = new JSONObject(req);
//         // Fetch the existing PO record
//         tb_Po existingPo = poRepo.findByRecordNo(recordNo);
//         if (existingPo == null) {
//             return response("Error", "Record does not exist for recordNo: " + recordNo);
//         }
//         // Ensure the poNumber is not altered
//         String poNumber = jsonObject.getString("poNumber").trim();
//         if (!existingPo.getPoNumber().equals(poNumber)) {
//             return response("Error", "Cannot alter poNumber for recordNo: " + recordNo);
//         }
//         // Create a new tb_Po_Modification entity and populate it with the request data
//         tb_Po_Modification modification = new tb_Po_Modification();
//         modification.setRecordNo(recordNo);
//         modification.setPoNumber(poNumber);     
//         // Set modelNumber, if available
//         modification.setModelNumber(jsonObject.optString("modelNumber", null));
//         modification.setUom(jsonObject.optString("uom", null));
//         modification.setQtyPerSite(jsonObject.optInt("qtyPerSite", 0));  // Default to 0 if not provided
//         modification.setTotalNumberOfSites(jsonObject.optInt("totalNumberOfSites", 0));  // Default to 0 if not provided
//         modification.setTotalQty(jsonObject.optInt("totalQty", 0));  // Default to 0 if not provided
//         modification.setAccumulatedDepreciation(jsonObject.optDouble("accumulatedDepreciation", 0.0));  // Default to 0.0 if not provided
//         modification.setSalvageValue(jsonObject.optDouble("salvageValue", 0.0));  // Default to 0.0 if not provided
//         modification.setFaCategoryNew(jsonObject.optString("faCategoryNew", null));
//         modification.setL1(jsonObject.optString("l1", null));
//         modification.setL2(jsonObject.optString("l2", null));
//         modification.setL3(jsonObject.optString("l3", null));
//         modification.setL4(jsonObject.optString("l4", null));
//         modification.setOldFaCategory(jsonObject.optString("oldFaCategory", null));
//         modification.setAccumulatedDepreciationCode(jsonObject.optString("accumulatedDepreciationCode", null));
//         modification.setDepreciationCode(jsonObject.optString("depreciationCode", null));
//         modification.setLifeYearsNew(jsonObject.optInt("lifeYearsNew", 0));  // Default to 0 if not provided
//         modification.setVendorName(jsonObject.optString("vendorName", null));
//         modification.setVendorNumber(jsonObject.optString("vendorNumber", null));
//         modification.setProjectNumber(jsonObject.optString("projectNumber", null));
//         modification.setCurrency(jsonObject.optString("currency", null));
//         modification.setUnitPrice(jsonObject.optDouble("unitPrice", 0.0));  // Default to 0.0 if not provided
//         modification.setPoLine(jsonObject.optInt("poLine", 0));  // Default to 0 if not provided
//         modification.setLevel1Description(jsonObject.optString("level1Description", null));
//         modification.setPartNumber(jsonObject.optString("partNumber", null));
//         modification.setCostCenter(jsonObject.optString("costCenter", null));
//         modification.setUpdatedBy(jsonObject.optString("updatedBy", null));
//         modification.setApprovalStatus("pending modification");
//         try {
//             // Save the modification request
//             modificationRepo.save(modification);
//             // Update the existing PO record's approval status
//             existingPo.setApproval_Status("pending modification");
//             poRepo.save(existingPo);
//             // Insert into Workflow Table
//             Workflow workflow = new Workflow();
//             workflow.setPoNumber(existingPo.getPoNumber());
//             workflow.setOriginalStatus(existingPo.getApproval_Status());
//             workflow.setProcessId(generateProcessId()); // Assuming this method generates a unique process ID
//             workflow.setInsertedBy("System"); // Default inserted by "System"
//             workflow.setInsertDate(new Date());
//             workflowRepository.save(workflow);
//             logger.info("Modification request saved for PO Number: " + poNumber + ", Record No: " + recordNo);
//             return response("Success", "Modification request submitted successfully for approval.");
//         } catch (Exception e) {
//             logger.error("Error saving modification request: " + e.getMessage(), e);
//             return response("Error", "Failed to save modification for recordNo: " + recordNo);
//         }
//     } catch (Exception e) {
//         logger.error("Exception | " + e.toString(), e);
//         return response("Error", "Failed to process update request: " + e.getMessage());
//     }
// }













}
