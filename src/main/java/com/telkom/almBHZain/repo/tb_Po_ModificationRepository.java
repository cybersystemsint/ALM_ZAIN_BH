package com.telkom.almBHZain.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telkom.almBHZain.model.tb_Po_Modification;

@Repository
public interface tb_Po_ModificationRepository extends JpaRepository<tb_Po_Modification, Long> {
    tb_Po_Modification findByPoNumber(String poNumber);
}

// @PostMapping(value = "/createOrUpdatePoItem")
// @CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
// public Map<String, String> createOrUpdatePo(@RequestBody String req) throws ParseException {
//     String batchfilename = "";
//     LocalDateTime now = LocalDateTime.now();
//     loggger.info("PO CREATE/UPDATE REQUEST |  " + req);
//     try {
//         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern as per your date format
//         JSONArray jsonArray = new JSONArray(req);
//         String responseinfo = "Failed to save or update data";

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

//             // Check if the record already exists in tb_Po
//             tb_Po existingPo = poRepo.findByPoNumber(poNumber);
//             tb_Po nwspldt = existingPo != null ? existingPo : new tb_Po();

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
//                 // Correct usage of Level.SEVERE
//                 // Logger.getLogger(APIController.class.getName()).log(Level.SEVERE, null, ex);
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
//                 poRepo.save(nwspldt);
//                 responseinfo = existingPo != null ? "Record Updated Success" : "Record Created Success";

//                 // Create workflow request only for new records
//                 if (existingPo == null) {
//                     Workflow workflow = new Workflow();
//                     workflow.setPoNumber(nwspldt.getPoNumber());
//                     workflow.setOriginalStatus(nwspldt.getApproval_Status());
//                     workflow.setProcessId(generateProcessId());
//                     workflow.setInsertedBy("System"); 
//                     workflow.setInsertDate(new Date());
//                     workflowRepository.save(workflow);
//                 }

//             } catch (Exception excc) {
//                 loggger.info("Exception |  " + excc.toString());
//                 responseinfo = excc.toString();
//             }
//         }

//         loggger.info("PO CREATE/UPDATE RESPONSE |  " + responseinfo);
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